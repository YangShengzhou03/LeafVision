import request from '../request'

export const getAlerts = () => request.get('/alerts')

export const getAlertList = () => request.get('/alerts')

export const getActiveAlerts = () => request.get('/alerts/active')

export const resolveAlert = (id) => request.put(`/alerts/${id}/resolve`)

export const getAlertRules = () => request.get('/alerts/rules')

export const addAlertRule = (data) => request.post('/alerts/rules', data)

export const updateAlertRule = (id, data) => request.put(`/alerts/rules/${id}`, data)

export const deleteAlertRule = (id) => request.delete(`/alerts/rules/${id}`)

export const toggleAlertRule = (id) => request.put(`/alerts/rules/${id}/toggle`)

export const syncAlerts = () => request.post('/alerts/sync')
