export * from './storage'

export const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms))

export const generateTimeLabels = (count, interval = 5) => {
  const labels = []
  const now = new Date()
  for (let i = count - 1; i >= 0; i--) {
    const t = new Date(now.getTime() - i * interval * 60 * 1000)
    labels.push(t.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }))
  }
  return labels
}

export const getProgressColor = (val) => {
  if (val > 80) return '#f56c6c'
  if (val > 60) return '#e6a23c'
  return '#409eff'
}

export const downloadFile = (data, filename, type = 'application/json') => {
  const blob = new Blob([JSON.stringify(data, null, 2)], { type })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}
