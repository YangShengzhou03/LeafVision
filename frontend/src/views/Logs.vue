<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Search, Download, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getLogs, getServerList } from '@/api'
import { downloadFile } from '@/utils'

const { t } = useI18n()

const loading = ref(false)
const logList = ref([])
const queryForm = ref({
  serverId: null,
  level: 'all',
  keyword: '',
  startTime: '',
  endTime: '',
  limit: 100
})

const serverOptions = ref([
  { label: t('全部服务器'), value: null }
])

const levelOptions = [
  { label: t('全部级别'), value: 'all' },
  { label: 'DEBUG', value: 'debug' },
  { label: 'INFO', value: 'info' },
  { label: 'WARN', value: 'warn' },
  { label: 'ERROR', value: 'error' }
]

const initLabels = () => {
  serverOptions.value = [
    { label: t('全部服务器'), value: null }
  ]
  levelOptions[0].label = t('全部级别')
}

const fetchServerOptions = async () => {
  try {
    const res = await getServerList()
    if (res.code === 200 && res.data) {
      serverOptions.value = [
        { label: t('全部服务器'), value: null },
        ...res.data.map(s => ({ label: s.name, value: s.id }))
      ]
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getLogs(queryForm.value)
    if (res.code === 200) {
      logList.value = res.data || []
      ElMessage.success(t('查询成功'))
    }
  } catch (error) {
    ElMessage.error(t('查询失败'))
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  if (!logList.value.length) {
    ElMessage.warning(t('暂无数据可导出'))
    return
  }
  downloadFile(logList.value, `logs_${Date.now()}.json`)
  ElMessage.success(t('导出成功'))
}

const getLevelClass = (level) => {
  if (level === 'error') return 'error'
  if (level === 'warn') return 'warn'
  if (level === 'info') return 'info'
  if (level === 'debug') return 'debug'
  return ''
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

onMounted(async () => {
  initLabels()
  await fetchServerOptions()
  handleSearch()
})
</script>

<template>
  <div class="logs-page">
    <div class="query-card">
      <div class="card-header">
        <span class="card-title">{{ t('查询条件') }}</span>
      </div>
      <div class="card-body">
        <div class="query-form">
          <div class="form-group">
            <label class="form-label">{{ t('服务器') }}</label>
            <select v-model="queryForm.serverId" class="form-select">
              <option v-for="item in serverOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('日志级别') }}</label>
            <select v-model="queryForm.level" class="form-select">
              <option v-for="item in levelOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
          <div class="form-group flex-1">
            <label class="form-label">{{ t('关键字') }}</label>
            <input
              v-model="queryForm.keyword"
              type="text"
              :placeholder="t('请输入关键字')"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('条数限制') }}</label>
            <select v-model="queryForm.limit" class="form-select">
              <option :value="100">100</option>
              <option :value="500">500</option>
              <option :value="1000">1000</option>
            </select>
          </div>
          <div class="form-actions">
            <button class="btn-primary" @click="handleSearch" :disabled="loading">
              <el-icon><Search /></el-icon>
              <span>{{ t('搜索') }}</span>
            </button>
            <button class="btn-secondary" @click="handleExport" :disabled="!logList.length">
              <el-icon><Download /></el-icon>
              <span>{{ t('导出') }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="result-card">
      <div class="card-header">
        <span class="card-title">{{ t('日志列表') }}</span>
        <span class="log-count">{{ t('共') }} {{ logList.length }} {{ t('条') }}</span>
      </div>
      <div class="card-body">
        <div class="log-list">
          <div v-for="(log, index) in logList" :key="index" class="log-item">
            <span class="log-time">{{ formatDateTime(log.time) }}</span>
            <span :class="['log-level', getLevelClass(log.level)]">{{ log.level.toUpperCase() }}</span>
            <span class="log-source">{{ log.source }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
          <div v-if="!logList.length && !loading" class="empty-state">
            <span class="empty-text">{{ t('暂无日志数据') }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.logs-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.query-card,
.result-card {
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}

.card-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.log-count {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

.card-body {
  padding: 20px;
}

.query-form {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 140px;
}

.form-group.flex-1 {
  flex: 1;
  min-width: 200px;
}

.form-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.form-select,
.form-input {
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  font-size: 13px;
  font-weight: 400;
  color: var(--color-text-primary);
  background: #ffffff;
  outline: none;
  transition: border-color 0.15s ease;
}

.form-select:focus,
.form-input:focus {
  border-color: var(--site-context-highlight-color);
}

.form-input {
  width: 100%;
}

.form-actions {
  display: flex;
  gap: 12px;
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
  padding: 9px 18px;
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

.btn-secondary {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 700;
  padding: 9px 18px;
  cursor: pointer;
  transition: all 0.15s ease;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.btn-secondary:hover {
  border-color: var(--color-text-primary);
  color: var(--color-text-primary);
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.log-list {
  display: flex;
  flex-direction: column;
  max-height: 500px;
  overflow-y: auto;
  font-family: monospace;
}

.log-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
  font-size: 12px;
  line-height: 1.5;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: var(--color-text-secondary);
  white-space: nowrap;
  min-width: 160px;
}

.log-level {
  font-weight: 700;
  min-width: 50px;
  text-align: center;
  padding: 2px 6px;
}

.log-level.error {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.log-level.warn {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.log-level.info {
  background: rgba(28, 105, 212, 0.1);
  color: var(--site-context-highlight-color);
}

.log-level.debug {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.log-source {
  color: var(--site-context-highlight-color);
  min-width: 100px;
}

.log-message {
  flex: 1;
  color: var(--color-text-primary);
  word-break: break-all;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-text {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .query-form {
    flex-direction: column;
    align-items: stretch;
  }
  .form-group {
    min-width: 100%;
  }
  .form-actions {
    width: 100%;
  }
  .form-actions button {
    flex: 1;
  }
}
</style>
