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
| 400 | 请求参数错误 |
| 401 | 认证失败 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 认证管理 API

### 1. 用户登录

**POST** `/api/auth/login`

用户登录认证。

**请求体**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "系统管理员",
    "role": "ADMIN",
    "permissions": ["user:view", "user:create", "user:update", "user:delete", "role:view", "role:create", "role:update", "role:delete", "server:view", "server:create", "server:update", "server:delete", "alert:view", "alert:create", "alert:update", "alert:delete", "settings:view", "settings:update"]
  }
}
```

### 2. 用户登出

**POST** `/api/auth/logout`

用户登出。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 用户管理 API

### 1. 获取用户列表

**GET** `/api/users`

获取所有用户信息列表。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "name": "系统管理员",
      "email": "admin@leafvision.com",
      "phone": "13800138000",
      "roleId": 1,
      "roleCode": "ADMIN",
      "roleName": "系统管理员",
      "status": 1,
      "createdAt": "2026-01-15 14:30:00",
      "lastLoginAt": "2026-01-15 14:30:00"
    }
  ]
}
```

**User 对象字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户唯一标识 |
| username | String | 用户名 |
| name | String | 姓名 |
| email | String | 邮箱 |
| phone | String | 电话 |
| roleId | Long | 角色ID |
| roleCode | String | 角色编码 |
| roleName | String | 角色名称 |
| status | Integer | 状态（1启用，0禁用） |
| createdAt | String | 创建时间 |
| lastLoginAt | String | 最后登录时间 |

### 2. 获取用户详情

**GET** `/api/users/{id}`

根据 ID 获取单个用户详细信息。

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户 ID |

### 3. 创建用户

**POST** `/api/users`

创建新用户。

**请求体**:
```json
{
  "username": "newuser",
  "password": "password123",
  "name": "新用户",
  "email": "user@leafvision.com",
  "phone": "13800138001",
  "roleId": 2
}
```

### 4. 更新用户

**PUT** `/api/users/{id}`

更新指定用户信息。

**请求体**:
```json
{
  "name": "更新后的名称",
  "email": "newemail@leafvision.com",
  "phone": "13800138002",
  "roleId": 2,
  "status": 1
}
```

### 5. 删除用户

**DELETE** `/api/users/{id}`

删除指定用户。

---

## 角色管理 API

### 1. 获取角色列表

**GET** `/api/roles`

获取所有角色信息。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "roleCode": "ADMIN",
      "roleName": "系统管理员",
      "description": "拥有所有权限",
      "status": 1,
      "permissions": ["user:view", "user:create", "user:update", "user:delete"]
    }
  ]
}
```

### 2. 获取角色详情

**GET** `/api/roles/{id}`

### 3. 创建角色

**POST** `/api/roles`

**请求体**:
```json
{
  "roleCode": "OPERATOR",
  "roleName": "运维人员",
  "description": "负责日常运维",
  "permissions": ["server:view", "alert:view"]
}
```

### 4. 更新角色

**PUT** `/api/roles/{id}`

### 5. 删除角色

**DELETE** `/api/roles/{id}`

注意：系统管理员角色（ADMIN）不可删除。

### 6. 更新角色权限

**PUT** `/api/roles/{id}/permissions`

**请求体**:
```json
{
  "permissions": ["server:view", "server:create", "alert:view"]
}
```

---

## 权限管理 API

### 1. 获取权限列表

**GET** `/api/permissions`

获取所有权限信息。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "permissionCode": "user:view",
      "permissionName": "查看用户",
      "module": "user",
      "sortOrder": 1,
      "status": 1
    }
  ]
}
```

---

## 服务器管理 API

### 1. 获取服务器列表

**GET** `/api/servers`

获取所有服务器信息列表。

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

### 2. 获取服务器详情

**GET** `/api/servers/{id}`

### 3. 添加服务器

**POST** `/api/servers`

**请求体**:
```json
{
  "name": "新服务器",
  "ip": "192.168.1.200",
  "port": 9090,
  "type": "prometheus-node"
}
```

### 4. 更新服务器

**PUT** `/api/servers/{id}`

### 5. 删除服务器

**DELETE** `/api/servers/{id}`

### 6. 刷新服务器状态

**POST** `/api/servers/refresh`

刷新所有服务器的 CPU 和内存使用率。

### 7. 获取实时指标

**GET** `/api/servers/{id}/realtime`

