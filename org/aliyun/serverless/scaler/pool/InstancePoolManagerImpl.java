package org.aliyun.serverless.scaler.pool;

import io.grpc.Context;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.model.AvailableResourcesDto;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.SlotResourceConfig;
import org.aliyun.serverless.model.Task;
import org.aliyun.serverless.platformClient.PlatformClientAgent;
import org.aliyun.serverless.scaler.loadBalancer.Forecast;
import org.aliyun.serverless.units.CallbackFunction;
import org.aliyun.serverless.units.XgboostAgent;
import org.aliyun.serverless.units.ExceptionHandler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * @description 固定参数资源池
 * @author peiying.yy
 * @date 2023.07.25
 */
public class InstancePoolManagerImpl implements InstancePoolManager {

    /*
     * todo:
     *  1. Config + gcLoop
     *  2. CreateInstance: delayCreateInMs, maxIdleTimeInMs
     *  3. 把initDurationInMs hack下来
     */

    private static final Logger logger = Logger.getLogger(InstancePoolManagerImpl.class.getName());

    private final PlatformClientAgent platformClient;

    private final FixParamInstancePool instancePool;

    private final Lock mutexInstancePool;

    private final Map<String, String> instanceId2TaskId;

    private final Function function;

    private final Config config;

    private final SlotResourceConfig slotResourceConfig;

    private final boolean useXgboost;
    private final int forecastInterval;
    private final Forecast forecast;
    private int lastForecastTimestampInMs;

    public InstancePoolManagerImpl(Function function, Config config, SlotResourceConfig slotResourceConfig, Forecast forecast, boolean useXgboost) {
        try {
            platformClient = new PlatformClientAgent(config.getPlatformHost(), config.getPlatformPort());
            this.instancePool = new FixParamInstancePool();
            this.mutexInstancePool = new ReentrantLock();
            this.instanceId2TaskId = new ConcurrentHashMap<>();
            this.config = config;
            this.function = function;
            this.slotResourceConfig = slotResourceConfig;

            this.forecast = forecast;
            this.lastForecastTimestampInMs = 0;
            this.useXgboost = useXgboost;
            int initDurationInMs = 10000;
            Object initDurationInMsObject = config.getParamsManager().getParamValue("InitDurationInMs." + function.getKey());
            if (initDurationInMsObject != null){
                initDurationInMs = (int) initDurationInMsObject;
            }
            forecastInterval = (int) ((double) (config.getParamsManager().getParamValue("ForecastBalanceMethod.forecastIntervalRate")) * initDurationInMs);


            new Thread(this::gcLoop).start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to init instance pool manager, " + e);
        }
    }

    private class FixParamInstancePool {
        Map<String, Task> readyInstances;
        Map<String, Task> initStageInstances;
        Map<String, Task> createStageInstances;

        public FixParamInstancePool() {
            readyInstances = new ConcurrentHashMap<>();
            initStageInstances = new ConcurrentHashMap<>();
            createStageInstances = new ConcurrentHashMap<>();
        }
    }

    @Override
    public AvailableResourcesDto listAvailableResources() {
        List<Task> readyStage = new ArrayList<>();
        List<Task> initStage = new ArrayList<>();
        List<Task> createStage = new ArrayList<>();

        this.mutexInstancePool.lock();
        Integer readyTotal = instancePool.readyInstances.size();
        for (String key: this.instancePool.readyInstances.keySet()) {
            if (null == key) {
                continue;
            }
            Task task = this.instancePool.readyInstances.getOrDefault(key, null);
            if(null != task && null != task.getInstance() && !task.getBusy()) {
                readyStage.add(task);
            }
        }
        Integer initTotal = instancePool.initStageInstances.size();
        for (String key: this.instancePool.initStageInstances.keySet()) {
            if (null == key) {
                continue;
            }
            Task task = this.instancePool.initStageInstances.getOrDefault(key, null);
            if(null != task && !task.getBusy()) {
                initStage.add(task);
            }
        }
        Integer createTotal = instancePool.createStageInstances.size();
        for (String key: this.instancePool.createStageInstances.keySet()) {
            if (null == key) {
                continue;
            }
            Task task = this.instancePool.createStageInstances.getOrDefault(key, null);
            if(null != task && !task.getBusy()) {
                createStage.add(task);
            }
        }
        this.mutexInstancePool.unlock();

        readyStage.sort(Comparator.comparing(o -> o.getInstance().getIdleSince()));

        logger.info(String.format("Current available resources: ready %d, inited %d, created %d", readyStage.size(), initStage.size(), createStage.size()));
        logger.info(String.format("Current busy resources: ready %d, inited %d, created %d", readyTotal - readyStage.size(), initTotal - initStage.size(), createTotal - createStage.size()));
        return new AvailableResourcesDto(readyStage, initStage, createStage);
    }

