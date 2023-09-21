package org.aliyun.serverless.scaler.loadBalancer.methods;

import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.SlotResourceConfig;
import org.aliyun.serverless.scaler.loadBalancer.Forecast;
import org.aliyun.serverless.scaler.loadBalancer.LoadBalancer;
import org.aliyun.serverless.scaler.pool.LightPoolManager;

public class ForecastBalanceMethod extends BalanceMethod {
    private final Forecast forecast;
    private final LoadBalancer.CreateSeveralInstance createSeveralInstance;
    private final LoadBalancer.DestroySeveralIdleInstance destroySeveralIdleInstance;
    private final LightPoolManager lightPoolManager;
    private final double reservedStageRate;
    private final HandleForecast handleForecast;


    public ForecastBalanceMethod(Function function, Config config, SlotResourceConfig slotResourceConfig,
                                 Forecast forecast,
                                 LoadBalancer.CreateSeveralInstance createSeveralInstance,
                                 LoadBalancer.DestroySeveralIdleInstance destroySeveralIdleInstance,
                                 LightPoolManager lightPoolManager) {
        super(function, config, slotResourceConfig);
        this.forecast = forecast;
        this.createSeveralInstance = createSeveralInstance;
        this.destroySeveralIdleInstance = destroySeveralIdleInstance;
        this.lightPoolManager = lightPoolManager;

        reservedStageRate = (double) config.getParamsManager().getParamValue("ForecastBalanceMethod.reservedStageRate");
        handleForecast = new HandleForecast();
    }

    public class HandleForecast {
        private int idleInstanceLowStage;
        private int idleInstanceHighStage;

        public int getIdleInstanceLowStage() {
            return idleInstanceLowStage;
        }

        public void setIdleInstanceLowStage(int idleInstanceLowStage) {
            this.idleInstanceLowStage = idleInstanceLowStage;
        }

        public int getIdleInstanceHighStage() {
            return idleInstanceHighStage;
        }

        public void setIdleInstanceHighStage(int idleInstanceHighStage) {
            this.idleInstanceHighStage = idleInstanceHighStage;
        }

        public Boolean call() {
            try {
                int forecastAssignCount = forecast.getForecastAssignCount();
                int forecastIdleCount = forecast.getForecastIdleCount();
                int curIdleInstanceSum = lightPoolManager.getPoolStatus().getReadyInstanceNum();
                int nearlyTimeoutInstanceSum = lightPoolManager.getPoolStatus().getTotalIdleInstanceNumGcInOneInitWindow();
                int reliableIdleInstanceSum = curIdleInstanceSum - nearlyTimeoutInstanceSum + (int) (forecastIdleCount * 0.5);
                int reservedStage = (int) Math.max(forecastAssignCount * reservedStageRate, 1);
                idleInstanceLowStage = forecastAssignCount + reservedStage;
                idleInstanceHighStage = Math.max(2 * forecastAssignCount - forecastIdleCount + reservedStage, idleInstanceLowStage);

                if (reliableIdleInstanceSum > idleInstanceHighStage) {
                    destroySeveralIdleInstance.call(reliableIdleInstanceSum - idleInstanceHighStage);
                } else if (reliableIdleInstanceSum < idleInstanceLowStage) {
                    createSeveralInstance.call(idleInstanceLowStage - reliableIdleInstanceSum);
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }


    @Override
    public int decideAssign(int idleInstanceSum) {
        return 1;
    }

    @Override
    public boolean decideIdle(int idleInstanceSum) {
        if (idleInstanceSum > handleForecast.idleInstanceHighStage) {
            destroySeveralIdleInstance.call(idleInstanceSum - handleForecast.idleInstanceHighStage);
            return true;
        } else if (idleInstanceSum <= handleForecast.idleInstanceLowStage) {
            return false;
        } else {
            return true;
        }
    }
}
