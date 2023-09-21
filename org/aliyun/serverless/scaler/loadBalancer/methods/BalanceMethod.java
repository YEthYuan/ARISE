package org.aliyun.serverless.scaler.loadBalancer.methods;

import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.model.AvailableResourcesDto;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.SlotResourceConfig;

import java.util.List;

public abstract class BalanceMethod {
    protected final Function function;
    protected final Config config;
    protected final SlotResourceConfig slotResourceConfig;

    public BalanceMethod(Function function, Config config, SlotResourceConfig slotResourceConfig) {
        this.function = function;
        this.config = config;
        this.slotResourceConfig = slotResourceConfig;
    }

    abstract public int decideAssign(int idleInstanceSum);

    abstract public boolean decideIdle(int idleInstanceSum);
}
