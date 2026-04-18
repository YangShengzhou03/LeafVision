import request from '../request'

export const getServerList = () => request.get('/servers')

export const getServers = () => request.get('/servers')

export const createServer = (data) => request.post('/servers', data)

export const updateServer = (id, data) => request.put(`/servers/${id}`, data)

export const deleteServer = (id) => request.delete(`/servers/${id}`)

export const getRealtimeMetrics = (serverId) => request.get(`/servers/${serverId}/realtime`)

export const refreshServerStatus = () => request.post('/servers/refresh')