    @Override
    public Task getTaskByTaskId(String taskId) {
        Task task = null;

        mutexInstancePool.lock();
        if (this.instancePool.readyInstances.containsKey(taskId)) {
            task = this.instancePool.readyInstances.get(taskId);
        } else if (this.instancePool.initStageInstances.containsKey(taskId)) {
            task = this.instancePool.initStageInstances.get(taskId);
        } else if (this.instancePool.createStageInstances.containsKey(taskId)) {
            task = this.instancePool.createStageInstances.get(taskId);
        }
        mutexInstancePool.unlock();

        return task;
    }

    @Override
    public Task getTaskByInstanceId(String instanceId) {
        return getTaskByTaskId(this.instanceId2TaskId.getOrDefault(instanceId, ""));
    }

    @Override
    public String createInstance(Context ctx, String requestId, Long delayCreateInMs, Long maxIdleTimeInMs) {
        Task task = new Task();
        task.setMaxIdleTimeInMs(maxIdleTimeInMs);
        String taskId = task.getTaskId();

        AtomicReference<LocalDateTime> createSince = new AtomicReference<>();
        AtomicReference<LocalDateTime> initSince = new AtomicReference<>();

        this.instancePool.createStageInstances.put(taskId, task);
        logger.info(String.format("Creating instance with task %s", taskId));

        CompletableFuture.supplyAsync(() -> {
            try {
                logger.info(String.format("Task %s: InstancePool.createInstance invoked, but creation will be delayed by %dms", taskId, delayCreateInMs));
                Thread.sleep(delayCreateInMs);
                logger.info(String.format("Task %s: thread awake, create slot operation submitted...", taskId));
                return platformClient.blockingCreateSlot(ctx, requestId, this.slotResourceConfig);
            } catch (Exception e) {
                this.instancePool.createStageInstances.remove(taskId);
                logger.severe(String.format("Task %s: Failed to create slot!", taskId));
                throw new RuntimeException("Failed to create slot! " + e);
            }
        }).thenApplyAsync((slot) -> {
            try {
                createSince.set(LocalDateTime.now());
                logger.info(String.format("Task %s: create slot operation finished!", taskId));
                mutexInstancePool.lock();
                this.instancePool.createStageInstances.remove(taskId);
                this.instancePool.initStageInstances.put(taskId, task);
                mutexInstancePool.unlock();

                String instanceId = UUID.randomUUID().toString();
                this.instanceId2TaskId.put(instanceId, taskId);
                logger.info(String.format("Task %s: Instance %s initialization submitted...", taskId, instanceId));
                return platformClient.blockingInit(ctx, requestId, instanceId, slot, this.function);
            } catch (Exception e) {
                this.instanceId2TaskId.remove(this.instancePool.initStageInstances.get(taskId).getInstance().getInstanceId());
                this.instancePool.initStageInstances.remove(taskId);
                logger.severe(String.format("Task %s: Failed to init!", taskId));
                throw new RuntimeException("Failed to init! " + e);
            }
        }).thenApplyAsync((instance) -> {
            initSince.set(LocalDateTime.now());
            logger.info(String.format("Task %s: init operation finished!", taskId));
            mutexInstancePool.lock();
            this.instancePool.initStageInstances.remove(taskId);
            this.instancePool.readyInstances.put(taskId, task);
            mutexInstancePool.unlock();

            task.lock();
            task.setInstance(instance);
            if(task.getBusy()) {
                task.getInstance().setBusy(true);
            }
            task.getInstance().setCreateSince(createSince.get());
            task.getInstance().setInitSince(initSince.get());
            task.unlock();

            return instance;
        }).thenAcceptAsync((instance) -> {
            task.lock();
            CallbackFunction<String, Boolean> callback = (CallbackFunction<String, Boolean>) task.getOnFinishMethod();
            if(null == callback) {
                task.unlock();
                return;
            }
            try {
                logger.info(String.format("Task %s: on registered callback method invoked...", taskId));
                callback.setParameter(instance.getInstanceId());
                callback.call();
                logger.info(String.format("Task %s: on registered callback method invocation successfully returned!", taskId));
                // 执行完毕后, 将callback置为null, 防止callback重复执行
                task.setOnFinishMethod(null);
                task.unlock();
            } catch (Exception e) {
                task.unlock();
                logger.severe(String.format("Task: %s: Failed to invoke callback method!", taskId));
                throw new RuntimeException(e);
            }
        }).exceptionally((e) -> {
            logger.info(String.format("Task %s: Recovered from exception \"%s\"", taskId, e.getMessage()));
            return null;
        });

        return taskId;
    }

