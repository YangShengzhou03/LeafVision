<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getRoles, updateRolePermissions } from '@/api'

const { t } = useI18n()

const loading = ref(false)
const roleList = ref([])
const selectedRole = ref(null)
const permissionTree = ref([
  {
    module: 'monitor',
    label: '监控中心',
    children: [
      { key: 'DASHBOARD', label: '仪表盘' },
      { key: 'REALTIME', label: '实时监控' },
      { key: 'SERVERS', label: '服务器管理' },
      { key: 'CONTAINERS', label: '容器管理' },
      { key: 'SERVICES', label: '服务管理' },
      { key: 'METRICS', label: '指标查询' }
    ]
  },
  {
    module: 'observability',
    label: '可观测性',
    children: [
      { key: 'LOGS', label: '日志管理' },
      { key: 'TRACES', label: '链路追踪' }
    ]
  },
  {
    module: 'alert',
    label: '告警管理',
    children: [
      { key: 'ALERTS', label: '告警列表' },
      { key: 'ALERT_RULES', label: '告警规则' }
    ]
  },
  {
    module: 'system',
    label: '系统管理',
    children: [
      { key: 'USERS', label: '用户管理' },
      { key: 'ROLES', label: '角色管理' },
      { key: 'PERMISSIONS', label: '权限管理' },
      { key: 'AUDIT_LOGS', label: '审计日志' },
      { key: 'SETTINGS', label: '系统设置' }
    ]
  }
])

const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await getRoles()
    if (res.code === 200) {
      roleList.value = res.data || []
      if (roleList.value.length > 0) {
        selectedRole.value = roleList.value[0]
      }
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleRoleSelect = (role) => {
  selectedRole.value = role
}

const handlePermissionChange = (permKey, checked) => {
  if (!selectedRole.value) return
  if (!selectedRole.value.permissions) {
    selectedRole.value.permissions = []
  }
  if (checked) {
    if (!selectedRole.value.permissions.includes(permKey)) {
      selectedRole.value.permissions.push(permKey)
    }
  } else {
    const index = selectedRole.value.permissions.indexOf(permKey)
    if (index > -1) {
      selectedRole.value.permissions.splice(index, 1)
    }
  }
}

const hasPermission = (permKey) => {
  return selectedRole.value?.permissions?.includes(permKey) || false
}

const handleSave = async () => {
  if (!selectedRole.value) return
  try {
    const res = await updateRolePermissions(selectedRole.value.id, selectedRole.value.permissions)
    if (res.code === 200) {
      ElMessage.success(t('保存成功'))
    }
  } catch (error) {
    ElMessage.error(t('保存失败'))
  }
}

onMounted(() => fetchRoles())
</script>

<template>
  <div class="permissions-page">
    <div class="page-header">
      <span class="page-title">{{ t('权限管理') }}</span>
      <div class="header-actions">
        <button class="btn-secondary" @click="fetchRoles" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>{{ t('刷新') }}</span>
        </button>
        <button class="btn-primary" @click="handleSave" :disabled="!selectedRole">
          <span>{{ t('保存配置') }}</span>
        </button>
      </div>
    </div>

    <div class="content-grid">
      <div class="roles-card">
        <div class="card-header">
          <span class="card-title">{{ t('角色列表') }}</span>
        </div>
        <div class="card-body">
          <div class="role-list">
            <div
              v-for="role in roleList"
              :key="role.id"
              :class="['role-item', { active: selectedRole?.id === role.id }]"
              @click="handleRoleSelect(role)"
            >
              <span class="role-name">{{ role.roleName }}</span>
              <span class="role-desc">{{ role.description }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="permissions-card">
        <div class="card-header">
          <span class="card-title">{{ t('权限设置') }}</span>
          <span v-if="selectedRole" class="selected-role">{{ t('当前角色') }}: {{ selectedRole.roleName }}</span>
        </div>
        <div class="card-body">
          <div v-if="selectedRole" class="permission-tree">
            <div v-for="module in permissionTree" :key="module.module" class="permission-module">
              <div class="module-header">
                <span class="module-label">{{ t(module.label) }}</span>
              </div>
              <div class="module-permissions">
                <label
                  v-for="perm in module.children"
                  :key="perm.key"
                  class="permission-item"
                >
                  <input
                    type="checkbox"
                    :checked="hasPermission(perm.key)"
                    @change="e => handlePermissionChange(perm.key, e.target.checked)"
                  />
                  <span class="permission-label">{{ t(perm.label) }}</span>
                </label>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <span class="empty-text">{{ t('请选择角色') }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.permissions-page {
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

.content-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
}

.roles-card,
.permissions-card {
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}

.card-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.selected-role {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

.card-body {
  padding: 20px;
}

.role-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.role-item {
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all 0.15s ease;
}

.role-item:hover {
  border-color: var(--color-text-secondary);
}

.role-item.active {
  border-color: var(--site-context-highlight-color);
  background: rgba(28, 105, 212, 0.05);
}

.role-name {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.role-desc {
  display: block;
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

.permission-tree {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.permission-module {
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 20px;
}

.permission-module:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.module-header {
  margin-bottom: 12px;
}

.module-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.module-permissions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.permission-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.permission-item input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: var(--site-context-highlight-color);
}

.permission-label {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-primary);
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-text {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
