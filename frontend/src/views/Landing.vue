<script setup>
import { useRouter } from 'vue-router'
import { onMounted, ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const mobileMenuOpen = ref(false)
const currentYear = computed(() => new Date().getFullYear())

const isLoggedIn = computed(() => userStore.isLoggedIn)

const features = [
  { title: '实时监控', desc: '毫秒级数据采集，全栈基础设施状态尽在掌控' },
  { title: '指标分析', desc: '多维指标检索与智能关联，洞察系统运行本质' },
  { title: '智能告警', desc: '精准规则引擎与多渠道触达，故障无处遁形' },
  { title: '链路追踪', desc: '端到端分布式追踪，秒级定位性能瓶颈' }
]

const stats = [
  { value: '99.9%', label: 'SLA 可用性' },
  { value: '<10ms', label: '采集延迟' },
  { value: '100+', label: '监控维度' },
  { value: '24/7', label: '持续守护' }
]

const handleLogin = () => router.push('/login')
const handleRegister = () => router.push('/register')
const handleGoToConsole = () => router.push('/monitor/dashboard')
const toggleMobileMenu = () => { mobileMenuOpen.value = !mobileMenuOpen.value }

onMounted(() => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible')
      }
    })
  }, { threshold: 0.1 })

  document.querySelectorAll('.animate-in').forEach(el => observer.observe(el))
})
</script>

<template>
  <div class="landing">
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="router.push('/')">
          <span class="brand">LEAFVISION</span>
        </div>
        <nav class="nav" :class="{ open: mobileMenuOpen }">
          <a href="#features" @click="mobileMenuOpen = false">功能</a>
          <a href="#stats" @click="mobileMenuOpen = false">数据</a>
          <a href="#contact" @click="mobileMenuOpen = false">联系</a>
        </nav>
        <div class="actions">
          <template v-if="isLoggedIn">
            <button class="btn-primary" @click="handleGoToConsole">进入控制台</button>
          </template>
          <template v-else>
            <button class="btn-ghost" @click="handleLogin">登录</button>
            <button class="btn-primary" @click="handleRegister">开始使用</button>
          </template>
        </div>
        <button class="mobile-toggle" @click="toggleMobileMenu">
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </header>

    <section class="hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <span class="hero-tag">INFRASTRUCTURE OBSERVABILITY</span>
        <h1>精准洞察<br/>全域感知</h1>
        <p class="subtitle">以工程精度重新定义基础设施可观测性——从指标、日志到链路，构建统一感知体系</p>
        <div class="hero-actions">
          <template v-if="isLoggedIn">
            <button class="btn-hero-primary" @click="handleGoToConsole">
              进入控制台
            </button>
          </template>
          <template v-else>
            <button class="btn-hero-primary" @click="handleRegister">
              立即开始
            </button>
            <button class="btn-hero-ghost" @click="handleLogin">
              登录控制台
            </button>
          </template>
        </div>
      </div>
    </section>

    <section id="stats" class="stats-section">
      <div class="stats-inner">
        <div v-for="stat in stats" :key="stat.label" class="stat-item animate-in">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </section>

    <section id="features" class="features">
      <div class="section-header animate-in">
        <span class="section-tag">CAPABILITIES</span>
        <h2>核心能力</h2>
        <p>为严肃的基础设施而构建</p>
      </div>
      <div class="features-grid">
        <div v-for="(feature, index) in features" :key="feature.title" class="feature-card animate-in" :style="{ transitionDelay: index * 0.1 + 's' }">
          <span class="feature-index">{{ String(index + 1).padStart(2, '0') }}</span>
          <h3>{{ feature.title }}</h3>
          <p>{{ feature.desc }}</p>
        </div>
      </div>
    </section>

    <section id="contact" class="contact">
      <div class="contact-content animate-in">
        <span class="contact-tag">GET STARTED</span>
        <h2>即刻启程</h2>
        <p>注册即可免费体验，让基础设施可观测性不再妥协</p>
        <button class="btn-hero-primary" @click="isLoggedIn ? handleGoToConsole() : handleRegister()">
          {{ isLoggedIn ? '进入控制台' : '免费试用' }}
        </button>
      </div>
    </section>

    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <span>LEAFVISION</span>
        </div>
        <div class="footer-copyright">© {{ currentYear }} LeafVision. All rights reserved.</div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.landing {
  min-height: 100vh;
  background: var(--site-context-theme-color);
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: var(--color-surface-dark);
  z-index: 100;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
}

