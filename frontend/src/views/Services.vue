<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceList, createService, updateService, deleteService } from '@/api'

const { t } = useI18n()
const loading = ref(false)
const serviceList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const serviceForm = ref({
  name: '',
  address: '',
  type: '',
  description: ''
})
const editingId = ref(null)

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
  if (status === 'healthy') return t('健康')
  if (status === 'unhealthy') return t('不健康')
  if (status === 'degraded') return t('降级')
  return t('未知')
}

const handleAdd = () => {
  dialogTitle.value = t('添加服务')
  editingId.value = null
  serviceForm.value = {
    name: '',
    address: '',
    type: '',
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = (service) => {
  dialogTitle.value = t('编辑服务')
  editingId.value = service.id
  serviceForm.value = {
    name: service.name,
    address: service.address,
    type: service.type,
    description: service.description || ''
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!serviceForm.value.name) {
    ElMessage.warning(t('服务名称必填'))
    return
  }
  
  try {
    let res
    if (editingId.value) {
      res = await updateService(editingId.value, serviceForm.value)
    } else {
      res = await createService(serviceForm.value)
    }
    
    if (res.code === 200) {
      ElMessage.success(editingId.value ? t('更新成功') : t('添加成功'))
      dialogVisible.value = false
      fetchServiceList()
    } else {
      ElMessage.error(res.message || t('操作失败'))
    }
  } catch (error) {
    ElMessage.error(t('操作失败'))
  }
}

const handleDelete = async (service) => {
  try {
    await ElMessageBox.confirm(t('确定删除此服务吗？'), t('提示'), {
      type: 'warning'
    })
    const res = await deleteService(service.id)
    if (res.code === 200) {
      ElMessage.success(t('删除成功'))
      fetchServiceList()
    } else {
      ElMessage.error(res.message || t('删除失败'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('删除失败'))
    }
  }
}

onMounted(() => fetchServiceList())
</script>

<template>
  <div class="services-page">
    <div class="page-header">
      <span class="page-title">{{ t('服务管理') }}</span>
      <div class="header-actions">
        <button class="btn-secondary" @click="fetchServiceList" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>{{ t('刷新') }}</span>
        </button>
        <button class="btn-primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>{{ t('添加服务') }}</span>
        </button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ t('服务名称') }}</th>
            <th>{{ t('服务类型') }}</th>
            <th>{{ t('状态') }}</th>
            <th>{{ t('实例数') }}</th>
            <th>{{ t('健康实例') }}</th>
            <th>{{ t('响应时间') }}</th>
            <th>{{ t('错误率') }}</th>
            <th>{{ t('操作') }}</th>
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
            <td>{{ service.instanceCount || service.instances || 1 }}</td>
            <td>{{ service.healthyCount || service.healthyInstances || 0 }} / {{ service.instanceCount || service.instances || 1 }}</td>
            <td>{{ service.responseTime ? service.responseTime.toFixed(2) + 'ms' : '-' }}</td>
            <td>
              <span :class="['error-rate', { high: (service.errorRate || 0) > 5 }]">
                {{ (service.errorRate || 0).toFixed(2) }}%
              </span>
            </td>
            <td>
              <div class="action-btns">
                <button class="btn-link" @click="handleEdit(service)">{{ t('编辑') }}</button>
                <button class="btn-link danger" @click="handleDelete(service)">{{ t('删除') }}</button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="serviceForm" label-width="80px">
        <el-form-item :label="t('服务名称')" required>
          <el-input v-model="serviceForm.name" :placeholder="t('请输入服务名称')" />
        </el-form-item>
        <el-form-item :label="t('地址')">
          <el-input v-model="serviceForm.address" :placeholder="t('请输入服务地址')" />
        </el-form-item>
        <el-form-item :label="t('服务类型')">
          <el-select v-model="serviceForm.type" style="width: 100%">
            <el-option label="HTTP" value="http" />
            <el-option label="TCP" value="tcp" />
            <el-option label="gRPC" value="grpc" />
            <el-option label="MySQL" value="mysql" />
            <el-option label="Redis" value="redis" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="MongoDB" value="mongodb" />
            <el-option label="Kafka" value="kafka" />
            <el-option label="RabbitMQ" value="rabbitmq" />
            <el-option :label="t('其他')" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('描述')">
          <el-input v-model="serviceForm.description" type="textarea" rows="3" :placeholder="t('请输入服务描述')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('取消') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ t('确认') }}</el-button>
      </template>
    </el-dialog>
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
