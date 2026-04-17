<script setup>
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { api } from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const activeTab = ref('alerts')
const alertList = ref([])
const alertRules = ref([])
const stats = ref({ total: 0, firing: 0, critical: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.getAlerts()
    if (res.code === 200) {
      alertList.value = res.data?.list || []
      alertRules.value = res.data?.rules || []
      stats.value = res.data?.stats || { total: alertList.value.length, firing: alertList.value.filter(a => a.status === 'firing').length, critical: alertList.value.filter(a => a.severity === 'critical').length }
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSync = () => {
  fetchData()
  ElMessage.success('同步完成')
}

const getSeverityType = (severity) => ({ critical: 'danger', warning: 'warning', info: 'info' }[severity] || 'info')

const getStatusText = (status) => ({ firing: '告警中', resolved: '已恢复' }[status] || status)

onMounted(() => fetchData())
</script>

<template>
  <div class="page-container">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总告警</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card warning">
          <div class="stat-value">{{ stats.firing }}</div>
          <div class="stat-label">告警中</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card danger">
          <div class="stat-value">{{ stats.critical }}</div>
          <div class="stat-label">严重</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card action-card">
          <el-button type="primary" size="small" @click="handleSync" :loading="loading"><el-icon><Refresh /></el-icon> 同步告警</el-button>
        </div>
      </el-col>
    </el-row>

    <div class="panel-card">
      <el-tabs v-model="activeTab" class="dark-tabs">
        <el-tab-pane label="告警列表" name="alerts">
          <el-table :data="alertList" v-loading="loading" class="dark-table">
            <el-table-column prop="name" label="名称" min-width="200">
              <template #default="{ row }"><span class="alert-name" :class="row.severity">{{ row.name }}</span></template>
            </el-table-column>
            <el-table-column prop="severity" label="级别" width="90" align="center">
              <template #default="{ row }"><span class="sev-tag" :class="row.severity">{{ row.severity === 'critical' ? '严重' : row.severity === 'warning' ? '警告' : '信息' }}</span></template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <span class="status-dot" :class="row.status"></span>
                {{ getStatusText(row.status) }}
              </template>
            </el-table-column>
            <el-table-column prop="instance" label="实例" width="160" />
            <el-table-column prop="firedAt" label="触发时间" width="170" />
            <el-table-column prop="duration" label="持续时间" width="100" align="center" />
            <el-table-column label="操作" width="80" align="center">
              <template #default><el-button type="primary" link size="small">详情</el-button></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="告警规则" name="rules">
          <el-table :data="alertRules" class="dark-table">
            <el-table-column prop="name" label="规则名称" min-width="200">
              <template #default="{ row }"><span class="rule-name">{{ row.name }}</span></template>
            </el-table-column>
            <el-table-column prop="expr" label="表达式" min-width="250" show-overflow-tooltip>
              <template #default="{ row }"><code class="expr-code">{{ row.expr }}</code></template>
            </el-table-column>
            <el-table-column prop="duration" label="持续时间" width="100" align="center" />
            <el-table-column prop="severity" label="级别" width="90" align="center">
              <template #default="{ row }"><span class="sev-tag" :class="row.severity">{{ row.severity === 'critical' ? '严重' : row.severity === 'warning' ? '警告' : '信息' }}</span></template>
            </el-table-column>
            <el-table-column prop="enabled" label="启用" width="70" align="center">
              <template #default="{ row }"><el-switch v-model="row.enabled" size="small" /></template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default><el-button type="primary" link size="small">编辑</el-button><el-button type="danger" link size="small">删除</el-button></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }

.stats-row { margin-bottom: 0; }

.stat-card {
  background: #1e1e1e;
  border: 1px solid #2a2a2a;
  border-radius: 3px;
  padding: 18px;
  text-align: center;
}

.stat-card.warning { border-left: 3px solid #f59e0b; }
.stat-card.danger { border-left: 3px solid #ef4444; }
.stat-card.action-card { display: flex; align-items: center; justify-content: center; }

.stat-value { font-size: 28px; font-weight: 600; color: #e5e5e5; }
.stat-label { font-size: 13px; color: #777; margin-top: 4px; }

.panel-card {
  background: #1e1e1e;
  border: 1px solid #2a2a2a;
  border-radius: 3px;
  padding: 16px;
}

.alert-name { color: #818cf8; cursor: pointer; }
.alert-name.critical { color: #f87171; }
.alert-name.warning { color: #fbbf24; }

.sev-tag {
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
}
.sev-tag.critical { background: rgba(239, 68, 68, 0.15); color: #f87171; }
.sev-tag.warning { background: rgba(245, 158, 11, 0.15); color: #fbbf24; }
.sev-tag.info { background: rgba(99, 102, 241, 0.15); color: #818cf8; }

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}
.status-dot.firing { background: #ef4444; box-shadow: 0 0 6px rgba(239, 68, 68, 0.5); animation: pulse 2s infinite; }
.status-dot.resolved { background: #22c55e; box-shadow: 0 0 6px rgba(34, 197, 94, 0.5); }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.rule-name { color: #ddd; font-weight: 500; }
.expr-code { color: #818cf8; font-size: 12px; background: #141414; padding: 2px 6px; border-radius: 3px; }
</style>
