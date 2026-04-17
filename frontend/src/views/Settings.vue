<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSettings, updateSettings } from '@/api'

const loading = ref(false)
const saving = ref(false)
const activeTab = ref('general')
const settings = ref({
  general: {
    siteName: 'LeafVision',
    siteDescription: '基础设施可观测性平台',
    language: 'zh-CN',
    timezone: 'Asia/Shanghai'
  },
  alert: {
    emailEnabled: true,
    emailSmtp: '',
    emailPort: 587,
    emailUser: '',
    emailPassword: '',
    webhookEnabled: false,
    webhookUrl: ''
  },
  security: {
    sessionTimeout: 30,
    maxLoginAttempts: 5,
    passwordMinLength: 8,
    passwordRequireUppercase: true,
    passwordRequireNumber: true,
    passwordRequireSpecial: false
  },
  data: {
    retentionDays: 30,
    metricsInterval: 15,
    logLevel: 'info'
  }
})

const fetchSettings = async () => {
  loading.value = true
  try {
    const res = await getSettings()
    if (res.code === 200 && res.data) {
      settings.value = { ...settings.value, ...res.data }
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const res = await updateSettings(settings.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => fetchSettings())
</script>

<template>
  <div class="settings-page">
    <div class="page-header">
      <span class="page-title">系统设置</span>
      <div class="header-actions">
        <button class="btn-primary" @click="handleSave" :disabled="saving">
          <span>{{ saving ? '保存中...' : '保存设置' }}</span>
        </button>
      </div>
    </div>

    <div class="settings-content">
      <div class="tabs-sidebar">
        <button
          :class="['tab-item', { active: activeTab === 'general' }]"
          @click="activeTab = 'general'"
        >
          基本设置
        </button>
        <button
          :class="['tab-item', { active: activeTab === 'alert' }]"
          @click="activeTab = 'alert'"
        >
          告警通知
        </button>
        <button
          :class="['tab-item', { active: activeTab === 'security' }]"
          @click="activeTab = 'security'"
        >
          安全设置
        </button>
        <button
          :class="['tab-item', { active: activeTab === 'data' }]"
          @click="activeTab = 'data'"
        >
          数据设置
        </button>
      </div>

      <div class="settings-panel">
        <div v-if="activeTab === 'general'" class="panel-content">
          <div class="panel-title">基本设置</div>
          <div class="form-group">
            <label class="form-label">站点名称</label>
            <input v-model="settings.general.siteName" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">站点描述</label>
            <input v-model="settings.general.siteDescription" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">语言</label>
            <select v-model="settings.general.language" class="form-select">
              <option value="zh-CN">简体中文</option>
              <option value="en-US">English</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">时区</label>
            <select v-model="settings.general.timezone" class="form-select">
              <option value="Asia/Shanghai">Asia/Shanghai (UTC+8)</option>
              <option value="UTC">UTC</option>
              <option value="America/New_York">America/New_York (UTC-5)</option>
            </select>
          </div>
        </div>

        <div v-if="activeTab === 'alert'" class="panel-content">
          <div class="panel-title">告警通知</div>
          <div class="form-section">
            <div class="section-title">邮件通知</div>
            <div class="form-group inline">
              <label class="form-label">启用邮件通知</label>
              <input v-model="settings.alert.emailEnabled" type="checkbox" class="form-checkbox" />
            </div>
            <div class="form-group">
              <label class="form-label">SMTP 服务器</label>
              <input v-model="settings.alert.emailSmtp" type="text" class="form-input" placeholder="smtp.example.com" />
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">端口</label>
                <input v-model.number="settings.alert.emailPort" type="number" class="form-input" />
              </div>
              <div class="form-group">
                <label class="form-label">用户名</label>
                <input v-model="settings.alert.emailUser" type="text" class="form-input" />
              </div>
              <div class="form-group">
                <label class="form-label">密码</label>
                <input v-model="settings.alert.emailPassword" type="password" class="form-input" />
              </div>
            </div>
          </div>
          <div class="form-section">
            <div class="section-title">Webhook 通知</div>
            <div class="form-group inline">
              <label class="form-label">启用 Webhook</label>
              <input v-model="settings.alert.webhookEnabled" type="checkbox" class="form-checkbox" />
            </div>
            <div class="form-group">
              <label class="form-label">Webhook URL</label>
              <input v-model="settings.alert.webhookUrl" type="text" class="form-input" placeholder="https://example.com/webhook" />
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'security'" class="panel-content">
          <div class="panel-title">安全设置</div>
          <div class="form-group">
            <label class="form-label">会话超时时间 (分钟)</label>
            <input v-model.number="settings.security.sessionTimeout" type="number" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">最大登录尝试次数</label>
            <input v-model.number="settings.security.maxLoginAttempts" type="number" class="form-input" />
          </div>
          <div class="form-section">
            <div class="section-title">密码策略</div>
            <div class="form-group">
              <label class="form-label">最小密码长度</label>
              <input v-model.number="settings.security.passwordMinLength" type="number" class="form-input" />
            </div>
            <div class="form-group inline">
              <label class="form-label">要求大写字母</label>
              <input v-model="settings.security.passwordRequireUppercase" type="checkbox" class="form-checkbox" />
            </div>
            <div class="form-group inline">
              <label class="form-label">要求数字</label>
              <input v-model="settings.security.passwordRequireNumber" type="checkbox" class="form-checkbox" />
            </div>
            <div class="form-group inline">
              <label class="form-label">要求特殊字符</label>
              <input v-model="settings.security.passwordRequireSpecial" type="checkbox" class="form-checkbox" />
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'data'" class="panel-content">
          <div class="panel-title">数据设置</div>
          <div class="form-group">
            <label class="form-label">数据保留天数</label>
            <input v-model.number="settings.data.retentionDays" type="number" class="form-input" />
            <span class="form-hint">超过此天数的历史数据将被自动清理</span>
          </div>
          <div class="form-group">
            <label class="form-label">指标采集间隔 (秒)</label>
            <input v-model.number="settings.data.metricsInterval" type="number" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">日志级别</label>
            <select v-model="settings.data.logLevel" class="form-select">
              <option value="debug">DEBUG</option>
              <option value="info">INFO</option>
              <option value="warn">WARN</option>
              <option value="error">ERROR</option>
            </select>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.page-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--site-context-highlight-color);
  border: none;
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  padding: 8px 16px;
  cursor: pointer;
  transition: background 0.15s ease;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.btn-primary:hover {
  background: var(--site-context-focus-color);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.settings-content {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 16px;
}

.tabs-sidebar {
  background: #ffffff;
  border: 1px solid var(--color-border);
  padding: 8px 0;
}

.tab-item {
  display: block;
  width: 100%;
  padding: 12px 20px;
  background: transparent;
  border: none;
  text-align: left;
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-secondary);
  cursor: pointer;
  letter-spacing: 0.02em;
  transition: all 0.15s ease;
}

.tab-item:hover {
  color: var(--color-text-primary);
  background: #fafafa;
}

.tab-item.active {
  color: var(--site-context-highlight-color);
  background: rgba(28, 105, 212, 0.05);
  border-left: 3px solid var(--site-context-highlight-color);
}

.settings-panel {
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.panel-content {
  padding: 24px;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
  letter-spacing: 0.02em;
}

.form-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border);
}

.form-section:first-of-type {
  margin-top: 0;
  padding-top: 0;
  border-top: none;
}

.section-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 16px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.form-group.inline {
  flex-direction: row;
  align-items: center;
  gap: 12px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.form-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.form-input,
.form-select {
  height: 40px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-primary);
  background: #ffffff;
  outline: none;
  transition: border-color 0.15s ease;
}

.form-input:focus,
.form-select:focus {
  border-color: var(--site-context-highlight-color);
}

.form-checkbox {
  width: 18px;
  height: 18px;
  accent-color: var(--site-context-highlight-color);
}

.form-hint {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .settings-content {
    grid-template-columns: 1fr;
  }
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
