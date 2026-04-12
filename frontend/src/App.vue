<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Monitor, Warning, DataLine, Setting, Menu } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const menuItems = [
  { path: '/dashboard', title: '监控看板', icon: Monitor },
  { path: '/servers', title: '服务器管理', icon: Setting },
  { path: '/alerts', title: '告警管理', icon: Warning },
  { path: '/metrics', title: '指标查询', icon: DataLine }
]

const activeMenu = computed(() => route.path)

const handleMenuSelect = (path) => {
  router.push(path)
}
</script>

<template>
  <el-container class="app-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="app-aside">
      <div class="logo">
        <el-icon :size="28"><Monitor /></el-icon>
        <span v-show="!isCollapse" class="logo-text">LeafVision</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        @select="handleMenuSelect"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
          <Menu />
        </el-icon>
        <div class="header-right">
          <el-tag type="success">系统运行正常</el-tag>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-container {
  height: 100vh;
}

.app-aside {
  background-color: #304156;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  border-bottom: 1px solid #3a4a5b;
}

.logo-text {
  margin-left: 10px;
}

.el-menu {
  border-right: none;
}

.app-header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}

.collapse-btn:hover {
  color: #409EFF;
}

.app-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
