import request from '../request'

export const login = (data) => request.post('/auth/login', data)

export const logout = () => request.post('/auth/logout')

export const register = (data) => request.post('/auth/register', data)

export const forgotPassword = (data) => request.post('/auth/forgot-password', data)

export const resetPassword = (data) => request.post('/auth/reset-password', data)

export const changePassword = (data) => request.post('/auth/change-password', data)
