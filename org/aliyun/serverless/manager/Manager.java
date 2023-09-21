package org.aliyun.serverless.manager;

import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.model.Function;
import org.aliyun.serverless.model.SlotResourceConfig;
import org.aliyun.serverless.scaler.CrazyScaler;
import org.aliyun.serverless.scaler.Scaler;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import protobuf.SchedulerProto;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class Manager {
    private static final Logger logger = Logger.getLogger(Manager.class.getName());
    private final ReadWriteLock rw = new ReentrantReadWriteLock();
    private final Map<String, Scaler> schedulers = new HashMap<>();
    private final Config config;
    private final CloseableHttpClient httpClient;
    private final ScheduledExecutorService scheduledExecutorService;

    public Manager(Config config) {
        this.config = config;
        this.httpClient = HttpClients.createDefault();

        scheduledExecutorService = Executors.newScheduledThreadPool(8);
        scheduledExecutorService.scheduleAtFixedRate(this::printScore, 0, config.getGcInterval().toMillis(), TimeUnit.MILLISECONDS);
    }

    public Scaler GetOrCreate(Function function, SchedulerProto.Meta meta) {
        String key = function.getKey();
        rw.readLock().lock();
        try {
            Scaler scheduler = schedulers.get(key);
            if (scheduler != null) {
                return scheduler;
            }
        } finally {
            rw.readLock().unlock();
        }

        rw.writeLock().lock();
        try {
            Scaler scheduler = schedulers.get(key);
            if (scheduler != null) {
                return scheduler;
            }

            logger.info("Create new scaler for app " + function.getKey());
            SchedulerProto.ResourceConfig resourceConfig = SchedulerProto.ResourceConfig.newBuilder()
                .setMemoryInMegabytes(meta.getMemoryInMb()).build();
            SlotResourceConfig slotResourceConfig = new SlotResourceConfig(resourceConfig);
            scheduler = new CrazyScaler(function, config, slotResourceConfig);
            schedulers.put(function.getKey(), scheduler);
            return scheduler;
        } finally {
            rw.writeLock().unlock();
        }
    }

    public Scaler Get(String functionKey) throws Exception {
        rw.readLock().lock();
        try {
            Scaler scheduler = schedulers.get(functionKey);
            if (scheduler != null) {
                return scheduler;
            }
            throw new Exception("scaler of app: " + functionKey + " not found");
        } finally {
            rw.readLock().unlock();
        }
    }

    private void printScore() {
        // use httpclient to visit http://127.0.0.1:9000/ and print response
        HttpGet httpGet = new HttpGet("http://127.0.0.1:9000/");
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);

            StringBuffer sb = new StringBuffer();
            // build the pretty output string to show content
            sb.append("\n\n").append("************************ [SCORE] ************************\n\n");
            sb.append(String.format("\t\tCurrent Score: %s\n\n", content));
            sb.append("*************************************************************");
            logger.info(sb.toString());
        } catch (IOException ignored) {

        }
    }
}
