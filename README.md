## java语言接口文档

### assign 方法
assign 方法是一个核心接口，其主要目的是分配资源实例以满足任务请求。
Assign方法的核心逻辑可能如下：
1. 解析输入的AssignRequest，提取出元数据信息以及请求时间等。
2. 根据元数据信息以及当前系统的资源状态，决定是否可以分配一个资源实例来执行任务，或者是否需要等待。
3. 如果可以分配，那么创建一个资源实例，记录其创建的时间、类型等信息，并将其标记为已占用状态。
4. 返回创建的资源实例的信息，包括其唯一ID等。
需要注意的是，你也可以在一个AssignRequest到来时创建多个资源实例，如果你预测到后面会有大量同类型的任务到来，这样可以提前创建好资源实例，以便后续的任务可以更快的被分配到资源实例上，减少冷启动时间。
#### 方法签名
Assign方法接收AssignRequest和StreamObserver\<AssignReply>对象作为输入参数。
```java
void assign(AssignRequest request, StreamObserver<AssignReply> responseObserver) 
```
下面介绍结构和对应的数据集字段。

#### AssignRequest
AssignRequest对象定义了分配请求的所有信息：
```proto
message AssignRequest {
  string request_id = 1;
  uint64 timestamp = 2;
  Meta meta_data = 3;
}
```
* request_id: 唯一的请求 ID。
* timestamp: 时间戳，表示请求发起的时间，对应requests数据集中的startTime 字段。
* meta_data: Meta 对象，包含任务运行所需的元数据信息，对应数据集matas中的字段。

#### StreamObserver\<AssignReply> 
gRPC 中，客户端和服务器端之间可以通过流传输协议进行通信， StreamObserver 提供了三个方法进行流传输：
* onNext(): 用于向客户端返回数据。
* onError(): 如果服务器端在处理请求时遇到异常，可以通过这个方法向客户端返回异常信息。
* onCompleted(): 在服务器端不再需要返回数据时调用此方法。

我们会通过 StreamObserver 返回 AssignReply 对象，包含分配的资源实例的唯一标识。其结构如下：
```proto
message AssignReply {
  Status status = 1;
  Assignment assigment = 2;
  optional string error_message = 3;
}

message Assignment {
  string request_id = 1;
  string meta_key = 2;
  string instance_id = 3;
}
```
* request_id: 唯一的请求 ID，与输入的 AssignRequest 对象中的 request_id 字段相同。
* meta_key: 与输入的 AssignRequest 对象中的 meta_data.key 字段相同。
* instance_id: 资源实例的唯一标识，用于后续的 idle 方法 以及 gc的资源回收。

### idle 方法
方法用于处理释放实例。你可以设计自己的策略，决定是否删除相应的资源实例。例如，如果短时间内可能会有大量同类型的请求到来，可以保留实例，资源实例将继续用于其他任务，降低冷启动时间；反之，则可能选择将资源实例释放，以节约资源。
#### 方法签名
```java
void idle(IdleRequest request, StreamObserver<IdleReply> responseObserver) 
```
idle 方法接收 IdleRequest 和 StreamObserver\<IdleReply>  对象作为输入参数。下面介绍结构和对应的数据集字段。

#### IdleRequest
```proto 
message IdleRequest{
  Assignment assigment = 1;
  Result result = 2;
}
``` 
* Assigment: Assignment对象，包含任务的分配信息，对应assign方法的输出。
* Result: Result 对象，包含任务的结果信息，对应requests数据集中的statusCode和durationsInMs字段
```proto 
message Result {
  int32 status_code = 1;
  uint64 duration_in_ms = 2;
  optional bool need_destroy = 3;
  optional string reason = 4;
}
````
* status_code: 任务的执行状态码，对应数据集requests中的statusCode字段。
* duration_in_ms: 任务的执行时间，对应数据集requests中的durationsInMs字段。
* need_destroy: 一个布尔值，表示是否需要释放资源实例。如果为true，则需要释放资源实例；如果为false，则不需要释放资源实例。
* reason: 释放资源实例的原因。

#### StreamObserver\<IdleReply>

```proto 
message IdleReply{
  Status status = 1;
  optional string error_message = 3;
}
```
* status: 状态码，表示idle方法的执行结果。
* error_message: 错误信息，如果status不为OK，则error_message不为空，表示错误信息。

### gcLoop方法
除了上述两个API之外，还有一个gcLoop方法，这个方法是由Scaler内部定期调用的，用于回收空闲的资源实例。
gcLoop方法会定期运行，每次运行时，会遍历idleInstances列表，检查每个实例的空闲时间，如果有实例的空闲时间超过了配置的时间 idleDurationBeforeGC，那么这个实例就会被销毁。

在销毁实例时，会调用platformClient的DeleteSlot方法删除这个slot，然后从instances映射和idleInstances列表中删除这个实例，最后会更新实例的状态为deleted。

这个方法的运行时间间隔由Config对象的 gcInterval 字段控制。
选手可以通过配置 gcInterval 和 idleDurationBeforeGC 来控制gcLoop方法的运行频率和空闲实例的回收时间。