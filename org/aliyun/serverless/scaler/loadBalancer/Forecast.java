package org.aliyun.serverless.scaler.loadBalancer;

import ml.dmlc.xgboost4j.java.XGBoostError;
import org.aliyun.serverless.units.RequestRecorder;
import org.aliyun.serverless.units.XgboostAgent;

public class Forecast {
    private final RequestRecorder assignRequestRecorder;
    private final RequestRecorder idleRequestRecorder;
    private final int timeSliceCount;
    private final XgboostAgent xgboostAgent;
    private int forecastAssignCount;
    private int forecastIdleCount;


    public int getForecastAssignCount() {
        return forecastAssignCount;
    }

    public int getForecastIdleCount() {
        return forecastIdleCount;
    }

    Forecast(RequestRecorder assignRequestRecorder, RequestRecorder idleRequestRecorder, int timeSliceCount, XgboostAgent xgboostAgent) {
        this.assignRequestRecorder = assignRequestRecorder;
        this.idleRequestRecorder = idleRequestRecorder;
        this.timeSliceCount = timeSliceCount;
        this.xgboostAgent = xgboostAgent;
        this.forecastAssignCount = 0;
        this.forecastIdleCount = 0;
    }

    public void updateForecast() throws XGBoostError {
        forecastAssignCount = xgboostAgent.predict(assignRequestRecorder.getRecentRequestCount(timeSliceCount, System.currentTimeMillis()), XgboostAgent.XgboostAgentType.ASSIGN);
        forecastIdleCount = xgboostAgent.predict(idleRequestRecorder.getRecentRequestCount(timeSliceCount, System.currentTimeMillis()), XgboostAgent.XgboostAgentType.IDLE);
    }
}
