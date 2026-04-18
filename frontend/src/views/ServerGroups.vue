<script setup>
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getServerGroupTree, 
  createServerGroup, 
  updateServerGroup, 
  deleteServerGroup,
  getServersByGroup,
  addServerToGroup,
  removeServerFromGroup
} from '@/api'
import { getServers } from '@/api'

const { t } = useI18n()

const loading = ref(false)
const groups = ref([])
const servers = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({
  id: null,
  name: '',
  description: '',
  parentId: 0
})

const selectedGroup = ref(null)
const groupServers = ref([])

const fetchGroups = async () => {
  loading.value = true
  try {
    const res = await getServerGroupTree()
    groups.value = res.data || []
  } catch (error) {
    ElMessage.error(t('获取分组失败'))
  } finally {
    loading.value = false
  }
}

const fetchServers = async () => {
  try {
    const res = await getServers()
    servers.value = res.data || []
  } catch (error) {
    console.error(t('获取服务器失败'), error)
  }
}

const handleCreate = (parentId = 0) => {
  dialogTitle.value = t('新建分组')
  form.value = {
    id: null,
    name: '',
    description: '',
    parentId: parentId
  }
  dialogVisible.value = true
}

const handleEdit = (group) => {
  dialogTitle.value = t('编辑分组')
  form.value = {
    id: group.id,
    name: group.name,
    description: group.description,
    parentId: group.parentId || 0
  }
  dialogVisible.value = true
}

const handleDelete = async (group) => {
  try {
    await ElMessageBox.confirm(t('确定删除分组', { name: group.name }), t('提示'), {
      type: 'warning'
    })
    await deleteServerGroup(group.id)
    ElMessage.success(t('删除成功'))
    fetchGroups()
    if (selectedGroup.value?.id === group.id) {
      selectedGroup.value = null
      groupServers.value = []
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('删除失败'))
    }
  }
}

const handleSubmit = async () => {
  if (!form.value.name) {
    ElMessage.warning(t('请输入分组名称'))
    return
  }
  try {
    if (form.value.id) {
      await updateServerGroup(form.value.id, form.value)
      ElMessage.success(t('更新成功'))
    } else {
      await createServerGroup(form.value)
      ElMessage.success(t('创建成功'))
    }
    dialogVisible.value = false
    fetchGroups()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || t('操作失败'))
  }
}

const handleSelectGroup = async (group) => {
  selectedGroup.value = group
  try {
    const res = await getServersByGroup(group.id)
    groupServers.value = res.data || []
  } catch (error) {
    groupServers.value = []
  }
}

const handleAddServer = async (server) => {
  if (!selectedGroup.value) return
  try {
    await addServerToGroup(selectedGroup.value.id, server.id)
    ElMessage.success(t('添加成功'))
    handleSelectGroup(selectedGroup.value)
  } catch (error) {
    ElMessage.error(t('添加失败'))
  }
}

const handleRemoveServer = async (server) => {
  if (!selectedGroup.value) return
  try {
    await removeServerFromGroup(selectedGroup.value.id, server.id)
    ElMessage.success(t('移除成功'))
    handleSelectGroup(selectedGroup.value)
  } catch (error) {
    ElMessage.error(t('移除失败'))
  }
}

const availableServers = computed(() => {
  if (!selectedGroup.value) return []
  const groupServerIds = groupServers.value.map(s => s.id)
  return servers.value.filter(s => !groupServerIds.includes(s.id))
})

onMounted(() => {
  fetchGroups()
  fetchServers()
})
</script>

