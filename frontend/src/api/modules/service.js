import request from '../request'

export const getServiceList = () => {
  return request.get('/services')
}

export const getServiceDetail = (id) => {
  return request.get(`/services/${id}`)
}

export const createService = (data) => {
  return request.post('/services', data)
}

export const updateService = (id, data) => {
  return request.put(`/services/${id}`, data)
}

export const deleteService = (id) => {
  return request.delete(`/services/${id}`)
}

export const getServiceMetrics = (id) => {
  return request.get(`/services/${id}/metrics`)
}
