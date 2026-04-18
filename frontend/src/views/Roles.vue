<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, createRole, updateRole, deleteRole, getPermissions } from '@/api'

const { t } = useI18n()

const loading = ref(false)
const roleList = ref([])
const permissionList = ref([])
const showModal = ref(false)
const modalTitle = ref('')
const formData = ref({
  id: null,
  roleCode: '',
  roleName: '',
  description: '',
  permissions: [],
  status: 1
})

const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await getRoles()
    if (res.code === 200) {
      roleList.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchPermissions = async () => {
  try {
    const res = await getPermissions()
    if (res.code === 200) {
      permissionList.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  }
}

const handleAdd = () => {
  modalTitle.value = t('添加角色')
  formData.value = { id: null, roleCode: '', roleName: '', description: '', permissions: [], status: 1 }
  showModal.value = true
}

const handleEdit = (role) => {
  modalTitle.value = t('编辑角色')
  formData.value = { ...role }
  showModal.value = true
}

const handleDelete = async (role) => {
  if (role.roleCode === 'ADMIN') {
    ElMessage.warning(t('管理员角色不可删除'))
    return
  }
  try {
    await ElMessageBox.confirm(t('确定删除角色', { name: role.roleName }), t('确认删除'), {
      confirmButtonText: t('确认'),
      cancelButtonText: t('取消'),
      type: 'warning'
    })
    const res = await deleteRole(role.id)
    if (res.code === 200) {
      ElMessage.success(t('删除成功'))
      fetchRoles()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('删除失败'))
    }
  }
}

const handleSubmit = async () => {
  if (!formData.value.roleCode || !formData.value.roleName) {
    ElMessage.warning(t('请填写必填项'))
    return
  }
  try {
    const res = formData.value.id
      ? await updateRole(formData.value)
      : await createRole(formData.value)
    if (res.code === 200) {
      ElMessage.success(formData.value.id ? t('更新成功') : t('创建成功'))
      showModal.value = false
      fetchRoles()
    }
  } catch (error) {
    ElMessage.error(t('操作失败'))
  }
}

const getStatusClass = (status) => {
  return status === 1 ? 'active' : 'inactive'
}

const getStatusText = (status) => {
  return status === 1 ? t('启用') : t('禁用')
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  fetchRoles()
  fetchPermissions()
})
</script>

<template>
  <div class="roles-page">
    <div class="page-header">
      <span class="page-title">{{ t('角色管理') }}</span>
      <div class="header-actions">
        <button class="btn-secondary" @click="fetchRoles" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>{{ t('刷新') }}</span>
        </button>
        <button class="btn-primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>{{ t('添加角色') }}</span>
        </button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ t('角色编码') }}</th>
            <th>{{ t('角色名称') }}</th>
            <th>{{ t('描述') }}</th>
            <th>{{ t('权限数量') }}</th>
            <th>{{ t('状态') }}</th>
            <th>{{ t('创建时间') }}</th>
            <th>{{ t('操作') }}</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="role in roleList" :key="role.id">
            <td class="code-cell">{{ role.roleCode }}</td>
            <td class="name-cell">{{ role.roleName }}</td>
            <td>{{ role.description || '-' }}</td>
            <td>{{ role.permissions?.length || 0 }}</td>
            <td>
              <span :class="['status-badge', getStatusClass(role.status)]">
                {{ getStatusText(role.status) }}
              </span>
            </td>
            <td>{{ formatDateTime(role.createdAt) }}</td>
            <td>
              <div class="action-btns">
                <button class="btn-link" @click="handleEdit(role)">{{ t('编辑') }}</button>
                <button 
                  class="btn-link danger" 
                  @click="handleDelete(role)"
                  :disabled="role.roleCode === 'ADMIN'"
                >{{ t('删除') }}</button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr>
            <td colspan="7" class="loading-cell">{{ t('加载中...') }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <span class="modal-title">{{ modalTitle }}</span>
          <button class="modal-close" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">{{ t('角色编码') }}</label>
            <input 
              v-model="formData.roleCode" 
              type="text" 
              class="form-input" 
              :placeholder="t('请输入角色编码')"
              :disabled="!!formData.id"
            />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('角色名称') }}</label>
            <input v-model="formData.roleName" type="text" class="form-input" :placeholder="t('请输入角色名称')" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('描述') }}</label>
            <input v-model="formData.description" type="text" class="form-input" :placeholder="t('请输入描述')" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('分配权限') }}</label>
            <div class="permission-list">
              <label 
                v-for="perm in permissionList" 
                :key="perm.permissionCode" 
                class="permission-item"
              >
                <input
                  type="checkbox"
                  :value="perm.permissionCode"
                  v-model="formData.permissions"
                />
                <span>{{ perm.permissionName }}</span>
              </label>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('状态') }}</label>
            <select v-model="formData.status" class="form-select">
              <option :value="1">{{ t('启用') }}</option>
              <option :value="0">{{ t('禁用') }}</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showModal = false">{{ t('取消') }}</button>
          <button class="btn-primary" @click="handleSubmit">{{ t('确认') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.roles-page {
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

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

.code-cell {
  font-family: monospace;
  font-weight: 700;
  color: var(--site-context-highlight-color);
}

.name-cell {
  font-weight: 700;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.status-badge.active {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.status-badge.inactive {
  background: rgba(245, 108, 108, 0.1);
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

.btn-link:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

.modal-overlay {
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

.modal-content {
  background: #ffffff;
  width: 100%;
  max-width: 560px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}

.modal-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--color-text-secondary);
  cursor: pointer;
  line-height: 1;
}

.modal-close:hover {
  color: var(--color-text-primary);
}

.modal-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.form-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.permission-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--color-border);
  max-height: 200px;
  overflow-y: auto;
}

.permission-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  min-width: 120px;
}

.permission-item input[type="checkbox"] {
  width: 14px;
  height: 14px;
  accent-color: var(--site-context-highlight-color);
}

.permission-item span {
  font-size: 13px;
  color: var(--color-text-primary);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
}
</style>
