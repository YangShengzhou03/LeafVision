<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeSection = ref('getting-started')

const handleBackHome = () => router.push('/')

const sections = [
  { id: 'getting-started', title: '快速开始' },
  { id: 'installation', title: '安装部署' },
  { id: 'configuration', title: '配置说明' },
  { id: 'api', title: 'API 参考' },
  { id: 'faq', title: '常见问题' }
]

const handleSectionChange = (id) => {
  activeSection.value = id
}
</script>

<template>
  <div class="docs-page">
    <header class="page-header">
      <div class="header-content">
        <span class="brand" @click="handleBackHome">LEAFVISION</span>
        <nav class="header-nav">
          <router-link to="/login" class="nav-link">登录</router-link>
        </nav>
      </div>
    </header>

    <main class="page-main">
      <aside class="sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">文档目录</span>
        </div>
        <nav class="sidebar-nav">
          <button
            v-for="section in sections"
            :key="section.id"
            :class="['nav-item', { active: activeSection === section.id }]"
            @click="handleSectionChange(section.id)"
          >
            {{ section.title }}
          </button>
        </nav>
      </aside>

      <div class="content-area">
        <div v-if="activeSection === 'getting-started'" class="doc-section">
          <h1>快速开始</h1>
          <p class="intro">
            LeafVision 是一款企业级基础设施可观测性平台，提供全方位的监控、告警和分析能力。
            本指南将帮助您快速上手。
          </p>

          <h2>系统要求</h2>
          <ul>
            <li>Node.js 18.0 或更高版本</li>
            <li>Go 1.21 或更高版本（后端服务）</li>
            <li>PostgreSQL 14.0 或更高版本</li>
            <li>Redis 6.0 或更高版本</li>
          </ul>

          <h2>快速部署</h2>
          <div class="code-block">
            <code>git clone https://gitee.com/Yangshengzhou/leaf-vision.git</code>
          </div>
          <div class="code-block">
            <code>cd leafvision && docker-compose up -d</code>
          </div>

          <h2>访问系统</h2>
          <p>部署完成后，访问 <code>http://localhost:8080</code> 即可进入系统。</p>
          <p>默认管理员账号：<code>admin@leafvision.io</code></p>
          <p>默认密码：<code>admin123</code></p>
        </div>

        <div v-if="activeSection === 'installation'" class="doc-section">
          <h1>安装部署</h1>
          <p class="intro">
            LeafVision 支持多种部署方式，您可以根据实际需求选择合适的方案。
          </p>

          <h2>Docker 部署（推荐）</h2>
          <p>使用 Docker Compose 可以快速部署完整的 LeafVision 系统。</p>
          <div class="code-block">
            <code>docker-compose up -d</code>
          </div>

          <h2>Kubernetes 部署</h2>
          <p>我们提供了完整的 Kubernetes Helm Chart，支持高可用部署。</p>
          <div class="code-block">
            <code>helm install leafvision ./helm/leafvision</code>
          </div>

          <h2>源码编译</h2>
          <p>您也可以从源码编译部署。</p>
          <div class="code-block">
            <code>make build && ./bin/leafvision-server</code>
          </div>
        </div>

        <div v-if="activeSection === 'configuration'" class="doc-section">
          <h1>配置说明</h1>
          <p class="intro">
            LeafVision 的配置文件位于 <code>config.yaml</code>，支持灵活的配置选项。
          </p>

          <h2>数据库配置</h2>
          <div class="code-block">
            <pre>database:
  host: localhost
  port: 5432
  name: leafvision
  user: postgres
  password: your_password</pre>
          </div>

          <h2>Redis 配置</h2>
          <div class="code-block">
            <pre>redis:
  host: localhost
  port: 6379
  password: ""
  db: 0</pre>
          </div>

          <h2>监控配置</h2>
          <div class="code-block">
            <pre>monitor:
  metrics_interval: 15s
  retention_days: 30
  log_level: info</pre>
          </div>
        </div>

        <div v-if="activeSection === 'api'" class="doc-section">
          <h1>API 参考</h1>
          <p class="intro">
            LeafVision 提供完整的 RESTful API，方便集成到您的系统中。
          </p>

          <h2>认证</h2>
          <p>所有 API 请求需要在 Header 中携带 JWT Token：</p>
          <div class="code-block">
            <code>Authorization: Bearer &lt;your_token&gt;</code>
          </div>

          <h2>服务器管理</h2>
          <table class="api-table">
            <thead>
              <tr>
                <th>方法</th>
                <th>路径</th>
                <th>描述</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><span class="method get">GET</span></td>
                <td>/api/v1/servers</td>
                <td>获取服务器列表</td>
              </tr>
              <tr>
                <td><span class="method post">POST</span></td>
                <td>/api/v1/servers</td>
                <td>添加服务器</td>
              </tr>
              <tr>
                <td><span class="method put">PUT</span></td>
                <td>/api/v1/servers/:id</td>
                <td>更新服务器信息</td>
              </tr>
              <tr>
                <td><span class="method delete">DELETE</span></td>
                <td>/api/v1/servers/:id</td>
                <td>删除服务器</td>
              </tr>
            </tbody>
          </table>

          <h2>指标查询</h2>
          <table class="api-table">
            <thead>
              <tr>
                <th>方法</th>
                <th>路径</th>
                <th>描述</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><span class="method get">GET</span></td>
                <td>/api/v1/metrics/query</td>
                <td>查询指标数据</td>
              </tr>
              <tr>
                <td><span class="method get">GET</span></td>
                <td>/api/v1/metrics/realtime</td>
                <td>实时指标数据</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="activeSection === 'faq'" class="doc-section">
          <h1>常见问题</h1>
          <p class="intro">
            以下是用户常见的问题及解答。
          </p>

          <h2>Q: 如何重置管理员密码？</h2>
          <p>A: 您可以通过命令行工具重置密码：</p>
          <div class="code-block">
            <code>leafvision-cli reset-password --email admin@leafvision.io</code>
          </div>

          <h2>Q: 支持哪些监控目标？</h2>
          <p>A: LeafVision 支持监控 Linux/Windows 服务器、Docker 容器、Kubernetes 集群、以及各类数据库和中间件。</p>

          <h2>Q: 如何配置告警通知？</h2>
          <p>A: 在系统设置中配置邮件服务器或 Webhook，然后在告警规则中设置通知渠道。</p>

          <h2>Q: 数据保留多长时间？</h2>
          <p>A: 默认保留 30 天，您可以在系统设置中调整保留策略。</p>

          <h2>Q: 如何升级系统？</h2>
          <p>A: 使用 Docker 部署的用户可以拉取最新镜像后重启服务：</p>
          <div class="code-block">
            <code>docker-compose pull && docker-compose up -d</code>
          </div>
        </div>
      </div>
    </main>

    <footer class="page-footer">
      <div class="footer-content">
        <span class="copyright">© {{ new Date().getFullYear() }} LeafVision. All rights reserved.</span>
        <div class="footer-links">
          <router-link to="/about">关于</router-link>
          <router-link to="/privacy">隐私</router-link>
          <router-link to="/terms">条款</router-link>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.docs-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(10px);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 40px;
  max-width: 1440px;
  margin: 0 auto;
}

