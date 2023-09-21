package org.aliyun.serverless.scaler.pool;

import io.grpc.Context;
import org.aliyun.serverless.model.LightPoolStatusDto;
import org.aliyun.serverless.scaler.CrazyScaler;

public interface LightPoolManager {
    LightPoolStatusDto getPoolStatus();

    Boolean createInstance(Context ctx, String requestId, Long delayCreateInMs, Long maxIdleTimeInMs);

    Boolean assignInstance(Context ctx, CrazyScaler.AssignCallBackFunction callbackMethod);

    Boolean idleInstance(Context ctx, String instanceId, CrazyScaler.IdleCallBackFunction callbackMethod, Boolean needDestroy);

    Boolean destroyInstance(Context ctx, Integer destroyNum, String reason);

}
