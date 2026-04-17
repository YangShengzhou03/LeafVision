<script setup>
import { ref, onMounted } from 'vue'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getTraces } from '@/api'
import { downloadFile } from '@/utils'

const loading = ref(false)
const traceList = ref([])
const selectedTrace = ref(null)
const queryForm = ref({
  service: '',
  operation: '',
  minDuration: '',
  maxDuration: '',
  limit: 50
})

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getTraces(queryForm.value)
    if (res.code === 200) {
      traceList.value = res.data || []
      ElMessage.success(`查询到 ${traceList.value.length} 条链路`)
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  if (!traceList.value.length) {
    ElMessage.warning('无数据可导出')
    return
  }
  downloadFile(traceList.value, `traces_${Date.now()}.json`)
  ElMessage.success('导出成功')
}

const handleViewDetail = (trace) => {
  selectedTrace.value = trace
}

const formatDuration = (duration) => {
  if (duration < 1000) return `${duration}μs`
  if (duration < 1000000) return `${(duration / 1000).toFixed(2)}ms`
  return `${(duration / 1000000).toFixed(2)}s`
}

const getStatusClass = (status) => {
  if (status === 'error') return 'error'
  if (status === 'ok') return 'ok'
  return ''
}

onMounted(() => handleSearch())
</script>

<template>
  <div class="traces-page">
    <div class="query-card">
      <div class="card-header">
        <span class="card-title">查询条件</span>
      </div>
      <div class="card-body">
        <div class="query-form">
          <div class="form-group">
            <label class="form-label">服务名称</label>
            <input
              v-model="queryForm.service"
              type="text"
              placeholder="输入服务名称"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label class="form-label">操作名称</label>
            <input
              v-model="queryForm.operation"
              type="text"
              placeholder="输入操作名称"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label class="form-label">最小耗时</label>
            <input
              v-model="queryForm.minDuration"
              type="text"
              placeholder="如: 100ms"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label class="form-label">最大耗时</label>
            <input
              v-model="queryForm.maxDuration"
              type="text"
              placeholder="如: 1s"
              class="form-input"
            />
          </div>
          <div class="form-actions">
            <button class="btn-primary" @click="handleSearch" :disabled="loading">
              <el-icon><Search /></el-icon>
              <span>查询</span>
            </button>
            <button class="btn-secondary" @click="handleExport" :disabled="!traceList.length">
              <el-icon><Download /></el-icon>
              <span>导出</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="result-card">
      <div class="card-header">
        <span class="card-title">链路列表</span>
        <span class="trace-count">共 {{ traceList.length }} 条</span>
      </div>
      <div class="card-body">
        <table class="data-table">
          <thead>
            <tr>
              <th>Trace ID</th>
              <th>服务</th>
              <th>操作</th>
              <th>耗时</th>
              <th>跨度数</th>
              <th>状态</th>
              <th>时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody v-if="!loading">
            <tr v-for="trace in traceList" :key="trace.traceId">
              <td class="trace-id">{{ trace.traceId }}</td>
              <td>{{ trace.service }}</td>
              <td>{{ trace.operation }}</td>
              <td class="duration">{{ formatDuration(trace.duration) }}</td>
              <td>{{ trace.spanCount }}</td>
              <td>
                <span :class="['status-badge', getStatusClass(trace.status)]">
                  {{ trace.status === 'ok' ? '成功' : '失败' }}
                </span>
              </td>
              <td>{{ trace.startTime }}</td>
              <td>
                <button class="btn-link" @click="handleViewDetail(trace)">详情</button>
              </td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr>
              <td colspan="8" class="loading-cell">加载中...</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="selectedTrace" class="detail-card">
      <div class="card-header">
        <span class="card-title">链路详情</span>
        <button class="btn-close" @click="selectedTrace = null">关闭</button>
      </div>
      <div class="card-body">
        <div class="trace-info">
          <div class="info-item">
            <span class="info-label">Trace ID:</span>
            <span class="info-value">{{ selectedTrace.traceId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">总耗时:</span>
            <span class="info-value">{{ formatDuration(selectedTrace.duration) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">跨度数:</span>
            <span class="info-value">{{ selectedTrace.spanCount }}</span>
          </div>
        </div>
        <div class="spans-list">
          <div v-for="(span, index) in selectedTrace.spans" :key="index" class="span-item">
            <div class="span-header">
              <span class="span-service">{{ span.service }}</span>
              <span class="span-operation">{{ span.operation }}</span>
              <span class="span-duration">{{ formatDuration(span.duration) }}</span>
            </div>
            <div class="span-timeline">
              <div class="timeline-bar" :style="{ width: (span.duration / selectedTrace.duration * 100) + '%' }"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.traces-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.query-card,
.result-card,
.detail-card {
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}

.card-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.trace-count {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

.card-body {
  padding: 20px;
}

.query-form {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 140px;
}

.form-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.form-input {
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  font-size: 13px;
  font-weight: 400;
  color: var(--color-text-primary);
  background: #ffffff;
  outline: none;
  transition: border-color 0.15s ease;
}

.form-input:focus {
  border-color: var(--site-context-highlight-color);
}

.form-actions {
  display: flex;
  gap: 12px;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--site-context-highlight-color);
  border: none;
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  padding: 9px 18px;
  cursor: pointer;
  transition: background 0.15s ease;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.btn-primary:hover {
  background: var(--site-context-focus-color);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 700;
  padding: 9px 18px;
  cursor: pointer;
  transition: all 0.15s ease;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.btn-secondary:hover {
  border-color: var(--color-text-primary);
  color: var(--color-text-primary);
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 14px 16px;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-secondary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--color-border);
}

.data-table td {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border);
  line-height: 1.15;
}

.data-table tr:last-child td {
  border-bottom: none;
}

.data-table tr:hover td {
  background: #fafafa;
}

.trace-id {
  font-family: monospace;
  font-size: 12px;
  color: var(--site-context-highlight-color);
}

.duration {
  font-weight: 700;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.status-badge.ok {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.status-badge.error {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.btn-link {
  background: none;
  border: none;
  color: var(--site-context-highlight-color);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  padding: 0;
  letter-spacing: 0.02em;
  transition: color 0.15s ease;
}

.btn-link:hover {
  color: var(--site-context-focus-color);
}

.btn-close {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 700;
  padding: 6px 12px;
  cursor: pointer;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  transition: all 0.15s ease;
}

.btn-close:hover {
  border-color: var(--color-text-primary);
  color: var(--color-text-primary);
}

.loading-cell {
  text-align: center;
  padding: 40px;
  color: var(--color-text-secondary);
}

.trace-info {
  display: flex;
  gap: 32px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-secondary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.info-value {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-primary);
}

.spans-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.span-item {
  padding: 12px;
  border: 1px solid var(--color-border);
}

.span-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.span-service {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.span-operation {
  font-size: 12px;
  color: var(--color-text-secondary);
  flex: 1;
}

.span-duration {
  font-size: 12px;
  font-weight: 700;
  color: var(--site-context-highlight-color);
}

.span-timeline {
  height: 6px;
  background: #f0f0f0;
}

.timeline-bar {
  height: 100%;
  background: var(--site-context-highlight-color);
  min-width: 2px;
}

@media (max-width: 768px) {
  .query-form {
    flex-direction: column;
    align-items: stretch;
  }
  .form-group {
    min-width: 100%;
  }
  .form-actions {
    width: 100%;
  }
  .form-actions button {
    flex: 1;
  }
  .trace-info {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
