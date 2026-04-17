<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const form = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  agree: false
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const formRef = ref(null)

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    if (!form.agree) {
      ElMessage.warning('请先同意服务条款')
      return
    }

    loading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 800))
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (error) {
      ElMessage.error('注册失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

const handleBackHome = () => router.push('/')
</script>

<template>
  <div class="register-page">
    <div class="register-left">
      <div class="left-overlay"></div>
      <div class="brand" @click="handleBackHome">
        <span>LEAFVISION</span>
      </div>
      <div class="left-content">
        <span class="left-tag">GET STARTED</span>
        <h2>开启<br/>可观测之旅</h2>
        <p>注册即可免费体验企业级基础设施监控</p>
        <div class="left-features">
          <div class="left-feature">
            <span class="feature-line"></span>
            <span>实时监控与告警</span>
          </div>
          <div class="left-feature">
            <span class="feature-line"></span>
            <span>多维度指标分析</span>
          </div>
          <div class="left-feature">
            <span class="feature-line"></span>
            <span>分布式链路追踪</span>
          </div>
        </div>
      </div>
    </div>

    <div class="register-right">
      <div class="register-form-wrapper">
        <div class="form-header">
          <h1>创建账户</h1>
          <p>填写以下信息完成注册</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="register-form"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="email">
            <div class="field-label">邮箱</div>
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱地址"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <div class="field-label">密码</div>
            <el-input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码（6-20个字符）"
            >
              <template #suffix>
                <span
                  class="password-toggle"
                  @click="showPassword = !showPassword"
                >
                  {{ showPassword ? '隐藏' : '显示' }}
                </span>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <div class="field-label">确认密码</div>
            <el-input
              v-model="form.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="请再次输入密码"
              @keyup.enter="handleRegister"
            >
              <template #suffix>
                <span
                  class="password-toggle"
                  @click="showConfirmPassword = !showConfirmPassword"
                >
                  {{ showConfirmPassword ? '隐藏' : '显示' }}
                </span>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-agree">
            <el-checkbox v-model="form.agree">
              我已阅读并同意 <router-link to="/terms" class="terms-link">服务条款</router-link> 和 <router-link to="/privacy" class="terms-link">隐私政策</router-link>
            </el-checkbox>
          </div>

          <el-form-item>
            <button
              type="button"
              class="btn-submit"
              :disabled="loading"
              @click="handleRegister"
            >
              {{ loading ? '注册中...' : '注 册' }}
            </button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>已有账户？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  display: flex;
  min-height: 100vh;
}

.register-left {
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
  margin: 0 0 48px;
}

.left-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center;
}

.left-feature {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.05em;
}

.feature-line {
  width: 24px;
  height: 1px;
  background: var(--site-context-highlight-color);
}

.register-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--site-context-theme-color);
  padding: 40px;
  overflow-y: auto;
}

.register-form-wrapper {
  width: 100%;
  max-width: 360px;
}

.form-header {
  margin-bottom: 32px;
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

.register-form {
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

.register-form :deep(.el-input__wrapper) {
  border-radius: 0 !important;
  border: 1px solid var(--color-border) !important;
  box-shadow: none !important;
  background: var(--site-context-theme-color) !important;
  padding: 8px 12px !important;
}

.register-form :deep(.el-input__wrapper:hover) {
  border-color: var(--color-text-secondary) !important;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--site-context-highlight-color) !important;
}

.register-form :deep(.el-input__inner) {
  font-family: var(--font-family) !important;
  font-size: 14px !important;
  font-weight: 400 !important;
  color: var(--color-text-primary) !important;
}

.register-form :deep(.el-input__inner::placeholder) {
  color: var(--color-text-muted) !important;
}

.password-toggle {
  cursor: pointer;
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 400;
  letter-spacing: 0.05em;
  user-select: none;
}

.password-toggle:hover {
  color: var(--site-context-highlight-color);
}

.form-agree {
  margin-bottom: 24px;
}

.form-agree :deep(.el-checkbox__label) {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.30;
}

.terms-link {
  color: var(--site-context-highlight-color);
  transition: color 0.2s;
}

.terms-link:hover {
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
  line-height: 1.15;
}

.login-link {
  color: var(--site-context-highlight-color);
  font-weight: 700;
  margin-left: 4px;
  letter-spacing: 0.02em;
  transition: color 0.2s;
}

.login-link:hover {
  color: var(--site-context-focus-color);
}

@media (max-width: 900px) {
  .register-left {
    display: none;
  }

  .register-right {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .register-right {
    padding: 24px;
  }
  .form-header h1 {
    font-size: 24px;
  }
}
</style>
