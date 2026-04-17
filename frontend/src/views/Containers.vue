<script setup>
import { ref, onMounted } from 'vue'
import { Refresh, Plus, Search } from '@element-plus/icons-vue'
import { getContainerList } from '@/api'

const loading = ref(false)
const containerList = ref([])
const searchQuery = ref('')
const filterStatus = ref('all')

const filteredContainers = ref([])

const fetchContainerList = async () => {
  loading.value = true
  try {
    const res = await getContainerList()
    if (res.code === 200) {
      containerList.value = res.data || []
      applyFilters()
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const applyFilters = () => {
  let result = [...containerList.value]
  if (filterStatus.value !== 'all') {
    result = result.filter(c => c.status === filterStatus.value)
  }
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(c =>
      c.name.toLowerCase().includes(query) ||
      c.image.toLowerCase().includes(query)
    )
  }
  filteredContainers.value = result
}

const getStatusClass = (status) => {
  if (status === 'running') return 'running'
  if (status === 'stopped') return 'stopped'
  return 'unknown'
}

const getStatusText = (status) => {
  if (status === 'running') return '运行中'
  if (status === 'stopped') return '已停止'
  return '未知'
}

const handleStart = (container) => {
  console.log('Start container:', container.id)
}

const handleStop = (container) => {
  console.log('Stop container:', container.id)
}

const handleRestart = (container) => {
  console.log('Restart container:', container.id)
}

const handleLogs = (container) => {
  console.log('View logs:', container.id)
}

onMounted(() => fetchContainerList())
</script>

<template>
  <div class="containers-page">
    <div class="page-header">
      <span class="page-title">容器管理</span>
      <div class="header-actions">
        <button class="btn-secondary" @click="fetchContainerList" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </button>
        <button class="btn-primary">
          <el-icon><Plus /></el-icon>
          <span>创建容器</span>
        </button>
      </div>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索容器名称或镜像"
          class="search-input"
          @input="applyFilters"
        />
      </div>
      <div class="filter-tabs">
        <button
          :class="['filter-tab', { active: filterStatus === 'all' }]"
          @click="filterStatus = 'all'; applyFilters()"
        >
          全部
        </button>
        <button
          :class="['filter-tab', { active: filterStatus === 'running' }]"
          @click="filterStatus = 'running'; applyFilters()"
        >
          运行中
        </button>
        <button
          :class="['filter-tab', { active: filterStatus === 'stopped' }]"
          @click="filterStatus = 'stopped'; applyFilters()"
        >
          已停止
        </button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>容器名称</th>
            <th>镜像</th>
            <th>状态</th>
            <th>CPU</th>
            <th>内存</th>
            <th>网络</th>
            <th>端口映射</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="container in filteredContainers" :key="container.id">
            <td class="container-name">{{ container.name }}</td>
            <td class="image-cell">{{ container.image }}</td>
            <td>
              <span :class="['status-badge', getStatusClass(container.status)]">
                {{ getStatusText(container.status) }}
              </span>
            </td>
            <td>
              <div class="progress-cell">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: (container.cpu || 0) + '%' }"></div>
                </div>
                <span class="progress-text">{{ container.cpu || 0 }}%</span>
              </div>
            </td>
            <td>
              <div class="progress-cell">
                <div class="progress-bar memory">
                  <div class="progress-fill" :style="{ width: (container.memory || 0) + '%' }"></div>
                </div>
                <span class="progress-text">{{ container.memory || 0 }}%</span>
              </div>
            </td>
            <td>{{ container.network || '0 KB/s' }}</td>
            <td class="ports-cell">{{ container.ports || '-' }}</td>
            <td>
              <div class="action-btns">
                <button
                  v-if="container.status === 'running'"
                  class="btn-link"
                  @click="handleStop(container)"
                >
                  停止
                </button>
                <button
                  v-if="container.status === 'stopped'"
                  class="btn-link"
                  @click="handleStart(container)"
                >
                  启动
                </button>
                <button
                  v-if="container.status === 'running'"
                  class="btn-link"
                  @click="handleRestart(container)"
                >
                  重启
                </button>
                <button class="btn-link" @click="handleLogs(container)">日志</button>
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
.containers-page {
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

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid var(--color-border);
  gap: 16px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  max-width: 320px;
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

.filter-tabs {
  display: flex;
  gap: 0;
}

.filter-tab {
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--color-border);
  font-size: 12px;
  font-weight: 700;
  color: var(--color-text-secondary);
  cursor: pointer;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  transition: all 0.15s ease;
  margin-left: -1px;
}

.filter-tab:first-child {
  margin-left: 0;
}

.filter-tab:hover {
  color: var(--color-text-primary);
}

.filter-tab.active {
  background: var(--site-context-highlight-color);
  border-color: var(--site-context-highlight-color);
  color: #ffffff;
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

.container-name {
  font-weight: 700;
}

.image-cell {
  font-family: monospace;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.ports-cell {
  font-family: monospace;
  font-size: 12px;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.status-badge.running {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.status-badge.stopped {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.status-badge.unknown {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-bar {
  width: 60px;
  height: 4px;
  background: #f0f0f0;
}

.progress-fill {
  height: 100%;
  background: var(--site-context-highlight-color);
  transition: width 0.3s ease;
}

.progress-bar.memory .progress-fill {
  background: #67c23a;
}

.progress-text {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
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

.loading-cell {
  text-align: center;
  padding: 40px;
  color: var(--color-text-secondary);
}
</style>
