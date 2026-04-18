<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Refresh, Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getContainerList, 
  startContainer, 
  stopContainer, 
  restartContainer, 
  getContainerLogs,
  createContainer
} from '@/api'

const { t } = useI18n()
const loading = ref(false)
const containerList = ref([])
const searchQuery = ref('')
const filterStatus = ref('all')

const filteredContainers = ref([])

const logDialogVisible = ref(false)
const currentContainer = ref(null)
const containerLogs = ref('')
const logsLoading = ref(false)

const createDialogVisible = ref(false)
const createForm = ref({
  name: '',
  image: '',
  ports: '',
  env: '',
  serverId: null
})
const createLoading = ref(false)

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
  if (status === 'exited' || status === 'stopped') return 'stopped'
  return 'unknown'
}

const getStatusText = (status) => {
  if (status === 'running') return t('运行中')
  if (status === 'exited' || status === 'stopped') return t('已停止')
  return t('未知')
}

const handleStart = async (container) => {
  try {
    const res = await startContainer(container.id, container.serverId)
    if (res.code === 200) {
      ElMessage.success(t('启动成功'))
      fetchContainerList()
    } else {
      ElMessage.error(res.message || t('启动失败'))
    }
  } catch (error) {
    ElMessage.error(t('启动失败'))
  }
}

