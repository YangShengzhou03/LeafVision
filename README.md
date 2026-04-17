<div align="center">

# LeafVision 轻羽服务器监控

[![AGPL-3.0 License](https://img.shields.io/badge/License-AGPL_v3-blue.svg?style=for-the-badge&logo=gnu)](https://www.gnu.org/licenses/agpl-3.0)[![GitHub Stars](https://img.shields.io/github/stars/YangShengzhou03/LeafVision?style=for-the-badge&logo=github)](https://github.com/YangShengzhou03/LeafVision)[![GitHub Forks](https://img.shields.io/github/forks/YangShengzhou03/LeafVision?style=for-the-badge&logo=github)](https://github.com/YangShengzhou03/LeafVision)[![GitHub Issues](https://img.shields.io/github/issues/YangShengzhou03/LeafVision?style=for-the-badge&logo=github)](https://github.com/YangShengzhou03/LeafVision/issues)[![GitHub Pull Requests](https://img.shields.io/github/issues-pr/YangShengzhou03/LeafVision?style=for-the-badge&logo=github)](https://github.com/YangShengzhou03/LeafVision/pulls)[![Last Commit](https://img.shields.io/github/last-commit/YangShengzhou03/LeafVision?style=for-the-badge&logo=git)](https://github.com/YangShengzhou03/LeafVision)

**Prometheus 集群监控管理平台**

[在线演示](#) | [环境准备](#环境准备) | [API 文档](./API-DOCUMENT.md)

</div>

---

## 项目简介

LeafVision 是一款面向云原生时代的 **Prometheus 集群监控管理平台**，致力于为企业提供一站式的可观测性解决方案。

在微服务架构与容器化技术广泛应用的今天，监控系统的复杂度呈指数级增长。LeafVision 通过统一管理多个 Prometheus 服务器节点与 Alertmanager 实例，实现了监控资源的集中管控、告警信息的智能聚合、指标数据的高效检索，帮助运维团队从容应对大规模分布式系统的监控挑战。

---

## 功能特性

![Features](https://gitee.com/Yangshengzhou/leaf-vision/raw/master/assets/features.png)

- **监控总览** - 实时展示服务器状态、CPU/内存使用率、网络流量等关键指标
- **主机管理** - 统一管理多个 Prometheus 节点，支持增删改查
- **告警管理** - 聚合展示 Alertmanager 告警，支持告警规则配置
- **指标查询** - 提供 PromQL 查询界面，支持时间范围筛选
- **日志追踪** - 集成日志查询与链路追踪能力
- **权限管理** - 完善的用户、角色、权限管理体系

---

## 界面预览

### 首页

![Landing](https://gitee.com/Yangshengzhou/leaf-vision/raw/master/assets/landing.png)

### 登录

![Login](https://gitee.com/Yangshengzhou/leaf-vision/raw/master/assets/login.png)

### 监控仪表盘

![Dashboard](https://gitee.com/Yangshengzhou/leaf-vision/raw/master/assets/dashboard.png)

---

## 环境准备

### Docker 安装 Prometheus 组件

#### 采集端服务器（server）

```bash
# 创建配置目录
sudo mkdir -p /opt/prometheus /opt/alertmanager

# 创建配置文件（必须先创建文件，否则 Docker 会创建目录导致挂载失败）
sudo touch /opt/prometheus/prometheus.yml
sudo touch /opt/alertmanager/alertmanager.yml

# 启动 Prometheus
docker run -d \
  --name prometheus \
  --restart always \
  -p 9090:9090 \
  -v /opt/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:latest

# 启动 Alertmanager
docker run -d \
  --name alertmanager \
  --restart always \
  -p 9093:9093 \
  -v /opt/alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml \
  prom/alertmanager:latest

# 启动 Node Exporter
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

# 验证服务是否正常启动
curl http://localhost:9090     # Prometheus
curl http://localhost:9093     # Alertmanager
curl http://localhost:9100/metrics  # Node Exporter
```

#### 被采集端服务器（exporter）

仅需安装 Node Exporter 用于采集系统指标：

```bash
# 启动 Node Exporter
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

# 验证运行状态
curl http://localhost:9100/metrics
```

**配置说明**：
- 被管机只需安装 Node Exporter
- 主管机 Prometheus 配置文件中需添加被管机 IP 地址
- 确保主管机与被管机网络互通

---

## Prometheus 配置示例

文件路径：`/opt/prometheus/prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - localhost:9093

rule_files:
  - /etc/prometheus/rules/*.yml

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'node-exporter'
    static_configs:
      - targets: 
        - '192.168.65.104:9100' # 采集端
        - '192.168.65.105:9100' # 被采集端1
        labels:
          group: 'production'

  - job_name: 'alertmanager'
    static_configs:
      - targets: ['localhost:9093']
```

---

## Alertmanager 配置示例

文件路径：`/opt/alertmanager/alertmanager.yml`

```yaml
global:
  resolve_timeout: 5m
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alertmanager@example.com'
  smtp_auth_username: 'alertmanager@example.com'
  smtp_auth_password: 'your-password'

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'email-notifications'

receivers:
  - name: 'email-notifications'
    email_configs:
      - to: 'admin@example.com'
        send_resolved: true
```

---

## 开源协议

本项目采用 **AGPL-3.0 License** 开源协议。

详细内容见 [LICENSE](./LICENSE) 文件。

---

<div align="center">

**如果这个项目对你有帮助，欢迎 Star 支持！**

Made with love by YangShengzhou03

</div>
