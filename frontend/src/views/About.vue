<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const handleBackHome = () => router.push('/')

const stats = [
  { value: '99.9%', label: 'SLA 可用性' },
  { value: '<10ms', label: '采集延迟' },
  { value: '100+', label: '监控维度' },
  { value: '24/7', label: '持续守护' }
]

const team = [
  { name: '技术团队', desc: '来自一线互联网企业的工程师，深耕可观测性领域' },
  { name: '产品理念', desc: '以工程精度重新定义基础设施可观测性' },
  { name: '服务承诺', desc: '企业级稳定性，毫秒级响应，全方位技术支持' }
]
</script>

<template>
  <div class="about-page">
    <header class="page-header">
      <div class="header-content">
        <span class="brand" @click="handleBackHome">LEAFVISION</span>
        <nav class="header-nav">
          <router-link to="/login" class="nav-link">登录</router-link>
        </nav>
      </div>
    </header>

    <main class="page-main">
      <section class="hero-section">
        <span class="section-tag">ABOUT US</span>
        <h1 class="hero-title">关于 LeafVision</h1>
        <p class="hero-desc">
          以工程精度重新定义基础设施可观测性——从指标、日志到链路，构建统一感知体系
        </p>
      </section>

      <section class="stats-section">
        <div class="stats-grid">
          <div v-for="stat in stats" :key="stat.label" class="stat-item">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </div>
      </section>

      <section class="content-section">
        <div class="content-block">
          <h2 class="block-title">我们的使命</h2>
          <p class="block-text">
            LeafVision 致力于为企业提供精准、高效、可靠的基础设施可观测性解决方案。
            我们相信，在数字化转型的浪潮中，基础设施的稳定性是企业成功的基石。
            通过先进的监控技术和智能分析能力，我们帮助企业实时洞察系统状态，
            快速定位问题，保障业务连续性。
          </p>
        </div>

        <div class="content-block">
          <h2 class="block-title">核心优势</h2>
          <div class="feature-grid">
            <div class="feature-item">
              <span class="feature-number">01</span>
              <h3>毫秒级监控</h3>
              <p>实时数据采集，全栈基础设施状态尽在掌控</p>
            </div>
            <div class="feature-item">
              <span class="feature-number">02</span>
              <h3>智能分析</h3>
              <p>多维指标检索与智能关联，洞察系统运行本质</p>
            </div>
            <div class="feature-item">
              <span class="feature-number">03</span>
              <h3>精准告警</h3>
              <p>规则引擎与多渠道触达，故障无处遁形</p>
            </div>
            <div class="feature-item">
              <span class="feature-number">04</span>
              <h3>链路追踪</h3>
              <p>端到端分布式追踪，秒级定位性能瓶颈</p>
            </div>
          </div>
        </div>

        <div class="content-block">
          <h2 class="block-title">团队与服务</h2>
          <div class="team-grid">
            <div v-for="item in team" :key="item.name" class="team-item">
              <h3>{{ item.name }}</h3>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </div>

        <div class="content-block">
          <h2 class="block-title">联系我们</h2>
          <div class="contact-info">
            <div class="contact-item">
              <span class="contact-label">电子邮件</span>
              <span class="contact-value">YangSZ03@foxmail.com</span>
            </div>
            <div class="contact-item">
              <span class="contact-label">所属机构</span>
              <span class="contact-value">江西科技师范大学</span>
            </div>
          </div>
        </div>
      </section>

      <section class="cta-section">
        <h2>即刻启程</h2>
        <p>注册即可免费体验，让基础设施可观测性不再妥协</p>
        <div class="cta-buttons">
          <router-link to="/register" class="btn-primary">免费试用</router-link>
          <router-link to="/login" class="btn-secondary">登录控制台</router-link>
        </div>
      </section>
    </main>

    <footer class="page-footer">
      <div class="footer-content">
        <span class="copyright">© {{ new Date().getFullYear() }} LeafVision. All rights reserved.</span>
        <div class="footer-links">
          <router-link to="/docs">文档</router-link>
          <router-link to="/privacy">隐私</router-link>
          <router-link to="/terms">条款</router-link>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.about-page {
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
  padding-top: 80px;
}

