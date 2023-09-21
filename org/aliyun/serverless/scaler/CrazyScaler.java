package org.aliyun.serverless.scaler;

import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.Instance;
import org.aliyun.serverless.model.SlotResourceConfig;
import org.aliyun.serverless.model.Stats;
import org.aliyun.serverless.scaler.loadBalancer.LoadBalancer;
import org.aliyun.serverless.units.CallbackFunction;
import org.codehaus.jackson.map.ObjectMapper;
import protobuf.SchedulerProto;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class CrazyScaler implements Scaler {
    private static final Logger logger = Logger.getLogger(ScalerImpl.class.getName());
    private static final Logger hacker = Logger.getLogger("#MARKER#");
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final Config config;
    private final Function function;
    private final Lock mu;
    private final CountDownLatch wg;
    private final Map<String, Instance> instances;
    private final Deque<Instance> idleInstances;
    private final LoadBalancer loadbalancer;

    public CrazyScaler(Function function, Config config, SlotResourceConfig slotResourceConfig) {
        try {
            this.config = config;
            this.function = function;
            this.mu = new ReentrantLock();
            this.wg = new CountDownLatch(1);
            this.instances = new ConcurrentHashMap<>();
            this.idleInstances = new LinkedList<>();
            this.loadbalancer = new LoadBalancer(function, config, slotResourceConfig);
            ///// Hack Data /////
//            StringBuffer logPath = new StringBuffer();
//            logPath.append("./logs");
//            File logDir = new File(logPath.toString());
//            if (!logDir.exists()) {
//                logDir.mkdirs();
//            }
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            logPath.append("/").append(sdf.format(new Date())).append(".log");
//            FileHandler fileHandler = new FileHandler(logPath.toString(), true);
//            hacker.addHandler(fileHandler);
            ///// End of Hack Data /////

            logger.info(String.format("New scaler for app: %s is created", function.getKey()));
        } catch (Exception e) {
            throw new RuntimeException("failed to create Simple scaler", e);
        }
    }

    @Override
    public void Assign(Context ctx, SchedulerProto.AssignRequest request, StreamObserver<SchedulerProto.AssignReply> responseObserver) throws Exception {
        HackAssignRequestData(request);
        Instant start = Instant.now();
        logger.info("Start assign, request id: " + request.getRequestId());
        // use load balance interface and give it callback function
        AssignCallBackFunction assignReplyCallback = new AssignCallBackFunction(request.getRequestId(),
                request.getMetaData().getKey(), ctx, responseObserver);
        logger.info("Before assignInstance");
        loadbalancer.assignInstance(ctx, request.getRequestId(), assignReplyCallback);
        logger.info(String.format("Finish assign, request id: %s, cost %dms",
                request.getRequestId(), Duration.between(start, Instant.now()).toMillis()));
//        try {
//
//        } catch (Exception e) {
//            String errorMessage = String.format("Failed to assign instance, request id: %s due to %s", request.getRequestId(), e.getMessage());
//            logger.severe(errorMessage);
//            responseObserver.onError(new RuntimeException(errorMessage, e));
//        }
    }

    @Override
    public void Idle(Context ctx, SchedulerProto.IdleRequest request, StreamObserver<SchedulerProto.IdleReply> responseObserver) throws Exception {
        HackIdleRequestData(request);

        if (!request.getAssigment().isInitialized()) {
            logger.warning(String.format("Request %s assignment is null, metaKey %s", request.getAssigment().getRequestId(), request.getAssigment().getMetaKey()));
            responseObserver.onError(new RuntimeException("assignment is null"));
            return;
        }

        long start = System.currentTimeMillis();

        logger.info(String.format("Start idle, request id: %s", request.getAssigment().getRequestId()));

        // use load balance interface and give it callback function
        IdleCallBackFunction idleReplyCallBack = new IdleCallBackFunction(responseObserver);
        loadbalancer.idleInstance(ctx, request.getAssigment().getInstanceId(), request.getAssigment().getRequestId(), request.getResult()
                .getReason(), request.getResult().getNeedDestroy(), idleReplyCallBack);

        long cost = System.currentTimeMillis() - start;
        logger.info(String.format("Idle, request id: %s, instance: %s, cost %dus%n",
                request.getAssigment().getRequestId(), request.getAssigment().getInstanceId(), cost));

//        try {
//
//        } catch (Exception e) {
//            String errorMessage = String.format("idle failed with: %s", e.getMessage());
//            logger.severe(errorMessage);
//            SchedulerProto.IdleReply.Builder replyBuilder = SchedulerProto.IdleReply.newBuilder()
//                .setStatus(SchedulerProto.Status.InternalError);
//            responseObserver.onNext(replyBuilder
//                .setStatus(SchedulerProto.Status.InternalError)
//                .setErrorMessage(errorMessage)
//                .build());
//            responseObserver.onCompleted();
//        } finally {
//
//        }
    }

    public Stats Stats() {
        mu.lock();
        Stats stats = new Stats();
        stats.setTotalInstance(instances.size());
        stats.setTotalIdleInstance(idleInstances.size());
        mu.unlock();

        return stats;
    }

    public void HackAssignRequestData(SchedulerProto.AssignRequest request) throws IOException {
//        logger.info("In the assign method");
//
//        String requestId = request.getRequestId();
//        Long timestamp = request.getTimestamp();
//
//        SchedulerProto.Meta metahack = request.getMetaData();
//        MetaHackerDo metaHackerDo = new MetaHackerDo(metahack.getKey(), metahack.getRuntime(), metahack.getTimeoutInSecs(), metahack.getMemoryInMb());
//        hacker.info("#####META#####" + JSON_MAPPER.writeValueAsString(metaHackerDo));
//
//        RequestHackerDo requestHackerDo = new RequestHackerDo(requestId, "assign", timestamp, System.currentTimeMillis(), Long.MAX_VALUE, metahack.getKey(), Integer.MAX_VALUE);
//        hacker.info("#####REQUEST#####" + JSON_MAPPER.writeValueAsString(requestHackerDo));

    }

    public void HackIdleRequestData(SchedulerProto.IdleRequest request) throws IOException {

//        logger.info("In the idle method");
//
//        SchedulerProto.Assignment assignment = request.getAssigment();
//        SchedulerProto.Result result = request.getResult();
//
//        String requestId = assignment.getRequestId();
//        String metaKey = assignment.getMetaKey();
//        Integer statusCode = result.getStatusCode();
//        Long durationInMs = result.getDurationInMs();
//
//        RequestHackerDo requestHackerDo = new RequestHackerDo(requestId, "idle", Long.MAX_VALUE, System.currentTimeMillis(), durationInMs, metaKey, statusCode);
//        hacker.info("#####REQUEST#####" + JSON_MAPPER.writeValueAsString(requestHackerDo));
        ///// End of Hack Data /////
    }


    public class AssignCallBackFunction extends CallbackFunction<String, Boolean> {
        /*
         * The callback function for load balance to use
         * Send AssignReply to serverless-simulator
         * */
        private final Logger logger = Logger.getLogger(AssignCallBackFunction.class.getName());
        private final String requestId;
        private final String metaKey;
        private final Context context;
        private final StreamObserver<SchedulerProto.AssignReply> responseObserver;

        public AssignCallBackFunction(String requestId, String metaKey, Context context, StreamObserver<SchedulerProto.AssignReply> responseObserver) {
            this.requestId = requestId;
            this.metaKey = metaKey;
            this.context = context;
            this.responseObserver = responseObserver;
        }

        public String getRequestId() {
            return requestId;
        }

        @Override
        public void setParameter (String instanceId) {
            super.setParameter(instanceId);
        }

        @Override
        public String getParameter() {
            return super.getParameter();
        }

        @Override
        public Boolean call() throws Exception {
            Context context = this.context;
            String instanceId = this.getParameter();
            logger.info(String.format("Invoking AssignCallBack of request %s with instance %s", requestId, instanceId));
            SchedulerProto.Assignment assignment = SchedulerProto.Assignment.newBuilder()
                    .setRequestId(requestId).setMetaKey(metaKey)
                    .setInstanceId(instanceId).build();
            logger.info("Before onNext");
            this.responseObserver.onNext(SchedulerProto.AssignReply.newBuilder().setStatus(SchedulerProto.Status.Ok).setAssigment(assignment).build());
            logger.info("Before onCompleted");
            this.responseObserver.onCompleted();
            logger.info("callback: Instance " + this.getParameter() + " send AssignReply Successfully");
            return true;
//            try {
//                String instanceId = this.getParameter();
//                logger.info(String.format("Invoking AssignCallBack of request %s with instance %s", requestId, instanceId));
//                SchedulerProto.Assignment assignment = SchedulerProto.Assignment.newBuilder()
//                    .setRequestId(requestId).setMetaKey(metaKey)
//                    .setInstanceId(instanceId).build();
//                this.responseObserver.onNext(SchedulerProto.AssignReply.newBuilder().setStatus(SchedulerProto.Status.Ok).setAssigment(assignment).build());
//                this.responseObserver.onCompleted();
//                logger.info("callback: Instance " + this.getParameter() + " send AssignReply Successfully");
//                return true;
//            }  catch (Exception e) {
//                logger.severe("callback: Instance " + this.getParameter() + " failed to send AssignReply");
//                return false;
//            }

        }
    }


    public class IdleCallBackFunction extends CallbackFunction<Boolean, Boolean> {
        /*
         * The callback function for load balance to use
         * Send IdleReply to serverless-simulator
         * */
        private final Logger logger = Logger.getLogger(IdleCallBackFunction.class.getName());
        private final StreamObserver<SchedulerProto.IdleReply> responseObserver;

        public IdleCallBackFunction(StreamObserver<SchedulerProto.IdleReply> responseObserver) {
            this.responseObserver = responseObserver;
        }

        @Override
        public void setParameter (Boolean releaseOrNot) {
            super.setParameter(releaseOrNot);
        }

        @Override
        public Boolean getParameter() {
            return super.getParameter();
        }

        @Override
        public Boolean call() throws Exception {
            SchedulerProto.IdleReply.Builder replyBuilder = SchedulerProto.IdleReply.newBuilder()
                    .setStatus(SchedulerProto.Status.Ok);
            this.responseObserver.onNext(replyBuilder.build());
            this.responseObserver.onCompleted();
            logger.info("callback: send AssignReply Successfully");
            return true;
//            try {
//
//            }
//            catch (Exception e) {
//                logger.severe("callback:  failed to send IdleReply");
//                return false;
//            }

        }
    }
}