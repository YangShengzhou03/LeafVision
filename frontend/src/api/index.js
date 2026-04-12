import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

request.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request

export const api = {
  getServerList: () => request.get('/servers'),
  getServerDetail: (id) => request.get(`/servers/${id}`),
  addServer: (data) => request.post('/servers', data),
  updateServer: (id, data) => request.put(`/servers/${id}`, data),
  deleteServer: (id) => request.delete(`/servers/${id}`),
  refreshStatus: () => request.post('/servers/refresh'),

  getAlertList: () => request.get('/alerts'),
  getActiveAlerts: () => request.get('/alerts/active'),
  getAlertRules: () => request.get('/alerts/rules'),
  resolveAlert: (id) => request.put(`/alerts/${id}/resolve`),
  addRule: (data) => request.post('/alerts/rules', data),
  updateRule: (id, data) => request.put(`/alerts/rules/${id}`, data),
  deleteRule: (id) => request.delete(`/alerts/rules/${id}`),
  toggleRule: (id) => request.put(`/alerts/rules/${id}/toggle`),
  syncAlerts: () => request.post('/alerts/sync'),

  queryMetrics: (params) => request.get('/metrics/query', { params }),
  queryMetricsRange: (params) => request.get('/metrics/query_range', { params }),
  getAvailableMetrics: (serverId) => request.get('/metrics/available', { params: { serverId } }),
  getInstantMetrics: (serverId) => request.get('/metrics/instant', { params: { serverId } }),
  getSystemMetrics: (serverId) => request.get('/metrics/system', { params: { serverId } }),

  getDashboardData: () => request.get('/dashboard/overview')
}
