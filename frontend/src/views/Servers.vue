<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { getServerList, createServer, updateServer, deleteServer, refreshServerStatus } from '@/api'
import { ElMessage } from 'element-plus'
import { SERVER_TYPES } from '@/constants'

const { t } = useI18n()
const loading = ref(false)
const refreshing = ref(false)
const dialogVisible = ref(false)
const serverList = ref([])

const formatPercent = (value) => {
  if (value == null) return '0.00'
  return Number(value).toFixed(2)
}

const formData = ref({ name: '', ip: '', port: 9090, type: 'prometheus-master' })

const fetchServerList = async () => {
  loading.value = true
  try {
    const res = await getServerList()
    if (res.code === 200) serverList.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleRefreshStatus = async () => {
  refreshing.value = true
  try {
    const res = await refreshServerStatus()
    if (res.code === 200) {
      ElMessage.success(t('刷新成功'))
      await fetchServerList()
    }
  } catch (error) {
    ElMessage.error(t('刷新失败'))
  } finally {
    refreshing.value = false
  }
}

const handleAdd = () => {
  formData.value = { name: '', ip: '', port: 9090, type: 'prometheus-master' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    const res = await deleteServer(row.id)
    if (res.code === 200) {
      ElMessage.success(t('删除成功'))
      fetchServerList()
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!formData.value.name || !formData.value.ip) {
    ElMessage.warning(t('请填写完整信息'))
    return
  }
  try {
    if (formData.value.id) {
      await updateServer(formData.value.id, formData.value)
      ElMessage.success(t('更新成功'))
    } else {
      await createServer(formData.value)
      ElMessage.success(t('添加成功'))
    }
    dialogVisible.value = false
    fetchServerList()
  } catch (error) {
    console.error(error)
  }
}

const getTypeLabel = (type) => SERVER_TYPES.find(t => t.value === type)?.label || type

onMounted(() => fetchServerList())
</script>

<template>
  <div class="servers-page">
    <div class="page-header">
      <span class="page-title">{{ t('主机列表') }}</span>
      <div class="header-actions">
        <button class="btn-secondary" @click="handleRefreshStatus" :disabled="refreshing">
          <el-icon><Refresh /></el-icon>
          <span>{{ refreshing ? t('刷新中...') : t('刷新状态') }}</span>
        </button>
        <button class="btn-secondary" @click="fetchServerList" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>{{ t('刷新列表') }}</span>
        </button>
        <button class="btn-primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>{{ t('添加') }}</span>
        </button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ t('名称') }}</th>
            <th>{{ t('类型') }}</th>
            <th>{{ t('状态') }}</th>
            <th>{{ t('CPU') }}</th>
            <th>{{ t('内存') }}</th>
            <th>{{ t('版本') }}</th>
            <th>{{ t('运行时间') }}</th>
            <th>{{ t('操作') }}</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="server in serverList" :key="server.id">
            <td>
              <div class="server-info">
                <span class="server-name">{{ server.name }}</span>
                <span class="server-ip">{{ server.ip }}:{{ server.port }}</span>
              </div>
            </td>
            <td>
              <span class="type-badge">{{ getTypeLabel(server.type) }}</span>
            </td>
            <td>
              <span :class="['status-badge', server.status === 'online' ? 'online' : 'offline']">
                {{ server.status === 'online' ? t('在线') : t('离线') }}
              </span>
            </td>
            <td>
              <div class="progress-cell">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: (server.cpuUsage || 0) + '%' }"></div>
                </div>
                <span class="progress-text">{{ formatPercent(server.cpuUsage) }}%</span>
              </div>
            </td>
            <td>
              <div class="progress-cell">
                <div class="progress-bar memory">
                  <div class="progress-fill" :style="{ width: (server.memoryUsage || 0) + '%' }"></div>
                </div>
                <span class="progress-text">{{ formatPercent(server.memoryUsage) }}%</span>
              </div>
            </td>
            <td>{{ server.version }}</td>
            <td>{{ server.uptime }}</td>
            <td>
              <div class="action-btns">
                <button class="btn-link" @click="handleEdit(server)">{{ t('编辑') }}</button>
                <button class="btn-link danger" @click="handleDelete(server)">{{ t('删除') }}</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="loading" class="loading-state">
        <span>{{ t('加载中...') }}</span>
      </div>
      <div v-if="!loading && serverList.length === 0" class="empty-state">
        <span>{{ t('暂无主机') }}</span>
      </div>
    </div>

    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <span class="dialog-title">{{ formData.id ? t('编辑主机') : t('添加主机') }}</span>
          <button class="dialog-close" @click="dialogVisible = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label class="form-label">{{ t('名称') }}</label>
            <input v-model="formData.name" class="form-input" :placeholder="t('请输入名称')" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('IP地址') }}</label>
            <input v-model="formData.ip" class="form-input" :placeholder="t('请输入IP地址')" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('端口') }}</label>
            <input v-model.number="formData.port" type="number" class="form-input" :placeholder="t('端口')" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('类型') }}</label>
            <select v-model="formData.type" class="form-select">
              <option v-for="item in SERVER_TYPES" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-secondary" @click="dialogVisible = false">{{ t('取消') }}</button>
          <button class="btn-primary" @click="handleSubmit">{{ t('确认') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.servers-page {
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

.server-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.server-name {
  font-weight: 700;
  color: var(--color-text-primary);
}

.server-ip {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.type-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  background: #f0f0f0;
  color: var(--color-text-primary);
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.status-badge.online {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.status-badge.offline {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.progress-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 100px;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: #e0e0e0;
}

.progress-bar.memory .progress-fill {
  background: #67c23a;
}

.progress-fill {
  height: 100%;
  background: var(--site-context-highlight-color);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
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

.btn-link.danger {
  color: #f56c6c;
}

.btn-link.danger:hover {
  color: #c45656;
}

.loading-state,
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  width: 480px;
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}

.dialog-title {
  font-size: 16px;
  font-weight: 900;
  color: var(--color-text-primary);
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.dialog-close {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--color-text-secondary);
  cursor: pointer;
  line-height: 1;
}

.dialog-close:hover {
  color: var(--color-text-primary);
}

.dialog-body {
  padding: 24px 20px;
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

.form-input::placeholder {
  color: var(--color-text-muted);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
}
</style>