    @Override
    public Boolean assignInstance(Context ctx, String taskId, CallbackFunction<String, Boolean> callbackMethod) {
        Task task = getTaskByTaskId(taskId);
        if (null == task) {
            logger.warning(String.format("Cannot find task by id %s", taskId));
            return Boolean.FALSE;
        }

        // Step 1: 将task标记为busy
        task.lock();
        // 如果当前task已经被占用了, 则返回失败 (处理并发异常)
        if (task.getBusy()) {
            logger.warning(String.format("Failed to assign instance for task %s, trying to assign a currently busy task!", taskId));
            task.unlock();
            return Boolean.FALSE;
        }

        task.setBusy(true);
        logger.info(String.format("Set task %s busy", taskId));
        if(null != task.getInstance()) {
            // 如果task已经有实例，则需要将instance也标记为busy
            task.getInstance().setBusy(true);
            logger.info(String.format("Set instance %s busy", task.getInstance().getInstanceId()));
        }
        task.unlock();

        // Step 2: 执行或注册回调函数
        mutexInstancePool.lock();
        if (this.instancePool.readyInstances.containsKey(taskId)) {
            // Step 2.1: 如果task已经ready, 直接执行callback函数, 无需将其注册至task
            logger.info(String.format("Task %s is ready now!", taskId));
            CompletableFuture.supplyAsync(() -> {
                try {
                    logger.info(String.format("Task %s: on registered callback method invoked...", taskId));
                    callbackMethod.setParameter(task.getInstance().getInstanceId());
                    callbackMethod.call();
                    logger.info(String.format("Task %s: on registered callback method invocation successfully returned!", taskId));
                } catch (Exception e) {
                    logger.severe(String.format("Task %s: Failed to invoke callback method! %s", taskId, e.getMessage()));
                    throw new RuntimeException("Failed to execute callback method! " + e);
                }
                return true;
            }).exceptionally((e) -> {
                logger.info(String.format("Task %s: Recovered from exception \"%s\"", taskId, e.getMessage()));
                return null;
            });
        } else {
            // Step 2.2: 当前task还未处于ready状态, 则注册回调函数, 等待CompletableFuture调用
            logger.info(String.format("Task %s is not yet ready, registering callback function...", taskId));
            task.lock();
            task.setOnFinishMethod(callbackMethod);
            logger.info(String.format("Task %s: Callback function registered!", taskId));
            task.unlock();
        }
        mutexInstancePool.unlock();

        return Boolean.TRUE;
    }

    @Override
    public Boolean idleInstance(Context ctx, String instanceId) {
        Task task = getTaskByInstanceId(instanceId);
        if (null == task) {
            logger.warning(String.format("Cannot find task by instance id %s", instanceId));
            return Boolean.FALSE;
        }

        task.lock();
        if (!task.getBusy()) {
            task.unlock();
            return Boolean.TRUE;
        }
        task.setBusy(false);
        task.getInstance().setBusy(false);
        task.getInstance().setIdleSince(LocalDateTime.now());
        // todo 设置busyIntervals
        task.unlock();

        logger.info(String.format("Instance %s has been idled!", instanceId));
        return Boolean.TRUE;
    }

