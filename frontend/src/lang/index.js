import { createI18n } from 'vue-i18n'

import zhCommon from './zh/common'
import zhNav from './zh/nav'
import zhSettings from './zh/settings'
import zhContainers from './zh/containers'
import zhServices from './zh/services'
import zhLogin from './zh/login'
import zhRegister from './zh/register'
import zhDashboard from './zh/dashboard'
import zhServers from './zh/servers'
import zhUsers from './zh/users'
import zhAlerts from './zh/alerts'
import zhLogs from './zh/logs'
import zhMetrics from './zh/metrics'
import zhTraces from './zh/traces'
import zhRoles from './zh/roles'
import zhPermissions from './zh/permissions'
import zhAuditLogs from './zh/auditLogs'
import zhServerGroups from './zh/serverGroups'
import zhRealtime from './zh/realtime'
import zhLanding from './zh/landing'
import zhForgotPassword from './zh/forgotPassword'

import enCommon from './en/common'
import enNav from './en/nav'
import enSettings from './en/settings'
import enContainers from './en/containers'
import enServices from './en/services'
import enLogin from './en/login'
import enRegister from './en/register'
import enDashboard from './en/dashboard'
import enServers from './en/servers'
import enUsers from './en/users'
import enAlerts from './en/alerts'
import enLogs from './en/logs'
import enMetrics from './en/metrics'
import enTraces from './en/traces'
import enRoles from './en/roles'
import enPermissions from './en/permissions'
import enAuditLogs from './en/auditLogs'
import enServerGroups from './en/serverGroups'
import enRealtime from './en/realtime'
import enLanding from './en/landing'
import enForgotPassword from './en/forgotPassword'

const zhMessages = {
  ...zhCommon,
  ...zhNav,
  ...zhSettings,
  ...zhContainers,
  ...zhServices,
  ...zhLogin,
  ...zhRegister,
  ...zhDashboard,
  ...zhServers,
  ...zhUsers,
  ...zhAlerts,
  ...zhLogs,
  ...zhMetrics,
  ...zhTraces,
  ...zhRoles,
  ...zhPermissions,
  ...zhAuditLogs,
  ...zhServerGroups,
  ...zhRealtime,
  ...zhLanding,
  ...zhForgotPassword
}

const enMessages = {
  ...enCommon,
  ...enNav,
  ...enSettings,
  ...enContainers,
  ...enServices,
  ...enLogin,
  ...enRegister,
  ...enDashboard,
  ...enServers,
  ...enUsers,
  ...enAlerts,
  ...enLogs,
  ...enMetrics,
  ...enTraces,
  ...enRoles,
  ...enPermissions,
  ...enAuditLogs,
  ...enServerGroups,
  ...enRealtime,
  ...enLanding,
  ...enForgotPassword
}

const messages = {
  'zh-CN': zhMessages,
  'en-US': enMessages
}

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages
})

export default i18n
