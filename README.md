# LeafVision

## 项目简介

LeafVision 是一个 **Prometheus 集群监控管理平台**，用于统一管理和监控多个 Prometheus 服务器节点和 Alertmanager 实例。

---

## 一、主流企业监控方案

### 1.1 标准架构

主流企业通常采用以下监控架构：

```
┌─────────────────────────────────────────────────────────────────┐
│                        应用层                                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ Web 服务  │  │ 数据库    │  │ 消息队列  │  │ 缓存服务  │        │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘        │
└───────┼─────────────┼─────────────┼─────────────┼───────────────┘
        │             │             │             │
        │ Exporter    │ Exporter    │ Exporter    │ Exporter
        ▼             ▼             ▼             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      数据采集层                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Prometheus 集群                              │  │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐         │  │
│  │  │ 主节点      │  │ 节点1       │  │ 节点2       │         │  │
│  │  │ :9090      │  │ :9090      │  │ :9090      │         │  │
│  │  └────────────┘  └────────────┘  └────────────┘         │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Alertmanager 集群                            │  │
│  │  ┌────────────┐  ┌────────────┐                         │  │
│  │  │ 主节点      │  │ 备节点      │                         │  │
│  │  │ :9093      │  │ :9093      │                         │  │
│  │  └────────────┘  └────────────┘                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
        │
        │ 告警通知
        ▼
┌─────────────────────────────────────────────────────────────────┐
│                        通知渠道                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ 邮件      │  │ 钉钉      │  │ 企业微信  │  │ Slack    │        │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
└─────────────────────────────────────────────────────────────────┘
        │
        │ 数据查询
        ▼
┌─────────────────────────────────────────────────────────────────┐
│                        可视化层                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                      Grafana                              │  │
│  │  (数据可视化、仪表盘、告警展示)                            │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 核心组件说明

| 组件 | 作用 | 常用端口 |
|------|------|----------|
| Prometheus | 时序数据库，负责数据采集和存储 | 9090 |
| Alertmanager | 告警管理，负责告警去重、分组、路由 | 9093 |
| Node Exporter | 主机指标采集 | 9100 |
| Grafana | 数据可视化平台 | 3000 |
| Pushgateway | 短期任务指标推送 | 9091 |

### 1.3 告警流程

1. **指标采集** - Prometheus 定时从 Exporter 拉取指标数据
2. **规则评估** - Prometheus 根据告警规则评估指标
3. **告警触发** - 规则触发后，发送告警到 Alertmanager
4. **告警处理** - Alertmanager 对告警进行去重、分组、静默
5. **通知发送** - Alertmanager 通过配置的渠道发送通知

---

## 二、LeafVision 的定位

### 2.1 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                      LeafVision 架构                            │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    LeafVision (Java)                     │   │
│  │                                                          │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐        │   │
│  │  │ 服务器管理  │  │  告警管理   │  │  指标查询   │        │   │
│  │  └────────────┘  └────────────┘  └────────────┘        │   │
│  │                                                          │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐        │   │
│  │  │ 定时同步    │  │  数据聚合   │  │  Web API   │        │   │
│  │  └────────────┘  └────────────┘  └────────────┘        │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              │ HTTP GET (主动请求)              │
│                              ▼                                  │
└─────────────────────────────────────────────────────────────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│ Prometheus    │    │ Prometheus    │    │ Alertmanager  │
│ :9090         │    │ :9090         │    │ :9093         │
└───────────────┘    └───────────────┘    └───────────────┘
```

### 2.2 交互方式说明

**核心原则：LeafVision 采用主动请求模式（Pull 模式）**

| 方向 | 说明 |
|------|------|
| LeafVision -> Prometheus | Java 主动发 HTTP GET 请求 |
| LeafVision -> Alertmanager | Java 主动发 HTTP GET 请求 |
| Prometheus -> LeafVision | **不会**主动发请求 |
| Alertmanager -> LeafVision | **不会**主动发请求 |

### 2.3 API 接口列表

LeafVision 调用 Prometheus 的 HTTP API：

