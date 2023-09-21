package org.aliyun.serverless.scaler.loadBalancer.methods;

import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.SlotResourceConfig;

public class SimpleBalanceMethod extends BalanceMethod {
    public SimpleBalanceMethod(Function function, Config config, SlotResourceConfig slotResourceConfig) {
        super(function, config, slotResourceConfig);
    }

    public int decideAssign(int idleInstanceSum) {
        return 1;
    }

    @Override
    public boolean decideIdle(int idleInstanceSum) {
        return false;
    }
}
