package org.aliyun.serverless.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


// TODO 未来可以把Config这个类彻底override掉, 现在还留着主要是为了兼容历史遗留代码, 请使用ParamsManager来获取配置
public class Config {

    private String platformHost;
    private Integer platformPort;
    private Duration gcInterval;
    private Duration idleDurationBeforeGC;

    public Config() {
        ParamsManager paramsMgr = getParamsManager();
        this.platformHost = (String) paramsMgr.getParamValue("general.platformHost");
        this.platformPort = (Integer) paramsMgr.getParamValue("general.platformPort");
        this.gcInterval = Duration.ofMillis((Integer) paramsMgr.getParamValue("fixParamInstancePool.gcLoopInMs"));
        this.idleDurationBeforeGC = Duration.ofMinutes(5);
    }

    public ParamsManager getParamsManager() {
        return ParamsManager.getManager();
    }

    public String getPlatformHost() {
        return platformHost;
    }

    public void setPlatformHost(String platformHost) {
        this.platformHost = platformHost;
    }

    public Integer getPlatformPort() {
        return platformPort;
    }

    public void setPlatformPort(Integer platformPort) {
        this.platformPort = platformPort;
    }

    public Duration getGcInterval() {
        return gcInterval;
    }

    public void setGcInterval(Duration gcInterval) {
        this.gcInterval = gcInterval;
    }

    public Duration getIdleDurationBeforeGC() {
        return idleDurationBeforeGC;
    }

    public void setIdleDurationBeforeGC(Duration idleDurationBeforeGC) {
        this.idleDurationBeforeGC = idleDurationBeforeGC;
    }

    public static final Config DEFAULT_CONFIG = new Config();
}
