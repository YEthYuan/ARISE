package org.aliyun.serverless.scaler.pool;

import io.grpc.Context;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.config.ParamsManager;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.Instance;
import org.aliyun.serverless.model.LightPoolStatusDto;
import org.aliyun.serverless.model.SlotResourceConfig;
import org.aliyun.serverless.platformClient.PlatformClientAgent;
import org.aliyun.serverless.scaler.CrazyScaler;
import org.aliyun.serverless.scaler.loadBalancer.Forecast;
import org.aliyun.serverless.units.ExceptionHandler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class LightPoolManagerImpl implements LightPoolManager {

    private static final Logger logger = Logger.getLogger(LightPoolManagerImpl.class.getName());
    private static final ParamsManager paramsManager = ParamsManager.getManager();

    private final Queue<Instance> idleInstanceQueue;
    private final Lock muIdleInstanceQueue;

    private final Map<String, Instance> busyInstanceMap;

    private final Queue<CrazyScaler.AssignCallBackFunction> queuedAssignCallbackFunctions;
    private final Lock muCallbackFunctionsQueue;

    private final Queue<Instance> failedDestroyInstanceQueue;
    private final Lock muFailedDestroyInstanceQueue;

    private final PlatformClientAgent platformClientAgent;

    private final Function function;

    private final Config config;

    private final SlotResourceConfig slotResourceConfig;

    private final Forecast forecast;

    private final Boolean useXgboost;

    private final int forecastInterval;

    private long lastForecastTimestampInMs;

    public LightPoolManagerImpl(Function function, Config config, SlotResourceConfig slotResourceConfig, Forecast forecast, boolean useXgboost) throws Exception {

        this.platformClientAgent = new PlatformClientAgent(config.getPlatformHost(), config.getPlatformPort());

        this.idleInstanceQueue = new ConcurrentLinkedQueue<>();
        this.muIdleInstanceQueue = new ReentrantLock();
        this.busyInstanceMap = new ConcurrentHashMap<>();
        this.queuedAssignCallbackFunctions = new ConcurrentLinkedQueue<>();
        this.muCallbackFunctionsQueue = new ReentrantLock();
        this.failedDestroyInstanceQueue = new ConcurrentLinkedQueue<>();
        this.muFailedDestroyInstanceQueue = new ReentrantLock();

        this.function = function;
        this.config = config;
        this.slotResourceConfig = slotResourceConfig;

        this.forecast = forecast;
        this.lastForecastTimestampInMs = 0L;
        this.useXgboost = useXgboost;
        int initDurationInMs = 10000;
        Object initDurationInMsObject = paramsManager.getParamValue("InitDurationInMs." + function.getKey());
        if (initDurationInMsObject != null) {
            initDurationInMs = (int) initDurationInMsObject;
        }
        this.forecastInterval = (int) Math.floor(((double) paramsManager.getParamValue("ForecastBalanceMethod.forecastIntervalRate")) * initDurationInMs);


        new Thread(this::gcLoop).start();
    }

    @Override
    public LightPoolStatusDto getPoolStatus() {
        LightPoolStatusDto lightPoolStatusDto = new LightPoolStatusDto();
        lightPoolStatusDto.setBusyInstanceNum(busyInstanceMap.size());
        lightPoolStatusDto.setQueuedAssignRequestNum(queuedAssignCallbackFunctions.size());

        this.muIdleInstanceQueue.lock();
        lightPoolStatusDto.setReadyInstanceNum(idleInstanceQueue.size());
        Integer initTimeByMetaKey = (Integer) paramsManager.getParamValue("InitDurationInMs." + this.function.getKey());
        if (null == initTimeByMetaKey) {
            initTimeByMetaKey = 10 * 1000;
        }
        lightPoolStatusDto.setTotalInstanceNum(lightPoolStatusDto.getBusyInstanceNum() + lightPoolStatusDto.getReadyInstanceNum());
        Integer nextInitWindowGcCnt = 0;
        StringBuffer forecastInfo = new StringBuffer();
        for (int i=0; i<idleInstanceQueue.size(); i++) {
            Instance instance = idleInstanceQueue.poll();
            if (null == instance) {
                logger.warning("Found a null in idleInstanceQueue! Dropped it!");
                continue;
            }
            // instance.getIdleSince() + instance.getMaxIdleTimeInMs() < System.currentTimeMillis() + initTimeByMetaKey
            LocalDateTime dieTime = instance.getIdleSince().plus(Duration.ofMillis(instance.getMaxIdleTimeInMs()));
            LocalDateTime nextInitTimeWindow = LocalDateTime.now().plus(Duration.ofMillis(initTimeByMetaKey));
            forecastInfo.append(String.format("[FORECAST] Instance %s will die at %s, before next init window at %s",
                    instance.getInstanceId(), dieTime, nextInitTimeWindow)).append('\n');
            if (dieTime.isBefore(nextInitTimeWindow)) {
                nextInitWindowGcCnt++;
            }
            idleInstanceQueue.offer(instance);
        }
        this.muIdleInstanceQueue.unlock();

        logger.info("\n" + forecastInfo.toString());
        logger.info(String.format("Pool Status:\nidle: %d\nbusy: %d\nnextInitWindowGcCnt: %d\nqueuedCallbacks: %d\n",
                lightPoolStatusDto.getReadyInstanceNum(), lightPoolStatusDto.getBusyInstanceNum(), nextInitWindowGcCnt, lightPoolStatusDto.getQueuedAssignRequestNum()));
        return lightPoolStatusDto;
    }

    @Override
    public Boolean createInstance(Context ctx, String requestId, Long delayCreateInMs, Long maxIdleTimeInMs) {
        AtomicReference<LocalDateTime> createSince = new AtomicReference<>();
        AtomicReference<LocalDateTime> initSince = new AtomicReference<>();

        logger.info(String.format("Request %s is handling by LightPool!", requestId));
        CompletableFuture.supplyAsync(() -> {
            try {
                logger.info(String.format("Request %s: InstancePool.createInstance invoked, but creation will be delayed by %dms", requestId, delayCreateInMs));
                Thread.sleep(delayCreateInMs);
                logger.info(String.format("Request %s: thread awake, create slot operation submitted...", requestId));
                return platformClientAgent.blockingCreateSlot(ctx, requestId, this.slotResourceConfig);
            } catch (Exception e) {
                logger.severe(String.format("Request %s: Failed to create slot! error:\n%s", requestId, ExceptionHandler.all(e)));
                throw new RuntimeException(e);
            }
        }).thenApplyAsync((slot) -> {
            try {
                createSince.set(LocalDateTime.now());
                logger.info(String.format("Request %s: Successfully created slot! SlotId: %s", requestId, slot.getId()));

                String instanceId = UUID.randomUUID().toString();
                logger.info(String.format("Request %s: Initializing slot %s with instance %s", requestId, slot.getId(), instanceId));
                return platformClientAgent.blockingInit(ctx, requestId, instanceId, slot, this.function);
            } catch (Exception e) {
                logger.severe(String.format("Request %s: Failed to init slot %s! error:\n%s", requestId, slot.getId(), ExceptionHandler.all(e)));
                throw new RuntimeException(e);
            }
        }).thenAcceptAsync((instance) -> {
            initSince.set(LocalDateTime.now());
            logger.info(String.format("Request %s: Success inited slot! SlotId: %s, instanceId: %s", requestId, instance.getSlot().getId(), instance.getInstanceId()));

            // 0. 初始化刚刚生成的instance的一些数据
            instance.setMaxIdleTimeInMs(maxIdleTimeInMs);
            instance.setCreateSince(createSince.get());
            instance.setInitSince(initSince.get());
            logger.info(String.format("Instance %s desc:\nMaxIdleTime: %dms\nCreateSince: %s\nInitSince: %s", instance.getInstanceId(), instance.getMaxIdleTimeInMs(),
                    instance.getCreateSince(), instance.getInitSince()));

            // 1. 如果回调函数队列中积压了Callbacks, 那么用刚生成的这个instance抵消掉一个Callback
            muCallbackFunctionsQueue.lock();
            int queuedTaskNum = queuedAssignCallbackFunctions.size();
            if (queuedTaskNum > 0) {
                CrazyScaler.AssignCallBackFunction execCallbackFunc = queuedAssignCallbackFunctions.poll();
                logger.info(String.format("Instance %s of request %s ready, BUT there are %d queued assign requests!\n" +
                        "Decide to allocate instance %s to request %s!", instance.getInstanceId(), requestId, queuedTaskNum,
                        instance.getInstanceId(), execCallbackFunc.getRequestId()));
                execCallbackFunc.setParameter(instance.getInstanceId());
                try {
                    // 1.1 调用积压回调函数
                    logger.info(String.format("Request %s: Invoking assign callback function with instance %s ...", execCallbackFunc.getRequestId(), instance.getInstanceId()));
                    execCallbackFunc.call();
                    logger.info(String.format("Request %s: Assign callback function successfully returned!", execCallbackFunc.getRequestId()));
                    // 1.2 将刚刚生成的instance放入busy instance Map中
                    busyInstanceMap.put(instance.getInstanceId(), instance);
                    logger.info(String.format("Put instance %s into busy instance map! Current busy instance number: %d", instance.getInstanceId(), busyInstanceMap.size()));
                } catch (Exception e) {
                    logger.severe(String.format("Request %s: Failed during the invocation of assign callback function! Error:\n%s", execCallbackFunc.getRequestId(), ExceptionHandler.all(e)));
                    throw new RuntimeException(e);
                } finally {
                    muCallbackFunctionsQueue.unlock();
                }
            } else {
                muCallbackFunctionsQueue.unlock();
                // 2. 如果没有积压的Callback, 那么将刚刚生成的instance放入idle队列中
                // 2.1 填充一下instance的idleSince数据
                instance.setIdleSince(LocalDateTime.now());
                // 2.2 将刚刚生成的instance放入idle队列中
                muIdleInstanceQueue.lock();
                idleInstanceQueue.offer(instance);
                logger.info(String.format("Request %s: put instance %s with slot %s into idle queue, idle since %s", requestId, instance.getInstanceId(), instance.getSlot().getId(), instance.getIdleSince()));
                muIdleInstanceQueue.unlock();
            }
        }).exceptionally((e) -> {
            logger.info(String.format("Request %s: Recovered from exception \"%s\"", requestId, ExceptionHandler.all((Exception) e)));
            return null;
        });

        return Boolean.TRUE;
    }

    @Override
    public Boolean assignInstance(Context ctx, CrazyScaler.AssignCallBackFunction callbackMethod) {
        muIdleInstanceQueue.lock();
        if (idleInstanceQueue.size() > 0) {
            // 1. 如果idleInstanceQueue中还有闲置instance, 直接取出相应instance执行Callback函数
            Instance instance = idleInstanceQueue.poll();
            muIdleInstanceQueue.unlock();
            logger.info(String.format("Request %s: Ready instance exist, allocate instance %s and slot %s for callback function", callbackMethod.getRequestId(), instance.getInstanceId(), instance.getSlot().getId()));
            CompletableFuture.supplyAsync(() -> {
                try {
                    // 1.1 执行Callback函数
                    logger.info(String.format("Request %s: Invoking callback function with instance %s", callbackMethod.getRequestId(), instance.getInstanceId()));
                    callbackMethod.setParameter(instance.getInstanceId());
                    callbackMethod.call();
                    logger.info(String.format("Request %s: Callback function returned successfully!", callbackMethod.getRequestId()));
                    // 1.2 将instance放入busy队列中
                    busyInstanceMap.put(instance.getInstanceId(), instance);
                    logger.info(String.format("Request %s: Put instance %s into busy instance map! Current busy instance num: %d", callbackMethod.getRequestId(), instance.getInstanceId(), busyInstanceMap.size()));
                } catch (Exception e) {
                    logger.severe(String.format("Request %s: Failed during the invocation of assign callback function, free instance %s! Error:\n%s", callbackMethod.getRequestId(), instance.getInstanceId(), ExceptionHandler.all(e)));
                    busyInstanceMap.remove(instance.getInstanceId());
                    insertInstanceIntoFirstPosOfIdleQueue(instance);
                    logger.info(String.format("Request %s: Free instance %s and put it into the first pos of idle queue", callbackMethod.getRequestId(), instance.getInstanceId()));
                    throw new RuntimeException(e);
                }
                return true;
            }).exceptionally((e) -> {
                logger.info(String.format("Recovered from exception \"%s\"", ExceptionHandler.all((Exception) e)));
                return null;
            });

        } else {
            muIdleInstanceQueue.unlock();

            // 2. 如果idleInstanceQueue中没有闲置instance, 那么将Callback压入
            muCallbackFunctionsQueue.lock();
            queuedAssignCallbackFunctions.offer(callbackMethod);
            muCallbackFunctionsQueue.unlock();
            logger.info(String.format("Request %s: No ready instance now! Decide to put callback method into working queue. Queue size: %d", callbackMethod.getRequestId(), queuedAssignCallbackFunctions.size()));
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean idleInstance(Context ctx, String instanceId, CrazyScaler.IdleCallBackFunction callbackMethod, Boolean needDestroy) {
        if (!busyInstanceMap.containsKey(instanceId)) {
            logger.warning(String.format("Cannot find instance %s in busy instance map! Skip idle operation!", instanceId));
            return Boolean.FALSE;
        }
        Instance instance = busyInstanceMap.get(instanceId);

        // 0. 将instance从busy instance map中移除
        busyInstanceMap.remove(instanceId);

        if (needDestroy) {
            // 1. 如果instance需要被立刻摧毁
            logger.info(String.format("Idle request indicates to destroy instance %s immediately!", instanceId));
            destroyInstanceImpl(ctx, instance, "Idle request indicates to destroy this instance");
        } else {
            // 2. 如果没有要求立刻摧毁instance, 则将instance放回idle队列中
            instance.setIdleSince(LocalDateTime.now());
            muIdleInstanceQueue.lock();
            idleInstanceQueue.offer(instance);
            muIdleInstanceQueue.unlock();
            logger.info(String.format("Idle request indicates not to destroy instance %s immediately! Put back to idle queue. Current idle nums: %d", instanceId, idleInstanceQueue.size()));
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean destroyInstance(Context ctx, Integer destroyNum, String reason) {
        muIdleInstanceQueue.lock();

        int idleInstanceCount = idleInstanceQueue.size();
        if (destroyNum > idleInstanceCount) {
            logger.warning(String.format("Destroy number %d exceeds idle instance count %d, reset destroyNum to %d", destroyNum, idleInstanceCount, idleInstanceCount));
            destroyNum = idleInstanceCount;
        }

        logger.info(String.format("LightPool will destroy %d instances due to %s", destroyNum, reason));
        for (int i=0; i<destroyNum; i++) {
            Instance instance = idleInstanceQueue.poll();
            destroyInstanceImpl(ctx, instance, reason);
        }
        logger.info(String.format("LightPool has submitted destroying %d instances due to %s", destroyNum, reason));

        muIdleInstanceQueue.unlock();
        return Boolean.TRUE;
    }

    // 根据instance Id 提交destroy instance的请求
    private Boolean destroyInstanceImpl(Context ctx, Instance instance, String reason) {
        if (null == instance) {
            logger.warning("Instance is null!");
            return Boolean.FALSE;
        }

        if (busyInstanceMap.containsKey(instance.getInstanceId())) {
            logger.warning(String.format("Instance %s is in busy list, cannot be destroyed", instance.getInstanceId()));
            return Boolean.FALSE;
        }

        CompletableFuture.supplyAsync(() -> {
            logger.info(String.format("Instance %s destroy request submitted ...", instance.getInstanceId()));
            try {
                platformClientAgent.blockingDestroy(ctx, UUID.randomUUID().toString(), instance.getSlot().getId(), reason);
                logger.info(String.format("Instance %s destroy request finished.", instance.getInstanceId()));
                return true;
            } catch (Exception e) {
                logger.severe(String.format("Failed to destroy instance %s with slot %s due to error:\n%s", instance.getInstanceId(), instance.getSlot().getId(), ExceptionHandler.all(e)));
                muFailedDestroyInstanceQueue.lock();
                failedDestroyInstanceQueue.offer(instance);
                muFailedDestroyInstanceQueue.unlock();
                logger.info(String.format("Put instance %s into failed destroy instance queue, will try to destroy in next GC loop!", instance.getInstanceId()));
                return false;
            }
        }).exceptionally((e) -> {
            logger.info(String.format("Recovered from exception \"%s\"", ExceptionHandler.all((Exception) e)));
            return null;
        });

        return Boolean.TRUE;
    }

    private void insertInstanceIntoFirstPosOfIdleQueue(Instance instance) {
        muIdleInstanceQueue.lock();
        int sz = idleInstanceQueue.size();
        idleInstanceQueue.offer(instance);
        for (int i=0; i<sz; i++) {
            idleInstanceQueue.offer(idleInstanceQueue.poll());
        }
        muIdleInstanceQueue.unlock();
    }

    private void gcLoop() {
        logger.info(String.format("gc loop for app: %s is starting", function.getKey()));
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // 0. 如果用模型, 更新Forecast
                if (useXgboost) {
                    if (System.currentTimeMillis() - lastForecastTimestampInMs >= forecastInterval) {
                        try {
                            forecast.updateForecast();
                            lastForecastTimestampInMs = System.currentTimeMillis();
                        } catch (XGBoostError e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                // 1. 释放idle instance队列中超时instance
                muIdleInstanceQueue.lock();
                int idleInstanceNums = idleInstanceQueue.size();
                for (int i=0; i<idleInstanceNums; i++) {
                    Instance instance = idleInstanceQueue.poll();
                    if (null == instance) {
                        continue;
                    }
                    long idleDuration = Duration.between(instance.getIdleSince(), LocalDateTime.now()).toMillis();
                    if (idleDuration > instance.getMaxIdleTimeInMs()) {
                        String reason = String.format("Idle duration: %dms, exceed configured duration: %dms",
                                idleDuration, instance.getMaxIdleTimeInMs());
                        destroyInstanceImpl(Context.current(), instance, reason);
                        logger.info(String.format("Instance %s slot %s of app %s is GCed due to idle for %dms",
                                instance.getInstanceId(), instance.getSlot().getId(), instance.getMeta().getKey(), idleDuration));
                    }
                }
                muIdleInstanceQueue.unlock();

                // 2. 释放failed destroy queue中, 之前遗留的instance
                muFailedDestroyInstanceQueue.lock();
                int failedDestroyInstanceNums = failedDestroyInstanceQueue.size();
                for (int i=0; i<failedDestroyInstanceNums; i++) {
                    Instance instance = failedDestroyInstanceQueue.poll();
                    if (null == instance) {
                        continue;
                    }
                    destroyInstanceImpl(Context.current(), instance, "Retry to destroy instance due to previous failure");
                    logger.info(String.format("Previously failed destroyed instance %s slot %s of app %s is GCed.",
                            instance.getInstanceId(), instance.getSlot().getId(), instance.getMeta().getKey()));
                }
                if (failedDestroyInstanceNums > 0) {
                    logger.info(String.format("GC loop destroyed %d instances that failed in previous destroy request!"));
                }
                muFailedDestroyInstanceQueue.unlock();
            }
        };

        timer.schedule(timerTask, 0, this.config.getGcInterval().toMillis());
    }

}
