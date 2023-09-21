package org.aliyun.serverless.scaler.pool;

import io.grpc.Context;
import org.aliyun.serverless.model.AvailableResourcesDto;
import org.aliyun.serverless.model.Task;
import org.aliyun.serverless.units.CallbackFunction;

public interface InstancePoolManager {
    AvailableResourcesDto listAvailableResources();

    Task getTaskByTaskId(String taskId);

    Task getTaskByInstanceId(String instanceId);

    String createInstance(Context ctx, String requestId, Long delayCreateInMs, Long maxIdleTimeInMs);

    Boolean assignInstance(Context ctx, String taskId, CallbackFunction<String, Boolean> callbackMethod);

    Boolean idleInstance(Context ctx, String instanceId);

    Boolean destroyInstance(Context ctx, String instanceId, String requestId, String reason, CallbackFunction<Boolean, Boolean> callbackMethod);

}
