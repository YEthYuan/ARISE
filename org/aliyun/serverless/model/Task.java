package org.aliyun.serverless.model;


import org.aliyun.serverless.config.ParamsManager;
import org.aliyun.serverless.units.CallbackFunction;

import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class Task {
    private static final Logger logger = Logger.getLogger(Task.class.getName());
    private final String taskId;
    private Instance instance;
    private CallbackFunction<?, ?> onFinishMethod;

    private Boolean busy;

    private Long maxIdleTimeInMs;

    private final Lock mutex;

    public Task() {
        this(UUID.randomUUID().toString());
    }

    public Task(String taskId) {
        ParamsManager paramsManager = ParamsManager.getManager();
        this.mutex = new ReentrantLock();
        this.taskId = taskId;
        this.busy = false;
        Integer defaultMaxIdleTimeInMs = (Integer) paramsManager.getParamValue("fixParamInstancePool.defaultMaxIdleTimeInMs");
        if (null == defaultMaxIdleTimeInMs) {
            defaultMaxIdleTimeInMs = 50000;
        }
        this.maxIdleTimeInMs = defaultMaxIdleTimeInMs.longValue();
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public CallbackFunction<?, ?> getOnFinishMethod() {
        return onFinishMethod;
    }

    public void setOnFinishMethod(CallbackFunction<?, ?> onFinishMethod) {
        this.onFinishMethod = onFinishMethod;
    }
    public String getTaskId() {
        return taskId;
    }

    public Boolean getBusy() {
        return busy;
    }

    public void setBusy(Boolean busy) {
        this.busy = busy;
    }

    public void lock(){
        this.mutex.lock();
    }

    public void unlock(){
        this.mutex.unlock();
    }

    public Long getMaxIdleTimeInMs() {
        return maxIdleTimeInMs;
    }

    public void setMaxIdleTimeInMs(Long maxIdleTimeInMs) {
        this.maxIdleTimeInMs = maxIdleTimeInMs;
    }
}
