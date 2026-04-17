<script setup>
import { ref, computed, markRaw, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Monitor, Warning, DataLine, Setting, Bell, Fold, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const menuItems = [
  { path: '/dashboard', title: '监控看板', icon: markRaw(Monitor) },
  { path: '/servers', title: '服务器管理', icon: markRaw(Setting) },
  { path: '/alerts', title: '告警管理', icon: markRaw(Warning) },
  { path: '/metrics', title: '指标查询', icon: markRaw(DataLine) }
]

const visitedTags = ref([
  { path: '/dashboard', title: '监控看板' }
])

const activeMenu = computed(() => route.path)

watch(() => route.path, (path) => {
  const exists = visitedTags.value.find(t => t.path === path)
  if (!exists) {
    const menuItem = menuItems.find(m => m.path === path)
    if (menuItem) {
      visitedTags.value.push({ path: menuItem.path, title: menuItem.title })
    }
  }
}, { immediate: true })

const handleMenuSelect = (path) => router.push(path)
const handleTagClick = (path) => router.push(path)

const closeTag = (e, tag) => {
  e.stopPropagation()
  const index = visitedTags.value.findIndex(t => t.path === tag.path)
  if (index > -1) {
    visitedTags.value.splice(index, 1)
    if (route.path === tag.path && visitedTags.value.length > 0) {
      const nextIndex = Math.min(index, visitedTags.value.length - 1)
      router.push(visitedTags.value[nextIndex].path)
    }
  }
}

const closeOtherTags = () => {
  visitedTags.value = visitedTags.value.filter(t => t.path === route.path)
}
</script>

<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '56px' : '200px'" class="layout-aside">
      <div class="logo-area">
        <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32'%3E%3Ccircle cx='16' cy='16' r='14' fill='%236366f1'/%3E%3Ctext x='16' y='21' text-anchor='middle' fill='white' font-size='14' font-weight='bold'%3EL%3C/text%3E%3C/svg%3E" alt="logo" class="logo-img" />
        <span v-show="!isCollapse" class="logo-text">LeafVision</span>
      </div>
      <el-menu :default-active="activeMenu" @select="handleMenuSelect" class="layout-menu" :collapse="isCollapse">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="layout-right">
      <div class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse"><Fold /></el-icon>
          <span class="version">v1.0.0</span>
        </div>
        <div class="header-right">
          <el-badge :value="3" :max="99">
            <el-button :icon="Bell" circle size="small" />
          </el-badge>
          <el-dropdown trigger="click">
            <div class="user-dropdown">
              <el-avatar :size="28" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">Admin</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="tag-bar">
        <div class="tag-nav">
          <el-tag
            v-for="tag in visitedTags"
            :key="tag.path"
            :closable="visitedTags.length > 1"
            size="default"
            class="nav-tag"
            :class="{ active: activeMenu === tag.path }"
            @click="handleTagClick(tag.path)"
            @close="(e) => closeTag(e, tag)"
          >{{ tag.title }}</el-tag>
        </div>
        <el-dropdown trigger="click" v-if="visitedTags.length > 1" class="more-action">
          <el-button size="small" link class="more-btn">···</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="closeOtherTags">关闭其他</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  background: #141414;
}

.layout-aside {
  background: #1a1a1a;
  border-right: 1px solid #303030;
  transition: width 0.3s;
  overflow: hidden;
}

.logo-area {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #303030;
  padding: 0 12px;
}

.logo-img { width: 26px; height: 26px; flex-shrink: 0; }
.logo-text { font-size: 15px; font-weight: 600; color: #e0e0e0; }

.layout-menu { border-right: none; background: transparent !important; }
.layout-menu:not(.el-menu--collapse) { width: 200px; }

:deep(.layout-menu .el-menu-item) {
  color: #999;
  margin: 4px 8px;
  border-radius: 3px;
  height: 42px;
  line-height: 42px;
}

:deep(.layout-menu .el-menu-item:hover) { background-color: #2a2a2a; color: #fff; }
:deep(.layout-menu .el-menu-item.is-active) { background-color: #6366f1; color: #fff; }
:deep(.layout-menu .el-menu-item .el-icon) { font-size: 17px; }

.layout-right { display: flex; flex-direction: column; }

.layout-header {
  background: #1e1e1e;
  border-bottom: 1px solid #303030;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 44px;
}

.header-left { display: flex; align-items: center; gap: 10px; }
.collapse-btn { cursor: pointer; color: #888; font-size: 18px; transition: color 0.2s; }
.collapse-btn:hover { color: #fff; }
.version { color: #555; font-size: 11px; margin-left: 4px; }

.header-right { display: flex; align-items: center; gap: 12px; }

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #ccc;
}

.username { font-size: 13px; }
:deep(.user-dropdown .el-icon) { font-size: 12px; }

.tag-bar {
  background: #18181b;
  border-bottom: 1px solid #27272a;
  display: flex;
  align-items: center;
  padding: 0 16px;
  height: 36px;
  gap: 8px;
}

.tag-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  overflow-x: auto;
  overflow-y: hidden;
  flex: 1;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tag-nav::-webkit-scrollbar { display: none; }

.nav-tag {
  cursor: pointer;
  border-radius: 3px;
  font-size: 12px;
  transition: all 0.15s;
  flex-shrink: 0;
  max-width: 120px;
}

.nav-tag.active {
  background-color: #6366f1;
  border-color: #6366f1;
  color: #fff;
}

.more-action { flex-shrink: 0; }
.more-btn { color: #666; font-size: 14px; }

.layout-main { background: #141414; padding: 16px; flex: 1; min-height: 0; }
</style>
