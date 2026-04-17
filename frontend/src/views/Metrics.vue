<script setup>
import { ref, onMounted, computed } from 'vue'
import { Download, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getServerList, queryMetrics } from '@/api'
import { METRIC_OPTIONS, TIME_RANGE_OPTIONS } from '@/constants'
import { downloadFile } from '@/utils'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent])

const queryForm = ref({ metric: 'up', serverId: null, timeRange: '1h' })

const serverOptions = ref([])
const loading = ref(false)
const chartData = ref([])

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: chartData.value.map(item => item.time)
  },
  yAxis: { type: 'value' },
  series: [{
    type: 'line',
    smooth: true,
    symbol: 'none',
    areaStyle: { opacity: 0.2 },
    lineStyle: { width: 2 },
    data: chartData.value.map(item => item.value)
  }]
}))

const fetchServerList = async () => {
  const res = await getServerList()
  if (res.code === 200 && res.data) {
    serverOptions.value = res.data.map(s => ({ label: s.name, value: s.id }))
    if (serverOptions.value.length > 0) queryForm.value.serverId = serverOptions.value[0].value
  }
}

const handleQuery = async () => {
  if (!queryForm.value.serverId) return ElMessage.warning('请选择服务器')
  loading.value = true
  try {
    const res = await queryMetrics(queryForm.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      if (data.values && data.values.length > 0) {
        chartData.value = data.values.map(item => ({
          time: new Date(item.timestamp * 1000).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
          value: parseFloat(item.value).toFixed(2)
        }))
        ElMessage.success('查询成功')
      } else {
        chartData.value = []
        ElMessage.warning('没有查询到数据')
      }
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  if (!chartData.value.length) return ElMessage.warning('无数据')
  downloadFile(chartData.value, `metrics_${Date.now()}.json`)
  ElMessage.success('已导出')
}

onMounted(() => fetchServerList())
</script>

<template>
  <div class="metrics-page">
    <div class="query-card">
      <div class="card-header">
        <span class="card-title">查询条件</span>
      </div>
      <div class="card-body">
        <div class="query-form">
          <div class="form-group">
            <label class="form-label">服务器</label>
            <select v-model="queryForm.serverId" class="form-select">
              <option :value="null">选择服务器</option>
              <option v-for="item in serverOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">指标</label>
            <select v-model="queryForm.metric" class="form-select">
              <option v-for="item in METRIC_OPTIONS" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">时间范围</label>
            <select v-model="queryForm.timeRange" class="form-select">
              <option v-for="item in TIME_RANGE_OPTIONS" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
          <div class="form-actions">
            <button class="btn-primary" @click="handleQuery" :disabled="loading">
              <el-icon><Search /></el-icon>
              <span>查询</span>
            </button>
            <button class="btn-secondary" @click="handleExport" :disabled="!chartData.length">
              <el-icon><Download /></el-icon>
              <span>导出</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="result-card">
      <div class="card-header">
        <span class="card-title">查询结果</span>
      </div>
      <div class="card-body">
        <div v-if="chartData.length > 0" class="chart-container">
          <v-chart :option="chartOption" autoresize style="height: 380px" />
        </div>
        <div v-else class="empty-state">
          <span class="empty-text">请先查询数据</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.metrics-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.query-card,
.result-card {
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.card-header {
  display: flex;
  align-items: center;
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

.card-body {
  padding: 20px;
}

.query-form {
  display: flex;
  align-items: flex-end;
  gap: 24px;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 180px;
}

.form-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.form-select {
  height: 40px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-primary);
  background: #ffffff;
  outline: none;
  transition: border-color 0.15s ease;
  min-width: 180px;
}

.form-select:focus {
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
  padding: 10px 20px;
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
  padding: 10px 20px;
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

.chart-container {
  width: 100%;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-text {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .query-form {
    flex-direction: column;
    align-items: stretch;
  }
  .form-group {
    min-width: 100%;
  }
  .form-select {
    min-width: 100%;
  }
  .form-actions {
    width: 100%;
  }
  .form-actions button {
    flex: 1;
  }
}
</style>
