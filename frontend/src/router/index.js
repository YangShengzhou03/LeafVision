import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/storage'
import { PERMISSION_CODE } from '@/constants'
import MainLayout from '@/layout/MainLayout.vue'

const routes = [
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/Landing.vue'),
    meta: { public: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue'),
    meta: { public: true }
  },
  {
    path: '/monitor',
    component: MainLayout,
    meta: { requiresAuth: true },
    redirect: '/monitor/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { permission: PERMISSION_CODE.DASHBOARD }
      },
      {
        path: 'realtime',
        name: 'Realtime',
        component: () => import('@/views/Realtime.vue'),
        meta: { permission: PERMISSION_CODE.REALTIME }
      },
      {
        path: 'servers',
        name: 'Servers',
        component: () => import('@/views/Servers.vue'),
        meta: { permission: PERMISSION_CODE.SERVERS }
      },
      {
        path: 'server-groups',
        name: 'ServerGroups',
        component: () => import('@/views/ServerGroups.vue'),
        meta: { permission: PERMISSION_CODE.SERVER_GROUPS }
      },
      {
        path: 'containers',
        name: 'Containers',
        component: () => import('@/views/Containers.vue'),
        meta: { permission: PERMISSION_CODE.CONTAINERS }
      },
      {
        path: 'services',
        name: 'Services',
        component: () => import('@/views/Services.vue'),
        meta: { permission: PERMISSION_CODE.SERVICES }
      },
      {
        path: 'metrics',
        name: 'Metrics',
        component: () => import('@/views/Metrics.vue'),
        meta: { permission: PERMISSION_CODE.METRICS }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/Logs.vue'),
        meta: { permission: PERMISSION_CODE.LOGS }
      },
      {
        path: 'traces',
        name: 'Traces',
        component: () => import('@/views/Traces.vue'),
        meta: { permission: PERMISSION_CODE.TRACES }
      },
      {
        path: 'alerts',
        name: 'Alerts',
        component: () => import('@/views/Alerts.vue'),
        meta: { permission: PERMISSION_CODE.ALERTS }
      },
      {
        path: 'alert-rules',
        name: 'AlertRules',
        component: () => import('@/views/Alerts.vue'),
        meta: { permission: PERMISSION_CODE.ALERT_RULES }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { permission: PERMISSION_CODE.USERS }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/Roles.vue'),
        meta: { permission: PERMISSION_CODE.ROLES }
      },
      {
        path: 'permissions',
        name: 'Permissions',
        component: () => import('@/views/Permissions.vue'),
        meta: { permission: PERMISSION_CODE.PERMISSIONS }
      },
      {
        path: 'audit-logs',
        name: 'AuditLogs',
        component: () => import('@/views/AuditLogs.vue'),
        meta: { permission: PERMISSION_CODE.AUDIT_LOGS }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { permission: PERMISSION_CODE.SETTINGS }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  const userStr = sessionStorage.getItem('user') || localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.public && token && to.name === 'Login') {
    next({ name: 'Dashboard' })
  } else if (to.meta.permission && user) {
    const userPermissions = user.permissions || []
    if (!userPermissions.includes(to.meta.permission)) {
      next({ name: 'Dashboard' })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
