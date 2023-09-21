package org.aliyun.serverless.model;

public class LightPoolStatusDto {
    private Integer totalInstanceNum;
    private Integer readyInstanceNum;
    private Integer busyInstanceNum;
    private Integer queuedAssignRequestNum;
    // 下一个init周期内即将被gc的instance数量
    private Integer totalIdleInstanceNumGcInOneInitWindow;

    public LightPoolStatusDto(Integer totalInstanceNum, Integer readyInstanceNum, Integer busyInstanceNum, Integer queuedAssignRequestNum) {
        this.totalInstanceNum = totalInstanceNum;
        this.readyInstanceNum = readyInstanceNum;
        this.busyInstanceNum = busyInstanceNum;
        this.queuedAssignRequestNum = queuedAssignRequestNum;
    }

    public LightPoolStatusDto() {
    }

    public Integer getTotalInstanceNum() {
        return totalInstanceNum;
    }

    public void setTotalInstanceNum(Integer totalInstanceNum) {
        this.totalInstanceNum = totalInstanceNum;
    }

    public Integer getReadyInstanceNum() {
        return readyInstanceNum;
    }

    public void setReadyInstanceNum(Integer readyInstanceNum) {
        this.readyInstanceNum = readyInstanceNum;
    }

    public Integer getBusyInstanceNum() {
        return busyInstanceNum;
    }

    public void setBusyInstanceNum(Integer busyInstanceNum) {
        this.busyInstanceNum = busyInstanceNum;
    }

    public Integer getQueuedAssignRequestNum() {
        return queuedAssignRequestNum;
    }

    public void setQueuedAssignRequestNum(Integer queuedAssignRequestNum) {
        this.queuedAssignRequestNum = queuedAssignRequestNum;
    }

    public Integer getTotalIdleInstanceNumGcInOneInitWindow() {
        return totalIdleInstanceNumGcInOneInitWindow;
    }

    public void setTotalIdleInstanceNumGcInOneInitWindow(Integer totalIdleInstanceNumGcInOneInitWindow) {
        this.totalIdleInstanceNumGcInOneInitWindow = totalIdleInstanceNumGcInOneInitWindow;
    }
}
