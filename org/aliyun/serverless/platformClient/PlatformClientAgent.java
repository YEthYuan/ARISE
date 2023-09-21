package org.aliyun.serverless.platformClient;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import io.grpc.Context;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.Instance;
import org.aliyun.serverless.model.Slot;
import org.aliyun.serverless.model.SlotResourceConfig;

import java.util.logging.Logger;

public class PlatformClientAgent {
    private static final Logger logger = Logger.getLogger(PlatformClientAgent.class.getName());

    private final PlatformClient platformClient;

    public PlatformClientAgent(String host, Integer port) throws Exception {
        platformClient = new PlatformClient(host, port);
    }

    public Slot blockingCreateSlot(Context ctx, String requestId, SlotResourceConfig slotResourceConfig) throws Exception {
        ListenableFuture<Slot> slotFuture = platformClient.CreateSlot(ctx, requestId, slotResourceConfig);
        Slot slot = slotFuture.get();
        logger.info("Slot created, slotId: " + slot.getId());
        return slot;
    }

    public Instance blockingInit(Context ctx, String requestId, String instanceId, Slot slot, Function function) throws Exception {
        ListenableFuture<Instance> instanceFuture = platformClient.Init(ctx, requestId, instanceId, slot, function);
        Instance instance = instanceFuture.get();
        logger.info(String.format("Slot %s inited within instance %s", slot.getId(), instance.getInstanceId()));
        return instance;
    }

    public void blockingDestroy(Context ctx, String requestId, String slotId, String reason) throws Exception {
        ListenableFuture<Empty> future = platformClient.DestroySLot(ctx, requestId, slotId, reason);
        future.get();
        logger.info(String.format("Slot %s destroyed", slotId));
        return;
    }

}
