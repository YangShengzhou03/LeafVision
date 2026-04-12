# LeafVision API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8081/api`
- **Content-Type**: `application/json`
- **字符编码**: `UTF-8`

---

## 统一响应格式

所有接口响应均采用统一的 JSON 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200 表示成功，其他表示失败 |
| message | String | 响应消息 |
| data | Object | 响应数据，类型根据接口而定 |

### 常见状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 服务器管理 API

### 1. 获取服务器列表

**GET** `/api/servers`

获取所有服务器信息列表。

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "Prometheus 主节点",
      "ip": "192.168.1.100",
      "port": 9090,
      "type": "prometheus-master",
      "status": "online",
      "cpuUsage": 45.0,
      "memoryUsage": 68.0
    }
  ]
}
```

**Server 对象字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 服务器唯一标识 |
| name | String | 服务器名称 |
| ip | String | IP 地址 |
| port | Integer | 端口号 |
| type | String | 服务器类型 (prometheus-master, prometheus-node, alertmanager) |
| status | String | 状态 (online, offline) |
| cpuUsage | Double | CPU 使用率 (0-100) |
| memoryUsage | Double | 内存使用率 (0-100) |

---

### 2. 获取服务器详情

**GET** `/api/servers/{id}`

根据 ID 获取单个服务器详细信息。

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 服务器 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "Prometheus 主节点",
    "ip": "192.168.1.100",
    "port": 9090,
    "type": "prometheus-master",
    "status": "online",
    "cpuUsage": 45.0,
    "memoryUsage": 68.0
  }
}
```

**错误响应**:
```json
{
  "code": 404,
  "message": "服务器不存在",
  "data": null
}
```

---

### 3. 添加服务器

**POST** `/api/servers`

添加新的服务器。

**请求体**:
```json
{
  "name": "新服务器",
  "ip": "192.168.1.200",
  "port": 9090,
  "type": "prometheus-node"
}
```

**请求字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 服务器名称 |
| ip | String | 是 | IP 地址 |
| port | Integer | 否 | 端口号，默认 9090 |
| type | String | 否 | 服务器类型，默认 prometheus-node |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 5,
    "name": "新服务器",
    "ip": "192.168.1.200",
    "port": 9090,
    "type": "prometheus-node",
    "status": "online",
    "cpuUsage": null,
    "memoryUsage": null
  }
}
```

---

### 4. 更新服务器

**PUT** `/api/servers/{id}`

更新指定服务器的信息。

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 服务器 ID |

**请求体**:
```json
{
  "name": "更新后的服务器",
  "ip": "192.168.1.201",
  "port": 9091,
  "type": "alertmanager",
  "status": "online",
  "cpuUsage": 50.0,
  "memoryUsage": 60.0
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "更新后的服务器",
    "ip": "192.168.1.201",
    "port": 9091,
    "type": "alertmanager",
    "status": "online",
    "cpuUsage": 50.0,
    "memoryUsage": 60.0
  }
}
```

---

### 5. 删除服务器

**DELETE** `/api/servers/{id}`

删除指定的服务器。

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 服务器 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 6. 刷新服务器状态

**POST** `/api/servers/refresh`

刷新所有服务器的 CPU 和内存使用率。

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 告警管理 API

### 1. 获取告警列表

**GET** `/api/alerts`

获取所有告警信息。

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "CPU 使用率过高",
      "level": "critical",
      "server": "Prometheus 主节点",
      "value": "92%",
      "time": "2024-01-15 14:30:00",
      "status": "firing"
    }
  ]
}
```

**Alert 对象字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 告警唯一标识 |
| name | String | 告警名称 |
| level | String | 告警级别 (critical, warning) |
| server | String | 关联服务器名称 |
| value | String | 当前值 |
| time | String | 触发时间 |
| status | String | 告警状态 (firing, resolved) |

---

### 2. 获取告警规则列表

**GET** `/api/alerts/rules`

