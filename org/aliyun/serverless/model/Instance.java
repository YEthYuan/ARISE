package org.aliyun.serverless.model;

import org.aliyun.serverless.config.ParamsManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Instance {
    private static final ParamsManager paramsManager = ParamsManager.getManager();
    private String instanceId;
    private Slot slot;
    private Function meta;
    private Long createSinceInMs; // 不再使用, 请使用下面的createSince字段!
    private Long initDurationInMs;
    private Long maxIdleTimeInMs;
    private Boolean busy;
    private LocalDateTime createSince;
    private LocalDateTime initSince;
    private LocalDateTime idleSince;
    private List<TimeInterval> busyIntervals;
    private final Lock mutex;

    public Instance() {
        // set default values to instance
        this.instanceId = "";
        this.createSinceInMs = System.currentTimeMillis();
        this.initDurationInMs = 0L;
        this.busy = false;
        this.createSince = LocalDateTime.now();
        this.initSince = LocalDateTime.now();
        this.idleSince = LocalDateTime.now();
        this.busyIntervals = new ArrayList<>();
        this.mutex = new ReentrantLock();

        this.maxIdleTimeInMs = 5 * 60 * 1000L;
        Integer defaultMaxIdleTimeInSec = (Integer) paramsManager.getParamValue("lightPool.defaultMaxInstanceIdleTimeInSec");
        if (null != defaultMaxIdleTimeInSec) {
            this.maxIdleTimeInMs = defaultMaxIdleTimeInSec * 1000L;
        }
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public Function getMeta() {
        return meta;
    }

    public void setMeta(Function meta) {
        this.meta = meta;
    }

    @Deprecated
    public Long getCreateSinceInMs() {
        return createSinceInMs;
    }

    @Deprecated
    public void setCreateSinceInMs(Long createSinceInMs) {
        this.createSinceInMs = createSinceInMs;
    }

    public Long getInitDurationInMs() {
        return initDurationInMs;
    }

    public void setInitDurationInMs(Long initDurationInMs) {
        this.initDurationInMs = initDurationInMs;
    }

    public Boolean getBusy() {
        return busy;
    }

    public void setBusy(Boolean busy) {
        this.busy = busy;
    }

    public LocalDateTime getIdleSince() {
        return idleSince;
    }

    public void setIdleSince(LocalDateTime idleSince) {
        this.idleSince = idleSince;
    }

    public LocalDateTime getCreateSince() {
        return createSince;
    }

    public void setCreateSince(LocalDateTime createSince) {
        this.createSince = createSince;
    }

    public LocalDateTime getInitSince() {
        return initSince;
    }

    public void setInitSince(LocalDateTime initSince) {
        this.initSince = initSince;
    }

    public List<TimeInterval> getBusyIntervals() {
        return busyIntervals;
    }

    public void setBusyIntervals(List<TimeInterval> busyIntervals) {
        this.busyIntervals = busyIntervals;
    }

    public Long getMaxIdleTimeInMs() {
        return maxIdleTimeInMs;
    }

    public void setMaxIdleTimeInMs(Long maxIdleTimeInMs) {
        this.maxIdleTimeInMs = maxIdleTimeInMs;
    }

    public void lock() {
        this.mutex.lock();
    }

    public void unlock() {
        this.mutex.unlock();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance instance = (Instance) o;
        return Objects.equals(instanceId, instance.instanceId) && Objects.equals(slot, instance.slot) && Objects.equals(meta, instance.meta) && Objects.equals(createSinceInMs, instance.createSinceInMs) && Objects.equals(initDurationInMs, instance.initDurationInMs) && Objects.equals(busy, instance.busy) && Objects.equals(idleSince, instance.idleSince);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId, slot, meta, createSinceInMs, initDurationInMs, busy, idleSince);
    }
}
