import request from '../request'

export const getUserList = () => request.get('/users')

export const getUserById = (id) => request.get(`/users/${id}`)

export const createUser = (data) => request.post('/users', data)

export const updateUser = (data) => request.put(`/users/${data.id}`, data)

export const deleteUser = (id) => request.delete(`/users/${id}`)

export const getUserInfo = (username) => request.get(`/users/me?username=${username}`)

export const updateUserInfo = (data) => request.put('/users/me', data)

export const changePassword = (data) => request.put('/users/me/password', data)
