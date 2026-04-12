# LeafVision Frontend

LeafVision 监控系统前端应用

## 技术栈

- Vue 3 + Composition API
- Element Plus UI 组件库
- ECharts + vue-echarts 图表库
- Vue Router 路由管理
- Pinia 状态管理
- Axios HTTP 客户端
- Vite 构建工具

## 开发

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

前端服务运行在 http://localhost:8080

## 目录结构

```
src/
├── api/           # API 请求封装
├── router/        # 路由配置
├── views/         # 页面组件
│   ├── Dashboard.vue  # 监控看板
│   ├── Servers.vue    # 服务器管理
│   ├── Alerts.vue     # 告警管理
│   └── Metrics.vue    # 指标查询
├── App.vue        # 主应用组件
├── main.js        # 应用入口
└── style.css      # 全局样式
```

## 功能模块

| 模块 | 路由 | 说明 |
|------|------|------|
| 监控看板 | /dashboard | 实时监控数据展示 |
| 服务器管理 | /servers | 服务器 CRUD 操作 |
| 告警管理 | /alerts | 告警列表和规则管理 |
| 指标查询 | /metrics | Prometheus 指标查询 |

## 代理配置

开发环境通过 Vite 代理转发 API 请求到后端：

```javascript
// vite.config.js
server: {
  port: 8080,
  proxy: {
    '/api': {
      target: 'http://localhost:8081',
      changeOrigin: true
    }
  }
}
```
