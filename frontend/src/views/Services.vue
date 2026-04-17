<script setup>
import { ref, onMounted } from 'vue'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { getServiceList } from '@/api'

const loading = ref(false)
const serviceList = ref([])

const fetchServiceList = async () => {
  loading.value = true
  try {
    const res = await getServiceList()
    if (res.code === 200) {
      serviceList.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getStatusClass = (status) => {
  if (status === 'healthy') return 'healthy'
  if (status === 'unhealthy') return 'unhealthy'
  if (status === 'degraded') return 'degraded'
  return 'unknown'
}

const getStatusText = (status) => {
  if (status === 'healthy') return '健康'
  if (status === 'unhealthy') return '异常'
  if (status === 'degraded') return '降级'
  return '未知'
}

const handleEdit = (service) => {
  console.log('Edit service:', service.id)
}

const handleDelete = (service) => {
  console.log('Delete service:', service.id)
}

onMounted(() => fetchServiceList())
</script>

<template>
  <div class="services-page">
    <div class="page-header">
      <span class="page-title">服务管理</span>
      <div class="header-actions">
        <button class="btn-secondary" @click="fetchServiceList" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </button>
        <button class="btn-primary">
          <el-icon><Plus /></el-icon>
          <span>添加服务</span>
        </button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>服务名称</th>
            <th>类型</th>
            <th>状态</th>
            <th>实例数</th>
            <th>健康实例</th>
            <th>响应时间</th>
            <th>错误率</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="service in serviceList" :key="service.id">
            <td class="service-name">{{ service.name }}</td>
            <td>
              <span class="type-badge">{{ service.type }}</span>
            </td>
            <td>
              <span :class="['status-badge', getStatusClass(service.status)]">
                {{ getStatusText(service.status) }}
              </span>
            </td>
            <td>{{ service.instances }}</td>
            <td>{{ service.healthyInstances }} / {{ service.instances }}</td>
            <td>{{ service.responseTime || '-' }}</td>
            <td>
              <span :class="['error-rate', { high: service.errorRate > 5 }]">
                {{ service.errorRate || 0 }}%
              </span>
            </td>
            <td>
              <div class="action-btns">
                <button class="btn-link" @click="handleEdit(service)">编辑</button>
                <button class="btn-link danger" @click="handleDelete(service)">删除</button>
              </div>
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
.services-page {
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

.service-name {
  font-weight: 700;
}

.type-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  background: rgba(28, 105, 212, 0.1);
  color: var(--site-context-highlight-color);
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.status-badge.healthy {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.status-badge.unhealthy {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.status-badge.degraded {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.status-badge.unknown {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.error-rate {
  font-weight: 700;
}

.error-rate.high {
  color: #f56c6c;
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

.loading-cell {
  text-align: center;
  padding: 40px;
  color: var(--color-text-secondary);
}
</style>
