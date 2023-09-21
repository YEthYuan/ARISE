#!/bin/bash

# 使用方法: bash deploy.sh <new_image_tag>
# 获取 Docker 镜像的名称, 注意不要有下划线, kubernetes命名空间不允许下划线, 使用 '-' 分割单词
IMAGE_TAG="$1"

rm -rf ./protobuf

# 构建 Docker 镜像
docker build --platform=linux/amd64 -t registry.cn-beijing.aliyuncs.com/hackathon_crazyscaler/scaler:${IMAGE_TAG} .

# 推送 Docker 镜像到阿里云容器镜像服务
docker push registry.cn-beijing.aliyuncs.com/hackathon_crazyscaler/scaler:${IMAGE_TAG}

# 不用删除已有的 Kubernetes 资源

cd ..
# 修改 Kubernetes YAML 文件中的 image 字段
sed -i 's|serverless-simulation[^"]*|serverless-simulation-'"${IMAGE_TAG}"'|g' ./manifest/serverless-simulation.yaml
sed -i 's|image: registry-vpc.cn-beijing.aliyuncs.com/hackathon_crazyscaler/scaler:[^"]*|image: registry-vpc.cn-beijing.aliyuncs.com/hackathon_crazyscaler/scaler:'"${IMAGE_TAG}"'|g' ./manifest/serverless-simulation.yaml

# 部署 Kubernetes 资源
kubectl apply -f ./manifest/serverless-simulation.yaml
sleep 60

# 获取 Job Pod 的名称
JOB_POD=$(kubectl get pod -l job-name=serverless-simulation-${IMAGE_TAG} -o jsonpath="{.items[0].metadata.name}")

# 创建日志文件，并后台运行 tail 命令
mkdir -p logs
nohup kubectl logs -f $JOB_POD -c scaler > logs/scaler-${IMAGE_TAG}.log &
nohup kubectl logs -f $JOB_POD -c serverless-simulator > logs/simulator-${IMAGE_TAG}.log &