.hero-section {
  background: var(--color-surface-dark);
  padding: 80px 40px;
  text-align: center;
}

.section-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  color: var(--site-context-highlight-color);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  margin-bottom: 24px;
}

.hero-title {
  font-size: 48px;
  font-weight: 300;
  color: #ffffff;
  letter-spacing: -0.02em;
  line-height: 1.15;
  margin: 0 0 24px;
}

.hero-desc {
  font-size: 16px;
  font-weight: 400;
  color: #bbbbbb;
  line-height: 1.5;
  max-width: 600px;
  margin: 0 auto;
}

.stats-section {
  background: #ffffff;
  padding: 60px 40px;
  border-bottom: 1px solid var(--color-border);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 36px;
  font-weight: 300;
  color: var(--site-context-highlight-color);
  line-height: 1.15;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.content-section {
  background: #ffffff;
  padding: 80px 40px;
}

.content-block {
  max-width: 1000px;
  margin: 0 auto 60px;
}

.content-block:last-child {
  margin-bottom: 0;
}

.block-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.02em;
  margin: 0 0 24px;
  text-transform: uppercase;
}

.block-text {
  font-size: 16px;
  font-weight: 400;
  color: var(--color-text-secondary);
  line-height: 1.8;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 32px;
}

.feature-item {
  padding: 24px;
  border: 1px solid var(--color-border);
}

.feature-number {
  display: block;
  font-size: 32px;
  font-weight: 300;
  color: var(--site-context-highlight-color);
  line-height: 1.15;
  margin-bottom: 16px;
}

.feature-item h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 8px;
  letter-spacing: 0.02em;
}

.feature-item p {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin: 0;
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.team-item {
  padding: 24px;
  background: #fafafa;
  border: 1px solid var(--color-border);
}

.team-item h3 {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 12px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.team-item p {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin: 0;
}

.contact-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.contact-item {
  padding: 24px;
  background: #fafafa;
  border: 1px solid var(--color-border);
}

.contact-label {
  display: block;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-secondary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.contact-value {
  display: block;
  font-size: 16px;
  font-weight: 400;
  color: var(--color-text-primary);
}

.cta-section {
  background: var(--color-surface-dark);
  padding: 80px 40px;
  text-align: center;
}

.cta-section h2 {
  font-size: 32px;
  font-weight: 300;
  color: #ffffff;
  margin: 0 0 16px;
  letter-spacing: -0.02em;
}

.cta-section p {
  font-size: 16px;
  font-weight: 400;
  color: #bbbbbb;
  margin: 0 0 32px;
}

.cta-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.btn-primary {
  display: inline-block;
  background: var(--site-context-highlight-color);
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  padding: 14px 32px;
  text-decoration: none;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  transition: background 0.15s ease;
}

.btn-primary:hover {
  background: var(--site-context-focus-color);
}

.btn-secondary {
  display: inline-block;
  background: transparent;
  border: 1px solid #ffffff;
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  padding: 14px 32px;
  text-decoration: none;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  transition: all 0.15s ease;
}

.btn-secondary:hover {
  background: #ffffff;
  color: var(--color-surface-dark);
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
  .header-content {
    padding: 16px 20px;
  }
  .hero-section {
    padding: 60px 20px;
  }
  .hero-title {
    font-size: 32px;
  }
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }
  .content-section {
    padding: 60px 20px;
  }
  .feature-grid,
  .team-grid {
    grid-template-columns: 1fr;
  }
  .cta-section {
    padding: 60px 20px;
  }
  .cta-buttons {
    flex-direction: column;
    align-items: center;
  }
  .footer-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}
</style>
