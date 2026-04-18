<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, GaugeChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Refresh, TrendCharts, Cpu, Monitor } from '@element-plus/icons-vue'
import { getDashboardData } from '@/api'
import { ElMessage } from 'element-plus'
import { generateTimeLabels, getProgressColor } from '@/utils'

use([CanvasRenderer, LineChart, GaugeChart, GridComponent, TooltipComponent, LegendComponent])

const { t } = useI18n()

const formatPercent = (value) => {
  if (value == null) return '0.00'
  return Number(value).toFixed(2)
}

const formatStatValue = (card) => {
  if (card.value == null) return '0'
  if (typeof card.value === 'string') {
    return card.value
  }
  if (card.title?.includes('CPU') || card.title?.includes('内存') || card.title?.includes('使用率')) {
    return formatPercent(card.value) + '%'
  }
  return String(card.value)
}

const statsCards = ref([])
const serverList = ref([])
const loading = ref(false)

const cpuOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: cpuOptionData.value.xAxisData },
  yAxis: { type: 'value', max: 100 },
  series: [{ name: t('CPU 使用趋势'), type: 'line', smooth: true, symbol: 'none', areaStyle: { opacity: 0.2 }, lineStyle: { width: 2 }, data: cpuOptionData.value.seriesData }]
}))

const cpuOptionData = ref({ xAxisData: [], seriesData: [] })

const memoryOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: memoryOptionData.value.xAxisData },
  yAxis: { type: 'value', max: 100 },
  series: [{ name: t('内存使用趋势'), type: 'line', smooth: true, symbol: 'none', areaStyle: { opacity: 0.2 }, lineStyle: { width: 2, color: '#67c23a' }, data: memoryOptionData.value.seriesData }]
}))

const memoryOptionData = ref({ xAxisData: [], seriesData: [] })

