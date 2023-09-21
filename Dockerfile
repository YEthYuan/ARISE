FROM registry.cn-beijing.aliyuncs.com/cloudnative-challenge/maven:3.8.3-jdk-8-openj9 as builder

# Set the working directory to /build.
WORKDIR /build

# Copy the source code excluding the 'data' directory
COPY . ./
COPY settings.xml /usr/share/maven/conf/
COPY config.yaml /
COPY ../models.tar.gz /
COPY ../models-half.tar.gz /

# compile proto and program
RUN mvn protobuf:compile && mvn protobuf:compile-custom
RUN mvn package

# In the second stage, we'll use a small, lightweight base image.
FROM registry.cn-beijing.aliyuncs.com/cloudnative-challenge/ubuntu:openjdk

# Set the working directory to /app.
WORKDIR /app

# Copy config file
COPY --from=builder /config.yaml /app/config.yaml

# Copy and unzip models
COPY --from=builder /models.tar.gz /app/models.tar.gz
COPY --from=builder /models-half.tar.gz /app/models-half.tar.gz
RUN tar -xzvf models.tar.gz && tar -xzvf models-half.tar.gz

# Copy the binary from the builder stage.
COPY --from=builder /build/target/fc-scheduler-jar-with-dependencies.jar /app/scheduler.jar

# Copy the source code into the container, excluding the 'data' directory
COPY --from=builder /root/.m2/repository/io/grpc/grpc-core/1.56.0/grpc-core-1.56.0.jar /app/grpc-core-1.56.0.jar

# install dependencies
# RUN apt update && apt install -y glibc-source && apt install -y libgomp1
RUN apt update && apt install -y libgomp1

# Copy the startup script.
COPY run.sh run.sh
RUN chmod +x run.sh
