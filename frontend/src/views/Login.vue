<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const showPassword = ref(false)

const form = ref({
  username: 'admin',
  password: '123456',
  remember: false
})

const rules = computed(() => ({
  username: [
    { required: true, message: t('请输入用户名'), trigger: 'blur' },
    { min: 3, max: 20, message: t('用户名长度3-20个字符'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('请输入密码'), trigger: 'blur' },
    { min: 6, max: 20, message: t('密码长度6-20个字符'), trigger: 'blur' }
  ]
}))

const formRef = ref(null)

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const result = await userStore.login(form.value.username, form.value.password, form.value.remember)

      if (!result.success) {
        ElMessage.error(result.message)
        return
      }

      ElMessage.success(t('登录成功') + ', ' + t('欢迎回来') + ' ' + result.user.name)

      const redirect = route.query.redirect
      router.push(redirect || '/monitor/dashboard')
    } catch (error) {
      console.error('Login error:', error)
    } finally {
      loading.value = false
    }
  })
}

const handleBackHome = () => router.push('/')
</script>

<template>
  <div class="login-page">
    <div class="login-left">
      <div class="left-overlay"></div>
      <div class="brand" @click="handleBackHome">
        <span>LEAFVISION</span>
      </div>
      <div class="left-content">
        <span class="left-tag">{{ t('基础设施可观测性') }}</span>
        <h2>{{ t('精准洞察') }}<br/>{{ t('全面感知') }}</h2>
        <p>{{ t('以工程级精度重新定义基础设施可观测性') }}</p>
        <div class="left-divider"></div>
        <div class="left-stats">
          <div class="left-stat">
            <span class="left-stat-value">99.9%</span>
            <span class="left-stat-label">{{ t('SLA可用性') }}</span>
          </div>
          <div class="left-stat">
            <span class="left-stat-value">&lt;10ms</span>
            <span class="left-stat-label">{{ t('采集延迟') }}</span>
          </div>
          <div class="left-stat">
            <span class="left-stat-value">24/7</span>
            <span class="left-stat-label">{{ t('全天候守护') }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-form-wrapper">
        <div class="form-header">
          <h1>{{ t('登录') }}</h1>
          <p>{{ t('登录您的账户以继续') }}</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <div class="field-label">{{ t('用户名') }}</div>
            <el-input
              v-model="form.username"
              :placeholder="t('请输入用户名')"
              clearable
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item prop="password">
            <div class="field-label">{{ t('密码') }}</div>
            <el-input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              :placeholder="t('请输入密码')"
              @keyup.enter="handleLogin"
            >
              <template #suffix>
                <span
                  class="password-toggle"
                  @click="showPassword = !showPassword"
                >
                  {{ showPassword ? t('隐藏密码') : t('显示密码') }}
                </span>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="form.remember">{{ t('记住我') }}</el-checkbox>
            <router-link to="/forgot-password" class="forgot-link">{{ t('忘记密码') }}</router-link>
          </div>

          <el-form-item>
            <button
              type="button"
              class="btn-submit"
              :disabled="loading"
              @click="handleLogin"
            >
              {{ loading ? t('登录中...') : t('登录') }}
            </button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>{{ t('没有账户？') }}</span>
          <router-link to="/register" class="register-link">{{ t('立即注册') }}</router-link>
        </div>

        <div class="demo-hint">
          <span class="demo-text">LEAFVISION: admin / operator / viewer, {{ t('密码') }}: 123456</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
}

.login-left {
  flex: 1;
  background: var(--color-surface-dark);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.left-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 60% 50% at 50% 50%, rgba(28, 105, 212, 0.06) 0%, transparent 70%);
  pointer-events: none;
}

.brand {
  position: absolute;
  top: 32px;
  left: 32px;
  cursor: pointer;
  z-index: 1;
}

.brand span {
  font-size: 18px;
  font-weight: 900;
  color: #ffffff;
  letter-spacing: 0.15em;
  line-height: 1.30;
}

.left-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.left-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  color: var(--site-context-highlight-color);
  letter-spacing: 0.2em;
  margin-bottom: 24px;
  padding: 6px 0;
  border-bottom: 1px solid var(--site-context-highlight-color);
}

.left-content h2 {
  font-size: 48px;
  font-weight: 300;
  color: #ffffff;
  line-height: 1.15;
  text-transform: uppercase;
  letter-spacing: 0.02em;
  margin: 0 0 16px;
}

.left-content p {
  font-size: 16px;
  font-weight: 400;
  color: var(--color-text-muted);
  line-height: 1.15;
  margin: 0 0 40px;
}

.left-divider {
  width: 40px;
  height: 1px;
  background: rgba(255, 255, 255, 0.15);
  margin: 0 auto 40px;
}

.left-stats {
  display: flex;
  gap: 48px;
  justify-content: center;
}

.left-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.left-stat-value {
  font-size: 28px;
  font-weight: 300;
  color: #ffffff;
  line-height: 1.15;
  letter-spacing: -0.02em;
}

.left-stat-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-muted);
  margin-top: 8px;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}

.login-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--site-context-theme-color);
  padding: 40px;
}

.login-form-wrapper {
  width: 100%;
  max-width: 360px;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h1 {
  font-size: 32px;
  font-weight: 300;
  color: var(--color-text-primary);
  line-height: 1.30;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0 0 8px;
}

.form-header p {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
  line-height: 1.15;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.field-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 0 !important;
  border: 1px solid var(--color-border) !important;
  box-shadow: none !important;
  background: var(--site-context-theme-color) !important;
  padding: 10px 12px !important;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: var(--color-text-secondary) !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--site-context-highlight-color) !important;
}

.login-form :deep(.el-input__inner) {
  font-family: var(--font-family) !important;
  font-size: 14px !important;
  font-weight: 400 !important;
  color: var(--color-text-primary) !important;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: var(--color-text-muted) !important;
}

.password-toggle {
  cursor: pointer;
  color: var(--color-text-secondary);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  user-select: none;
}

.password-toggle:hover {
  color: var(--site-context-highlight-color);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.form-options :deep(.el-checkbox__label) {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.forgot-link {
  color: var(--site-context-highlight-color);
  font-size: 13px;
  font-weight: 400;
  letter-spacing: 0.02em;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: var(--site-context-focus-color);
}

.btn-submit {
  width: 100%;
  height: 48px;
  background: var(--site-context-highlight-color);
  border: none;
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.1em;
  cursor: pointer;
  transition: background 0.2s;
  line-height: 1.20;
}

.btn-submit:hover {
  background: var(--site-context-focus-color);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-footer {
  text-align: center;
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
  margin-bottom: 24px;
  line-height: 1.15;
}

.register-link {
  color: var(--site-context-highlight-color);
  font-weight: 700;
  margin-left: 4px;
  letter-spacing: 0.02em;
  transition: color 0.2s;
}

.register-link:hover {
  color: var(--site-context-focus-color);
}

.demo-hint {
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  background: #fafafa;
}

.demo-text {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
  line-height: 1.15;
  letter-spacing: 0.02em;
}

@media (max-width: 900px) {
  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .login-right {
    padding: 24px;
  }
  .form-header h1 {
    font-size: 24px;
  }
}
</style>