    @Override
    public Boolean destroyInstance(Context ctx, String instanceId, String requestId, String reason, CallbackFunction<Boolean, Boolean> callbackMethod) {
        Task task = getTaskByInstanceId(instanceId);
        if (null == task) {
            logger.warning(String.format("Cannot find task by instance id %s", instanceId));
            return Boolean.FALSE;
        }

        // 如果实例正忙, 不可直接destroy, 需先调用idleInstance方法释放实例
        if (task.getBusy()) {
            logger.warning(String.format("Cannot destroy instance %s because it's busy, idle the instance first!", instanceId));
            return Boolean.FALSE;
        }

        // 如果实例不在ready列表中, 说明实例还未初始化完成, 不可摧毁
        this.mutexInstancePool.lock();
        if (!this.instancePool.readyInstances.containsKey(task.getTaskId())) {
            this.mutexInstancePool.unlock();
            logger.warning(String.format("Cannot destroy instance %s because it's still creating!", instanceId));
            return Boolean.FALSE;
        }
        // Step 1: 从ready列表中移除该实例
        this.instancePool.readyInstances.remove(task.getTaskId());
        this.mutexInstancePool.unlock();
        this.instanceId2TaskId.remove(task.getInstance().getInstanceId());
        logger.info(String.format("Removed instance %s from ready list.", instanceId));

        // Step 2: 向Platform提交摧毁请求
        CompletableFuture.supplyAsync(() -> {
            logger.info(String.format("Instance %s destroy request submitted...", instanceId));
            try {
                platformClient.blockingDestroy(ctx, requestId, task.getInstance().getSlot().getId(), reason);
                return true;
            } catch (Exception e) {
                logger.info(String.format("Failed to destroy instance %s", instanceId));
//                throw new RuntimeException("Failed to destroy instance, " + e);
                return false;
            }
        }).thenAcceptAsync((result) -> {
            try {
                logger.info(String.format("Instance %s destroyed! Invoking callback function...", instanceId));
                callbackMethod.setParameter(result);
                callbackMethod.call();
                logger.info("Callback function invoked!");
            } catch (Exception e) {
                logger.severe("Failed to invoke callback method!");
                throw new RuntimeException("Failed to invoke callback method, " + e);
            }
        }).exceptionally((e) -> {
            logger.info(String.format("Recovered from exception \"%s\"", e.getMessage()));
            return null;
        });

        return Boolean.TRUE;
    }

    private void gcLoop() {
        logger.info(String.format("gc loop for app: %s is starting", function.getKey()));
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (useXgboost) {
                    if (System.currentTimeMillis() - lastForecastTimestampInMs >= forecastInterval) {
                        try {
                            forecast.updateForecast();
                        } catch (XGBoostError e) {
                            throw new RuntimeException(e);
                        }
                    }
                }


                for (String taskId : instancePool.readyInstances.keySet()) {
                    Task task = instancePool.readyInstances.getOrDefault(taskId, null);
                    task.lock();
                    if (null == task || task.getBusy()) {
                        task.unlock();
                        continue;
                    }
                    Long idleDuration = Duration.between(task.getInstance().getIdleSince(), LocalDateTime.now()).toMillis();
                    if (idleDuration.longValue() > task.getMaxIdleTimeInMs()) {
                        String reason = String.format("Task %s instance %s idle duration: %dms, exceed configured duration: %dms",
                                taskId, task.getInstance().getInstanceId(), idleDuration, task.getMaxIdleTimeInMs());
                        CompletableFuture.runAsync(() -> {
                            try {
                                platformClient.blockingDestroy(Context.current(), UUID.randomUUID().toString(), task.getInstance().getSlot().getId(), reason);
                                mutexInstancePool.lock();
                                instancePool.readyInstances.remove(taskId);
                                mutexInstancePool.unlock();
                                instanceId2TaskId.remove(task.getInstance().getInstanceId());
                                logger.info(String.format("Instance %s of app %s is GCed due to idle for %dms",
                                        task.getInstance().getInstanceId(), function.getMeta().getKey(), idleDuration));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            } finally {
                                task.unlock();
                            }
                        }).exceptionally((e) -> {
                            logger.severe("Recovered from exception " + e.getMessage());
                            logger.severe("trace: " + ExceptionHandler.getTrace(e));
                            logger.severe("Details: " + ExceptionHandler.getExceptionAllinformation((Exception) e));
                            return null;
                        });
                    } else {
                        task.unlock();
                    }
                }
                logger.info(String.format("Resource Pool Status: readyTotal (busy+idle): %d, initTotal: %d, createTotal: %d", instancePool.readyInstances.size(), instancePool.initStageInstances.size(), instancePool.createStageInstances.size()));
            }
        };

        timer.schedule(timerTask, 0, this.config.getGcInterval().toMillis());
    }

}
