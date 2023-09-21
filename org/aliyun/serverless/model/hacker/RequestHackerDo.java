package org.aliyun.serverless.model.hacker;

public class RequestHackerDo {
    public String requestId;
    public String action;
    public Long timestamp;
    public Long currentTimeMillis;
    public Long durationInMs;
    public String metaKey;
    public Integer statusCode;

    public RequestHackerDo(String requestId, String action, Long timestamp, Long currentTimeMillis, Long durationInMs, String metaKey, Integer statusCode) {
        this.requestId = requestId;
        this.action = action;
        this.timestamp = timestamp;
        this.currentTimeMillis = currentTimeMillis;
        this.durationInMs = durationInMs;
        this.metaKey = metaKey;
        this.statusCode = statusCode;
    }
}
