<script setup>
import { ref, computed, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  Monitor, Setting, Warning, DataLine, Document, User, Tools,
  Fold, Expand, Connection, Timer, Bell, PieChart, Key, House, SwitchButton, List, Folder
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { PERMISSION_CODE } from '@/constants'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const allMenuItems = computed(() => [
  { path: '/monitor/dashboard', title: t('监控总览'), icon: markRaw(Monitor), permission: PERMISSION_CODE.DASHBOARD },
  { path: '/monitor/realtime', title: t('实时监控'), icon: markRaw(Timer), permission: PERMISSION_CODE.REALTIME },
  { path: '/monitor/servers', title: t('主机管理'), icon: markRaw(House), permission: PERMISSION_CODE.SERVERS },
  { path: '/monitor/server-groups', title: t('服务器分组'), icon: markRaw(Folder), permission: PERMISSION_CODE.SERVER_GROUPS },
  { path: '/monitor/containers', title: t('容器管理'), icon: markRaw(Connection), permission: PERMISSION_CODE.CONTAINERS },
  { path: '/monitor/services', title: t('服务管理'), icon: markRaw(Tools), permission: PERMISSION_CODE.SERVICES },
  { path: '/monitor/metrics', title: t('指标检索'), icon: markRaw(DataLine), permission: PERMISSION_CODE.METRICS },
  { path: '/monitor/logs', title: t('日志查询'), icon: markRaw(Document), permission: PERMISSION_CODE.LOGS },
  { path: '/monitor/traces', title: t('链路追踪'), icon: markRaw(PieChart), permission: PERMISSION_CODE.TRACES },
  { path: '/monitor/alerts', title: t('告警列表'), icon: markRaw(Warning), permission: PERMISSION_CODE.ALERTS },
  { path: '/monitor/alert-rules', title: t('告警规则'), icon: markRaw(Bell), permission: PERMISSION_CODE.ALERT_RULES },
  { path: '/monitor/users', title: t('用户管理'), icon: markRaw(User), permission: PERMISSION_CODE.USERS },
  { path: '/monitor/roles', title: t('角色管理'), icon: markRaw(Key), permission: PERMISSION_CODE.ROLES },
  { path: '/monitor/permissions', title: t('权限配置'), icon: markRaw(Setting), permission: PERMISSION_CODE.PERMISSIONS },
  { path: '/monitor/audit-logs', title: t('审计日志'), icon: markRaw(List), permission: PERMISSION_CODE.AUDIT_LOGS },
  { path: '/monitor/settings', title: t('系统设置'), icon: markRaw(Setting), permission: PERMISSION_CODE.SETTINGS },
])

const menuItems = computed(() => {
  return allMenuItems.value.filter(item => userStore.hasPermission(item.permission))
})

const activeMenu = computed(() => route.path)
const currentMenuItem = computed(() => menuItems.value.find((item) => item.path === route.path) || menuItems.value[0])

const recentPages = ref([
  { path: '/monitor/dashboard', titleKey: '监控总览', closable: true },
  { path: '/monitor/servers', titleKey: '主机管理', closable: true },
])

const handleTagClick = (path) => router.push(path)

const handleTagClose = (path) => {
  const index = recentPages.value.findIndex(tag => tag.path === path)
  if (index > -1) recentPages.value.splice(index, 1)
  if (route.path === path && recentPages.value.length > 0) {
    router.push(recentPages.value[Math.max(0, index - 1)].path)
  }
}

const handleMenuSelect = (path) => {
  router.push(path)
  if (!recentPages.value.find(tag => tag.path === path)) {
    const item = menuItems.value.find(menuItem => menuItem.path === path)
    if (item) {
      const titleKey = allMenuItems.value.find(m => m.path === path)?.title || '监控总览'
      recentPages.value.push({ path: item.path, titleKey: titleKey, closable: true })
    }
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(t('确定要退出登录吗？'), t('提示'), {
      confirmButtonText: t('确认'),
      cancelButtonText: t('取消'),
      type: 'warning'
    })
    await userStore.logout()
    ElMessage.success(t('退出成功'))
    router.push('/')
  } catch {
  }
}
</script>

<template>
  <el-container class="layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <header class="brand">
        <span class="brand-text">{{ isCollapse ? 'LV' : 'LEAFVISION' }}</span>
      </header>
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          class="menu"
          @select="handleMenuSelect"
        >
          <el-menu-item
            v-for="item in menuItems"
            :key="item.path"
            :index="item.path"
            class="menu-item"
          >
            <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="main-container">
      <header class="header">
        <button class="collapse-btn" @click="isCollapse = !isCollapse">
          <el-icon><component :is="isCollapse ? Expand : Fold" /></el-icon>
        </button>
        <span class="page-title">{{ currentMenuItem?.title || t('监控中心') }}</span>
        <div class="header-actions">
          <el-dropdown trigger="click">
            <div class="user-dropdown">
              <span class="user-avatar">
                {{ (userStore.name || userStore.username)?.charAt(0)?.toUpperCase() || 'A' }}
              </span>
              <span class="user-name">{{ userStore.name || userStore.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-menu">
                <el-dropdown-item @click="router.push('/monitor/profile')">
                  <el-icon><Setting /></el-icon>
                  <span>{{ t('个人设置') }}</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>{{ t('退出登录') }}</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <nav class="tags-nav">
        <el-scrollbar class="tags-scrollbar">
          <div class="tags-list">
            <span
              v-for="tag in recentPages"
              :key="tag.path"
              :class="['tag-item', { active: route.path === tag.path }]"
              @click="handleTagClick(tag.path)"
            >
              <span class="tag-text">{{ t(tag.titleKey) }}</span>
              <span class="tag-close" @click.stop="handleTagClose(tag.path)">×</span>
            </span>
          </div>
        </el-scrollbar>
      </nav>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
  overflow: hidden;
}

.aside {
  display: flex;
  flex-direction: column;
  background: var(--color-surface-dark);
  overflow: hidden;
  transition: width 0.2s ease;
}

.brand {
  flex-shrink: 0;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.brand-text {
  font-size: 14px;
  font-weight: 900;
  color: #ffffff;
  letter-spacing: 0.15em;
  line-height: 1.15;
}

.menu-scrollbar {
  flex: 1;
  overflow: hidden;
}

.menu {
  border-right: none !important;
  background: transparent !important;
}

.menu-item {
  color: rgba(255, 255, 255, 0.6) !important;
  height: 48px;
  line-height: 48px;
  margin: 0;
  border-radius: 0 !important;
  font-weight: 400;
  font-size: 14px;
  letter-spacing: 0.02em;
}

.menu-item:hover {
  color: #ffffff !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

.menu-item.is-active {
  color: #ffffff !important;
  background: var(--site-context-highlight-color) !important;
  font-weight: 700;
}

.menu-icon {
  font-size: 18px;
}

.main-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 16px;
  height: 56px;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid var(--color-border);
}

.collapse-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border);
  border-radius: 0;
  color: var(--color-text-secondary);
  background: #ffffff;
  cursor: pointer;
  transition: all 0.15s ease;
}

.collapse-btn:hover {
  background: var(--site-context-highlight-color);
  border-color: var(--site-context-highlight-color);
  color: #ffffff;
}

.page-title {
  font-size: 16px;
  font-weight: 900;
  color: var(--color-text-primary);
  letter-spacing: 0.02em;
  line-height: 1.15;
}

.header-actions {
  margin-left: auto;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  transition: background 0.15s ease;
}

.user-dropdown:hover {
  background: rgba(0, 0, 0, 0.04);
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: var(--site-context-highlight-color);
  color: #ffffff;
  font-weight: 700;
  font-size: 13px;
  line-height: 1;
}

.user-name {
  font-size: 14px;
  color: var(--color-text-primary);
}

.tags-nav {
  flex-shrink: 0;
  height: 36px;
  background: #ffffff;
  border-bottom: 1px solid var(--color-border);
  padding: 0 16px;
  display: flex;
  align-items: center;
}

.tags-scrollbar {
  height: 100%;
  width: 100%;
}

.tags-scrollbar :deep(.el-scrollbar__wrap) {
  height: 100%;
  overflow-x: auto;
  overflow-y: hidden;
}

.tags-scrollbar :deep(.el-scrollbar__view) {
  height: 100%;
  display: flex;
  align-items: center;
}

.tags-list {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 8px;
  flex-wrap: nowrap;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  height: 26px;
  background: #f5f5f5;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
  white-space: nowrap;
  flex-shrink: 0;
}

.tag-item:hover {
  background: #e8e8e8;
}

.tag-item.active {
  background: var(--site-context-highlight-color);
  border-color: var(--site-context-highlight-color);
  color: #ffffff;
}

.tag-text {
  white-space: nowrap;
}

.tag-close {
  font-size: 14px;
  line-height: 1;
  opacity: 0.6;
}

.tag-close:hover {
  opacity: 1;
}

.main {
  flex: 1;
  background: var(--color-background);
  padding: 20px;
  overflow: auto;
}
</style>
