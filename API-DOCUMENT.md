# LeafVision API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8081/api`
- **Content-Type**: `application/json`

---

## 认证管理

### 用户登录
**POST** `/api/auth/login`
```json
{
  "username": "admin",
  "password": "123456"
}
```

### 用户登出
**POST** `/api/auth/logout`

### 用户注册
**POST** `/api/auth/register`
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### 忘记密码
**POST** `/api/auth/forgot-password`
```json
{
  "email": "user@example.com"
}
```

### 重置密码
**POST** `/api/auth/reset-password`
```json
{
  "token": "reset-token-string",
  "password": "newpassword123"
}
```

### 修改密码
**POST** `/api/auth/change-password`
```json
{
  "oldPassword": "oldpassword123",
  "newPassword": "newpassword123"
}
```

---

## 用户管理

### 获取用户列表
**GET** `/api/users`

### 获取用户详情
**GET** `/api/users/{id}`

### 创建用户
**POST** `/api/users`
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

### 更新用户
**PUT** `/api/users/{id}`
```json
{
  "name": "更新后的名称",
  "email": "newemail@leafvision.com",
  "phone": "13800138002",
  "roleId": 2,
  "status": 1
}
```

### 删除用户
**DELETE** `/api/users/{id}`

### 获取当前用户信息
**GET** `/api/users/me?username={username}`

### 更新当前用户信息
**PUT** `/api/users/me`
```json
{
  "username": "admin",
  "name": "新名称",
  "email": "new@email.com",
  "phone": "13800138000"
}
```

### 修改当前用户密码
**PUT** `/api/users/me/password`
```json
{
  "username": "admin",
  "oldPassword": "oldpass",
  "newPassword": "newpass"
}
```

---

## 角色管理

### 获取角色列表
**GET** `/api/roles`

### 获取角色详情
**GET** `/api/roles/{id}`

### 创建角色
**POST** `/api/roles`
```json
{
  "roleCode": "OPERATOR",
  "roleName": "运维人员",
  "description": "负责日常运维",
  "permissions": ["server:view", "alert:view"]
}
```

### 更新角色
**PUT** `/api/roles/{id}`
```json
{
  "roleName": "新角色名",
  "description": "新描述"
}
```

### 删除角色
**DELETE** `/api/roles/{id}`

### 更新角色权限
**PUT** `/api/roles/{id}/permissions`
```json
{
  "permissions": ["server:view", "server:create", "alert:view"]
}
```

---

## 权限管理

### 获取权限列表
**GET** `/api/permissions`

---

## 服务器管理

### 获取服务器列表
**GET** `/api/servers`

### 获取服务器详情
**GET** `/api/servers/{id}`

### 添加服务器
**POST** `/api/servers`
```json
{
  "name": "新服务器",
  "ip": "192.168.1.200",
  "port": 9090,
  "type": "prometheus-node"
}
```

### 更新服务器
**PUT** `/api/servers/{id}`
```json
{
  "name": "更新后的名称",
  "ip": "192.168.1.200",
  "port": 9090
}
```

### 删除服务器
**DELETE** `/api/servers/{id}`

### 刷新服务器状态
**POST** `/api/servers/refresh`

### 获取实时指标
**GET** `/api/servers/{id}/realtime`

---

## 服务器分组

### 获取分组列表
**GET** `/api/server-groups`

### 获取分组树
**GET** `/api/server-groups/tree`

### 获取分组详情
**GET** `/api/server-groups/{id}`

### 创建分组
**POST** `/api/server-groups`
```json
{
  "name": "测试环境",
  "description": "测试服务器组",
  "parentId": 0,
  "sortOrder": 2
}
```

### 更新分组
**PUT** `/api/server-groups/{id}`
```json
{
  "name": "新分组名",
  "description": "新描述"
}
```

### 删除分组
**DELETE** `/api/server-groups/{id}`

### 添加服务器到分组
**POST** `/api/server-groups/{groupId}/servers/{serverId}`

### 从分组移除服务器
**DELETE** `/api/server-groups/{groupId}/servers/{serverId}`

