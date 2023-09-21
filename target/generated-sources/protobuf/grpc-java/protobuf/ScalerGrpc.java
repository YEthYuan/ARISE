package protobuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.56.0)",
    comments = "Source: serverless-sim.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ScalerGrpc {

  private ScalerGrpc() {}

  public static final String SERVICE_NAME = "serverless.simulator.Scaler";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<protobuf.SchedulerProto.AssignRequest,
      protobuf.SchedulerProto.AssignReply> getAssignMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Assign",
      requestType = protobuf.SchedulerProto.AssignRequest.class,
      responseType = protobuf.SchedulerProto.AssignReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<protobuf.SchedulerProto.AssignRequest,
      protobuf.SchedulerProto.AssignReply> getAssignMethod() {
    io.grpc.MethodDescriptor<protobuf.SchedulerProto.AssignRequest, protobuf.SchedulerProto.AssignReply> getAssignMethod;
    if ((getAssignMethod = ScalerGrpc.getAssignMethod) == null) {
      synchronized (ScalerGrpc.class) {
        if ((getAssignMethod = ScalerGrpc.getAssignMethod) == null) {
          ScalerGrpc.getAssignMethod = getAssignMethod =
              io.grpc.MethodDescriptor.<protobuf.SchedulerProto.AssignRequest, protobuf.SchedulerProto.AssignReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Assign"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.SchedulerProto.AssignRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.SchedulerProto.AssignReply.getDefaultInstance()))
              .setSchemaDescriptor(new ScalerMethodDescriptorSupplier("Assign"))
              .build();
        }
      }
    }
    return getAssignMethod;
  }

  private static volatile io.grpc.MethodDescriptor<protobuf.SchedulerProto.IdleRequest,
      protobuf.SchedulerProto.IdleReply> getIdleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Idle",
      requestType = protobuf.SchedulerProto.IdleRequest.class,
      responseType = protobuf.SchedulerProto.IdleReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<protobuf.SchedulerProto.IdleRequest,
      protobuf.SchedulerProto.IdleReply> getIdleMethod() {
    io.grpc.MethodDescriptor<protobuf.SchedulerProto.IdleRequest, protobuf.SchedulerProto.IdleReply> getIdleMethod;
    if ((getIdleMethod = ScalerGrpc.getIdleMethod) == null) {
      synchronized (ScalerGrpc.class) {
        if ((getIdleMethod = ScalerGrpc.getIdleMethod) == null) {
          ScalerGrpc.getIdleMethod = getIdleMethod =
              io.grpc.MethodDescriptor.<protobuf.SchedulerProto.IdleRequest, protobuf.SchedulerProto.IdleReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Idle"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.SchedulerProto.IdleRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.SchedulerProto.IdleReply.getDefaultInstance()))
              .setSchemaDescriptor(new ScalerMethodDescriptorSupplier("Idle"))
              .build();
        }
      }
    }
    return getIdleMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ScalerStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ScalerStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ScalerStub>() {
        @java.lang.Override
        public ScalerStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ScalerStub(channel, callOptions);
        }
      };
    return ScalerStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ScalerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ScalerBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ScalerBlockingStub>() {
        @java.lang.Override
        public ScalerBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ScalerBlockingStub(channel, callOptions);
        }
      };
    return ScalerBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ScalerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ScalerFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ScalerFutureStub>() {
        @java.lang.Override
        public ScalerFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ScalerFutureStub(channel, callOptions);
        }
      };
    return ScalerFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void assign(protobuf.SchedulerProto.AssignRequest request,
        io.grpc.stub.StreamObserver<protobuf.SchedulerProto.AssignReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAssignMethod(), responseObserver);
    }

    /**
     */
    default void idle(protobuf.SchedulerProto.IdleRequest request,
        io.grpc.stub.StreamObserver<protobuf.SchedulerProto.IdleReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getIdleMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service Scaler.
   */
  public static abstract class ScalerImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return ScalerGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service Scaler.
   */
  public static final class ScalerStub
      extends io.grpc.stub.AbstractAsyncStub<ScalerStub> {
    private ScalerStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ScalerStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ScalerStub(channel, callOptions);
    }

    /**
     */
    public void assign(protobuf.SchedulerProto.AssignRequest request,
        io.grpc.stub.StreamObserver<protobuf.SchedulerProto.AssignReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAssignMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void idle(protobuf.SchedulerProto.IdleRequest request,
        io.grpc.stub.StreamObserver<protobuf.SchedulerProto.IdleReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getIdleMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service Scaler.
   */
  public static final class ScalerBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ScalerBlockingStub> {
    private ScalerBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ScalerBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ScalerBlockingStub(channel, callOptions);
    }

    /**
     */
    public protobuf.SchedulerProto.AssignReply assign(protobuf.SchedulerProto.AssignRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAssignMethod(), getCallOptions(), request);
    }

    /**
     */
    public protobuf.SchedulerProto.IdleReply idle(protobuf.SchedulerProto.IdleRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getIdleMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service Scaler.
   */
  public static final class ScalerFutureStub
      extends io.grpc.stub.AbstractFutureStub<ScalerFutureStub> {
    private ScalerFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ScalerFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ScalerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<protobuf.SchedulerProto.AssignReply> assign(
        protobuf.SchedulerProto.AssignRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAssignMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<protobuf.SchedulerProto.IdleReply> idle(
        protobuf.SchedulerProto.IdleRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getIdleMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_ASSIGN = 0;
  private static final int METHODID_IDLE = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ASSIGN:
          serviceImpl.assign((protobuf.SchedulerProto.AssignRequest) request,
              (io.grpc.stub.StreamObserver<protobuf.SchedulerProto.AssignReply>) responseObserver);
          break;
        case METHODID_IDLE:
          serviceImpl.idle((protobuf.SchedulerProto.IdleRequest) request,
              (io.grpc.stub.StreamObserver<protobuf.SchedulerProto.IdleReply>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getAssignMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              protobuf.SchedulerProto.AssignRequest,
              protobuf.SchedulerProto.AssignReply>(
                service, METHODID_ASSIGN)))
        .addMethod(
          getIdleMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              protobuf.SchedulerProto.IdleRequest,
              protobuf.SchedulerProto.IdleReply>(
                service, METHODID_IDLE)))
        .build();
  }

  private static abstract class ScalerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ScalerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return protobuf.SchedulerProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Scaler");
    }
  }

  private static final class ScalerFileDescriptorSupplier
      extends ScalerBaseDescriptorSupplier {
    ScalerFileDescriptorSupplier() {}
  }

  private static final class ScalerMethodDescriptorSupplier
      extends ScalerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ScalerMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ScalerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ScalerFileDescriptorSupplier())
              .addMethod(getAssignMethod())
              .addMethod(getIdleMethod())
              .build();
        }
      }
    }
    return result;
  }
}
