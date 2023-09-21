package org.aliyun.serverless.scaler.loadBalancer;

import io.grpc.Context;
import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.SlotResourceConfig;
import org.aliyun.serverless.scaler.CrazyScaler;
import org.aliyun.serverless.scaler.loadBalancer.methods.BalanceMethod;
import org.aliyun.serverless.scaler.loadBalancer.methods.ForecastBalanceMethod;
import org.aliyun.serverless.scaler.loadBalancer.methods.SimpleBalanceMethod;
import org.aliyun.serverless.scaler.pool.LightPoolManagerImpl;
import org.aliyun.serverless.units.ExceptionHandler;
import org.aliyun.serverless.units.RequestRecorder;
import org.aliyun.serverless.units.XgboostAgent;

import org.aliyun.serverless.scaler.pool.LightPoolManager;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class LoadBalancer {

    private final Function function;
    private final SlotResourceConfig slotResourceConfig;
    private final LightPoolManager lightPoolManager;
    private final RequestRecorder assignRequestRecorder;
    private final RequestRecorder idleRequestRecorder;
    private final BalanceMethod balanceMethod;
    private final long maxIdleTimeInMs;

    private static final Logger logger = Logger.getLogger(LoadBalancer.class.getName());
    private final Lock mutex;

    private Context ctx;
    String requestId;
    String reason;

    public Function getFunction() {
        return function;
    }

    public SlotResourceConfig getSlotResourceConfig() {
        return slotResourceConfig;
    }

    public LoadBalancer(Function function, Config config, SlotResourceConfig slotResourceConfig) throws Exception {
        this.function = function;
        this.slotResourceConfig = slotResourceConfig;

        int initDurationInMs = 10000;
        Object initDurationInMsObject = config.getParamsManager().getParamValue("InitDurationInMs." + function.getKey());
        if (initDurationInMsObject != null){
            initDurationInMs = (int) initDurationInMsObject;
        }
        int assignTimeSliceInMs = (int) (((double) config.getParamsManager().getParamValue("LoadBalancer.assignTimeSliceInMsRate")) * initDurationInMs);
        int idleTimeSliceInMs = (int) (((double) config.getParamsManager().getParamValue("LoadBalancer.idleTimeSliceInMsRate")) * initDurationInMs);
        int assignRecordMaxLength = (int) config.getParamsManager().getParamValue("LoadBalancer.assignRecordMaxLength");
        int idleRecordMaxLength = (int) config.getParamsManager().getParamValue("LoadBalancer.idleRecordMaxLength");
        assignRequestRecorder = new RequestRecorder(assignTimeSliceInMs, assignRecordMaxLength);
        idleRequestRecorder = new RequestRecorder(idleTimeSliceInMs, idleRecordMaxLength);


        CreateSeveralInstance createSeveralInstance = new CreateSeveralInstance();
        DestroySeveralIdleInstance destroySeveralIdleInstance = new DestroySeveralIdleInstance();
        List<String> forecastBalanceMethodApplyKeys = (List<String>) config.getParamsManager().getParamValue("ForecastBalanceMethod.applyKeys");
        if (forecastBalanceMethodApplyKeys.contains(function.getKey())) {
            maxIdleTimeInMs = (long) (((double) config.getParamsManager().getParamValue("LoadBalancer.maxIdleTimeInMsRateForecast")) * initDurationInMs);
            int timeSliceCount = (int) config.getParamsManager().getParamValue("ForecastBalanceMethod.timeSliceCount");
            Forecast forecast = new Forecast(assignRequestRecorder, idleRequestRecorder, timeSliceCount, new XgboostAgent(function.getKey()));
            lightPoolManager = new LightPoolManagerImpl(function, config, slotResourceConfig, forecast, true);
            balanceMethod = new ForecastBalanceMethod(function, config, slotResourceConfig, forecast, createSeveralInstance, destroySeveralIdleInstance, lightPoolManager);
        } else {
            maxIdleTimeInMs = (long) (((double) config.getParamsManager().getParamValue("LoadBalancer.maxIdleTimeInMsRateSimple")) * initDurationInMs);
            lightPoolManager = new LightPoolManagerImpl(function, config, slotResourceConfig, null, false);
            balanceMethod = new SimpleBalanceMethod(function, config, slotResourceConfig);
        }

        mutex = new ReentrantLock();
    }

    public class CreateSeveralInstance{
        public Boolean call(int createSum) {
            try {
                for (int i = 0; i < createSum; ++i) {
                    lightPoolManager.createInstance(ctx, requestId, 0L, maxIdleTimeInMs);
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public class DestroySeveralIdleInstance{
        public Boolean call(int destroySum) {
            try {
                lightPoolManager.destroyInstance(ctx, destroySum, requestId);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public void assignInstance(Context ctx, String requestId, CrazyScaler.AssignCallBackFunction assignCallbackFunction) {
        this.ctx = ctx;
        this.requestId = requestId;

        int decideResult = 0;
        try {
            mutex.lock();
            assignRequestRecorder.addRequest(new RequestRecorder.Request(System.currentTimeMillis()));
            logger.info("LoadBalancer is processing the request " + requestId);
            decideResult = this.balanceMethod.decideAssign(lightPoolManager.getPoolStatus().getReadyInstanceNum());
        } catch (Exception e) {
            logger.severe("LoadBalancer encounters exception when processing the assign request " + requestId + " ..." + ExceptionHandler.all(e));
            return;
        } finally {
            mutex.unlock();
        }

        for (int i = 0; i < decideResult; i++) {
            lightPoolManager.createInstance(ctx, requestId, 0L, maxIdleTimeInMs);
        }

        if (!lightPoolManager.assignInstance(ctx, assignCallbackFunction)) {
            logger.info(String.format("Call instancePoolManager.assignInstance with requestId %s return false, try again.", requestId));
            assignInstance(ctx, requestId, assignCallbackFunction);
        }
    }

    public void idleInstance(Context ctx, String instanceId, String requestId, String reason, Boolean needDestroy,
                             CrazyScaler.IdleCallBackFunction idleCallBackFunction){
        this.ctx = ctx;
        this.requestId = requestId;
        this.reason = reason;

        boolean decideResult = needDestroy;
        if (!needDestroy) {
            try {
                mutex.lock();
                idleRequestRecorder.addRequest(new RequestRecorder.Request(System.currentTimeMillis()));
                decideResult = this.balanceMethod.decideIdle(lightPoolManager.getPoolStatus().getReadyInstanceNum());
            } catch (Exception e) {
                logger.severe("LoadBalancer encounters exception when processing the idle request " + requestId + " ..." + e.getMessage());
                return;
            } finally {
                mutex.unlock();
            }
        }

        this.lightPoolManager.idleInstance(ctx, instanceId, idleCallBackFunction, decideResult);
    }
}
