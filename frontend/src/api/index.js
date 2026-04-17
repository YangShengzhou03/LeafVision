import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export const api = {
  getServerList: () => request.get('/servers'),
  addServer: (data) => request.post('/servers', data),
  updateServer: (id, data) => request.put(`/servers/${id}`, data),
  deleteServer: (id) => request.delete(`/servers/${id}`),
  refreshStatus: () => request.post('/servers/refresh'),
  getAlertList: () => request.get('/alerts'),
  getAlertRules: () => request.get('/alerts/rules'),
  resolveAlert: (id) => request.put(`/alerts/${id}/resolve`),
  deleteRule: (id) => request.delete(`/alerts/rules/${id}`),
  toggleRule: (id) => request.put(`/alerts/rules/${id}/toggle`),
  syncAlerts: () => request.post('/alerts/sync'),
  queryMetrics: (params) => request.get('/metrics/query', { params }),
  getDashboardData: () => request.get('/dashboard/overview')
}