<template>
  <div class="server-groups-page">
    <div class="page-header">
      <span class="page-title">{{ t('服务器分组') }}</span>
      <div class="header-actions">
        <button class="btn-primary" @click="handleCreate()">{{ t('新建分组') }}</button>
      </div>
    </div>

    <div class="page-content">
      <div class="group-tree">
        <div class="tree-header">
          <span>{{ t('分组列表') }}</span>
        </div>
        <div class="tree-content">
          <div v-if="loading" class="loading-state">{{ t('加载中...') }}</div>
          <div v-else-if="groups.length === 0" class="empty-state">{{ t('暂无分组') }}</div>
          <div v-else class="group-list">
            <template v-for="group in groups" :key="group.id">
              <div 
                :class="['group-item', { active: selectedGroup?.id === group.id }]"
                @click="handleSelectGroup(group)"
              >
                <div class="group-info">
                  <span class="group-name">{{ group.name }}</span>
                  <span class="group-desc">{{ group.description || t('暂无描述') }}</span>
                </div>
                <div class="group-actions">
                  <button class="btn-link" @click.stop="handleCreate(group.id)">{{ t('添加子分组') }}</button>
                  <button class="btn-link" @click.stop="handleEdit(group)">{{ t('编辑') }}</button>
                  <button class="btn-link danger" @click.stop="handleDelete(group)">{{ t('删除') }}</button>
                </div>
              </div>
              <template v-if="group.children && group.children.length > 0">
                <div 
                  v-for="child in group.children" 
                  :key="child.id"
                  :class="['group-item child', { active: selectedGroup?.id === child.id }]"
                  @click="handleSelectGroup(child)"
                >
                  <div class="group-info">
                    <span class="group-name">{{ child.name }}</span>
                    <span class="group-desc">{{ child.description || t('暂无描述') }}</span>
                  </div>
                  <div class="group-actions">
                    <button class="btn-link" @click.stop="handleEdit(child)">{{ t('编辑') }}</button>
                    <button class="btn-link danger" @click.stop="handleDelete(child)">{{ t('删除') }}</button>
                  </div>
                </div>
              </template>
            </template>
          </div>
        </div>
      </div>

      <div class="group-detail">
        <div v-if="!selectedGroup" class="empty-detail">
          <span>{{ t('请选择分组') }}</span>
        </div>
        <template v-else>
          <div class="detail-header">
            <span class="detail-title">{{ selectedGroup.name }}</span>
            <span class="detail-desc">{{ selectedGroup.description || t('暂无描述') }}</span>
          </div>
          
          <div class="detail-section">
            <div class="section-header">
              <span>{{ t('分组服务器') }} ({{ groupServers.length }})</span>
            </div>
            <div class="server-list">
              <div v-if="groupServers.length === 0" class="empty-servers">
                {{ t('分组暂无服务器') }}
              </div>
              <div v-for="server in groupServers" :key="server.id" class="server-item">
                <div class="server-info">
                  <span class="server-name">{{ server.name }}</span>
                  <span class="server-ip">{{ server.ip }}:{{ server.port }}</span>
                </div>
                <button class="btn-link danger" @click="handleRemoveServer(server)">{{ t('移除') }}</button>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-header">
              <span>{{ t('可用服务器') }}</span>
            </div>
            <div class="server-list">
              <div v-if="availableServers.length === 0" class="empty-servers">
                {{ t('暂无可用服务器') }}
              </div>
              <div v-for="server in availableServers" :key="server.id" class="server-item">
                <div class="server-info">
                  <span class="server-name">{{ server.name }}</span>
                  <span class="server-ip">{{ server.ip }}:{{ server.port }}</span>
                </div>
                <button class="btn-link" @click="handleAddServer(server)">{{ t('添加') }}</button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <span class="dialog-title">{{ dialogTitle }}</span>
          <button class="dialog-close" @click="dialogVisible = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label class="form-label">{{ t('分组名称') }}</label>
            <input v-model="form.name" class="form-input" :placeholder="t('请输入分组名称')" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('描述') }}</label>
            <textarea v-model="form.description" class="form-textarea" rows="3" :placeholder="t('请输入描述')"></textarea>
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
.server-groups-page {
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
  border-color: var(--site-context-highlight-color);
  color: var(--site-context-highlight-color);
}

.btn-link {
  background: none;
  border: none;
  color: var(--site-context-highlight-color);
  font-size: 13px;
  font-weight: 400;
  cursor: pointer;
  padding: 0;
  transition: color 0.15s ease;
}

.btn-link:hover {
  color: var(--site-context-focus-color);
}

.btn-link.danger {
  color: #f56c6c;
}

.btn-link.danger:hover {
  color: #e64242;
}

.page-content {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

.group-tree {
  width: 400px;
  background: #ffffff;
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
}

.tree-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
  font-weight: 700;
  font-size: 13px;
  color: var(--color-text-primary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.tree-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.loading-state, .empty-state {
  padding: 40px;
  text-align: center;
  color: var(--color-text-muted);
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-item {
  padding: 12px;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all 0.15s ease;
  background: #ffffff;
}

.group-item:hover {
  border-color: var(--site-context-highlight-color);
}

.group-item.active {
  border-color: var(--site-context-highlight-color);
  background: rgba(28, 105, 212, 0.05);
}

.group-item.child {
  margin-left: 24px;
}

.group-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-primary);
}

.group-desc {
  font-size: 12px;
  color: var(--color-text-muted);
}

.group-actions {
  margin-top: 8px;
  display: flex;
  gap: 12px;
}

.group-detail {
  flex: 1;
  background: #ffffff;
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
}

.empty-detail {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
}

.detail-header {
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
}

.detail-title {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.detail-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.detail-section {
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
}

.section-header {
  font-weight: 700;
  font-size: 13px;
  color: var(--color-text-primary);
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.server-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.empty-servers {
  padding: 20px;
  text-align: center;
  color: var(--color-text-muted);
  background: var(--color-surface-dark);
  border: 1px solid var(--color-border);
}

.server-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid var(--color-border);
}

.server-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.server-name {
  font-weight: 500;
  font-size: 14px;
  color: var(--color-text-primary);
}

.server-ip {
  font-size: 12px;
  color: var(--color-text-muted);
  font-family: monospace;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: #ffffff;
  width: 480px;
  max-width: 90vw;
  border: 1px solid var(--color-border);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
}

.dialog-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.dialog-close {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.dialog-close:hover {
  color: var(--color-text-primary);
}

.dialog-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  font-size: 14px;
  color: var(--color-text-primary);
  background: #ffffff;
  box-sizing: border-box;
  transition: border-color 0.15s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--site-context-highlight-color);
}

.form-input::placeholder {
  color: var(--color-text-muted);
}

.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  font-size: 14px;
  color: var(--color-text-primary);
  background: #ffffff;
  box-sizing: border-box;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
  transition: border-color 0.15s ease;
}

.form-textarea:focus {
  outline: none;
  border-color: var(--site-context-highlight-color);
}

.form-textarea::placeholder {
  color: var(--color-text-muted);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid var(--color-border);
}
</style>