### 获取分组下的服务器
**GET** `/api/server-groups/{id}/servers`

---

## 容器管理

### 获取容器列表
**GET** `/api/containers`

### 获取容器详情
**GET** `/api/containers/{id}?serverId={serverId}`

### 启动容器
**POST** `/api/containers/{id}/start`
```json
{
  "serverId": "server-id"
}
```

### 停止容器
**POST** `/api/containers/{id}/stop`
```json
{
  "serverId": "server-id"
}
```

### 重启容器
**POST** `/api/containers/{id}/restart`
```json
{
  "serverId": "server-id"
}
```

### 获取容器日志
**GET** `/api/containers/{id}/logs?tail=100&serverId={serverId}`

### 获取容器统计信息
**GET** `/api/containers/{id}/stats?serverId={serverId}`

### 创建容器
**POST** `/api/containers`
```json
{
  "serverId": "server-id",
  "image": "nginx:latest",
  "name": "my-nginx",
  "ports": ["80:80"]
}
```

---

## 服务管理

### 获取服务列表
**GET** `/api/services`

### 获取服务详情
**GET** `/api/services/{id}`

### 创建服务
**POST** `/api/services`
```json
{
  "name": "prometheus",
  "type": "monitoring",
  "endpoint": "http://192.168.1.100:9090"
}
```

### 更新服务
**PUT** `/api/services/{id}`
```json
{
  "name": "新服务名",
  "endpoint": "http://new.endpoint:9090"
}
```

### 删除服务
**DELETE** `/api/services/{id}`

### 获取服务指标
**GET** `/api/services/{id}/metrics`

---

## 告警管理

### 获取告警列表
**GET** `/api/alerts`

### 获取活跃告警
**GET** `/api/alerts/active`

### 解决告警
**PUT** `/api/alerts/{id}/resolve`

### 同步告警
**POST** `/api/alerts/sync`

### 获取告警规则列表
**GET** `/api/alerts/rules`

### 添加告警规则
**POST** `/api/alerts/rules`
```json
{
  "name": "磁盘使用率告警",
  "expr": "disk_usage > 85",
  "duration": "5m",
  "severity": "warning",
  "enabled": true
}
```

### 更新告警规则
**PUT** `/api/alerts/rules/{id}`
```json
{
  "name": "新规则名",
  "expr": "new_expr",
  "enabled": true
}
```

### 删除告警规则
**DELETE** `/api/alerts/rules/{id}`

### 切换告警规则状态
**PUT** `/api/alerts/rules/{id}/toggle`

### 告警 Webhook
**POST** `/api/alerts/webhook`

---

## 指标查询

### 即时查询
**GET** `/api/metrics/query?metric={metric}&serverId={serverId}&timeRange=1h`

### 范围查询
**GET** `/api/metrics/query_range?metric={metric}&serverId={serverId}&start={start}&end={end}&step=15s`

### 获取可用指标
**GET** `/api/metrics/available?serverId={serverId}`

### 获取即时指标
**GET** `/api/metrics/instant?serverId={serverId}`

### 获取系统指标
**GET** `/api/metrics/system?serverId={serverId}`

---

## 日志查询

### 获取日志列表
**GET** `/api/logs?serverId={serverId}&level={level}&keyword={keyword}&limit=100`

---

## 链路追踪

### 获取追踪列表
**GET** `/api/traces?service={service}&operation={operation}&limit=50`

---

## 仪表盘

### 获取仪表盘数据
**GET** `/api/dashboard/overview`

---

## 审计日志

### 获取审计日志
**GET** `/api/audit-logs?module={module}&operation={operation}&keyword={keyword}`

---

## 系统设置

### 获取系统设置
**GET** `/api/settings`

### 更新系统设置
**PUT** `/api/settings`
```json
{
  "siteName": "LeafVision",
  "siteDescription": "Prometheus 集群监控管理平台",
  "alertEmail": "admin@leafvision.com",
  "dataRetention": 30,
  "refreshInterval": 60
}
```
