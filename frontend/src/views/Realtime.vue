<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Refresh, VideoPause, VideoPlay } from '@element-plus/icons-vue'
import { getServerList, getRealtimeMetrics } from '@/api'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, GaugeChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, LineChart, GaugeChart, GridComponent, TooltipComponent])

const { t } = useI18n()

const formatPercent = (value) => {
  if (value == null) return '0.00'
  return Number(value).toFixed(2)
}

const serverList = ref([])
const selectedServer = ref(null)
const loading = ref(false)
const isPaused = ref(false)
const realtimeData = ref({ cpu: [], memory: [], network: [] })
let refreshTimer = null

const cpuOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: realtimeData.value.cpu.map(item => item.time)
  },
  yAxis: { type: 'value', max: 100 },
  series: [{
    type: 'line',
    smooth: true,
    symbol: 'none',
    areaStyle: { opacity: 0.2 },
    lineStyle: { width: 2, color: '#1c69d4' },
    data: realtimeData.value.cpu.map(item => item.value)
  }]
}))

const memoryOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: realtimeData.value.memory.map(item => item.time)
  },
  yAxis: { type: 'value', max: 100 },
  series: [{
    type: 'line',
    smooth: true,
    symbol: 'none',
    areaStyle: { opacity: 0.2 },
    lineStyle: { width: 2, color: '#67c23a' },
    data: realtimeData.value.memory.map(item => item.value)
  }]
}))

const networkOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: realtimeData.value.network.map(item => item.time)
  },
  yAxis: { type: 'value' },
  series: [{
    type: 'line',
    smooth: true,
    symbol: 'none',
    areaStyle: { opacity: 0.2 },
    lineStyle: { width: 2, color: '#e6a23c' },
    data: realtimeData.value.network.map(item => item.in)
  }, {
    type: 'line',
    smooth: true,
    symbol: 'none',
    areaStyle: { opacity: 0.2 },
    lineStyle: { width: 2, color: '#f56c6c' },
    data: realtimeData.value.network.map(item => item.out)
  }]
}))

const fetchServerList = async () => {
  const res = await getServerList()
  if (res.code === 200 && res.data?.length) {
    serverList.value = res.data
    selectedServer.value = res.data[0].id
  }
}

const fetchRealtimeData = async () => {
  if (!selectedServer.value || isPaused.value) return
  try {
    const res = await getRealtimeMetrics(selectedServer.value)
    if (res.code === 200 && res.data) {
      const now = new Date().toLocaleTimeString()
      const pushData = (arr, value, maxLen = 60) => {
        arr.push({ time: now, value })
        if (arr.length > maxLen) arr.shift()
      }
      pushData(realtimeData.value.cpu, res.data.cpu)
      pushData(realtimeData.value.memory, res.data.memory)
      realtimeData.value.network.push({
        time: now,
        in: res.data.networkIn,
        out: res.data.networkOut
      })
      if (realtimeData.value.network.length > 60) {
        realtimeData.value.network.shift()
      }
    }
  } catch (error) {
    console.error(error)
  }
}

const startRefresh = () => {
  stopRefresh()
  refreshTimer = setInterval(fetchRealtimeData, 3000)
}

const stopRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const togglePause = () => {
  isPaused.value = !isPaused.value
  if (isPaused.value) {
    stopRefresh()
  } else {
    startRefresh()
  }
}

const handleRefresh = () => {
  loading.value = true
  fetchRealtimeData().finally(() => {
    loading.value = false
  })
}

onMounted(async () => {
  await fetchServerList()
  startRefresh()
})

onUnmounted(() => {
  stopRefresh()
})
</script>

<template>
  <div class="realtime-page">
    <div class="page-header">
      <div class="header-left">
        <span class="page-title">{{ t('实时监控') }}</span>
        <select v-model="selectedServer" class="server-select" :disabled="serverList.length === 0" @change="handleRefresh">
          <option v-if="serverList.length === 0" disabled value="">{{ t('暂无服务器') }}</option>
          <option v-for="server in serverList" :key="server.id" :value="server.id">
            {{ server.name }}
          </option>
        </select>
      </div>
      <div class="header-actions">
        <button class="btn-secondary" @click="togglePause">
          <el-icon><component :is="isPaused ? VideoPlay : VideoPause" /></el-icon>
          <span>{{ isPaused ? t('继续') : t('暂停') }}</span>
        </button>
        <button class="btn-primary" @click="handleRefresh" :disabled="loading">
          <el-icon><Refresh /></el-icon>
          <span>{{ t('刷新') }}</span>
        </button>
      </div>
    </div>

    <div class="charts-grid">
      <div class="chart-card">
        <div class="card-header">
          <span class="card-title">{{ t('CPU使用率') }}</span>
          <span class="card-value">{{ formatPercent(realtimeData.cpu[realtimeData.cpu.length - 1]?.value) }}%</span>
        </div>
        <div class="card-body">
          <v-chart :option="cpuOption" autoresize style="height: 200px" />
        </div>
      </div>

      <div class="chart-card">
        <div class="card-header">
          <span class="card-title">{{ t('内存使用率') }}</span>
          <span class="card-value">{{ formatPercent(realtimeData.memory[realtimeData.memory.length - 1]?.value) }}%</span>
        </div>
        <div class="card-body">
          <v-chart :option="memoryOption" autoresize style="height: 200px" />
        </div>
      </div>

      <div class="chart-card full-width">
        <div class="card-header">
          <span class="card-title">{{ t('网络流量') }}</span>
          <div class="legend">
            <span class="legend-item in">{{ t('入站') }}</span>
            <span class="legend-item out">{{ t('出站') }}</span>
          </div>
        </div>
        <div class="card-body">
          <v-chart :option="networkOption" autoresize style="height: 200px" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.realtime-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.server-select {
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  font-size: 13px;
  font-weight: 400;
  color: var(--color-text-primary);
  background: #ffffff;
  outline: none;
  min-width: 160px;
}

.server-select:focus {
  border-color: var(--site-context-highlight-color);
}

.server-select:disabled {
  background: #f5f5f5;
  color: var(--color-text-secondary);
  cursor: not-allowed;
}

.header-actions {
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
  padding: 8px 16px;
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
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.15s ease;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.btn-secondary:hover {
  border-color: var(--color-text-primary);
  color: var(--color-text-primary);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.chart-card {
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.chart-card.full-width {
  grid-column: span 2;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--color-border);
}

.card-title {
  font-size: 12px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.card-value {
  font-size: 24px;
  font-weight: 300;
  color: var(--site-context-highlight-color);
  line-height: 1.15;
}

.legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-secondary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.legend-item.in::before {
  content: '';
  display: inline-block;
  width: 12px;
  height: 3px;
  background: #e6a23c;
  margin-right: 6px;
  vertical-align: middle;
}

.legend-item.out::before {
  content: '';
  display: inline-block;
  width: 12px;
  height: 3px;
  background: #f56c6c;
  margin-right: 6px;
  vertical-align: middle;
}

.card-body {
  padding: 16px;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  .chart-card.full-width {
    grid-column: span 1;
  }
}
</style>