获取指定服务器的实时监控指标。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "cpu": 45.5,
    "memory": 68.2,
    "networkIn": 1024.5,
    "networkOut": 512.3
  }
}
```

---

## 容器管理 API

### 1. 获取容器列表

**GET** `/api/containers`

获取所有容器信息。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "prometheus",
      "image": "prom/prometheus:latest",
      "status": "running",
      "cpu": 2.5,
      "memory": 256.0,
      "network": "bridge",
      "ports": "9090:9090"
    }
  ]
}
```

---

## 服务管理 API

### 1. 获取服务列表

**GET** `/api/services`

获取所有服务信息。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "prometheus",
      "type": "monitoring",
      "status": "healthy",
      "endpoint": "http://192.168.1.100:9090",
      "instanceCount": 1,
      "healthyCount": 1,
      "responseTime": 15.5,
      "errorRate": 0.0
    }
  ]
}
```

---

## 告警管理 API

### 1. 获取告警列表

**GET** `/api/alerts`

获取所有告警信息。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "CPU 使用率过高",
      "severity": "critical",
      "server": "Prometheus 主节点",
      "value": "92%",
      "time": "2026-01-15 14:30:00",
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
| severity | String | 告警级别 (critical, warning) |
| server | String | 关联服务器名称 |
| value | String | 当前值 |
| time | String | 触发时间 |
| status | String | 告警状态 (firing, resolved) |

### 2. 获取活跃告警

**GET** `/api/alerts/active`

获取所有未解决的告警。

### 3. 解决告警

**PUT** `/api/alerts/{id}/resolve`

将指定告警标记为已解决。

### 4. 同步告警

**POST** `/api/alerts/sync`

从 Alertmanager 和 Prometheus 同步告警数据和规则。

### 5. 获取告警规则列表

**GET** `/api/alerts/rules`

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
      "severity": "warning",
      "enabled": true
    }
  ]
}
```

### 6. 添加告警规则

**POST** `/api/alerts/rules`

**请求体**:
```json
{
  "name": "磁盘使用率告警",
  "expr": "disk_usage > 85",
  "duration": "5m",
  "severity": "warning",
  "enabled": true
}
```

### 7. 更新告警规则

**PUT** `/api/alerts/rules/{id}`

### 8. 删除告警规则

**DELETE** `/api/alerts/rules/{id}`

### 9. 切换告警规则状态

**PUT** `/api/alerts/rules/{id}/toggle`

切换告警规则的启用/禁用状态。

---

## 指标查询 API

### 1. 即时查询

**GET** `/api/metrics/query`

查询指定指标的即时数据。

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| metric | String | 是 | - | 指标名称 |
| serverId | Long | 是 | - | 服务器 ID |
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
      "result": [...]
    }
  }
}
```

### 2. 范围查询

**GET** `/api/metrics/query_range`

查询指定时间范围内的指标数据。

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| metric | String | 是 | - | 指标名称 |
| serverId | Long | 是 | - | 服务器 ID |
| start | Long | 是 | - | 开始时间戳 |
| end | Long | 是 | - | 结束时间戳 |
| step | String | 否 | 15s | 查询步长 |

### 3. 获取可用指标

**GET** `/api/metrics/available`

获取服务器支持的指标列表。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| serverId | Long | 是 | 服务器 ID |

### 4. 获取即时指标

**GET** `/api/metrics/instant`

获取服务器的即时监控指标。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| serverId | Long | 是 | 服务器 ID |

### 5. 获取系统指标

**GET** `/api/metrics/system`

获取服务器的系统指标。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| serverId | Long | 是 | 服务器 ID |

---

## 日志查询 API

### 1. 获取日志列表

**GET** `/api/logs`

查询日志数据。

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| serverId | Long | 否 | - | 服务器 ID |
| level | String | 否 | - | 日志级别 (debug, info, warn, error, all) |
| keyword | String | 否 | - | 关键词搜索 |
| startTime | String | 否 | - | 开始时间 |
| endTime | String | 否 | - | 结束时间 |
| limit | Integer | 否 | 100 | 返回数量限制 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "time": "2026-01-15 14:30:00",
      "level": "info",
      "source": "web-server",
      "message": "Request processed successfully"
    }
  ]
}
```

---

## 链路追踪 API

### 1. 获取追踪列表

**GET** `/api/traces`

