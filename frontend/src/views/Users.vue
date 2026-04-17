<script setup>
import { ref, onMounted } from 'vue'
import { Refresh, Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, getRoles } from '@/api'

const loading = ref(false)
const userList = ref([])
const searchQuery = ref('')
const showModal = ref(false)
const modalTitle = ref('添加用户')
const formData = ref({
  id: null,
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  roleId: null,
  status: 1
})

const roleOptions = ref([])

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const fetchRoles = async () => {
  try {
    const res = await getRoles()
    if (res.code === 200) {
      roleOptions.value = (res.data || []).map(r => ({
        label: r.roleName,
        value: r.id
      }))
    }
  } catch (error) {
    console.error(error)
  }
}

const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await getUserList()
    if (res.code === 200) {
      userList.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const filteredUsers = ref([])

const applySearch = () => {
  if (!searchQuery.value) {
    filteredUsers.value = [...userList.value]
  } else {
    const query = searchQuery.value.toLowerCase()
    filteredUsers.value = userList.value.filter(u =>
      u.name.toLowerCase().includes(query) ||
      u.email.toLowerCase().includes(query)
    )
  }
}

const handleAdd = () => {
  modalTitle.value = '添加用户'
  formData.value = { id: null, username: '', password: '', name: '', email: '', phone: '', roleId: roleOptions.value[0]?.value || null, status: 1 }
  showModal.value = true
}

const handleEdit = (user) => {
  modalTitle.value = '编辑用户'
  formData.value = { ...user }
  showModal.value = true
}

const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 ${user.name}？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteUser(user.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchUserList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    const res = formData.value.id
      ? await updateUser(formData.value)
      : await createUser(formData.value)
    if (res.code === 200) {
      ElMessage.success(formData.value.id ? '更新成功' : '创建成功')
      showModal.value = false
      fetchUserList()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getRoleText = (roleId) => {
  const item = roleOptions.value.find(r => r.value === roleId)
  return item ? item.label : '-'
}

const getStatusClass = (status) => {
  return status === 1 ? 'active' : 'inactive'
}

const getStatusText = (status) => {
  return status === 1 ? '启用' : '禁用'
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

onMounted(async () => {
  await fetchRoles()
  await fetchUserList()
  applySearch()
})
</script>

<template>
  <div class="users-page">
    <div class="page-header">
      <span class="page-title">用户管理</span>
      <div class="header-actions">
        <button class="btn-secondary" @click="fetchUserList" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </button>
        <button class="btn-primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>添加用户</span>
        </button>
      </div>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索用户名或邮箱"
          class="search-input"
          @input="applySearch"
        />
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>最后登录</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="user in filteredUsers" :key="user.id">
            <td>
              <div class="user-info">
                <span class="user-avatar">{{ user.name?.charAt(0)?.toUpperCase() }}</span>
                <span class="user-name">{{ user.name }}</span>
              </div>
            </td>
            <td>{{ user.email }}</td>
            <td>
              <span class="role-badge">{{ getRoleText(user.roleId) }}</span>
            </td>
            <td>
              <span :class="['status-badge', getStatusClass(user.status)]">
                {{ getStatusText(user.status) }}
              </span>
            </td>
            <td>{{ formatDateTime(user.createdAt) }}</td>
            <td>{{ formatDateTime(user.lastLogin) }}</td>
            <td>
              <div class="action-btns">
                <button class="btn-link" @click="handleEdit(user)">编辑</button>
                <button class="btn-link danger" @click="handleDelete(user)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr>
            <td colspan="7" class="loading-cell">加载中...</td>
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
            <label class="form-label">登录账号</label>
            <input v-model="formData.username" type="text" class="form-input" placeholder="输入登录账号" />
          </div>
          <div v-if="!formData.id" class="form-group">
            <label class="form-label">密码</label>
            <input v-model="formData.password" type="password" class="form-input" placeholder="输入密码" />
          </div>
          <div class="form-group">
            <label class="form-label">姓名</label>
            <input v-model="formData.name" type="text" class="form-input" placeholder="输入姓名" />
          </div>
          <div class="form-group">
            <label class="form-label">邮箱</label>
            <input v-model="formData.email" type="email" class="form-input" placeholder="输入邮箱" />
          </div>
          <div class="form-group">
            <label class="form-label">角色</label>
            <select v-model="formData.roleId" class="form-select">
              <option v-for="item in roleOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">状态</label>
            <select v-model="formData.status" class="form-select">
              <option v-for="item in statusOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showModal = false">取消</button>
          <button class="btn-primary" @click="handleSubmit">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.users-page {
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
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid var(--color-border);
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

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: var(--site-context-highlight-color);
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
}

.user-name {
  font-weight: 700;
}

.role-badge {
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
  max-width: 480px;
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

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
}
</style>
