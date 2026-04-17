<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, GaugeChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Refresh, TrendCharts, Cpu, Monitor, Connection } from '@element-plus/icons-vue'
import { api } from '@/api'
import { ElMessage } from 'element-plus'

use([CanvasRenderer, LineChart, PieChart, GaugeChart, GridComponent, TooltipComponent, LegendComponent])

const statsCards = ref([])
const serverList = ref([])
const loading = ref(false)

const cpuOption = ref({
  tooltip: { trigger: 'axis', backgroundColor: '#252525', borderColor: '#404040', textStyle: { color: '#ccc' } },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: [], axisLine: { lineStyle: { color: '#333' } }, axisLabel: { color: '#888' } },
  yAxis: { type: 'value', max: 100, splitLine: { lineStyle: { color: '#222' } }, axisLabel: { color: '#888' } },
  series: [{ name: 'CPU使用率', type: 'line', smooth: true, symbol: 'none', areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(99, 102, 241, 0.3)' }, { offset: 1, color: 'rgba(99, 102, 241, 0.02)' }] } }, lineStyle: { color: '#6366f1', width: 1.5 }, data: [] }]
})

const memoryOption = ref({
  tooltip: { trigger: 'axis', backgroundColor: '#252525', borderColor: '#404040', textStyle: { color: '#ccc' } },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: [], axisLine: { lineStyle: { color: '#333' } }, axisLabel: { color: '#888' } },
  yAxis: { type: 'value', max: 100, splitLine: { lineStyle: { color: '#222' } }, axisLabel: { color: '#888' } },
  series: [{ name: '内存使用率', type: 'line', smooth: true, symbol: 'none', areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(34, 197, 94, 0.3)' }, { offset: 1, color: 'rgba(34, 197, 94, 0.02)' }] } }, lineStyle: { color: '#22c55e', width: 1.5 }, data: [] }]
})

const networkOption = ref({
  tooltip: { trigger: 'item', backgroundColor: '#252525', borderColor: '#404040', textStyle: { color: '#ccc' } },
  legend: { bottom: '2%', left: 'center', textStyle: { color: '#888' } },
  series: [{ name: '网络流量', type: 'pie', radius: ['45%', '70%'], avoidLabelOverlap: false, itemStyle: { borderRadius: 4, borderColor: '#1a1a1a', borderWidth: 1 }, label: { show: false }, emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold', color: '#fff' } }, labelLine: { show: false }, data: [] }]
})

const healthOption = computed(() => ({
  series: [{
    type: 'gauge',
    startAngle: 180, endAngle: 0, min: 0, max: 100, splitNumber: 5,
    axisLine: { lineStyle: { width: 10, color: [[0.3, '#22c55e'], [0.7, '#f59e0b'], [1, '#ef4444']] } },
    pointer: { icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z', length: '60%', width: 6, offsetCenter: [0, '-10%'], itemStyle: { color: 'auto' } },
    axisTick: { length: 5, lineStyle: { color: 'auto', width: 1 } },
    splitLine: { length: 10, lineStyle: { color: 'auto', width: 1.5 } },
    axisLabel: { color: '#888', fontSize: 9, distance: -35, formatter: (v) => v + '%' },
    title: { offsetCenter: [0, '28%'], fontSize: 13, color: '#999' },
    detail: { fontSize: 22, offsetCenter: [0, '0%'], valueAnimation: true, formatter: (v) => Math.round(v) + '%', color: 'auto' },
    data: [{ value: serverList.value.filter(s => s.status === 'online').length / (serverList.value.length || 1) * 100, name: '健康度' }]
  }]
}))

const fetchDashboardData = async () => {
  loading.value = true
  try {
    const res = await api.getDashboardData()
    if (res.code === 200 && res.data) {
      const d = res.data
      statsCards.value = d.statsCards || []
      serverList.value = d.serverList || []
      if (d.cpuTrend?.length > 0) {
        cpuOption.value.series[0].data = d.cpuTrend
        cpuOption.value.xAxis.data = generateTimeLabels(d.cpuTrend.length)
      }
      if (d.memoryTrend?.length > 0) {
        memoryOption.value.series[0].data = d.memoryTrend
        memoryOption.value.xAxis.data = generateTimeLabels(d.memoryTrend.length)
      }
      if (d.networkDistribution?.length > 0) networkOption.value.series[0].data = d.networkDistribution
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const generateTimeLabels = (count) => {
  const labels = []
  const now = new Date()
  for (let i = count - 1; i >= 0; i--) {
    const t = new Date(now.getTime() - i * 5 * 60 * 1000)
    labels.push(t.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }))
  }
  return labels
}

const getProgressColor = (val) => val > 80 ? '#ef4444' : val > 60 ? '#f59e0b' : '#6366f1'

const handleRefresh = () => { fetchDashboardData(); ElMessage.success('数据已刷新') }

let refreshTimer = null

onMounted(() => { fetchDashboardData(); refreshTimer = setInterval(() => fetchDashboardData(), 60000) })
onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<template>
  <div class="dashboard">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6" v-for="(card, index) in statsCards" :key="card.title">
        <div class="stat-card" :class="'type-' + (index % 4)">
          <div class="stat-icon">
            <el-icon :size="20"><Monitor v-if="index === 0" /><Cpu v-else-if="index === 1" /><Connection v-else-if="index === 2" /><TrendCharts v-else /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-title">{{ card.title }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="18">
        <el-row :gutter="16" class="chart-row">
          <el-col :span="12">
            <div class="panel-card">
              <div class="panel-header">
                <span>CPU 使用率趋势</span>
                <el-button type="primary" link size="small" @click="handleRefresh" :loading="loading"><el-icon><Refresh /></el-icon> 刷新</el-button>
              </div>
              <v-chart :option="cpuOption" autoresize style="height: 240px" />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="panel-card">
              <div class="panel-header"><span>内存使用率趋势</span></div>
              <v-chart :option="memoryOption" autoresize style="height: 240px" />
            </div>
          </el-col>
        </el-row>

        <div class="panel-card">
          <div class="panel-header"><span>服务器状态</span></div>
          <el-table :data="serverList" style="width: 100%" class="dark-table">
            <el-table-column label="服务器" min-width="180">
              <template #default="{ row }">
                <div class="server-cell">
                  <span class="server-name">{{ row.name }}</span>
                  <span class="server-ip">{{ row.ip }}:{{ row.port }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <span class="status-dot" :class="row.status"></span>
                {{ row.status === 'online' ? '在线' : '离线' }}
              </template>
            </el-table-column>
            <el-table-column label="CPU" width="160">
              <template #default="{ row }">
                <div class="progress-wrapper">
                  <div class="progress-bar" :style="{ width: (row.cpuUsage || 0) + '%', background: getProgressColor(row.cpuUsage) }"></div>
                  <span>{{ row.cpuUsage || 0 }}%</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="内存" width="160">
              <template #default="{ row }">
                <div class="progress-wrapper">
                  <div class="progress-bar" :style="{ width: (row.memoryUsage || 0) + '%', background: getProgressColor(row.memoryUsage) }"></div>
                  <span>{{ row.memoryUsage || 0 }}%</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="version" label="版本" width="80" align="center" />
            <el-table-column prop="uptime" label="运行时间" width="100" align="center" />
          </el-table>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="panel-card health-panel">
          <div class="panel-header"><span>系统健康度</span></div>
          <v-chart :option="healthOption" autoresize style="height: 170px" />
        </div>
        <div class="panel-card" style="margin-top: 16px">
          <div class="panel-header"><span>网络流量分布</span></div>
          <v-chart :option="networkOption" autoresize style="height: 210px" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 16px; }
.stats-row { margin-bottom: 0; }

.stat-card {
  background: #1e1e1e;
  border: 1px solid #2a2a2a;
  border-radius: 3px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-card:hover { border-color: #383838; }

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.type-0 .stat-icon { background-color: #6366f1; }
.type-1 .stat-icon { background-color: #22c55e; }
.type-2 .stat-icon { background-color: #f59e0b; }
.type-3 .stat-icon { background-color: #ef4444; }

.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: 600; color: #e5e5e5; line-height: 1.2; }
.stat-title { font-size: 13px; color: #777; margin-top: 4px; }

.chart-row { margin-bottom: 16px; }

.panel-card {
  background: #1e1e1e;
  border: 1px solid #2a2a2a;
  border-radius: 3px;
  padding: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  font-weight: 500;
  color: #bbb;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #2a2a2a;
}

.server-cell { display: flex; flex-direction: column; gap: 2px; }
.server-name { font-weight: 500; color: #ddd; }
.server-ip { font-size: 12px; color: #666; }

.status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  margin-right: 6px;
}
.status-dot.online { background: #22c55e; box-shadow: 0 0 4px rgba(34, 197, 94, 0.5); }
.status-dot.offline { background: #ef4444; box-shadow: 0 0 4px rgba(239, 68, 68, 0.5); }

.progress-wrapper { display: flex; align-items: center; gap: 8px; padding: 0 6px; }
.progress-bar { height: 5px; border-radius: 2px; transition: width 0.3s; flex: 1; }
.progress-wrapper span { color: #999; font-size: 12px; min-width: 30px; text-align: right; }

.health-panel { height: fit-content; }
</style>
