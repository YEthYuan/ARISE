package org.aliyun.serverless.model.hacker;

public class MetaHackerDo {
    public String key;
    public String runtime;
    public Integer timeoutInSecs;
    public Long memoryInMb;

    public Long initDurationInMs;

    public MetaHackerDo(String key, String runtime, Integer timeoutInSecs, Long memoryInMb) {
        this.key = key;
        this.runtime = runtime;
        this.timeoutInSecs = timeoutInSecs;
        this.memoryInMb = memoryInMb;
    }

    public MetaHackerDo(String key, String runtime, Integer timeoutInSecs, Long memoryInMb, Long initDurationInMs) {
        this.key = key;
        this.runtime = runtime;
        this.timeoutInSecs = timeoutInSecs;
        this.memoryInMb = memoryInMb;
        this.initDurationInMs = initDurationInMs;
    }
}
