package org.aliyun.serverless.scaler;

import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.Stats;
import org.aliyun.serverless.model.hacker.MetaHackerDo;
import org.aliyun.serverless.model.hacker.RequestHackerDo;
import org.codehaus.jackson.map.ObjectMapper;
import protobuf.SchedulerProto;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class ScalerImpl implements Scaler {

    private static final Logger logger = Logger.getLogger(ScalerImpl.class.getName());
    private static final Logger hacker = Logger.getLogger("#MARKER#");
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final Config config;
    private final Function function;

    public ScalerImpl(Function function, Config config) {
        try {
            this.config = config;
            this.function = function;

            StringBuffer logPath = new StringBuffer();
            logPath.append("./logs");
            File logDir = new File(logPath.toString());
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            logPath.append("/").append(sdf.format(new Date())).append(".log");
            FileHandler fileHandler = new FileHandler(logPath.toString(), true);
            hacker.addHandler(fileHandler);

            logger.info(String.format("New scaler for app: %s is created", function.getKey()));
        } catch (Exception e) {
            throw new RuntimeException("failed to create Simple scaler", e);
        }
    }

    @Override
    public void Assign(Context ctx, SchedulerProto.AssignRequest request, StreamObserver<SchedulerProto.AssignReply> responseObserver) throws Exception {
        logger.info("In the assign method");

        String requestId = request.getRequestId();
        Long timestamp = request.getTimestamp();

        SchedulerProto.Meta meta = request.getMetaData();
        MetaHackerDo metaHackerDo = new MetaHackerDo(meta.getKey(), meta.getRuntime(), meta.getTimeoutInSecs(), meta.getMemoryInMb());
        hacker.info("#####META#####" + JSON_MAPPER.writeValueAsString(metaHackerDo));

        RequestHackerDo requestHackerDo = new RequestHackerDo(requestId, "assign", timestamp, System.currentTimeMillis(), Long.MAX_VALUE, meta.getKey(), Integer.MAX_VALUE);
        hacker.info("#####REQUEST#####" + JSON_MAPPER.writeValueAsString(requestHackerDo));

        return;
    }

    @Override
    public void Idle(Context ctx, SchedulerProto.IdleRequest request, StreamObserver<SchedulerProto.IdleReply> responseObserver) throws Exception {
        logger.info("In the idle method");

        SchedulerProto.Assignment assignment = request.getAssigment();
        SchedulerProto.Result result = request.getResult();

        String requestId = assignment.getRequestId();
        String metaKey = assignment.getMetaKey();
        Integer statusCode = result.getStatusCode();
        Long durationInMs = result.getDurationInMs();

        RequestHackerDo requestHackerDo = new RequestHackerDo(requestId, "idle", Long.MAX_VALUE, System.currentTimeMillis(), durationInMs, metaKey, statusCode);
        hacker.info("#####REQUEST#####" + JSON_MAPPER.writeValueAsString(requestHackerDo));

        return;
    }

    @Override
    public Stats Stats() {
        return null;
    }
}
