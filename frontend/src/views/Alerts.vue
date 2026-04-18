<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAlertList, getActiveAlerts, resolveAlert, getAlertRules, addAlertRule, updateAlertRule, deleteAlertRule, toggleAlertRule, syncAlerts } from '@/api'

const { t } = useI18n()
const loading = ref(false)
const activeTab = ref('alerts')
const alertList = ref([])
const alertRules = ref([])
const stats = ref({ total: 0, firing: 0, critical: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const ruleForm = ref({
  name: '',
  expr: '',
  duration: '5m',
  severity: 'warning',
  summary: '',
  description: ''
})
const editingId = ref(null)

const fetchAlerts = async () => {
  loading.value = true
  try {
    const [alertsRes, rulesRes] = await Promise.all([
      getAlertList(),
      getAlertRules()
    ])
    
    if (alertsRes.code === 200) {
      alertList.value = alertsRes.data || []
      stats.value.total = alertList.value.length
      stats.value.firing = alertList.value.filter(a => a.status === 'firing').length
      stats.value.critical = alertList.value.filter(a => a.severity === 'critical').length
    }
    
    if (rulesRes.code === 200) {
      alertRules.value = rulesRes.data || []
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSync = async () => {
  loading.value = true
  try {
    const res = await syncAlerts()
    if (res.code === 200) {
      ElMessage.success(t('同步成功'))
      fetchAlerts()
    } else {
      ElMessage.error(res.message || t('同步失败'))
    }
  } catch (error) {
    ElMessage.error(t('同步失败'))
  } finally {
    loading.value = false
  }
}

const handleResolve = async (alert) => {
  try {
    await ElMessageBox.confirm(t('确定解决此告警吗？'), t('提示'), {
      type: 'warning'
    })
    const res = await resolveAlert(alert.id)
    if (res.code === 200) {
      ElMessage.success(t('告警已解决'))
      fetchAlerts()
    } else {
      ElMessage.error(res.message || t('操作失败'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('操作失败'))
    }
  }
}

const handleAddRule = () => {
  dialogTitle.value = t('添加规则')
  editingId.value = null
  ruleForm.value = {
    name: '',
    expr: '',
    duration: '5m',
    severity: 'warning',
    summary: '',
    description: ''
  }
  dialogVisible.value = true
}

const handleEditRule = (rule) => {
  dialogTitle.value = t('编辑规则')
  editingId.value = rule.id
  ruleForm.value = {
    name: rule.name,
    expr: rule.expr,
    duration: rule.duration,
    severity: rule.severity,
    summary: rule.summary || '',
    description: rule.description || ''
  }
  dialogVisible.value = true
}

const handleSubmitRule = async () => {
  if (!ruleForm.value.name || !ruleForm.value.expr) {
    ElMessage.warning(t('请填写完整信息'))
    return
  }
  
  try {
    let res
    if (editingId.value) {
      res = await updateAlertRule(editingId.value, ruleForm.value)
    } else {
      res = await addAlertRule(ruleForm.value)
    }
    
    if (res.code === 200) {
      ElMessage.success(editingId.value ? t('规则更新成功') : t('规则添加成功'))
      dialogVisible.value = false
      fetchAlerts()
    } else {
      ElMessage.error(res.message || t('操作失败'))
    }
  } catch (error) {
    ElMessage.error(t('操作失败'))
  }
}

const handleToggleRule = async (rule) => {
  try {
    const res = await toggleAlertRule(rule.id)
    if (res.code === 200) {
      ElMessage.success(t('状态更新成功'))
      fetchAlerts()
    } else {
      ElMessage.error(res.message || t('操作失败'))
    }
  } catch (error) {
    ElMessage.error(t('操作失败'))
  }
}

const handleDeleteRule = async (rule) => {
  try {
    await ElMessageBox.confirm(t('确定删除此规则吗？'), t('提示'), {
      type: 'warning'
    })
    const res = await deleteAlertRule(rule.id)
    if (res.code === 200) {
      ElMessage.success(t('规则删除成功'))
      fetchAlerts()
    } else {
      ElMessage.error(res.message || t('删除失败'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('删除失败'))
    }
  }
}

const getSeverityText = (severity) => {
  if (severity === 'critical') return t('严重')
  if (severity === 'warning') return t('警告')
  return t('信息')
}

const getSeverityClass = (severity) => {
  if (severity === 'critical') return 'critical'
  if (severity === 'warning') return 'warning'
  return 'info'
}

const getStatusText = (status) => {
  if (status === 'firing') return t('触发中')
  if (status === 'resolved') return t('已解决')
  return status
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

onMounted(() => fetchAlerts())
</script>

<template>
  <div class="alerts-page">
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">{{ t('告警总数') }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value firing">{{ stats.firing }}</span>
          <span class="stat-label">{{ t('触发') }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value critical">{{ stats.critical }}</span>
          <span class="stat-label">{{ t('严重告警') }}</span>
        </div>
      </div>
      <div class="stat-card action-card">
        <button class="btn-primary" @click="handleSync" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>{{ t('同步告警') }}</span>
        </button>
      </div>
    </div>

    <div class="table-card">
      <div class="tabs-header">
        <button
          :class="['tab-btn', { active: activeTab === 'alerts' }]"
          @click="activeTab = 'alerts'"
        >
          {{ t('告警列表') }}
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'rules' }]"
          @click="activeTab = 'rules'"
        >
          {{ t('告警规则') }}
        </button>
      </div>

      <div v-if="activeTab === 'alerts'" class="table-content">
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ t('告警名称') }}</th>
              <th>{{ t('严重程度') }}</th>
              <th>{{ t('状态') }}</th>
              <th>{{ t('实例') }}</th>
              <th>{{ t('触发时间') }}</th>
              <th>{{ t('操作') }}</th>
            </tr>
          </thead>
          <tbody v-if="!loading">
            <tr v-for="alert in alertList" :key="alert.id">
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
              <td>{{ formatDateTime(alert.startsAt) }}</td>
              <td>
                <button v-if="alert.status === 'firing'" class="btn-link" @click="handleResolve(alert)">{{ t('解决') }}</button>
                <span v-else class="text-muted">-</span>
              </td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr>
              <td colspan="6" class="loading-cell">{{ t('加载中...') }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="activeTab === 'rules'" class="table-content">
        <div class="table-header">
          <button class="btn-secondary" @click="handleAddRule">
            <el-icon><Plus /></el-icon>
            <span>{{ t('添加规则') }}</span>
          </button>
        </div>
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ t('规则名称') }}</th>
              <th>{{ t('表达式') }}</th>
              <th>{{ t('持续时间') }}</th>
              <th>{{ t('严重程度') }}</th>
              <th>{{ t('启用') }}</th>
              <th>{{ t('操作') }}</th>
            </tr>
          </thead>
          <tbody v-if="!loading">
            <tr v-for="rule in alertRules" :key="rule.id">
              <td class="rule-name">{{ rule.name }}</td>
              <td class="expr-cell">{{ rule.expr }}</td>
              <td>{{ rule.duration }}</td>
              <td>
                <span :class="['severity-badge', getSeverityClass(rule.severity)]">
                  {{ getSeverityText(rule.severity) }}
                </span>
              </td>
              <td>
                <span 
                  :class="['toggle-badge', rule.enabled ? 'enabled' : 'disabled']"
                  @click="handleToggleRule(rule)"
                  style="cursor: pointer"
                >
                  {{ rule.enabled ? t('是') : t('否') }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-link" @click="handleEditRule(rule)">{{ t('编辑') }}</button>
                  <button class="btn-link danger" @click="handleDeleteRule(rule)">{{ t('删除') }}</button>
                </div>
              </td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr>
              <td colspan="6" class="loading-cell">{{ t('加载中...') }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="ruleForm" label-width="80px">
        <el-form-item :label="t('规则名称')" required>
          <el-input v-model="ruleForm.name" :placeholder="t('规则名称占位符')" />
        </el-form-item>
        <el-form-item :label="t('表达式')" required>
          <el-input v-model="ruleForm.expr" :placeholder="t('表达式占位符')" />
        </el-form-item>
        <el-form-item :label="t('持续时间')">
          <el-select v-model="ruleForm.duration" style="width: 100%">
            <el-option label="1m" value="1m" />
            <el-option label="5m" value="5m" />
            <el-option label="10m" value="10m" />
            <el-option label="30m" value="30m" />
            <el-option label="1h" value="1h" />
            <el-option label="2h" value="2h" />
            <el-option label="6h" value="6h" />
            <el-option label="12h" value="12h" />
            <el-option label="24h" value="24h" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('严重程度')">
          <el-select v-model="ruleForm.severity" style="width: 100%">
            <el-option :label="t('严重')" value="critical" />
            <el-option :label="t('警告')" value="warning" />
            <el-option :label="t('信息')" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('摘要')">
          <el-input v-model="ruleForm.summary" />
        </el-form-item>
        <el-form-item :label="t('描述')">
          <el-input v-model="ruleForm.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('取消') }}</el-button>
        <el-button type="primary" @click="handleSubmitRule">{{ t('确认') }}</el-button>
      </template>
    </el-dialog>
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

.btn-secondary {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 700;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.15s ease;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.btn-secondary:hover {
  border-color: var(--color-text-primary);
  color: var(--color-text-primary);
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

.table-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
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

.text-muted {
  color: var(--color-text-secondary);
}

.loading-cell {
  text-align: center;
  padding: 40px;
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
