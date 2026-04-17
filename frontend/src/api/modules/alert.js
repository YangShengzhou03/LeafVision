import request from '../request'

export const getAlerts = async () => {
  const [list, rules] = await Promise.all([
    request.get('/alerts'),
    request.get('/alerts/rules')
  ])
  const alertList = list.data || list || []
  const ruleList = rules.data || rules || []
  return {
    code: 200,
    data: {
      list: alertList,
      rules: ruleList,
      stats: {
        total: alertList.length,
        firing: alertList.filter((item) => item.status === 'firing').length,
        critical: alertList.filter((item) => item.severity === 'critical').length,
      },
    },
  }
}
