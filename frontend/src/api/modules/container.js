import request from '../request'

export const getContainerList = () => {
  return request.get('/containers')
}

export const getContainerDetail = (id) => {
  return request.get(`/containers/${id}`)
}

export const startContainer = (id, serverId) => {
  return request.post(`/containers/${id}/start`, { serverId })
}

export const stopContainer = (id, serverId) => {
  return request.post(`/containers/${id}/stop`, { serverId })
}

export const restartContainer = (id, serverId) => {
  return request.post(`/containers/${id}/restart`, { serverId })
}

export const getContainerLogs = (id, params) => {
  return request.get(`/containers/${id}/logs`, { params })
}

export const getContainerStats = (id) => {
  return request.get(`/containers/${id}/stats`)
}

export const createContainer = (data) => {
  return request.post('/containers', data)
}

export const deleteContainer = (id) => {
  return request.delete(`/containers/${id}`)
}
