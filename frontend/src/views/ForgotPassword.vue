<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { forgotPassword } from '@/api'

const router = useRouter()
const loading = ref(false)
const step = ref(1)

const form = reactive({
  email: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

const formRef = ref(null)

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await forgotPassword({ email: form.email })
      step.value = 2
      ElMessage.success('重置链接已发送到您的邮箱')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '发送失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

const handleBackHome = () => router.push('/')
</script>

<template>
  <div class="forgot-page">
    <div class="forgot-left">
      <div class="left-overlay"></div>
      <div class="brand" @click="handleBackHome">
        <span>LEAFVISION</span>
      </div>
      <div class="left-content">
        <span class="left-tag">ACCOUNT RECOVERY</span>
        <h2>重置<br/>密码</h2>
        <p>通过邮箱验证恢复您的账户访问</p>
      </div>
    </div>

    <div class="forgot-right">
      <div class="forgot-form-wrapper">
        <template v-if="step === 1">
          <div class="form-header">
            <h1>忘记密码</h1>
            <p>请输入您的注册邮箱，我们将发送重置链接</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="forgot-form"
            @submit.prevent="handleSubmit"
          >
            <el-form-item prop="email">
              <div class="field-label">邮箱地址</div>
              <el-input
                v-model="form.email"
                placeholder="请输入注册时使用的邮箱"
                clearable
                @keyup.enter="handleSubmit"
              />
            </el-form-item>

            <el-form-item>
              <button
                type="button"
                class="btn-submit"
                :disabled="loading"
                @click="handleSubmit"
              >
                {{ loading ? '发送中...' : '发送重置链接' }}
              </button>
            </el-form-item>
          </el-form>
        </template>

        <template v-else>
          <div class="success-state">
            <div class="success-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <rect width="48" height="48" fill="none"/>
                <path d="M14 24L22 32L34 16" stroke="#1c69d4" stroke-width="2"/>
              </svg>
            </div>
            <h2>邮件已发送</h2>
            <p>重置链接已发送至 <strong>{{ form.email }}</strong>，请查收邮件并按照提示重置密码。</p>
            <button
              type="button"
              class="btn-submit"
              @click="step = 1"
            >
              重新发送
            </button>
          </div>
        </template>

        <div class="form-footer">
          <router-link to="/login" class="back-link">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.forgot-page {
  display: flex;
  min-height: 100vh;
}

.forgot-left {
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
  margin: 0;
}

.forgot-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--site-context-theme-color);
  padding: 40px;
}

.forgot-form-wrapper {
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
  letter-spacing: 0.02em;
  margin: 0 0 8px;
}

.form-header p {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
  line-height: 1.15;
  margin: 0;
}

.forgot-form {
  margin-bottom: 24px;
}

.field-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.forgot-form :deep(.el-input__wrapper) {
  border-radius: 0 !important;
  border: 1px solid var(--color-border) !important;
  box-shadow: none !important;
  background: var(--site-context-theme-color) !important;
  padding: 8px 12px !important;
}

.forgot-form :deep(.el-input__wrapper:hover) {
  border-color: var(--color-text-secondary) !important;
}

.forgot-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--site-context-highlight-color) !important;
}

.forgot-form :deep(.el-input__inner) {
  font-family: var(--font-family) !important;
  font-size: 14px !important;
  font-weight: 400 !important;
  color: var(--color-text-primary) !important;
}

.forgot-form :deep(.el-input__inner::placeholder) {
  color: var(--color-text-muted) !important;
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

.success-state {
  text-align: center;
  margin-bottom: 32px;
}

.success-icon {
  margin-bottom: 24px;
}

.success-state h2 {
  font-size: 32px;
  font-weight: 300;
  color: var(--color-text-primary);
  line-height: 1.30;
  text-transform: uppercase;
  letter-spacing: 0.02em;
  margin: 0 0 16px;
}

.success-state p {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
  line-height: 1.15;
  margin: 0 0 32px;
}

.success-state strong {
  color: var(--color-text-primary);
  font-weight: 700;
}

.form-footer {
  text-align: center;
}

.back-link {
  color: var(--site-context-highlight-color);
  font-size: 14px;
  font-weight: 400;
  letter-spacing: 0.02em;
  transition: color 0.2s;
}

.back-link:hover {
  color: var(--site-context-focus-color);
}

@media (max-width: 900px) {
  .forgot-left {
    display: none;
  }

  .forgot-right {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .forgot-right {
    padding: 24px;
  }
  .form-header h1 {
    font-size: 24px;
  }
}
</style>