| API 路径 | 用途 |
|----------|------|
| `/api/v1/query` | 即时查询指标 |
| `/api/v1/query_range` | 范围查询指标 |
| `/api/v1/rules` | 获取告警规则 |
| `/api/v1/alerts` | 获取告警列表 |
| `/api/v1/status/config` | 获取配置信息 |
| `/api/v1/targets` | 获取采集目标状态 |

LeafVision 调用 Alertmanager 的 HTTP API：

| API 路径 | 用途 |
|----------|------|
| `/api/v1/alerts` | 获取告警列表 |
| `/api/v1/silences` | 获取静默规则 |
| `/api/v1/status` | 获取状态信息 |

### 2.4 功能定位

| 功能 | 说明 |
|------|------|
| 多集群管理 | 当企业有多个 Prometheus 集群时，LeafVision 提供统一入口 |
| 告警聚合 | 聚合多个 Alertmanager 的告警信息 |
| 跨集群查询 | 提供跨 Prometheus 集群的指标查询能力 |
| 配置管理 | 提供可视化的服务器管理和规则配置 |

---

## 三、技术栈

### 3.1 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.1.6 | Web 框架 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| Spring WebFlux | 3.1.6 | HTTP 客户端 |
| H2 Database | 2.x | 嵌入式数据库 |

### 3.2 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 前端框架 |
| Element Plus | 2.4.x | UI 组件库 |
| ECharts | 5.4.x | 图表库 |
| Pinia | 2.1.x | 状态管理 |
| Vue Router | 4.2.x | 路由管理 |
| Axios | 1.6.x | HTTP 客户端 |
| Vite | 8.0.x | 构建工具 |

---

## 四、项目结构

```
leaf-vision/
├── backend/                          # 后端项目
│   ├── src/main/java/com/leafvision/
│   │   ├── client/                   # Prometheus/Alertmanager 客户端
│   │   │   ├── PrometheusClient.java
│   │   │   └── AlertmanagerClient.java
│   │   ├── config/                   # 配置类
│   │   │   ├── CorsConfig.java
│   │   │   └── LeafVisionConfig.java
│   │   ├── constant/                 # 常量定义
│   │   │   ├── AlertStatus.java
│   │   │   ├── ServerStatus.java
│   │   │   └── ServerType.java
│   │   ├── controller/               # 控制器
│   │   │   ├── AlertController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── MetricsController.java
│   │   │   └── ServerController.java
│   │   ├── entity/                   # 实体类
│   │   │   ├── Alert.java
│   │   │   ├── AlertRule.java
│   │   │   ├── DashboardData.java
│   │   │   ├── Result.java
│   │   │   └── Server.java
│   │   ├── exception/                # 异常处理
│   │   │   ├── BusinessException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── mapper/                   # MyBatis Mapper
│   │   │   ├── AlertMapper.java
│   │   │   ├── AlertRuleMapper.java
│   │   │   └── ServerMapper.java
│   │   ├── service/                  # 服务层
│   │   │   ├── AlertService.java
│   │   │   ├── DashboardService.java
│   │   │   ├── MetricsService.java
│   │   │   └── ServerService.java
│   │   ├── task/                     # 定时任务
│   │   │   └── SyncTask.java
│   │   └── LeafVisionApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml           # 应用配置
│   │   └── schema.sql                # 数据库初始化
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                         # 前端项目
│   ├── public/                       # 静态资源
│   │   └── favicon.svg
│   ├── src/
│   │   ├── api/                      # API 请求
│   │   │   ├── modules/              # API 模块
│   │   │   │   ├── alert.js
│   │   │   │   ├── dashboard.js
│   │   │   │   ├── metrics.js
│   │   │   │   └── server.js
│   │   │   ├── index.js
│   │   │   └── request.js
│   │   ├── constants/                # 常量定义
│   │   │   └── index.js
│   │   ├── layout/                   # 布局组件
│   │   │   └── MainLayout.vue
│   │   ├── router/                   # 路由配置
│   │   │   └── index.js
│   │   ├── stores/                   # 状态管理
│   │   │   └── user.js
│   │   ├── utils/                    # 工具函数
│   │   │   ├── index.js
│   │   │   └── storage.js
│   │   ├── views/                    # 页面组件
│   │   │   ├── About.vue
│   │   │   ├── Alerts.vue
│   │   │   ├── Containers.vue
│   │   │   ├── Dashboard.vue
│   │   │   ├── Docs.vue
│   │   │   ├── ForgotPassword.vue
│   │   │   ├── Landing.vue
│   │   │   ├── Login.vue
│   │   │   ├── Logs.vue
│   │   │   ├── Metrics.vue
│   │   │   ├── Permissions.vue
│   │   │   ├── Privacy.vue
│   │   │   ├── Realtime.vue
│   │   │   ├── Register.vue
│   │   │   ├── Servers.vue
│   │   │   ├── Services.vue
│   │   │   ├── Settings.vue
│   │   │   ├── Terms.vue
│   │   │   ├── Traces.vue
│   │   │   └── Users.vue
│   │   ├── App.vue
│   │   └── main.js
│   ├── .gitignore
│   ├── Dockerfile
│   ├── index.html
│   ├── leaf-vision.conf
│   ├── package.json
│   └── vite.config.js
│
├── deploy/                           # 部署配置
│   ├── prometheus/
│   │   └── prometheus.yml
│   └── alertmanager/
│       └── alertmanager.yml
│
├── docker-compose.yml                # Docker Compose 配置
├── API-DOCUMENT.md                   # API 文档
└── README.md                         # 项目说明
```

