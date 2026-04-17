<script setup>
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getAlerts } from '@/api'
import { ALERT_STATUS_MAP, ALERT_SEVERITY_MAP } from '@/constants'

const loading = ref(false)
const activeTab = ref('alerts')
const alertList = ref([])
const alertRules = ref([])
const stats = ref({ total: 0, firing: 0, critical: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAlerts()
    if (res.code === 200) {
      alertList.value = res.data?.list || []
      alertRules.value = res.data?.rules || []
      stats.value = res.data?.stats || {
        total: alertList.value.length,
        firing: alertList.value.filter(a => a.status === 'firing').length,
        critical: alertList.value.filter(a => a.severity === 'critical').length
      }
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => ALERT_STATUS_MAP[status] || status

const getSeverityText = (severity) => ALERT_SEVERITY_MAP[severity] || severity

const getSeverityClass = (severity) => {
  if (severity === 'critical') return 'critical'
  if (severity === 'warning') return 'warning'
  return 'info'
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

onMounted(() => fetchData())
</script>

<template>
  <div class="alerts-page">
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">总告警</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value firing">{{ stats.firing }}</span>
          <span class="stat-label">告警中</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value critical">{{ stats.critical }}</span>
          <span class="stat-label">严重</span>
        </div>
      </div>
      <div class="stat-card action-card">
        <button class="btn-primary" @click="fetchData" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>同步告警</span>
        </button>
      </div>
    </div>

    <div class="table-card">
      <div class="tabs-header">
        <button
          :class="['tab-btn', { active: activeTab === 'alerts' }]"
          @click="activeTab = 'alerts'"
        >
          告警列表
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'rules' }]"
          @click="activeTab = 'rules'"
        >
          告警规则
        </button>
      </div>

      <div v-if="activeTab === 'alerts'" class="table-content">
        <table class="data-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>级别</th>
              <th>状态</th>
              <th>实例</th>
              <th>触发时间</th>
              <th>持续时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="alert in alertList" :key="alert.name">
              <td class="alert-name">{{ alert.name }}</td>
              <td>
                <span :class="['severity-badge', getSeverityClass(alert.severity)]">
                  {{ getSeverityText(alert.severity) }}
                </span>
              </td>
              <td>
                <span :class="['status-badge', alert.status === 'firing' ? 'firing' : 'resolved']">
                  {{ getStatusText(alert.status) }}
                </span>
              </td>
              <td>{{ alert.instance }}</td>
              <td>{{ formatDateTime(alert.firedAt) }}</td>
              <td>{{ alert.duration }}</td>
              <td>
                <button class="btn-link">详情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="activeTab === 'rules'" class="table-content">
        <table class="data-table">
          <thead>
            <tr>
              <th>规则名称</th>
              <th>表达式</th>
              <th>持续时间</th>
              <th>级别</th>
              <th>启用</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rule in alertRules" :key="rule.name">
              <td class="rule-name">{{ rule.name }}</td>
              <td class="expr-cell">{{ rule.expr }}</td>
              <td>{{ rule.duration }}</td>
              <td>
                <span :class="['severity-badge', getSeverityClass(rule.severity)]">
                  {{ getSeverityText(rule.severity) }}
                </span>
              </td>
              <td>
                <span :class="['toggle-badge', rule.enabled ? 'enabled' : 'disabled']">
                  {{ rule.enabled ? '是' : '否' }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-link">编辑</button>
                  <button class="btn-link danger">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.alerts-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-value {
  font-size: 36px;
  font-weight: 300;
  color: var(--color-text-primary);
  line-height: 1.15;
  letter-spacing: -0.02em;
}

.stat-value.firing {
  color: #e6a23c;
}

.stat-value.critical {
  color: #f56c6c;
}

.stat-label {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.action-card {
  background: transparent;
  border: none;
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
  padding: 12px 24px;
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

.table-card {
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid var(--color-border);
}

.tab-btn {
  padding: 16px 24px;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-secondary);
  cursor: pointer;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  transition: all 0.15s ease;
}

.tab-btn:hover {
  color: var(--color-text-primary);
}

.tab-btn.active {
  color: var(--site-context-highlight-color);
  border-bottom-color: var(--site-context-highlight-color);
}

.table-content {
  padding: 0;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 14px 16px;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-secondary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--color-border);
}

.data-table td {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border);
  line-height: 1.15;
}

.data-table tr:last-child td {
  border-bottom: none;
}

.data-table tr:hover td {
  background: #fafafa;
}

.alert-name,
.rule-name {
  font-weight: 700;
}

.expr-cell {
  font-family: monospace;
  font-size: 12px;
  color: var(--site-context-highlight-color);
}

.severity-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.severity-badge.critical {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.severity-badge.warning {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.severity-badge.info {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.status-badge.firing {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.status-badge.resolved {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.toggle-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.toggle-badge.enabled {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.toggle-badge.disabled {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.action-btns {
  display: flex;
  gap: 12px;
}

.btn-link {
  background: none;
  border: none;
  color: var(--site-context-highlight-color);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  padding: 0;
  letter-spacing: 0.02em;
  transition: color 0.15s ease;
}

.btn-link:hover {
  color: var(--site-context-focus-color);
}

.btn-link.danger {
  color: #f56c6c;
}

.btn-link.danger:hover {
  color: #c45656;
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