查询链路追踪数据。

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| service | String | 否 | - | 服务名称过滤 |
| operation | String | 否 | - | 操作名称过滤 |
| minDuration | String | 否 | - | 最小持续时间 |
| maxDuration | String | 否 | - | 最大持续时间 |
| limit | Integer | 否 | 50 | 返回数量限制 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "traceId": "a1b2c3d4e5f6g7h8",
      "service": "user-service",
      "operation": "GET /api/users",
      "duration": 15000,
      "spanCount": 5,
      "status": "ok",
      "startTime": "2026-01-15 14:30:00"
    }
  ]
}
```

---

## 仪表盘 API

### 获取仪表盘数据

**GET** `/api/dashboard/overview`

获取仪表盘概览数据。

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
    "serverList": [...]
  }
}
```

---

## 审计日志 API

### 1. 获取审计日志

**GET** `/api/audit-logs`

查询审计日志。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| module | String | 否 | 模块过滤 |
| operation | String | 否 | 操作类型过滤 |
| keyword | String | 否 | 关键词搜索 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "username": "admin",
      "operation": "LOGIN",
      "module": "auth",
      "targetType": null,
      "targetId": null,
      "targetName": "登录成功",
      "detail": null,
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "status": 1,
      "createdAt": "2026-01-15 14:30:00"
    }
  ]
}
```

---

## 系统设置 API

### 1. 获取系统设置

**GET** `/api/settings`

获取系统配置信息。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "siteName": "LeafVision",
    "siteDescription": "Prometheus 集群监控管理平台",
    "alertEmail": "admin@leafvision.com",
    "alertWebhook": "http://localhost:8081/api/alerts/webhook",
    "dataRetention": 30,
    "refreshInterval": 60
  }
}
```

### 2. 更新系统设置

**PUT** `/api/settings`

更新系统配置。

**请求体**:
```json
{
  "siteName": "LeafVision",
  "siteDescription": "Prometheus 集群监控管理平台",
  "alertEmail": "admin@leafvision.com",
  "dataRetention": 30,
  "refreshInterval": 60
}
```

---

## 告警 Webhook API

### 接收告警通知

**POST** `/api/alerts/webhook`

接收来自 Alertmanager 的告警通知。

**请求体**: Alertmanager 标准告警格式

---

## 用户注册 API

### 1. 用户注册

**POST** `/api/auth/register`

用户注册，用户名由系统自动生成（格式：u + 6位随机数字）。

**请求体**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "username": "u123456",
    "name": "u123456"
  }
}
```

### 2. 忘记密码

**POST** `/api/auth/forgot-password`

发送密码重置邮件。

**请求体**:
```json
{
  "email": "user@example.com"
}
```

### 3. 重置密码

**POST** `/api/auth/reset-password`

通过令牌重置密码。

**请求体**:
```json
{
  "token": "reset-token-string",
  "password": "newpassword123"
}
```

### 4. 修改密码

**POST** `/api/auth/change-password`

修改当前用户密码。

**请求体**:
```json
{
  "oldPassword": "oldpassword123",
  "newPassword": "newpassword123"
}
```

---

## 服务器分组 API

### 1. 获取分组列表

**GET** `/api/server-groups`

获取所有服务器分组。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "生产环境",
      "description": "生产服务器组",
      "parentId": 0,
      "sortOrder": 1,
      "status": 1,
      "createdAt": "2026-01-15 14:30:00"
    }
  ]
}
```

### 2. 获取分组树

**GET** `/api/server-groups/tree`

获取分组树形结构（包含子分组）。

### 3. 创建分组

**POST** `/api/server-groups`

**请求体**:
```json
{
  "name": "测试环境",
  "description": "测试服务器组",
  "parentId": 0,
  "sortOrder": 2
}
```

### 4. 更新分组

**PUT** `/api/server-groups/{id}`

### 5. 删除分组

**DELETE** `/api/server-groups/{id}`

### 6. 添加服务器到分组

**POST** `/api/server-groups/{groupId}/servers/{serverId}`

### 7. 从分组移除服务器

**DELETE** `/api/server-groups/{groupId}/servers/{serverId}`

### 8. 获取分组内的服务器

**GET** `/api/server-groups/{id}/servers`

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
      "status": "online"
    }
  ]
}
```

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
import { login, getServerList, getAlertList, queryMetrics, getDashboardData } from '@/api'

// 用户登录
const user = await login({ username: 'admin', password: 'admin123' })

// 获取服务器列表
const servers = await getServerList()

// 获取告警列表
const alerts = await getAlertList()

// 查询指标
const metrics = await queryMetrics({
  metric: 'cpu_usage',
  serverId: 1,
  timeRange: '1h'
})

// 获取仪表盘数据
const dashboard = await getDashboardData()
```