.brand {
  font-size: 18px;
  font-weight: 900;
  color: #ffffff;
  letter-spacing: 0.1em;
  cursor: pointer;
}

.header-nav {
  display: flex;
  gap: 24px;
}

.nav-link {
  font-size: 13px;
  font-weight: 700;
  color: #ffffff;
  text-decoration: none;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  transition: color 0.15s ease;
}

.nav-link:hover {
  color: var(--site-context-highlight-color);
}

.page-main {
  flex: 1;
  display: grid;
  grid-template-columns: 240px 1fr;
  padding-top: 80px;
}

.sidebar {
  position: fixed;
  top: 80px;
  left: 0;
  bottom: 0;
  width: 240px;
  background: #fafafa;
  border-right: 1px solid var(--color-border);
  overflow-y: auto;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-title {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-secondary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.sidebar-nav {
  padding: 12px 0;
}

.nav-item {
  display: block;
  width: 100%;
  padding: 12px 20px;
  background: transparent;
  border: none;
  text-align: left;
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.15s ease;
}

.nav-item:hover {
  background: #ffffff;
  color: var(--color-text-primary);
}

.nav-item.active {
  background: #ffffff;
  color: var(--site-context-highlight-color);
  border-left: 3px solid var(--site-context-highlight-color);
}

.content-area {
  grid-column: 2;
  padding: 40px;
  max-width: 900px;
}

.doc-section {
  background: #ffffff;
  padding: 40px;
  border: 1px solid var(--color-border);
}

.doc-section h1 {
  font-size: 32px;
  font-weight: 300;
  color: var(--color-text-primary);
  margin: 0 0 16px;
  letter-spacing: -0.02em;
}

.doc-section .intro {
  font-size: 16px;
  font-weight: 400;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--color-border);
}

.doc-section h2 {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 32px 0 16px;
  letter-spacing: 0.02em;
}

.doc-section h2:first-of-type {
  margin-top: 0;
}

.doc-section p {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-primary);
  line-height: 1.6;
  margin: 0 0 16px;
}

.doc-section ul {
  margin: 0 0 16px;
  padding-left: 20px;
}

.doc-section li {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-primary);
  line-height: 1.8;
}

.doc-section code {
  background: #f5f5f5;
  padding: 2px 6px;
  font-family: monospace;
  font-size: 13px;
  color: var(--site-context-highlight-color);
}

.code-block {
  background: #1e1e1e;
  padding: 16px;
  margin: 16px 0;
  overflow-x: auto;
}

.code-block code,
.code-block pre {
  background: transparent;
  padding: 0;
  color: #d4d4d4;
  font-family: monospace;
  font-size: 13px;
  line-height: 1.5;
}

.api-table {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.api-table th {
  text-align: left;
  padding: 12px;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-secondary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--color-border);
}

.api-table td {
  padding: 12px;
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border);
}

.method {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
}

.method.get {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.method.post {
  background: rgba(28, 105, 212, 0.1);
  color: var(--site-context-highlight-color);
}

.method.put {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.method.delete {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.page-footer {
  background: #000000;
  padding: 24px 40px;
  grid-column: 1 / -1;
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1440px;
  margin: 0 auto;
}

.copyright {
  font-size: 12px;
  font-weight: 400;
  color: #757575;
}

.footer-links {
  display: flex;
  gap: 24px;
}

.footer-links a {
  font-size: 12px;
  font-weight: 400;
  color: #757575;
  text-decoration: none;
  transition: color 0.15s ease;
}

.footer-links a:hover {
  color: #ffffff;
}

@media (max-width: 768px) {
  .page-main {
    grid-template-columns: 1fr;
  }
  .sidebar {
    display: none;
  }
  .content-area {
    grid-column: 1;
    padding: 20px;
  }
  .doc-section {
    padding: 24px;
  }
}
</style>
