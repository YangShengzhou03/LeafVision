<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const handleBackHome = () => router.push('/')

const lastUpdated = '2024年1月1日'
</script>

<template>
  <div class="terms-page">
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
          <h1>服务条款</h1>
          <p class="update-date">最后更新：{{ lastUpdated }}</p>
        </header>

        <section class="article-section">
          <h2>1. 服务协议</h2>
          <p>
            欢迎使用 LeafVision 服务。本服务条款（以下简称"条款"）构成您与 LeafVision
            之间具有法律约束力的协议。使用我们的服务即表示您同意遵守本条款。
          </p>
          <p>
            如果您代表公司或其他法律实体使用我们的服务，您声明您有权代表该实体接受本条款。
          </p>
        </section>

        <section class="article-section">
          <h2>2. 服务描述</h2>
          <p>
            LeafVision 提供基础设施可观测性平台服务，包括但不限于服务器监控、容器监控、
            日志分析、链路追踪、告警管理等功能。我们保留随时修改、暂停或终止服务的权利。
          </p>
        </section>

        <section class="article-section">
          <h2>3. 账户注册与使用</h2>
          <p>使用我们的服务需要注册账户。您同意：</p>
          <ul>
            <li>提供真实、准确、完整的注册信息</li>
            <li>及时更新您的账户信息</li>
            <li>妥善保管账户凭据，对账户下的所有活动负责</li>
            <li>如发现未经授权使用，立即通知我们</li>
            <li>不与他人共享账户</li>
          </ul>
        </section>

        <section class="article-section">
          <h2>4. 使用限制</h2>
          <p>您同意不会：</p>
          <ul>
            <li>以违反任何法律法规的方式使用服务</li>
            <li>侵犯他人的知识产权或其他权利</li>
            <li>传播病毒、恶意代码或其他有害内容</li>
            <li>尝试未经授权访问我们的系统或数据</li>
            <li>干扰或破坏服务的正常运行</li>
            <li>将服务用于任何非法或未经授权的目的</li>
            <li>转售或再分发我们的服务</li>
          </ul>
        </section>

        <section class="article-section">
          <h2>5. 知识产权</h2>
          <p>
            LeafVision 服务中的所有内容，包括软件、文本、图像、商标等，均受知识产权法保护。
            我们保留所有未明确授予的权利。
          </p>
          <p>
            您通过服务上传或创建的内容，您保留其所有权。授予我们使用、存储和处理该内容
            以提供服务的许可。
          </p>
        </section>

        <section class="article-section">
          <h2>6. 服务级别协议</h2>
          <p>
            我们致力于提供高可用的服务。对于付费用户，我们承诺月度服务可用性不低于 99.9%。
            如未达到该标准，您有权根据我们的服务级别协议获得相应补偿。
          </p>
        </section>

        <section class="article-section">
          <h2>7. 付费与退款</h2>
          <p>
            付费服务的费用将在您购买时明确说明。费用一经支付，除法律另有规定或我们另有承诺外，
            不予退还。
          </p>
          <p>
            我们保留调整服务价格的权利。价格调整将提前通知您，您可以选择接受新价格或终止服务。
          </p>
        </section>

        <section class="article-section">
          <h2>8. 数据保留与删除</h2>
          <p>
            您的监控数据将根据您选择的保留策略进行存储。账户终止后，我们将在合理期限内
            删除您的个人数据，法律要求保留的除外。
          </p>
        </section>

        <section class="article-section">
          <h2>9. 免责声明</h2>
          <p>
            我们的服务按"现状"提供，不提供任何明示或暗示的保证。在法律允许的最大范围内，
            我们不对以下情况承担责任：
          </p>
          <ul>
            <li>服务中断、数据丢失或服务不可用</li>
            <li>第三方行为或不可抗力事件</li>
            <li>间接、附带、特殊或后果性损害</li>
          </ul>
        </section>

        <section class="article-section">
          <h2>10. 责任限制</h2>
          <p>
            在法律允许的最大范围内，我们对任何索赔的总责任不超过您在过去十二个月内
            支付的服务费用。
          </p>
        </section>

        <section class="article-section">
          <h2>11. 赔偿</h2>
          <p>
            您同意赔偿并使我们免受因您违反本条款、侵犯第三方权利或违反任何法律而引起的
            任何索赔、损失、费用或损害。
          </p>
        </section>

        <section class="article-section">
          <h2>12. 终止</h2>
          <p>
            我们有权因任何原因随时终止或暂停您的账户，包括但不限于违反本条款。
            您也可以随时终止账户。终止后，本条款中应当存续的条款将继续有效。
          </p>
        </section>

        <section class="article-section">
          <h2>13. 条款修改</h2>
          <p>
            我们保留随时修改本条款的权利。修改后的条款将在本页面发布。重大修改将提前通知您。
            继续使用服务即表示您接受修改后的条款。
          </p>
        </section>

        <section class="article-section">
          <h2>14. 适用法律与争议解决</h2>
          <p>
            本条款受中华人民共和国法律管辖。因本条款引起的任何争议应首先通过友好协商解决。
            协商不成的，任何一方可向我们所在地有管辖权的人民法院提起诉讼。
          </p>
        </section>

        <section class="article-section">
          <h2>15. 其他条款</h2>
          <p>
            如果本条款的任何条款被认定为无效或不可执行，该条款应在最小必要范围内修改，
            其他条款继续有效。
          </p>
          <p>
            我们未能行使本条款下的任何权利不构成对该权利的放弃。
          </p>
        </section>

        <section class="article-section">
          <h2>16. 联系我们</h2>
          <p>如果您对本服务条款有任何疑问，请通过以下方式联系我们：</p>
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
          <router-link to="/privacy">隐私</router-link>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.terms-page {
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
