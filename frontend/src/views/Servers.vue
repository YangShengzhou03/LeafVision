<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { api } from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const serverList = ref([])

const serverTypes = [
  { value: 'linux', label: 'Linux' },
  { value: 'windows', label: 'Windows' },
  { value: 'docker', label: 'Docker' }
]

const formRef = ref(null)
const formData = ref({ name: '', ip: '', port: 22, type: 'linux', tags: [] })

const rules = {
  name: [{ required: true, message: '请输入服务器名称', trigger: 'blur' }],
  ip: [{ required: true, message: '请输入IP地址', trigger: 'blur' }]
}

const fetchServerList = async () => {
  loading.value = true
  try {
    const res = await api.getServerList()
    if (res.code === 200) {
      serverList.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  formData.value = { name: '', ip: '', port: 22, type: 'linux', tags: [] }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    const res = await api.deleteServer(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchServerList()
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (formData.value.id) {
        await api.updateServer(formData.value.id, formData.value)
        ElMessage.success('更新成功')
      } else {
        await api.createServer(formData.value)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchServerList()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleRefresh = () => {
  fetchServerList()
  ElMessage.success('刷新完成')
}

const getTypeLabel = (type) => serverTypes.find(t => t.value === type)?.label || type

onMounted(() => fetchServerList())
</script>

<template>
  <div class="page-container">
    <div class="panel-card">
      <div class="panel-header">
        <span>服务器列表</span>
        <div class="header-actions">
          <el-button size="small" @click="handleRefresh"><el-icon><Refresh /></el-icon> 刷新</el-button>
          <el-button type="primary" size="small" @click="handleAdd"><el-icon><Plus /></el-icon> 添加</el-button>
        </div>
      </div>

      <el-table :data="serverList" v-loading="loading" class="dark-table">
        <el-table-column label="服务器" min-width="180">
          <template #default="{ row }">
            <div class="server-cell">
              <span class="server-name">{{ row.name }}</span>
              <span class="server-ip">{{ row.ip }}:{{ row.port }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }"><span class="type-tag">{{ getTypeLabel(row.type) }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="status-dot" :class="row.status"></span>
            {{ row.status === 'online' ? '在线' : '离线' }}
          </template>
        </el-table-column>
        <el-table-column label="CPU使用率" width="160">
          <template #default="{ row }">
            <div class="progress-wrapper">
              <div class="progress-bar" :style="{ width: (row.cpuUsage || 0) + '%' }"></div>
              <span>{{ row.cpuUsage || 0 }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="内存使用率" width="160">
          <template #default="{ row }">
            <div class="progress-wrapper">
              <div class="progress-bar mem" :style="{ width: (row.memoryUsage || 0) + '%' }"></div>
              <span>{{ row.memoryUsage || 0 }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column prop="uptime" label="运行时间" width="100" align="center" />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑服务器' : '添加服务器'" width="480px" class="dark-dialog">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="90px" label-position="right">
        <el-form-item label="名称" prop="name"><el-input v-model="formData.name" placeholder="输入服务器名称" /></el-form-item>
        <el-form-item label="IP地址" prop="ip"><el-input v-model="formData.ip" placeholder="输入IP地址" /></el-form-item>
        <el-form-item label="端口" prop="port"><el-input-number v-model="formData.port" :min="1" :max="65535" style="width: 100%" /></el-form-item>
        <el-form-item label="类型" prop="type"><el-select v-model="formData.type" placeholder="选择类型" style="width: 100%"><el-option v-for="item in serverTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }

.panel-card {
  background: #1e1e1e;
  border: 1px solid #2a2a2a;
  border-radius: 3px;
  padding: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  color: #ccc;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #2a2a2a;
}

.header-actions { display: flex; gap: 8px; }

.server-cell { display: flex; flex-direction: column; gap: 2px; }
.server-name { font-weight: 500; color: #ddd; }
.server-ip { font-size: 12px; color: #666; }

.type-tag {
  background: rgba(99, 102, 241, 0.2);
  color: #818cf8;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}
.status-dot.online { background: #22c55e; box-shadow: 0 0 6px rgba(34, 197, 94, 0.5); }
.status-dot.offline { background: #ef4444; box-shadow: 0 0 6px rgba(239, 68, 68, 0.5); }

.progress-wrapper { display: flex; align-items: center; gap: 10px; padding: 0 4px; }
.progress-bar {
  height: 5px;
  border-radius: 3px;
  transition: all 0.3s;
  flex: 1;
  background-color: #6366f1;
}
.progress-bar.mem { background-color: #22c55e; }
.progress-wrapper span { color: #999; font-size: 12px; min-width: 32px; text-align: right; }
</style>
