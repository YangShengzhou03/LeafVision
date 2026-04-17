import request from '../request'

export const queryMetrics = (params) => request.get('/metrics/query', { params })
