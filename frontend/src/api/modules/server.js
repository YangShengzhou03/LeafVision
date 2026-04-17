import request from '../request'

export const getServerList = () => request.get('/servers')

export const getServers = () => request.get('/servers')

export const createServer = (data) => request.post('/servers', data)

export const updateServer = (id, data) => request.put(`/servers/${id}`, data)

export const deleteServer = (id) => request.delete(`/servers/${id}`)

export const getRealtimeMetrics = (serverId) => request.get(`/servers/${serverId}/realtime`)

export const getContainerList = () => request.get('/containers')

export const getServiceList = () => request.get('/services')

export const getLogs = (params) => request.get('/logs', { params })

export const getTraces = (params) => request.get('/traces', { params })

export const getUserList = () => request.get('/users')

export const createUser = (data) => request.post('/users', data)

export const updateUser = (data) => request.put(`/users/${data.id}`, data)

export const deleteUser = (id) => request.delete(`/users/${id}`)

export const getRoles = () => request.get('/roles')

export const createRole = (data) => request.post('/roles', data)

export const updateRole = (data) => request.put(`/roles/${data.id}`, data)

export const deleteRole = (id) => request.delete(`/roles/${id}`)

export const updateRolePermissions = (roleId, permissions) => request.put(`/roles/${roleId}/permissions`, { permissions })

export const getPermissions = () => request.get('/permissions')

export const getAuditLogs = (params) => request.get('/audit-logs', { params })

export const getSettings = () => request.get('/settings')

export const updateSettings = (data) => request.put('/settings', data)
