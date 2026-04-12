<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Edit, Delete, Refresh, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'

const serverList = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('添加服务器')
const serverForm = ref({
  id: null,
  name: '',
  ip: '',
  port: 9090,
  type: 'prometheus-node'
})

const serverTypes = [
  { label: 'Prometheus 主节点', value: 'prometheus-master' },
  { label: 'Prometheus 节点', value: 'prometheus-node' },
  { label: 'Alertmanager', value: 'alertmanager' }
]

const fetchServerList = async () => {
  loading.value = true
  try {
    const res = await api.getServerList()
    if (res.code === 200) {
      serverList.value = res.data || []
    }
  } catch (error) {
    console.error('获取服务器列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加服务器'
  serverForm.value = { id: null, name: '', ip: '', port: 9090, type: 'prometheus-node' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑服务器'
  serverForm.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除服务器 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.deleteServer(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchServerList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除服务器失败:', error)
    }
  }
}

const handleRefresh = async () => {
  try {
    const res = await api.refreshStatus()
    if (res.code === 200) {
      ElMessage.success('服务器状态已刷新')
      fetchServerList()
    }
  } catch (error) {
    console.error('刷新状态失败:', error)
  }
}

const handleView = (row) => {
  ElMessageBox.alert(
    `<p><strong>名称:</strong> ${row.name}</p>
     <p><strong>IP:</strong> ${row.ip}</p>
     <p><strong>端口:</strong> ${row.port}</p>
     <p><strong>类型:</strong> ${serverTypes.find(t => t.value === row.type)?.label || row.type}</p>
     <p><strong>状态:</strong> ${row.status === 'online' ? '在线' : '离线'}</p>
     <p><strong>版本:</strong> ${row.version || '-'}</p>
     <p><strong>运行时间:</strong> ${row.uptime || '-'}</p>
     <p><strong>CPU使用率:</strong> ${row.cpuUsage ? row.cpuUsage.toFixed(1) + '%' : '-'}</p>
     <p><strong>内存使用率:</strong> ${row.memoryUsage ? row.memoryUsage.toFixed(1) + '%' : '-'}</p>`,
    '服务器详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  )
}

const handleSubmit = async () => {
  if (!serverForm.value.name || !serverForm.value.ip) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    let res
    if (serverForm.value.id) {
      res = await api.updateServer(serverForm.value.id, serverForm.value)
      if (res.code === 200) {
        ElMessage.success('更新成功')
      } else {
        ElMessage.error(res.message || '更新失败')
        return
      }
    } else {
      res = await api.addServer(serverForm.value)
      if (res.code === 200) {
        ElMessage.success('添加成功')
      } else {
        ElMessage.error(res.message || '添加失败')
        return
      }
    }
    dialogVisible.value = false
    fetchServerList()
  } catch (error) {
    console.error('保存服务器失败:', error)
  }
}

onMounted(() => {
  fetchServerList()
})
</script>

<template>
  <div class="servers-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务器管理</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="handleAdd">添加服务器</el-button>
            <el-button :icon="Refresh" @click="handleRefresh">刷新状态</el-button>
          </div>
        </div>
      </template>

      <el-table :data="serverList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="服务器名称" />
        <el-table-column prop="ip" label="IP 地址" />
        <el-table-column prop="port" label="端口" width="100" />
        <el-table-column prop="type" label="类型">
          <template #default="{ row }">
            <el-tag>{{ serverTypes.find(t => t.value === row.type)?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'online' ? 'success' : 'danger'" size="small">
              {{ row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="100">
          <template #default="{ row }">
            {{ row.version || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="cpuUsage" label="CPU" width="120">
          <template #default="{ row }">
            <el-progress 
              v-if="row.cpuUsage" 
              :percentage="Math.round(row.cpuUsage)" 
              :color="row.cpuUsage > 80 ? '#F56C6C' : '#409EFF'" 
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="memoryUsage" label="内存" width="120">
          <template #default="{ row }">
            <el-progress 
              v-if="row.memoryUsage" 
              :percentage="Math.round(row.memoryUsage)" 
              :color="row.memoryUsage > 80 ? '#F56C6C' : '#67C23A'" 
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">详情</el-button>
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="serverForm" label-width="100px">
        <el-form-item label="服务器名称" required>
          <el-input v-model="serverForm.name" placeholder="请输入服务器名称" />
        </el-form-item>
        <el-form-item label="IP 地址" required>
          <el-input v-model="serverForm.ip" placeholder="请输入 IP 地址，如 192.168.1.100" />
        </el-form-item>
        <el-form-item label="端口" required>
          <el-input-number v-model="serverForm.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        <el-form-item label="服务器类型" required>
          <el-select v-model="serverForm.type" style="width: 100%">
            <el-option
              v-for="item in serverTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.servers-page {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}
</style>