---

## 五、快速开始

### 5.1 方式一：Docker Compose 部署（推荐）

**前提条件：**
- Docker 20.10+
- Docker Compose 2.0+

**部署步骤：**

```bash
# 1. 克隆项目
git clone https://gitee.com/Yangshengzhou/leaf-vision.git
cd leaf-vision

# 2. 启动所有服务
docker-compose up -d

# 3. 查看服务状态
docker-compose ps

# 4. 查看日志
docker-compose logs -f backend
```

**服务访问地址：**

| 服务 | 地址 | 说明 |
|------|------|------|
| LeafVision 前端 | http://localhost | Web 界面 |
| LeafVision 后端 | http://localhost:8081 | API 服务 |
| Prometheus | http://localhost:9090 | 监控数据 |
| Alertmanager | http://localhost:9093 | 告警管理 |

### 5.2 方式二：本地开发部署

#### 后端启动

```bash
cd backend

# 安装依赖并编译
mvn clean package -DskipTests

# 启动服务
java -jar target/leafvision-*.jar
```

后端服务运行在 http://localhost:8081

#### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 开发模式启动
npm run dev

# 或构建生产版本
npm run build
```

前端服务运行在 http://localhost:5173

---

## 六、使用指南

### 6.1 添加 Prometheus 节点

1. 进入「服务器管理」页面
2. 点击「添加服务器」按钮
3. 填写节点信息：
   - **名称**：如 `Prometheus 主节点`
   - **IP 地址**：如 `192.168.1.100`
   - **端口**：默认 `9090`
   - **类型**：选择 `prometheus-master` 或 `prometheus-node`
4. 点击「确定」保存
5. 系统自动检测节点状态并获取版本信息

### 6.2 添加 Alertmanager 实例

1. 进入「服务器管理」页面
2. 点击「添加服务器」按钮
3. 填写实例信息：
   - **名称**：如 `Alertmanager`
   - **IP 地址**：如 `192.168.1.103`
   - **端口**：默认 `9093`
   - **类型**：选择 `alertmanager`
4. 点击「确定」保存
5. 系统自动同步告警信息

### 6.3 查看监控数据

1. 进入「监控看板」页面
2. 查看各节点的 CPU、内存使用率
3. 查看历史趋势图表
4. 点击「刷新」按钮获取最新数据

### 6.4 管理告警

1. 进入「告警管理」页面
2. 在「告警列表」标签查看当前告警
3. 点击「详情」查看告警详细信息
4. 点击「恢复」手动解决告警
5. 在「告警规则」标签管理告警规则

### 6.5 查询指标

1. 进入「指标查询」页面
2. 选择目标服务器
3. 选择或输入指标名称
4. 选择时间范围
5. 点击「查询」获取数据
6. 点击「导出」下载 JSON 数据

---

## 七、配置说明

### 7.1 后端配置 (application.yml)

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:file:./data/leafvision;AUTO_SERVER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password: 

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true

leafvision:
  sync:
    enabled: true           # 是否启用定时同步
    interval: 60000         # 同步间隔(毫秒)
  prometheus:
    timeout: 10000          # Prometheus 请求超时(毫秒)
  alertmanager:
    timeout: 10000          # Alertmanager 请求超时(毫秒)
```

