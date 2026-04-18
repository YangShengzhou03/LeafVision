import request from '../request'

export const getSettings = () => {
  return request.get('/settings')
}

export const updateSettings = (data) => {
  return request.put('/settings', data)
}
