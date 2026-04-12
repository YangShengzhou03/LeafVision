<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, GaugeChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Cpu, Monitor, Connection, Timer, Refresh } from '@element-plus/icons-vue'
import { api } from '@/api'
import { ElMessage } from 'element-plus'

use([CanvasRenderer, LineChart, GaugeChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

const cpuChartRef = ref(null)
const memoryChartRef = ref(null)
const networkChartRef = ref(null)

const statsCards = ref([])
const serverList = ref([])
const loading = ref(false)

const cpuOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: []
  },
  yAxis: { type: 'value', max: 100 },
  series: [
    {
      name: 'CPU使用率',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      data: []
    }
  ]
})

const memoryOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: []
  },
  yAxis: { type: 'value', max: 100 },
  series: [
    {
      name: '内存使用率',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      data: []
    }
  ]
})

const networkOption = ref({
  tooltip: { trigger: 'item' },
  legend: { orient: 'vertical', left: 'left' },
  series: [
    {
      name: '网络流量',
      type: 'pie',
      radius: '50%',
      data: [
        { value: 1048, name: '入站流量' },
        { value: 735, name: '出站流量' },
        { value: 580, name: '内部通信' }
      ]
    }
  ]
})

const iconMap = {
  cpu: Cpu,
  memory: Monitor,
  network: Connection,
  uptime: Timer
}

const fetchDashboardData = async () => {
  loading.value = true
  try {
    const res = await api.getDashboardData()
    if (res.code === 200 && res.data) {
      const data = res.data
      statsCards.value = (data.statsCards || []).map(card => ({
        title: card.title,
        value: card.value,
        icon: iconMap[card.type] || Cpu,
        color: card.color,
        type: card.type
      }))
      serverList.value = data.serverList || []
      if (data.cpuTrend && data.cpuTrend.length > 0) {
        cpuOption.value.series[0].data = data.cpuTrend
        cpuOption.value.xAxis.data = generateTimeLabels(data.cpuTrend.length)
      }
      if (data.memoryTrend && data.memoryTrend.length > 0) {
        memoryOption.value.series[0].data = data.memoryTrend
        memoryOption.value.xAxis.data = generateTimeLabels(data.memoryTrend.length)
      }
      if (data.networkDistribution && data.networkDistribution.length > 0) {
        networkOption.value.series[0].data = data.networkDistribution
      }
    }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  } finally {
    loading.value = false
  }
}

const generateTimeLabels = (count) => {
  const labels = []
  const now = new Date()
  for (let i = count - 1; i >= 0; i--) {
    const time = new Date(now.getTime() - i * 5 * 60 * 1000)
    labels.push(time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }))
  }
  return labels
}

const handleRefresh = () => {
  fetchDashboardData()
  ElMessage.success('数据已刷新')
}

let refreshTimer = null

onMounted(() => {
  fetchDashboardData()
  refreshTimer = setInterval(() => {
    fetchDashboardData()
  }, 60000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in statsCards" :key="card.title">
        <el-card class="stats-card" shadow="hover">
          <div class="stats-content">
            <div class="stats-icon" :style="{ backgroundColor: card.color + '20', color: card.color }">
              <el-icon :size="28"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ card.value }}</div>
              <div class="stats-title">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>CPU 使用率趋势</span>
              <el-button type="primary" link :icon="Refresh" @click="handleRefresh" :loading="loading">刷新</el-button>
            </div>
          </template>
          <v-chart ref="cpuChartRef" :option="cpuOption" autoresize style="height: 300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>内存使用率趋势</span>
            </div>
          </template>
          <v-chart ref="memoryChartRef" :option="memoryOption" autoresize style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <span>网络流量分布</span>
          </template>
          <v-chart ref="networkChartRef" :option="networkOption" autoresize style="height: 300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="server-card">
          <template #header>
            <span>服务器状态</span>
          </template>
          <el-table :data="serverList" style="width: 100%" v-loading="loading">
            <el-table-column prop="name" label="服务器名称" />
            <el-table-column prop="ip" label="IP 地址" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'online' ? 'success' : 'danger'" size="small">
                  {{ row.status === 'online' ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="cpuUsage" label="CPU">
              <template #default="{ row }">
                <el-progress :percentage="row.cpuUsage ? Math.round(row.cpuUsage) : 0" :color="row.cpuUsage > 80 ? '#F56C6C' : '#409EFF'" />
              </template>
            </el-table-column>
            <el-table-column prop="memoryUsage" label="内存">
              <template #default="{ row }">
                <el-progress :percentage="row.memoryUsage ? Math.round(row.memoryUsage) : 0" :color="row.memoryUsage > 80 ? '#F56C6C' : '#67C23A'" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-content {
  display: flex;
  align-items: center;
}

.stats-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stats-title {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card, .server-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
