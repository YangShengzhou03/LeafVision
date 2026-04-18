import request from '../request'

export const getTraces = (params) => request.get('/traces', { params })

export const getTraceById = (traceId) => request.get(`/traces/${traceId}`)
