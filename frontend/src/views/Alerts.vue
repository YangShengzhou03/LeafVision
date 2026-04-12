<script setup>
import { ref, onMounted } from 'vue'
import { Bell, Check, Close, View, Edit, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'

const alertList = ref([])
const alertRules = ref([])
const loading = ref(false)

const activeTab = ref('alerts')

const getLevelType = (level) => {
  if (level === 'critical') return 'danger'
  if (level === 'warning') return 'warning'
  return 'info'
}

const getLevelText = (level) => {
  if (level === 'critical') return '严重'
  if (level === 'warning') return '警告'
  return '信息'
}

const getStatusType = (status) => {
  if (status === 'firing') return 'danger'
  if (status === 'resolved') return 'success'
  return 'warning'
}

const getStatusText = (status) => {
  if (status === 'firing') return '告警中'
  if (status === 'resolved') return '已恢复'
  return '待处理'
}

const fetchAlertList = async () => {
  loading.value = true
  try {
    const res = await api.getAlertList()
    if (res.code === 200) {
      alertList.value = res.data || []
    }
  } catch (error) {
    console.error('获取告警列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchAlertRules = async () => {
  try {
    const res = await api.getAlertRules()
    if (res.code === 200) {
      alertRules.value = res.data || []
    }
  } catch (error) {
    console.error('获取告警规则失败:', error)
  }
}

const handleResolve = async (row) => {
  try {
    const res = await api.resolveAlert(row.id)
    if (res.code === 200) {
      ElMessage.success('告警已标记为已恢复')
      fetchAlertList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('恢复告警失败:', error)
  }
}

const handleView = (row) => {
  ElMessageBox.alert(
    `<p><strong>告警名称:</strong> ${row.name}</p>
     <p><strong>级别:</strong> ${getLevelText(row.level)}</p>
     <p><strong>服务器:</strong> ${row.server || '-'}</p>
     <p><strong>当前值:</strong> ${row.value || '-'}</p>
     <p><strong>摘要:</strong> ${row.summary || '-'}</p>
     <p><strong>描述:</strong> ${row.description || '-'}</p>
     <p><strong>触发时间:</strong> ${row.startsAt || row.time || '-'}</p>`,
    '告警详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  )
}

const handleToggleRule = async (row) => {
  try {
    const res = await api.toggleRule(row.id)
    if (res.code === 200) {
      ElMessage.success(row.enabled ? '规则已启用' : '规则已禁用')
      fetchAlertRules()
    } else {
      row.enabled = !row.enabled
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    row.enabled = !row.enabled
    console.error('切换规则状态失败:', error)
  }
}

const handleDeleteRule = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除规则 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.deleteRule(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchAlertRules()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除规则失败:', error)
    }
  }
}

const handleSync = async () => {
  loading.value = true
  try {
    const res = await api.syncAlerts()
    if (res.code === 200) {
      ElMessage.success('同步成功')
      fetchAlertList()
      fetchAlertRules()
    } else {
      ElMessage.error(res.message || '同步失败')
    }
  } catch (error) {
    console.error('同步告警失败:', error)
    ElMessage.error('同步失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAlertList()
  fetchAlertRules()
})
</script>

<template>
  <div class="alerts-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span><el-icon><Bell /></el-icon> 告警管理</span>
          <el-button type="primary" :icon="Refresh" @click="handleSync" :loading="loading">同步数据</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="告警列表" name="alerts">
          <el-table :data="alertList" style="width: 100%" v-loading="loading">
            <el-table-column prop="name" label="告警名称" />
            <el-table-column prop="level" label="级别" width="100">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.level)" size="small">
                  {{ getLevelText(row.level) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="server" label="服务器" />
            <el-table-column prop="value" label="当前值" width="100" />
            <el-table-column prop="startsAt" label="触发时间" width="180">
              <template #default="{ row }">
                {{ row.startsAt || row.time || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link :icon="View" @click="handleView(row)">详情</el-button>
                <el-button
                  v-if="row.status === 'firing'"
                  type="success"
                  link
                  :icon="Check"
                  @click="handleResolve(row)"
                >恢复</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="告警规则" name="rules">
          <el-table :data="alertRules" style="width: 100%">
            <el-table-column prop="name" label="规则名称" />
            <el-table-column prop="expr" label="表达式" show-overflow-tooltip />
            <el-table-column prop="duration" label="持续时间" width="100" />
            <el-table-column prop="level" label="级别" width="100">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.level)" size="small">
                  {{ getLevelText(row.level) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" width="100">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" @change="handleToggleRule(row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link :icon="Edit">编辑</el-button>
                <el-button type="danger" link :icon="Close" @click="handleDeleteRule(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.alerts-page {
  padding: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