获取所有告警规则。

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "CPU 使用率告警",
      "expr": "cpu_usage > 80",
      "duration": "5m",
      "level": "warning",
      "enabled": true
    }
  ]
}
```

**AlertRule 对象字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 规则唯一标识 |
| name | String | 规则名称 |
| expr | String | 告警表达式 |
| duration | String | 持续时间 |
| level | String | 告警级别 (critical, warning) |
| enabled | Boolean | 是否启用 |

---

### 3. 解决告警

**PUT** `/api/alerts/{id}/resolve`

将指定告警标记为已解决。

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 告警 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 4. 添加告警规则

**POST** `/api/alerts/rules`

添加新的告警规则。

**请求体**:
```json
{
  "name": "磁盘使用率告警",
  "expr": "disk_usage > 85",
  "duration": "5m",
  "level": "warning",
  "enabled": true
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 5,
    "name": "磁盘使用率告警",
    "expr": "disk_usage > 85",
    "duration": "5m",
    "level": "warning",
    "enabled": true
  }
}
```

---

### 5. 更新告警规则

**PUT** `/api/alerts/rules/{id}`

更新指定的告警规则。

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 规则 ID |

**请求体**:
```json
{
  "name": "更新后的规则",
  "expr": "cpu_usage > 85",
  "duration": "3m",
  "level": "critical",
  "enabled": true
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "更新后的规则",
    "expr": "cpu_usage > 85",
    "duration": "3m",
    "level": "critical",
    "enabled": true
  }
}
```

---

### 6. 删除告警规则

**DELETE** `/api/alerts/rules/{id}`

删除指定的告警规则。

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 规则 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 7. 切换告警规则状态

**PUT** `/api/alerts/rules/{id}/toggle`

切换告警规则的启用/禁用状态。

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 规则 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 指标查询 API

### 1. 即时查询

**GET** `/api/metrics/query`

查询指定指标的即时数据。

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| metric | String | 否 | - | 指标名称 |
| server | String | 否 | - | 服务器 IP |
| timeRange | String | 否 | 1h | 时间范围 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "success",
    "data": {
      "resultType": "vector",
      "result": [
        {
          "metric": {
            "server": "192.168.1.100",
            "job": "prometheus"
          },
          "value": [1705315800, "45.5"]
        }
      ]
    }
  }
}
```

---

### 2. 范围查询

**GET** `/api/metrics/query_range`

查询指定时间范围内的指标数据。

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| metric | String | 否 | - | 指标名称 |
| server | String | 否 | - | 服务器 IP |
| timeRange | String | 否 | 1h | 时间范围 |
| step | String | 否 | 15s | 查询步长 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "success",
    "data": {
      "resultType": "matrix",
      "result": [
        {
          "metric": {
            "server": "192.168.1.100",
            "job": "prometheus"
          },
          "values": [
            [1705315800, "45.5"],
            [1705315740, "42.3"],
            [1705315680, "38.7"]
          ]
        }
      ]
    }
  }
}
```

---

## 仪表盘 API

### 获取仪表盘数据

**GET** `/api/dashboard/overview`

获取仪表盘概览数据，包括统计卡片、趋势图数据、网络分布和服务器列表。

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "statsCards": [
      {
        "title": "CPU 使用率",
        "value": "45%",
        "type": "cpu",
        "color": "#409EFF"
      },
      {
        "title": "内存使用率",
        "value": "68%",
        "type": "memory",
        "color": "#67C23A"
      },
      {
        "title": "网络流量",
        "value": "1.2 GB/s",
        "type": "network",
        "color": "#E6A23C"
      },
      {
        "title": "运行时间",
        "value": "15天 8小时",
        "type": "uptime",
        "color": "#909399"
      }
    ],
    "cpuTrend": [35.5, 42.3, 48.7, 55.2, 45.8, 38.9, 42.1],
    "memoryTrend": [58.2, 62.5, 68.3, 72.1, 65.4, 60.8, 63.2],
    "networkDistribution": [
      { "name": "入站流量", "value": 1048 },
      { "name": "出站流量", "value": 735 },
      { "name": "内部通信", "value": 580 }
    ],
    "serverList": [
      {
        "id": 1,
        "name": "Prometheus 主节点",
        "ip": "192.168.1.100",
        "port": 9090,
        "type": "prometheus-master",
        "status": "online",
        "cpuUsage": 45.0,
        "memoryUsage": 68.0
      }
    ]
  }
}
```

**DashboardData 对象字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| statsCards | List<Map> | 统计卡片数据 |
| cpuTrend | List<Double> | CPU 使用率趋势数据 (7个数据点) |
| memoryTrend | List<Double> | 内存使用率趋势数据 (7个数据点) |
| networkDistribution | List<Map> | 网络流量分布数据 |
| serverList | List<Server> | 服务器列表 |

---

## 错误处理

所有接口在发生错误时，会返回相应的错误码和错误信息：

```json
{
  "code": 404,
  "message": "服务器不存在",
  "data": null
}
```

### 常见错误码

| 错误码 | 说明 |
|--------|------|
| 404 | 资源不存在（服务器、告警、规则等） |
| 500 | 服务器内部错误 |

---

## CORS 配置

API 支持跨域请求，允许所有来源访问：

- `Access-Control-Allow-Origin`: `*`
- `Access-Control-Allow-Methods`: `GET, POST, PUT, DELETE, OPTIONS`
- `Access-Control-Allow-Headers`: `*`
- `Access-Control-Allow-Credentials`: `true`

---

## 前端调用示例

```javascript
import { api } from '@/api'

// 获取服务器列表
const servers = await api.getServerList()

// 添加服务器
const newServer = await api.addServer({
  name: '新服务器',
  ip: '192.168.1.200',
  port: 9090
})

// 获取告警列表
const alerts = await api.getAlertList()

// 查询指标
const metrics = await api.queryMetrics({
  metric: 'cpu_usage',
  server: '192.168.1.100',
  timeRange: '1h'
})

// 获取仪表盘数据
const dashboard = await api.getDashboardData()
```
