package protobuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 *the following proto should used in scaler module
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.56.0)",
    comments = "Source: serverless-sim.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class PlatformGrpc {

  private PlatformGrpc() {}

  public static final String SERVICE_NAME = "serverless.simulator.Platform";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<protobuf.SchedulerProto.CreateSlotRequest,
      protobuf.SchedulerProto.CreateSlotReply> getCreateSlotMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateSlot",
      requestType = protobuf.SchedulerProto.CreateSlotRequest.class,
      responseType = protobuf.SchedulerProto.CreateSlotReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<protobuf.SchedulerProto.CreateSlotRequest,
      protobuf.SchedulerProto.CreateSlotReply> getCreateSlotMethod() {
    io.grpc.MethodDescriptor<protobuf.SchedulerProto.CreateSlotRequest, protobuf.SchedulerProto.CreateSlotReply> getCreateSlotMethod;
    if ((getCreateSlotMethod = PlatformGrpc.getCreateSlotMethod) == null) {
      synchronized (PlatformGrpc.class) {
        if ((getCreateSlotMethod = PlatformGrpc.getCreateSlotMethod) == null) {
          PlatformGrpc.getCreateSlotMethod = getCreateSlotMethod =
              io.grpc.MethodDescriptor.<protobuf.SchedulerProto.CreateSlotRequest, protobuf.SchedulerProto.CreateSlotReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateSlot"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.SchedulerProto.CreateSlotRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.SchedulerProto.CreateSlotReply.getDefaultInstance()))
              .setSchemaDescriptor(new PlatformMethodDescriptorSupplier("CreateSlot"))
              .build();
        }
      }
    }
    return getCreateSlotMethod;
  }

  private static volatile io.grpc.MethodDescriptor<protobuf.SchedulerProto.DestroySlotRequest,
      protobuf.SchedulerProto.DestroySlotReply> getDestroySlotMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DestroySlot",
      requestType = protobuf.SchedulerProto.DestroySlotRequest.class,
      responseType = protobuf.SchedulerProto.DestroySlotReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<protobuf.SchedulerProto.DestroySlotRequest,
      protobuf.SchedulerProto.DestroySlotReply> getDestroySlotMethod() {
    io.grpc.MethodDescriptor<protobuf.SchedulerProto.DestroySlotRequest, protobuf.SchedulerProto.DestroySlotReply> getDestroySlotMethod;
    if ((getDestroySlotMethod = PlatformGrpc.getDestroySlotMethod) == null) {
      synchronized (PlatformGrpc.class) {
        if ((getDestroySlotMethod = PlatformGrpc.getDestroySlotMethod) == null) {
          PlatformGrpc.getDestroySlotMethod = getDestroySlotMethod =
              io.grpc.MethodDescriptor.<protobuf.SchedulerProto.DestroySlotRequest, protobuf.SchedulerProto.DestroySlotReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DestroySlot"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.SchedulerProto.DestroySlotRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.SchedulerProto.DestroySlotReply.getDefaultInstance()))
              .setSchemaDescriptor(new PlatformMethodDescriptorSupplier("DestroySlot"))
              .build();
        }
      }
    }
    return getDestroySlotMethod;
  }

  private static volatile io.grpc.MethodDescriptor<protobuf.SchedulerProto.InitRequest,
      protobuf.SchedulerProto.InitReply> getInitMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Init",
      requestType = protobuf.SchedulerProto.InitRequest.class,
      responseType = protobuf.SchedulerProto.InitReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<protobuf.SchedulerProto.InitRequest,
      protobuf.SchedulerProto.InitReply> getInitMethod() {
    io.grpc.MethodDescriptor<protobuf.SchedulerProto.InitRequest, protobuf.SchedulerProto.InitReply> getInitMethod;
    if ((getInitMethod = PlatformGrpc.getInitMethod) == null) {
      synchronized (PlatformGrpc.class) {
        if ((getInitMethod = PlatformGrpc.getInitMethod) == null) {
          PlatformGrpc.getInitMethod = getInitMethod =
              io.grpc.MethodDescriptor.<protobuf.SchedulerProto.InitRequest, protobuf.SchedulerProto.InitReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Init"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.SchedulerProto.InitRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  protobuf.SchedulerProto.InitReply.getDefaultInstance()))
              .setSchemaDescriptor(new PlatformMethodDescriptorSupplier("Init"))
              .build();
        }
      }
    }
    return getInitMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PlatformStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PlatformStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PlatformStub>() {
        @java.lang.Override
        public PlatformStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PlatformStub(channel, callOptions);
        }
      };
    return PlatformStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PlatformBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PlatformBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PlatformBlockingStub>() {
        @java.lang.Override
        public PlatformBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PlatformBlockingStub(channel, callOptions);
        }
      };
    return PlatformBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PlatformFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PlatformFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PlatformFutureStub>() {
        @java.lang.Override
        public PlatformFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PlatformFutureStub(channel, callOptions);
        }
      };
    return PlatformFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   *the following proto should used in scaler module
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     *Slot
     * </pre>
     */
    default void createSlot(protobuf.SchedulerProto.CreateSlotRequest request,
        io.grpc.stub.StreamObserver<protobuf.SchedulerProto.CreateSlotReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateSlotMethod(), responseObserver);
    }

    /**
     */
    default void destroySlot(protobuf.SchedulerProto.DestroySlotRequest request,
        io.grpc.stub.StreamObserver<protobuf.SchedulerProto.DestroySlotReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDestroySlotMethod(), responseObserver);
    }

    /**
     * <pre>
     *Init
     * </pre>
     */
    default void init(protobuf.SchedulerProto.InitRequest request,
        io.grpc.stub.StreamObserver<protobuf.SchedulerProto.InitReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getInitMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service Platform.
   * <pre>
   *the following proto should used in scaler module
   * </pre>
   */
  public static abstract class PlatformImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return PlatformGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service Platform.
   * <pre>
   *the following proto should used in scaler module
   * </pre>
   */
  public static final class PlatformStub
      extends io.grpc.stub.AbstractAsyncStub<PlatformStub> {
    private PlatformStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PlatformStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PlatformStub(channel, callOptions);
    }

    /**
     * <pre>
     *Slot
     * </pre>
     */
    public void createSlot(protobuf.SchedulerProto.CreateSlotRequest request,
        io.grpc.stub.StreamObserver<protobuf.SchedulerProto.CreateSlotReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateSlotMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void destroySlot(protobuf.SchedulerProto.DestroySlotRequest request,
        io.grpc.stub.StreamObserver<protobuf.SchedulerProto.DestroySlotReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDestroySlotMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *Init
     * </pre>
     */
    public void init(protobuf.SchedulerProto.InitRequest request,
        io.grpc.stub.StreamObserver<protobuf.SchedulerProto.InitReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getInitMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service Platform.
   * <pre>
   *the following proto should used in scaler module
   * </pre>
   */
  public static final class PlatformBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<PlatformBlockingStub> {
    private PlatformBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PlatformBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PlatformBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     *Slot
     * </pre>
     */
    public protobuf.SchedulerProto.CreateSlotReply createSlot(protobuf.SchedulerProto.CreateSlotRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateSlotMethod(), getCallOptions(), request);
    }

    /**
     */
    public protobuf.SchedulerProto.DestroySlotReply destroySlot(protobuf.SchedulerProto.DestroySlotRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDestroySlotMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *Init
     * </pre>
     */
    public protobuf.SchedulerProto.InitReply init(protobuf.SchedulerProto.InitRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getInitMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service Platform.
   * <pre>
   *the following proto should used in scaler module
   * </pre>
   */
  public static final class PlatformFutureStub
      extends io.grpc.stub.AbstractFutureStub<PlatformFutureStub> {
    private PlatformFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PlatformFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PlatformFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     *Slot
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<protobuf.SchedulerProto.CreateSlotReply> createSlot(
        protobuf.SchedulerProto.CreateSlotRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateSlotMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<protobuf.SchedulerProto.DestroySlotReply> destroySlot(
        protobuf.SchedulerProto.DestroySlotRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDestroySlotMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     *Init
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<protobuf.SchedulerProto.InitReply> init(
        protobuf.SchedulerProto.InitRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getInitMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_SLOT = 0;
  private static final int METHODID_DESTROY_SLOT = 1;
  private static final int METHODID_INIT = 2;

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
        case METHODID_CREATE_SLOT:
          serviceImpl.createSlot((protobuf.SchedulerProto.CreateSlotRequest) request,
              (io.grpc.stub.StreamObserver<protobuf.SchedulerProto.CreateSlotReply>) responseObserver);
          break;
        case METHODID_DESTROY_SLOT:
          serviceImpl.destroySlot((protobuf.SchedulerProto.DestroySlotRequest) request,
              (io.grpc.stub.StreamObserver<protobuf.SchedulerProto.DestroySlotReply>) responseObserver);
          break;
        case METHODID_INIT:
          serviceImpl.init((protobuf.SchedulerProto.InitRequest) request,
              (io.grpc.stub.StreamObserver<protobuf.SchedulerProto.InitReply>) responseObserver);
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
          getCreateSlotMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              protobuf.SchedulerProto.CreateSlotRequest,
              protobuf.SchedulerProto.CreateSlotReply>(
                service, METHODID_CREATE_SLOT)))
        .addMethod(
          getDestroySlotMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              protobuf.SchedulerProto.DestroySlotRequest,
              protobuf.SchedulerProto.DestroySlotReply>(
                service, METHODID_DESTROY_SLOT)))
        .addMethod(
          getInitMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              protobuf.SchedulerProto.InitRequest,
              protobuf.SchedulerProto.InitReply>(
                service, METHODID_INIT)))
        .build();
  }

  private static abstract class PlatformBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PlatformBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return protobuf.SchedulerProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Platform");
    }
  }

  private static final class PlatformFileDescriptorSupplier
      extends PlatformBaseDescriptorSupplier {
    PlatformFileDescriptorSupplier() {}
  }

  private static final class PlatformMethodDescriptorSupplier
      extends PlatformBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    PlatformMethodDescriptorSupplier(String methodName) {
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
      synchronized (PlatformGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PlatformFileDescriptorSupplier())
              .addMethod(getCreateSlotMethod())
              .addMethod(getDestroySlotMethod())
              .addMethod(getInitMethod())
              .build();
        }
      }
    }
    return result;
  }
}
