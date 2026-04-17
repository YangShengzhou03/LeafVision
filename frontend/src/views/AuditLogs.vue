<script setup>
import { ref, onMounted, computed } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { getAuditLogs } from '@/api'

const loading = ref(false)
const auditLogs = ref([])
const searchQuery = ref('')
const filterModule = ref('')
const filterOperation = ref('')
const dateRange = ref([])

const moduleOptions = [
  { label: '全部', value: '' },
  { label: '用户管理', value: 'user' },
  { label: '角色管理', value: 'role' },
  { label: '权限管理', value: 'permission' },
  { label: '服务器管理', value: 'server' },
  { label: '告警管理', value: 'alert' },
  { label: '系统设置', value: 'settings' }
]

const operationOptions = [
  { label: '全部', value: '' },
  { label: '登录', value: 'LOGIN' },
  { label: '登出', value: 'LOGOUT' },
  { label: '新增', value: 'CREATE' },
  { label: '修改', value: 'UPDATE' },
  { label: '删除', value: 'DELETE' },
  { label: '查看', value: 'VIEW' }
]

const fetchAuditLogs = async () => {
  loading.value = true
  try {
    const res = await getAuditLogs({
      module: filterModule.value,
      operation: filterOperation.value,
      keyword: searchQuery.value,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    })
    if (res.code === 200) {
      auditLogs.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const filteredLogs = computed(() => {
  if (!searchQuery.value) return auditLogs.value
  const query = searchQuery.value.toLowerCase()
  return auditLogs.value.filter(log =>
    log.username?.toLowerCase().includes(query) ||
    log.targetName?.toLowerCase().includes(query) ||
    log.detail?.toLowerCase().includes(query)
  )
})

const getOperationClass = (operation) => {
  const classMap = {
    'LOGIN': 'login',
    'LOGOUT': 'logout',
    'CREATE': 'create',
    'UPDATE': 'update',
    'DELETE': 'delete',
    'VIEW': 'view'
  }
  return classMap[operation] || 'default'
}

const getOperationText = (operation) => {
  const textMap = {
    'LOGIN': '登录',
    'LOGOUT': '登出',
    'CREATE': '新增',
    'UPDATE': '修改',
    'DELETE': '删除',
    'VIEW': '查看'
  }
  return textMap[operation] || operation
}

const getStatusClass = (status) => {
  return status === 1 ? 'success' : 'fail'
}

const getStatusText = (status) => {
  return status === 1 ? '成功' : '失败'
}

const handleSearch = () => {
  fetchAuditLogs()
}

const handleReset = () => {
  searchQuery.value = ''
  filterModule.value = ''
  filterOperation.value = ''
  dateRange.value = []
  fetchAuditLogs()
}

onMounted(() => fetchAuditLogs())
</script>

<template>
  <div class="audit-logs-page">
    <div class="page-header">
      <span class="page-title">审计日志</span>
      <div class="header-actions">
        <button class="btn-secondary" @click="fetchAuditLogs" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </button>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-row">
        <div class="search-box">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索用户名、操作对象"
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <select v-model="filterModule" class="filter-select" @change="handleSearch">
          <option v-for="item in moduleOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
        <select v-model="filterOperation" class="filter-select" @change="handleSearch">
          <option v-for="item in operationOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
        <button class="btn-secondary" @click="handleSearch">搜索</button>
        <button class="btn-secondary" @click="handleReset">重置</button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>时间</th>
            <th>用户</th>
            <th>操作</th>
            <th>模块</th>
            <th>操作对象</th>
            <th>详情</th>
            <th>IP地址</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="log in filteredLogs" :key="log.id">
            <td class="time-cell">{{ log.createdAt }}</td>
            <td class="user-cell">{{ log.username || '-' }}</td>
            <td>
              <span :class="['operation-badge', getOperationClass(log.operation)]">
                {{ getOperationText(log.operation) }}
              </span>
            </td>
            <td>{{ log.module || '-' }}</td>
            <td>{{ log.targetName || '-' }}</td>
            <td class="detail-cell">{{ log.detail || '-' }}</td>
            <td class="ip-cell">{{ log.ipAddress || '-' }}</td>
            <td>
              <span :class="['status-badge', getStatusClass(log.status)]">
                {{ getStatusText(log.status) }}
              </span>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr>
            <td colspan="8" class="loading-cell">加载中...</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.audit-logs-page {
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

.header-actions {
  display: flex;
  gap: 12px;
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

.filter-bar {
  display: flex;
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  max-width: 280px;
}

.search-icon {
  color: var(--color-text-secondary);
}

.search-input {
  flex: 1;
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  font-size: 13px;
  color: var(--color-text-primary);
  outline: none;
  transition: border-color 0.15s ease;
}

.search-input:focus {
  border-color: var(--site-context-highlight-color);
}

.filter-select {
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  font-size: 13px;
  color: var(--color-text-primary);
  background: #ffffff;
  outline: none;
  min-width: 120px;
}

.table-card {
  background: #ffffff;
  border: 1px solid var(--color-border);
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

.time-cell {
  font-family: monospace;
  font-size: 12px;
  white-space: nowrap;
}

.user-cell {
  font-weight: 700;
}

.detail-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ip-cell {
  font-family: monospace;
  font-size: 12px;
}

.operation-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.operation-badge.login {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.operation-badge.logout {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.operation-badge.create {
  background: rgba(28, 105, 212, 0.1);
  color: var(--site-context-highlight-color);
}

.operation-badge.update {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.operation-badge.delete {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.operation-badge.view {
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

.status-badge.success {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.status-badge.fail {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.loading-cell {
  text-align: center;
  padding: 40px;
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    max-width: none;
  }
}
</style>
