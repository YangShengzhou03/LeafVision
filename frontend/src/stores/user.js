import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUser, clearAuth, getToken } from '@/utils/storage'
import { login as loginApi, logout as logoutApi } from '@/api/modules/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(getUser())

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const name = computed(() => userInfo.value?.name || '')
  const role = computed(() => userInfo.value?.role || '')
  const permissions = computed(() => userInfo.value?.permissions || [])

  const login = async (usernameInput, password, remember = false) => {
    try {
      const res = await loginApi({ username: usernameInput, password })
      
      if (res.code !== 200) {
        return { success: false, message: res.message || '登录失败' }
      }

      const userData = res.data
      const newToken = 'token-' + Date.now() + '-' + Math.random().toString(36).substr(2)

      token.value = newToken
      userInfo.value = userData

      const storage = remember ? localStorage : sessionStorage
      storage.setItem('token', newToken)
      storage.setItem('user', JSON.stringify(userData))

      return { success: true, user: userData }
    } catch (error) {
      return { success: false, message: '网络错误，请稍后重试' }
    }
  }

  const logout = async () => {
    try {
      await logoutApi()
    } catch (e) {
    }
    token.value = null
    userInfo.value = null
    clearAuth()
  }

  const hasPermission = (permissionCode) => {
    if (!userInfo.value) return false
    return permissions.value.includes(permissionCode)
  }

  const hasAnyPermission = (permissionCodeList) => {
    if (!userInfo.value) return false
    return permissionCodeList.some(code => permissions.value.includes(code))
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    name,
    role,
    permissions,
    login,
    logout,
    hasPermission,
    hasAnyPermission,
  }
})
