import request from '../request'

export const getDashboardData = () => request.get('/dashboard/overview')