const healthOption = computed(() => ({
  series: [{
    type: 'gauge',
    startAngle: 180, endAngle: 0, min: 0, max: 100, splitNumber: 5,
    axisLine: { lineStyle: { width: 10, color: [[0.3, '#67c23a'], [0.7, '#e6a23c'], [1, '#f56c6c']] } },
    pointer: { icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z', length: '60%', width: 6, offsetCenter: [0, '-10%'], itemStyle: { color: 'auto' } },
    axisTick: { length: 5, lineStyle: { color: 'auto', width: 1 } },
    splitLine: { length: 10, lineStyle: { color: 'auto', width: 1.5 } },
    axisLabel: { fontSize: 10, distance: -35, formatter: (v) => v + '%' },
    title: { offsetCenter: [0, '28%'], fontSize: 14 },
    detail: { fontSize: 24, offsetCenter: [0, '0%'], valueAnimation: true, formatter: (v) => Math.round(v) + '%', color: 'auto' },
    data: [{ value: serverList.value.filter(s => s.status === 'online').length / (serverList.value.length || 1) * 100, name: t('健康度评分') }]
  }]
}))

const fetchDashboardData = async () => {
  loading.value = true
  try {
    const res = await getDashboardData()
    if (res.code === 200 && res.data) {
      const d = res.data
      statsCards.value = d.statsCards || []
      serverList.value = d.serverList || []
      if (d.cpuTrend?.length > 0) {
        cpuOptionData.value.seriesData = d.cpuTrend
        cpuOptionData.value.xAxisData = generateTimeLabels(d.cpuTrend.length)
      }
      if (d.memoryTrend?.length > 0) {
        memoryOptionData.value.seriesData = d.memoryTrend
        memoryOptionData.value.xAxisData = generateTimeLabels(d.memoryTrend.length)
      }
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => { fetchDashboardData(); ElMessage.success(t('数据已刷新')) }

const icons = [Monitor, Cpu, TrendCharts]
const iconColors = ['#1c69d4', '#67c23a', '#e6a23c', '#f56c6c']

let refreshTimer = null

onMounted(() => { fetchDashboardData(); refreshTimer = setInterval(() => fetchDashboardData(), 60000) })
onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<template>
  <div class="dashboard">
    <div class="stats-row">
      <div v-for="(card, i) in statsCards" :key="card.title" class="stat-card">
        <div class="stat-icon" :style="{ background: iconColors[i % 4] }">
          <el-icon :size="20"><component :is="icons[i % 3]" /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ formatStatValue(card) }}</span>
          <span class="stat-label">{{ card.title }}</span>
        </div>
      </div>
    </div>

    <div class="main-grid">
      <div class="charts-section">
        <div class="charts-row">
          <div class="chart-card">
            <div class="card-header">
              <span class="card-title">{{ t('CPU 使用趋势') }}</span>
              <button class="btn-refresh" @click="handleRefresh" :disabled="loading">
                <el-icon><Refresh /></el-icon>
                <span>{{ t('刷新') }}</span>
              </button>
            </div>
            <div class="card-body">
              <v-chart :option="cpuOption" autoresize style="height: 240px" />
            </div>
          </div>
          <div class="chart-card">
            <div class="card-header">
              <span class="card-title">{{ t('内存使用趋势') }}</span>
            </div>
            <div class="card-body">
              <v-chart :option="memoryOption" autoresize style="height: 240px" />
            </div>
          </div>
        </div>

        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">{{ t('服务器状态') }}</span>
          </div>
          <div class="card-body">
            <table class="data-table">
              <thead>
                <tr>
                  <th>{{ t('服务器') }}</th>
                  <th>{{ t('状态') }}</th>
                  <th>{{ t('CPU') }}</th>
                  <th>{{ t('内存') }}</th>
                  <th>{{ t('版本') }}</th>
                  <th>{{ t('运行时间') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="server in serverList" :key="server.id">
                  <td>
                    <div class="server-info">
                      <span class="server-name">{{ server.name }}</span>
                      <span class="server-ip">{{ server.ip }}:{{ server.port }}</span>
                    </div>
                  </td>
                  <td>
                    <span :class="['status-badge', server.status === 'online' ? 'online' : 'offline']">
                      {{ server.status === 'online' ? t('在线') : t('离线') }}
                    </span>
                  </td>
                  <td>
                    <div class="progress-bar">
                      <div class="progress-fill" :style="{ width: (server.cpuUsage || 0) + '%', background: getProgressColor(server.cpuUsage) }"></div>
                    </div>
                    <span class="progress-text">{{ formatPercent(server.cpuUsage) }}%</span>
                  </td>
                  <td>
                    <div class="progress-bar">
                      <div class="progress-fill" :style="{ width: (server.memoryUsage || 0) + '%', background: getProgressColor(server.memoryUsage) }"></div>
                    </div>
                    <span class="progress-text">{{ formatPercent(server.memoryUsage) }}%</span>
                  </td>
                  <td>{{ server.version }}</td>
                  <td>{{ server.uptime }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="side-section">
        <div class="chart-card side-card full-height">
          <div class="card-header">
            <span class="card-title">{{ t('系统健康度') }}</span>
          </div>
          <div class="card-body">
            <v-chart :option="healthOption" autoresize style="height: 280px" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #ffffff;
  border: 1px solid var(--color-border);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  color: #ffffff;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 300;
  color: var(--color-text-primary);
  line-height: 1.15;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-text-secondary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.main-grid {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 16px;
}

.charts-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.chart-card {
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

.btn-refresh {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 700;
  padding: 6px 12px;
  cursor: pointer;
  transition: all 0.15s ease;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.btn-refresh:hover {
  background: var(--site-context-highlight-color);
  border-color: var(--site-context-highlight-color);
  color: #ffffff;
}

.btn-refresh:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.card-body {
  padding: 20px;
}

.side-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: calc(100vh - 180px);
  overflow: hidden;
}

.side-card .card-body {
  padding: 16px;
}

.full-height {
  height: 100%;
  overflow: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 12px 16px;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-secondary);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--color-border);
}

.data-table td {
  padding: 12px 16px;
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

.server-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.server-name {
  font-weight: 700;
  color: var(--color-text-primary);
}

.server-ip {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.status-badge.online {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.status-badge.offline {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: #e0e0e0;
  margin-bottom: 4px;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: var(--color-text-secondary);
}

@media (max-width: 1280px) {
  .main-grid {
    grid-template-columns: 1fr;
  }
  .side-section {
    flex-direction: row;
  }
  .side-card {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-row {
    grid-template-columns: 1fr;
  }
  .side-section {
    flex-direction: column;
  }
}
</style>
