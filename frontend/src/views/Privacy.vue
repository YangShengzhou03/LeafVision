<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const handleBackHome = () => router.push('/')

const lastUpdated = '2024年1月1日'
</script>

<template>
  <div class="privacy-page">
    <header class="page-header">
      <div class="header-content">
        <span class="brand" @click="handleBackHome">LEAFVISION</span>
        <nav class="header-nav">
          <router-link to="/login" class="nav-link">登录</router-link>
        </nav>
      </div>
    </header>

    <main class="page-main">
      <article class="legal-content">
        <header class="article-header">
          <span class="section-tag">LEGAL</span>
          <h1>隐私政策</h1>
          <p class="update-date">最后更新：{{ lastUpdated }}</p>
        </header>

        <section class="article-section">
          <h2>引言</h2>
          <p>
            LeafVision（以下简称"我们"）非常重视您的隐私保护。本隐私政策旨在向您说明我们如何收集、
            使用、存储和保护您的个人信息。使用我们的服务即表示您同意本隐私政策的条款。
          </p>
        </section>

        <section class="article-section">
          <h2>信息收集</h2>
          <p>我们可能收集以下类型的信息：</p>
          <ul>
            <li><strong>账户信息</strong>：包括您的姓名、电子邮件地址、公司名称等注册信息。</li>
            <li><strong>使用数据</strong>：包括登录时间、使用功能、操作记录等服务使用情况。</li>
            <li><strong>技术数据</strong>：包括 IP 地址、浏览器类型、设备信息等技术参数。</li>
            <li><strong>监控数据</strong>：您通过我们的服务收集的基础设施监控数据。</li>
          </ul>
        </section>

        <section class="article-section">
          <h2>信息使用</h2>
          <p>我们使用收集的信息用于以下目的：</p>
          <ul>
            <li>提供、维护和改进我们的服务</li>
            <li>处理您的请求和提供客户支持</li>
            <li>发送服务相关的通知和更新</li>
            <li>分析服务使用情况以优化用户体验</li>
            <li>检测和防止欺诈或其他非法活动</li>
          </ul>
        </section>

        <section class="article-section">
          <h2>信息存储与安全</h2>
          <p>
            我们采用行业标准的安全措施来保护您的个人信息，包括但不限于数据加密、访问控制、
            安全审计等。您的数据存储在安全的服务器上，并定期进行备份。
          </p>
          <p>
            我们会尽力保护您的个人信息安全，但请注意，没有任何互联网传输或存储方式是绝对安全的。
          </p>
        </section>

        <section class="article-section">
          <h2>信息共享</h2>
          <p>我们不会出售您的个人信息。我们仅在以下情况下共享您的信息：</p>
          <ul>
            <li>经您明确同意</li>
            <li>与我们的服务提供商共享（他们需遵守保密义务）</li>
            <li>法律要求或响应合法的政府请求</li>
            <li>保护我们或用户的权利、财产或安全</li>
          </ul>
        </section>

        <section class="article-section">
          <h2>您的权利</h2>
          <p>您对您的个人信息享有以下权利：</p>
          <ul>
            <li><strong>访问权</strong>：您可以请求获取我们持有的您的个人信息副本。</li>
            <li><strong>更正权</strong>：您可以请求更正不准确或不完整的个人信息。</li>
            <li><strong>删除权</strong>：您可以请求删除您的个人信息。</li>
            <li><strong>限制处理权</strong>：您可以请求限制对您个人信息的处理。</li>
            <li><strong>数据可携带权</strong>：您可以请求以结构化格式获取您的数据。</li>
          </ul>
        </section>

        <section class="article-section">
          <h2>Cookie 政策</h2>
          <p>
            我们使用 Cookie 和类似技术来提供、保护和改进我们的服务。您可以通过浏览器设置
            管理 Cookie，但这可能影响某些功能的正常使用。
          </p>
        </section>

        <section class="article-section">
          <h2>第三方链接</h2>
          <p>
            我们的服务可能包含指向第三方网站的链接。我们对这些网站的隐私实践不负责任。
            建议您阅读这些网站的隐私政策。
          </p>
        </section>

        <section class="article-section">
          <h2>未成年人保护</h2>
          <p>
            我们的服务不面向 18 岁以下的未成年人。我们不会故意收集未成年人的个人信息。
            如果您发现我们可能收集了未成年人的信息，请联系我们。
          </p>
        </section>

        <section class="article-section">
          <h2>政策更新</h2>
          <p>
            我们可能会不时更新本隐私政策。更新后的政策将在本页面发布，重大变更时我们会
            通过电子邮件或服务内通知告知您。
          </p>
        </section>

        <section class="article-section">
          <h2>联系我们</h2>
          <p>如果您对本隐私政策有任何疑问或建议，请通过以下方式联系我们：</p>
          <ul>
            <li>电子邮件：YangSZ03@foxmail.com</li>
            <li>地址：江西科技师范大学</li>
          </ul>
        </section>
      </article>
    </main>

    <footer class="page-footer">
      <div class="footer-content">
        <span class="copyright">© {{ new Date().getFullYear() }} LeafVision. All rights reserved.</span>
        <div class="footer-links">
          <router-link to="/about">关于</router-link>
          <router-link to="/docs">文档</router-link>
          <router-link to="/terms">条款</router-link>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.privacy-page {
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
  padding: 120px 40px 60px;
  background: #ffffff;
}

.legal-content {
  max-width: 800px;
  margin: 0 auto;
}

.article-header {
  text-align: center;
  margin-bottom: 48px;
  padding-bottom: 32px;
  border-bottom: 1px solid var(--color-border);
}

.section-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  color: var(--site-context-highlight-color);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  margin-bottom: 16px;
}

.article-header h1 {
  font-size: 36px;
  font-weight: 300;
  color: var(--color-text-primary);
  margin: 0 0 16px;
  letter-spacing: -0.02em;
}

.update-date {
  font-size: 13px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

.article-section {
  margin-bottom: 40px;
}

.article-section h2 {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 16px;
  letter-spacing: 0.02em;
}

.article-section p {
  font-size: 15px;
  font-weight: 400;
  color: var(--color-text-primary);
  line-height: 1.8;
  margin: 0 0 16px;
}

.article-section ul {
  margin: 0 0 16px;
  padding-left: 24px;
}

.article-section li {
  font-size: 15px;
  font-weight: 400;
  color: var(--color-text-primary);
  line-height: 1.8;
  margin-bottom: 8px;
}

.article-section li strong {
  font-weight: 700;
  color: var(--color-text-primary);
}

.page-footer {
  background: #000000;
  padding: 24px 40px;
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
    padding: 100px 20px 40px;
  }
  .article-header h1 {
    font-size: 28px;
  }
  .footer-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}
</style>
