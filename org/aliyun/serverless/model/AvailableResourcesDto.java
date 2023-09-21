package org.aliyun.serverless.model;

import java.util.ArrayList;
import java.util.List;

public class AvailableResourcesDto {
    private List<Task> readyInstances;
    private List<Task> initStageInstances;
    private List<Task> createStageInstances;

    public AvailableResourcesDto() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public AvailableResourcesDto(List<Task> readyInstances, List<Task> initStageInstances, List<Task> createStageInstances) {
        this.readyInstances = readyInstances;
        this.initStageInstances = initStageInstances;
        this.createStageInstances = createStageInstances;
    }

    public List<Task> getReadyInstances() {
        return readyInstances;
    }

    public void setReadyInstances(List<Task> readyInstances) {
        this.readyInstances = readyInstances;
    }

    public List<Task> getInitStageInstances() {
        return initStageInstances;
    }

    public void setInitStageInstances(List<Task> initStageInstances) {
        this.initStageInstances = initStageInstances;
    }

    public List<Task> getCreateStageInstances() {
        return createStageInstances;
    }

    public void setCreateStageInstances(List<Task> createStageInstances) {
        this.createStageInstances = createStageInstances;
    }
}
