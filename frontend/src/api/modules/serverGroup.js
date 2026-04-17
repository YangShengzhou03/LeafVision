import request from '../request'

export const getServerGroups = () => request.get('/server-groups')

export const getServerGroupTree = () => request.get('/server-groups/tree')

export const getServerGroupById = (id) => request.get(`/server-groups/${id}`)

export const createServerGroup = (data) => request.post('/server-groups', data)

export const updateServerGroup = (id, data) => request.put(`/server-groups/${id}`, data)

export const deleteServerGroup = (id) => request.delete(`/server-groups/${id}`)

export const addServerToGroup = (groupId, serverId) => request.post(`/server-groups/${groupId}/servers/${serverId}`)

export const removeServerFromGroup = (groupId, serverId) => request.delete(`/server-groups/${groupId}/servers/${serverId}`)

export const getServersByGroup = (groupId) => request.get(`/server-groups/${groupId}/servers`)
