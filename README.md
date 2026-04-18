<div align="center">

# LeafVision 轻羽服务器监控

[![AGPL-3.0 License](https://img.shields.io/badge/License-AGPL_v3-blue.svg?style=for-the-badge&logo=gnu)](https://www.gnu.org/licenses/agpl-3.0)[![GitHub Stars](https://img.shields.io/github/stars/YangShengzhou03/LeafVision?style=for-the-badge&logo=github)](https://github.com/YangShengzhou03/LeafVision)[![GitHub Forks](https://img.shields.io/github/forks/YangShengzhou03/LeafVision?style=for-the-badge&logo=github)](https://github.com/YangShengzhou03/LeafVision)[![GitHub Issues](https://img.shields.io/github/issues/YangShengzhou03/LeafVision?style=for-the-badge&logo=github)](https://github.com/YangShengzhou03/LeafVision/issues)[![GitHub Pull Requests](https://img.shields.io/github/issues-pr/YangShengzhou03/LeafVision?style=for-the-badge&logo=github)](https://github.com/YangShengzhou03/LeafVision/pulls)[![Last Commit](https://img.shields.io/github/last-commit/YangShengzhou03/LeafVision?style=for-the-badge&logo=git)](https://github.com/YangShengzhou03/LeafVision)

**Prometheus 集群监控管理平台**

[在线演示](#) | [环境准备](#环境准备) | [API 文档](./API-DOCUMENT.md)

</div>

---

## 项目简介

LeafVision 是一个 Prometheus 集群监控管理平台。

现在微服务和容器化越来越普及，监控系统也变得越来越复杂。如果你需要管理多个 Prometheus 节点和 Alertmanager 实例，这个项目可能会帮到你。它能集中管理监控资源、聚合告警信息、查询指标数据，让大规模分布式系统的监控变得简单一些。

---

## 快速开始

一行命令部署：

```bash
curl -sSL https://gitee.com/Yangshengzhou/leaf-vision/raw/master/docker-compose.yml -o docker-compose.yml && docker-compose up -d && sleep 20 && docker exec leafvision-mysql sh -c "curl -s https://gitee.com/Yangshengzhou/leaf-vision/raw/master/data.sql | mysql -uroot -p123456 leaf_vision" && docker ps
```

部署完成后访问 `http://服务器IP`，默认账号 `admin`，密码 `123456`。

删除容器：

```bash
docker-compose down -v
```

---

## 功能特性

![Landing](https://gitee.com/Yangshengzhou/leaf-vision/raw/master/assets/landing.png)

- **监控总览** - 服务器状态、CPU/内存使用率、网络流量一目了然
- **实时监控** - 实时查看服务器性能指标
- **主机管理** - 管理多个 Prometheus 节点，支持分组
- **容器管理** - Docker 容器的启动、停止、重启、日志查看
- **服务管理** - 服务状态监控和健康检查
- **告警管理** - 聚合展示告警，配置告警规则
- **指标查询** - PromQL 查询界面，支持时间范围筛选
- **日志追踪** - 日志查询和链路追踪
- **权限管理** - 用户、角色、权限管理
- **审计日志** - 记录用户操作
- **系统设置** - 系统配置管理

---

## 界面预览

### 首页

![Features](https://gitee.com/Yangshengzhou/leaf-vision/raw/master/assets/features.png)

### 登录

![Login](https://gitee.com/Yangshengzhou/leaf-vision/raw/master/assets/login.png)

### 监控仪表盘

![Dashboard](https://gitee.com/Yangshengzhou/leaf-vision/raw/master/assets/dashboard.png)

---

## 环境准备

### Docker 安装 Prometheus 组件

#### 创建监控网络

建议创建一个自定义 Docker 网络，这样容器之间可以通过容器名互相访问：

```bash
docker network create monitor-net
```

#### 采集端服务器配置

```bash
# 创建配置目录
sudo mkdir -p /opt/prometheus /opt/alertmanager

# 创建配置文件（必须先创建，否则 Docker 会把它当成目录）
sudo touch /opt/prometheus/prometheus.yml
sudo touch /opt/alertmanager/alertmanager.yml

# 启动 Node Exporter
docker run -d \
  --name node-exporter \
  --restart always \
  --net monitor-net \
  -p 9100:9100 \
  -v /proc:/host/proc:ro \
  -v /sys:/host/sys:ro \
  -v /:/rootfs:ro \
  prom/node-exporter:latest \
  --path.procfs=/host/proc \
  --path.sysfs=/host/sys \
  --path.rootfs=/rootfs

# 启动 Prometheus
docker run -d \
  --name prometheus \
  --restart always \
  --net monitor-net \
  -p 9090:9090 \
  -v /opt/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:latest

# 启动 Alertmanager
docker run -d \
  --name alertmanager \
  --restart always \
  --net monitor-net \
  -p 9093:9093 \
  -v /opt/alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml \
  prom/alertmanager:latest

# 验证服务
curl "http://localhost:9090/api/v1/query?query=up"
curl "http://localhost:9093/api/v2/alerts"
curl http://localhost:9100/metrics
```

#### 被采集端服务器配置

只需要安装 Node Exporter：

```bash
docker run -d \
  --name node-exporter \
  --restart always \
  -p 9100:9100 \
  -v /proc:/host/proc:ro \
  -v /sys:/host/sys:ro \
  -v /:/rootfs:ro \
  prom/node-exporter:latest \
  --path.procfs=/host/proc \
  --path.sysfs=/host/sys \
  --path.rootfs=/rootfs

curl http://localhost:9100/metrics
```

---

## Prometheus 配置示例

配置文件路径：`/opt/prometheus/prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

rule_files:
  - /etc/prometheus/rules/*.yml

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['prometheus:9090']

  - job_name: 'node-exporter'
    static_configs:
      - targets:
          - node-exporter:9100
          # - 192.168.65.105:9100  # 其他服务器
        labels:
          group: 'production'

  - job_name: 'alertmanager'
    static_configs:
      - targets: ['alertmanager:9093']
```

修改配置后记得重启：`docker restart prometheus`

---

## Alertmanager 配置示例

配置文件路径：`/opt/alertmanager/alertmanager.yml`

```yaml
global:
  resolve_timeout: 5m

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'default-receiver'

receivers:
  - name: 'default-receiver'
```

重启容器：`docker restart alertmanager`

---

## 常见问题

### 监控目标连接失败

如果 `up{job="node-exporter"}` 查询为空或显示 `0`：

1. 检查容器网络：`docker network inspect monitor-net`
2. 进入 Prometheus 容器测试：
   ```bash
   docker exec -it prometheus sh
   wget -qO- node-exporter:9100/metrics
   ```
3. 检查端口：`telnet <目标IP> 9100`
4. 检查防火墙

### Docker 网络配置

同一台机器上用容器名访问：
```yaml
targets: ['node-exporter:9100']
```

跨机器用 IP 访问：
```yaml
targets: ['192.168.1.100:9100']
```

### 容器重启后网络问题

```bash
# 检查网络
docker inspect prometheus --format='{{range .NetworkSettings.Networks}}{{.NetworkID}}{{end}}'

# 重新加入网络
docker network connect monitor-net prometheus
```

---

## 开源协议

本项目采用 AGPL-3.0 开源协议，详见 [LICENSE](./LICENSE) 文件。

---

<div align="center">

**如果这个项目对你有帮助，欢迎 Star 支持！**

Made with love by YangShengzhou

</div>