const handleStop = async (container) => {
  try {
    await ElMessageBox.confirm(t('确定停止此容器吗？'), t('提示'), {
      type: 'warning'
    })
    const res = await stopContainer(container.id, container.serverId)
    if (res.code === 200) {
      ElMessage.success(t('停止成功'))
      fetchContainerList()
    } else {
      ElMessage.error(res.message || t('停止失败'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('停止失败'))
    }
  }
}

const handleRestart = async (container) => {
  try {
    await ElMessageBox.confirm(t('确定重启此容器吗？'), t('提示'), {
      type: 'warning'
    })
    const res = await restartContainer(container.id, container.serverId)
    if (res.code === 200) {
      ElMessage.success(t('重启成功'))
      fetchContainerList()
    } else {
      ElMessage.error(res.message || t('重启失败'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('重启失败'))
    }
  }
}

const handleLogs = async (container) => {
  currentContainer.value = container
  logDialogVisible.value = true
  logsLoading.value = true
  containerLogs.value = ''
  
  try {
    const res = await getContainerLogs(container.id, { tail: 200 })
    if (res.code === 200 && res.data) {
      containerLogs.value = res.data
    } else {
      containerLogs.value = t('暂无日志')
    }
  } catch (error) {
    containerLogs.value = t('获取日志失败')
  } finally {
    logsLoading.value = false
  }
}

const handleOpenCreate = () => {
  createForm.value = {
    name: '',
    image: '',
    ports: '',
    env: '',
    serverId: null
  }
  createDialogVisible.value = true
}

const handleCreate = async () => {
  if (!createForm.value.name || !createForm.value.image) {
    ElMessage.warning(t('请填写容器名称和镜像'))
    return
  }
  createLoading.value = true
  try {
    const res = await createContainer(createForm.value)
    if (res.code === 200) {
      ElMessage.success(t('创建成功'))
      createDialogVisible.value = false
      fetchContainerList()
    } else {
      ElMessage.error(res.message || t('创建失败'))
    }
  } catch (error) {
    ElMessage.error(t('创建失败'))
  } finally {
    createLoading.value = false
  }
}

onMounted(() => fetchContainerList())
</script>

<template>
  <div class="containers-page">
    <div class="page-header">
      <span class="page-title">{{ t('容器管理') }}</span>
      <div class="header-actions">
        <button class="btn-secondary" @click="fetchContainerList" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>{{ t('刷新') }}</span>
        </button>
        <button class="btn-primary" @click="handleOpenCreate">
          <el-icon><Plus /></el-icon>
          <span>{{ t('创建容器') }}</span>
        </button>
      </div>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="searchQuery"
          type="text"
          :placeholder="t('搜索容器...')"
          class="search-input"
          @input="applyFilters"
        />
      </div>
      <div class="filter-tabs">
        <button
          :class="['filter-tab', { active: filterStatus === 'all' }]"
          @click="filterStatus = 'all'; applyFilters()"
        >
          {{ t('全部') }}
        </button>
        <button
          :class="['filter-tab', { active: filterStatus === 'running' }]"
          @click="filterStatus = 'running'; applyFilters()"
        >
          {{ t('运行中') }}
        </button>
        <button
          :class="['filter-tab', { active: filterStatus === 'exited' }]"
          @click="filterStatus = 'exited'; applyFilters()"
        >
          {{ t('已停止') }}
        </button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ t('容器名称') }}</th>
            <th>{{ t('镜像') }}</th>
            <th>{{ t('状态') }}</th>
            <th>{{ t('CPU') }}</th>
            <th>{{ t('内存') }}</th>
            <th>{{ t('网络') }}</th>
            <th>{{ t('端口') }}</th>
            <th>{{ t('操作') }}</th>
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
                  v-if="container.status === 'exited' || container.status === 'stopped'"
                  class="btn-link success"
                  @click="handleStart(container)"
                >
                  {{ t('启动') }}
                </button>
                <button
                  v-if="container.status === 'running'"
                  class="btn-link warning"
                  @click="handleStop(container)"
                >
                  {{ t('停止') }}
                </button>
                <button
                  v-if="container.status === 'running'"
                  class="btn-link"
                  @click="handleRestart(container)"
                >
                  {{ t('重启') }}
                </button>
                <button class="btn-link" @click="handleLogs(container)">{{ t('日志') }}</button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr>
            <td colspan="8" class="loading-cell">{{ t('加载中...') }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <el-dialog
      v-model="logDialogVisible"
      :title="currentContainer ? `${currentContainer.name} - ${t('日志')}` : t('容器日志')"
      width="800px"
      class="log-dialog"
    >
      <div class="log-content" v-loading="logsLoading">
        <pre v-if="containerLogs">{{ containerLogs }}</pre>
        <span v-else class="no-logs">{{ t('暂无日志') }}</span>
      </div>
    </el-dialog>

    <el-dialog
      v-model="createDialogVisible"
      :title="t('创建容器')"
      width="500px"
    >
      <div class="create-form">
        <div class="form-group">
          <label class="form-label">{{ t('容器名称') }}</label>
          <input v-model="createForm.name" class="form-input" :placeholder="t('请输入容器名称')" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ t('镜像') }}</label>
          <input v-model="createForm.image" class="form-input" :placeholder="t('例如: nginx:latest')" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ t('端口映射') }}</label>
          <input v-model="createForm.ports" class="form-input" :placeholder="t('例如: 8080:80')" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ t('环境变量') }}</label>
          <textarea v-model="createForm.env" class="form-textarea" :placeholder="t('每行一个，例如: KEY=value')" rows="3"></textarea>
        </div>
      </div>
      <template #footer>
        <button class="btn-secondary" @click="createDialogVisible = false">{{ t('取消') }}</button>
        <button class="btn-primary" @click="handleCreate" :disabled="createLoading">
          {{ createLoading ? t('创建中...') : t('创建') }}
        </button>
      </template>
    </el-dialog>
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

.btn-link.success {
  color: #67c23a;
}

.btn-link.warning {
  color: #e6a23c;
}

.loading-cell {
  text-align: center;
  padding: 40px;
  color: var(--color-text-secondary);
}

.log-content {
  max-height: 500px;
  overflow: auto;
  background: #1e1e1e;
  padding: 16px;
}

.log-content pre {
  margin: 0;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.no-logs {
  color: #757575;
  font-size: 14px;
}

.create-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.form-input {
  height: 40px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  font-size: 14px;
  color: var(--color-text-primary);
  outline: none;
  transition: border-color 0.15s ease;
}

.form-input:focus {
  border-color: var(--site-context-highlight-color);
}

.form-textarea {
  padding: 12px;
  border: 1px solid var(--color-border);
  font-size: 14px;
  font-family: inherit;
  color: var(--color-text-primary);
  outline: none;
  resize: vertical;
  transition: border-color 0.15s ease;
}

.form-textarea:focus {
  border-color: var(--site-context-highlight-color);
}
</style>