### 7.2 环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| SERVER_PORT | 8081 | 服务端口 |
| LEAFVISION_SYNC_ENABLED | true | 是否启用定时同步 |
| LEAFVISION_SYNC_INTERVAL | 60000 | 同步间隔(毫秒) |
| LEAFVISION_PROMETHEUS_TIMEOUT | 10000 | Prometheus 超时(毫秒) |
| LEAFVISION_ALERTMANAGER_TIMEOUT | 10000 | Alertmanager 超时(毫秒) |
| JAVA_OPTS | -Xms256m -Xmx512m | JVM 参数 |

---

## 八、API 接口

详细接口文档请查看 [API-DOCUMENT.md](./API-DOCUMENT.md)

### 8.1 服务器管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/servers | 获取服务器列表 |
| GET | /api/servers/{id} | 获取服务器详情 |
| POST | /api/servers | 添加服务器 |
| PUT | /api/servers/{id} | 更新服务器 |
| DELETE | /api/servers/{id} | 删除服务器 |
| POST | /api/servers/refresh | 刷新服务器状态 |

### 8.2 告警管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/alerts | 获取告警列表 |
| GET | /api/alerts/active | 获取活跃告警 |
| GET | /api/alerts/rules | 获取告警规则 |
| PUT | /api/alerts/{id}/resolve | 解决告警 |
| POST | /api/alerts/rules | 添加告警规则 |
| PUT | /api/alerts/rules/{id} | 更新告警规则 |
| DELETE | /api/alerts/rules/{id} | 删除告警规则 |
| PUT | /api/alerts/rules/{id}/toggle | 切换规则状态 |
| POST | /api/alerts/sync | 同步告警数据 |

### 8.3 指标查询

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/metrics/query | 即时查询 |
| GET | /api/metrics/query_range | 范围查询 |
| GET | /api/metrics/available | 获取可用指标列表 |
| GET | /api/metrics/instant | 获取即时指标 |
| GET | /api/metrics/system | 获取系统指标 |

### 8.4 仪表盘

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/dashboard/overview | 获取仪表盘数据 |

---

## 九、常见问题

### Q1: LeafVision 和 Grafana 有什么区别？

| 对比项 | LeafVision | Grafana |
|--------|------------|---------|
| 定位 | Prometheus 集群管理平台 | 通用数据可视化平台 |
| 核心功能 | 多集群管理、告警聚合 | 仪表盘、数据可视化 |
| 数据源 | 仅 Prometheus | 支持多种数据源 |
| 适用场景 | 管理多个 Prometheus 集群 | 数据可视化和展示 |

### Q2: 为什么 Java 要主动请求 Prometheus？

1. **Prometheus 是被动服务** - 它只提供 HTTP API，不会主动推送数据
2. **架构简单** - 不需要在 Prometheus 端配置 webhook
3. **灵活控制** - LeafVision 可以控制查询频率和时机
4. **减少耦合** - Prometheus 无需知道 LeafVision 的存在

### Q3: 如何连接已有的 Prometheus 集群？

1. 在「服务器管理」中添加 Prometheus 节点
2. 确保 LeafVision 能够访问 Prometheus 的 IP 和端口
3. 系统会自动检测连接状态

### Q4: 数据存储在哪里？

LeafVision 使用 H2 嵌入式数据库存储：
- 服务器配置信息
- 同步的告警记录
- 同步的告警规则

数据文件位于 `./data/leafvision.mv.db`

---

## 十、开发计划

- [ ] 支持用户认证和权限管理
- [ ] 支持自定义仪表盘
- [ ] 支持告警通知渠道配置
- [ ] 支持 Prometheus 规则热更新
- [ ] 支持指标数据导出
- [ ] 支持 Prometheus 联邦查询
- [ ] 支持告警静默管理

---

## 十一、作者信息

**开发者**: LeafVision Team  
**电子邮件**: YangSZ03@foxmail.com  
**所属机构**: 江西科技师范大学

如有问题或建议，欢迎通过邮件联系。

---

## 十二、许可证

MIT License
