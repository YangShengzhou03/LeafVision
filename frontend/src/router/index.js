import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '监控看板' }
  },
  {
    path: '/servers',
    name: 'Servers',
    component: () => import('../views/Servers.vue'),
    meta: { title: '服务器管理' }
  },
  {
    path: '/alerts',
    name: 'Alerts',
    component: () => import('../views/Alerts.vue'),
    meta: { title: '告警管理' }
  },
  {
    path: '/metrics',
    name: 'Metrics',
    component: () => import('../views/Metrics.vue'),
    meta: { title: '指标查询' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - LeafVision` : 'LeafVision'
  next()
})

export default router
