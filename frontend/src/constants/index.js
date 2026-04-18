export const SERVER_TYPES = [
  { value: 'prometheus-master', label: 'Prometheus 主节点' },
  { value: 'prometheus-node', label: 'Prometheus 从节点' },
  { value: 'alertmanager', label: 'Alertmanager' },
  { value: 'docker', label: 'Docker 主机' }
]

export const METRIC_OPTIONS = [
  { label: '服务状态', value: 'up' },
  { label: 'CPU 使用率', value: 'process_cpu_seconds_total' },
  { label: '内存使用', value: 'process_resident_memory_bytes' },
  { label: '请求总数', value: 'http_requests_total' }
]

export const TIME_RANGE_OPTIONS = [
  { label: '1小时', value: '1h' },
  { label: '6小时', value: '6h' },
  { label: '24小时', value: '24h' },
  { label: '7天', value: '7d' }
]

export const ALERT_STATUS_MAP = {
  firing: '告警中',
  resolved: '已恢复'
}

export const ALERT_SEVERITY_MAP = {
  critical: '严重',
  warning: '警告',
  info: '信息'
}

export const PERMISSION_CODE = {
  DASHBOARD: 'DASHBOARD',
  REALTIME: 'REALTIME',
  SERVERS: 'SERVERS',
  CONTAINERS: 'CONTAINERS',
  SERVICES: 'SERVICES',
  METRICS: 'METRICS',
  LOGS: 'LOGS',
  TRACES: 'TRACES',
  ALERTS: 'ALERTS',
  ALERT_RULES: 'ALERT_RULES',
  USERS: 'USERS',
  ROLES: 'ROLES',
  PERMISSIONS: 'PERMISSIONS',
  AUDIT_LOGS: 'AUDIT_LOGS',
  SETTINGS: 'SETTINGS',
  SERVER_GROUPS: 'SERVER_GROUPS'
}

export const ROLE_NAMES = {
  ADMIN: '系统管理员',
  OPERATOR: '运维人员',
  VIEWER: '访客'
}
