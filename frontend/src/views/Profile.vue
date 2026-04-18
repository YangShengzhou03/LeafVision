<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo, changePassword } from '@/api'

const { t } = useI18n()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const activeTab = ref('profile')

const profileForm = ref({
  name: '',
  email: '',
  phone: ''
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const fetchUserInfo = async () => {
  loading.value = true
  try {
    const res = await getUserInfo(userStore.username)
    if (res.code === 200 && res.data) {
      profileForm.value.name = res.data.name || ''
      profileForm.value.email = res.data.email || ''
      profileForm.value.phone = res.data.phone || ''
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSaveProfile = async () => {
  saving.value = true
  try {
    const res = await updateUserInfo({
      username: userStore.username,
      name: profileForm.value.name,
      email: profileForm.value.email,
      phone: profileForm.value.phone
    })
    if (res.code === 200) {
      ElMessage.success(t('保存成功'))
      userStore.userInfo.name = profileForm.value.name
      userStore.userInfo.email = profileForm.value.email
      localStorage.setItem('user', JSON.stringify(userStore.userInfo))
    } else {
      ElMessage.error(res.message || t('保存失败'))
    }
  } catch (error) {
    ElMessage.error(t('保存失败'))
  } finally {
    saving.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordForm.value.currentPassword || !passwordForm.value.newPassword) {
    ElMessage.warning(t('请填写完整密码信息'))
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning(t('两次输入的新密码不一致'))
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning(t('密码长度不能少于6位'))
    return
  }

  saving.value = true
  try {
    const res = await changePassword({
      username: userStore.username,
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    })
    if (res.code === 200) {
      ElMessage.success(t('密码修改成功'))
      passwordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    } else {
      ElMessage.error(res.message || t('密码修改失败'))
    }
  } catch (error) {
    ElMessage.error(t('密码修改失败'))
  } finally {
    saving.value = false
  }
}

onMounted(() => fetchUserInfo())
</script>

<template>
  <div class="profile-page">
    <div class="page-header">
      <span class="page-title">{{ t('个人设置') }}</span>
    </div>

    <div class="profile-content">
      <div class="tabs-sidebar">
        <button
          :class="['tab-item', { active: activeTab === 'profile' }]"
          @click="activeTab = 'profile'"
        >
          {{ t('基本信息') }}
        </button>
        <button
          :class="['tab-item', { active: activeTab === 'password' }]"
          @click="activeTab = 'password'"
        >
          {{ t('修改密码') }}
        </button>
      </div>

      <div class="profile-panel">
        <div v-if="activeTab === 'profile'" class="panel-content">
          <div class="panel-title">{{ t('基本信息') }}</div>
          <div class="form-group">
            <label class="form-label">{{ t('用户名') }}</label>
            <input :value="userStore.username" type="text" class="form-input" disabled />
            <span class="form-hint">{{ t('用户名不可修改') }}</span>
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('姓名') }}</label>
            <input v-model="profileForm.name" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('邮箱') }}</label>
            <input v-model="profileForm.email" type="email" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('手机号') }}</label>
            <input v-model="profileForm.phone" type="tel" class="form-input" />
          </div>
          <div class="form-actions">
            <button class="btn-primary" @click="handleSaveProfile" :disabled="saving">
              {{ saving ? t('保存中...') : t('保存修改') }}
            </button>
          </div>
        </div>

        <div v-if="activeTab === 'password'" class="panel-content">
          <div class="panel-title">{{ t('修改密码') }}</div>
          <div class="form-group">
            <label class="form-label">{{ t('当前密码') }}</label>
            <input v-model="passwordForm.currentPassword" type="password" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('新密码') }}</label>
            <input v-model="passwordForm.newPassword" type="password" class="form-input" />
            <span class="form-hint">{{ t('密码长度不能少于6位') }}</span>
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('确认新密码') }}</label>
            <input v-model="passwordForm.confirmPassword" type="password" class="form-input" />
          </div>
          <div class="form-actions">
            <button class="btn-primary" @click="handleChangePassword" :disabled="saving">
              {{ saving ? t('修改中...') : t('修改密码') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
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

.profile-content {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 16px;
}

.tabs-sidebar {
  background: #ffffff;
  border: 1px solid var(--color-border);
  padding: 8px 0;
}

.tab-item {
  display: block;
  width: 100%;
  padding: 12px 20px;
  background: transparent;
  border: none;
  text-align: left;
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-secondary);
  cursor: pointer;
  letter-spacing: 0.02em;
  transition: all 0.15s ease;
}

.tab-item:hover {
  color: var(--color-text-primary);
  background: #fafafa;
}

.tab-item.active {
  color: var(--site-context-highlight-color);
  background: rgba(28, 105, 212, 0.05);
  border-left: 3px solid var(--site-context-highlight-color);
}

.profile-panel {
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.panel-content {
  padding: 24px;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
  letter-spacing: 0.02em;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.form-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.form-input {
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

.form-input:focus {
  border-color: var(--site-context-highlight-color);
}

.form-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.form-hint {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

.form-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border);
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
  padding: 10px 20px;
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

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
}
</style>
