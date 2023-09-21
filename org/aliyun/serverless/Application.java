package org.aliyun.serverless;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class Application {

    private static final Logger logger = Logger.getLogger(Application.class.getName());
    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("Starting application...");
        Server server = ServerBuilder.forPort(9001)
                .addService(new org.aliyun.serverless.server.Server())
                .build()
                .start();
        System.out.println("Server started, listening on 9001");
        logger.info("Server started, listening on 9001");
        server.awaitTermination();
    }
}
