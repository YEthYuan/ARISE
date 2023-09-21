package org.aliyun.serverless.server;

import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.manager.Manager;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.scaler.Scaler;
import org.aliyun.serverless.units.ExceptionHandler;
import protobuf.ScalerGrpc;
import protobuf.SchedulerProto;

import java.util.logging.Logger;

public class Server extends ScalerGrpc.ScalerImplBase {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private Manager mgr;

    public Server() {
        this.mgr = new Manager(Config.DEFAULT_CONFIG);
    }

    @Override
    public void assign(SchedulerProto.AssignRequest request, StreamObserver<SchedulerProto.AssignReply> responseObserver) {
        SchedulerProto.Meta meta = request.getMetaData();
        if (!meta.isInitialized()) {
            logger.warning(String.format("meta %s is not initialized, respond error!", meta.getKey()));
            responseObserver.onError(new RuntimeException("app meta is nil"));
            return;
        }

        Function function = new Function(meta);
        Scaler scaler = this.mgr.GetOrCreate(function, meta);
        try {
            logger.info(String.format("Request %s for app %s is sending to scaler.", request.getRequestId(), meta.getKey()));
            scaler.Assign(Context.current(), request, responseObserver);
            logger.info(String.format("Request %s of app %s has been successfully handled!", request.getRequestId(), meta.getKey()));
        } catch (Exception e) {
            logger.severe(String.format("request %s responseObserver error for app %s, caused by %s", request.getRequestId(), meta.getKey(), e.getMessage()));
            logger.severe("trace: " + ExceptionHandler.getTrace(e));
            logger.severe("Details: " + ExceptionHandler.getExceptionAllinformation(e));
            responseObserver.onError(e);
        }
    }

    @Override
    public void idle(SchedulerProto.IdleRequest request, StreamObserver<SchedulerProto.IdleReply> responseObserver) {
        SchedulerProto.Assignment assignment = request.getAssigment();
        if (!assignment.isInitialized()) {
            responseObserver.onError(new RuntimeException("assignment is nil"));
            return;
        }

        String metaKey = request.getAssigment().getMetaKey();
        try {
            Scaler scaler = this.mgr.Get(metaKey);
            scaler.Idle(Context.current(), request, responseObserver);
        } catch (Exception e) {
            logger.severe(String.format("idle responseObserver error for app %s, caused by %s", metaKey, e.getMessage()));
            logger.severe("trace: " + ExceptionHandler.getTrace(e));
            logger.severe("Details: " + ExceptionHandler.getExceptionAllinformation(e));
            responseObserver.onError(e);
        }
    }
}