.logo {
  cursor: pointer;
}

.brand {
  font-size: 18px;
  font-weight: 900;
  color: #ffffff;
  letter-spacing: 0.15em;
  line-height: 1.30;
}

.nav {
  display: flex;
  gap: 40px;
}

.nav a {
  color: #ffffff;
  font-size: 14px;
  font-weight: 400;
  letter-spacing: 0.05em;
  transition: color 0.2s;
}

.nav a:hover {
  color: var(--site-context-highlight-color);
}

.actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.btn-ghost {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #ffffff;
  font-size: 14px;
  font-weight: 400;
  padding: 8px 20px;
  cursor: pointer;
  transition: all 0.2s;
  letter-spacing: 0.05em;
}

.btn-ghost:hover {
  border-color: #ffffff;
}

.btn-primary {
  background: var(--site-context-highlight-color);
  border: none;
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  padding: 8px 20px;
  cursor: pointer;
  transition: background 0.2s;
  letter-spacing: 0.05em;
}

.btn-primary:hover {
  background: var(--site-context-focus-color);
}

.mobile-toggle {
  display: none;
  flex-direction: column;
  gap: 5px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
}

.mobile-toggle span {
  width: 24px;
  height: 2px;
  background: #ffffff;
  transition: all 0.3s;
}

.hero {
  position: relative;
  min-height: 100vh;
  background: var(--color-surface-dark);
  display: flex;
  align-items: center;
  padding: 100px 180px 80px;
  overflow: hidden;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 60% at 30% 50%, rgba(28, 105, 212, 0.08) 0%, transparent 60%),
    radial-gradient(ellipse 40% 80% at 80% 80%, rgba(28, 105, 212, 0.04) 0%, transparent 50%);
  pointer-events: none;
}

.hero-content {
  max-width: 720px;
  position: relative;
  z-index: 1;
}

.hero-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  color: var(--site-context-highlight-color);
  letter-spacing: 0.2em;
  margin-bottom: 24px;
  padding: 6px 0;
  border-bottom: 1px solid var(--site-context-highlight-color);
}

.hero h1 {
  font-size: 60px;
  font-weight: 300;
  color: #ffffff;
  line-height: 1.15;
  text-transform: uppercase;
  letter-spacing: 0.02em;
  margin: 0 0 24px;
}

.subtitle {
  font-size: 16px;
  font-weight: 400;
  color: var(--color-text-muted);
  line-height: 1.15;
  margin: 0 0 40px;
  max-width: 480px;
}

.hero-actions {
  display: flex;
  gap: 16px;
}

.btn-hero-primary {
  background: var(--site-context-highlight-color);
  border: none;
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  padding: 14px 32px;
  cursor: pointer;
  transition: background 0.2s;
  letter-spacing: 0.05em;
  line-height: 1.20;
}

.btn-hero-primary:hover {
  background: var(--site-context-focus-color);
}

.btn-hero-ghost {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #ffffff;
  font-size: 16px;
  font-weight: 400;
  padding: 14px 32px;
  cursor: pointer;
  transition: border-color 0.2s;
  letter-spacing: 0.05em;
  line-height: 1.20;
}

.btn-hero-ghost:hover {
  border-color: #ffffff;
}

.stats-section {
  background: var(--site-context-theme-color);
  padding: 64px 32px;
  border-bottom: 1px solid var(--color-border);
}

.stats-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  border-right: 1px solid var(--color-border);
}

.stat-item:last-child {
  border-right: none;
}

.stat-value {
  font-size: 36px;
  font-weight: 300;
  color: var(--color-text-primary);
  line-height: 1.15;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 13px;
  font-weight: 400;
  color: var(--color-text-secondary);
  margin-top: 8px;
  letter-spacing: 0.05em;
}

.features {
  padding: 96px 32px;
  background: var(--site-context-theme-color);
}

.section-header {
  text-align: center;
  margin-bottom: 64px;
}

