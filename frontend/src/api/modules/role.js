import request from '../request'

export const getRoles = () => request.get('/roles')

export const getRoleById = (id) => request.get(`/roles/${id}`)

export const createRole = (data) => request.post('/roles', data)

export const updateRole = (data) => request.put(`/roles/${data.id}`, data)

export const deleteRole = (id) => request.delete(`/roles/${id}`)

export const updateRolePermissions = (id, permissions) => 
  request.put(`/roles/${id}/permissions`, { permissions })