.section-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  color: var(--site-context-highlight-color);
  letter-spacing: 0.2em;
  margin-bottom: 16px;
  padding: 4px 0;
  border-bottom: 1px solid var(--site-context-highlight-color);
}

.section-header h2 {
  font-size: 32px;
  font-weight: 300;
  color: var(--color-text-primary);
  line-height: 1.30;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0 0 16px;
}

.section-header p {
  font-size: 16px;
  font-weight: 400;
  color: var(--color-text-secondary);
  line-height: 1.15;
  margin: 0;
}

.features-grid {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0;
}

.feature-card {
  padding: 40px 32px;
  border-right: 1px solid var(--color-border);
  transition: background 0.3s;
}

.feature-card:last-child {
  border-right: none;
}

.feature-card:hover {
  background: #fafafa;
}

.feature-index {
  font-size: 48px;
  font-weight: 300;
  color: var(--site-context-highlight-color);
  line-height: 1.15;
  display: block;
  margin-bottom: 24px;
}

.feature-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.30;
  margin: 0 0 12px;
  letter-spacing: 0.02em;
}

.feature-card p {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
  line-height: 1.15;
  margin: 0;
}

.contact {
  padding: 96px 32px;
  background: var(--color-surface-dark);
  text-align: center;
}

.contact-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  color: var(--site-context-highlight-color);
  letter-spacing: 0.2em;
  margin-bottom: 16px;
  padding: 4px 0;
  border-bottom: 1px solid var(--site-context-highlight-color);
}

.contact-content h2 {
  font-size: 48px;
  font-weight: 300;
  color: #ffffff;
  line-height: 1.15;
  text-transform: uppercase;
  letter-spacing: 0.02em;
  margin: 0 0 16px;
}

.contact-content p {
  font-size: 16px;
  font-weight: 400;
  color: var(--color-text-muted);
  line-height: 1.15;
  margin: 0 0 40px;
}

.footer {
  background: var(--color-surface-dark);
  padding: 40px 32px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-brand span {
  font-size: 14px;
  font-weight: 900;
  color: #ffffff;
  letter-spacing: 0.15em;
}

.footer-links {
  display: flex;
  gap: 32px;
}

.footer-links a {
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 400;
  letter-spacing: 0.05em;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: #ffffff;
}

.footer-copyright {
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 400;
  letter-spacing: 0.02em;
}

.animate-in {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.animate-in.visible {
  opacity: 1;
  transform: translateY(0);
}

@media (max-width: 1024px) {
  .hero h1 { font-size: 48px; }
  .features-grid { grid-template-columns: repeat(2, 1fr); }
  .feature-card:nth-child(2) { border-right: none; }
  .feature-card:nth-child(1),
  .feature-card:nth-child(2) { border-bottom: 1px solid var(--color-border); }
  .footer-inner { flex-direction: column; gap: 20px; text-align: center; }
}

@media (max-width: 768px) {
  .nav { display: none; }
  .nav.open {
    display: flex;
    flex-direction: column;
    position: absolute;
    top: 64px;
    left: 0;
    right: 0;
    background: var(--color-surface-dark);
    padding: 24px 32px;
    gap: 20px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  }
  .actions { 
    display: flex; 
    gap: 8px;
  }
  .actions .btn-ghost { padding: 6px 12px; font-size: 12px; }
  .actions .btn-primary { padding: 6px 12px; font-size: 12px; }
  .mobile-toggle { display: flex; }
  .hero h1 { font-size: 36px; }
  .subtitle { font-size: 14px; }
  .stats-inner { grid-template-columns: repeat(2, 1fr); }
  .stat-item:nth-child(2) { border-right: none; }
  .stat-item:nth-child(1),
  .stat-item:nth-child(2) { border-bottom: 1px solid var(--color-border); }
  .features-grid { grid-template-columns: 1fr; }
  .feature-card { border-right: none; border-bottom: 1px solid var(--color-border); }
  .feature-card:last-child { border-bottom: none; }
  .section-header h2 { font-size: 24px; }
  .contact-content h2 { font-size: 32px; }
}

@media (max-width: 480px) {
  .hero { padding: 100px 16px 60px; }
  .hero h1 { font-size: 28px; }
  .hero-actions { flex-direction: column; }
  .btn-hero-primary, .btn-hero-ghost { width: 100%; text-align: center; }
}
</style>
