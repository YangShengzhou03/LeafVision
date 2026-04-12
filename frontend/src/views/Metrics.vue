<script setup>
import { ref, onMounted, computed } from 'vue'
import { Search, Download, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const queryForm = ref({
  metric: 'up',
  serverId: null,
  timeRange: '1h'
})

const metricOptions = ref([
  { label: '服务状态', value: 'up' },
  { label: 'CPU 使用率', value: 'process_cpu_seconds_total' },
  { label: '内存使用', value: 'process_resident_memory_bytes' },
  { label: '请求总数', value: 'http_requests_total' },
  { label: '请求延迟', value: 'http_request_duration_seconds' }
])

const serverOptions = ref([])
const loading = ref(false)
const queryHistory = ref([])
const chartData = ref([])
const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['Value'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: chartData.value.map(item => item.time)
  },
  yAxis: { type: 'value' },
  series: [
    {
      name: 'Value',
      type: 'line',
      smooth: true,
      data: chartData.value.map(item => item.value)
    }
  ]
}))

const timeRangeOptions = [
  { label: '最近 1 小时', value: '1h' },
  { label: '最近 6 小时', value: '6h' },
  { label: '最近 24 小时', value: '24h' },
  { label: '最近 7 天', value: '7d' }
]

const fetchServerList = async () => {
  try {
    const res = await api.getServerList()
    if (res.code === 200 && res.data) {
      serverOptions.value = res.data.map(server => ({
        label: server.name,
        value: server.id
      }))
      if (serverOptions.value.length > 0) {
        queryForm.value.serverId = serverOptions.value[0].value
      }
    }
  } catch (error) {
    console.error('获取服务器列表失败:', error)
  }
}

const handleQuery = async () => {
  if (!queryForm.value.serverId) {
    ElMessage.warning('请选择服务器')
    return
  }

  loading.value = true
  try {
    const res = await api.queryMetrics({
      metric: queryForm.value.metric,
      serverId: queryForm.value.serverId,
      timeRange: queryForm.value.timeRange
    })
    if (res.code === 200) {
      const data = res.data || {}
      if (data.values && Array.isArray(data.values)) {
        chartData.value = data.values.map(item => ({
          time: new Date(item[0] * 1000).toLocaleTimeString(),
          value: parseFloat(item[1])
        }))
      } else if (data.value !== undefined) {
        chartData.value = [{
          time: new Date().toLocaleTimeString(),
          value: parseFloat(data.value)
        }]
      }
      queryHistory.value.unshift({
        id: Date.now(),
        query: queryForm.value.metric,
        time: new Date().toLocaleString()
      })
      if (queryHistory.value.length > 10) {
        queryHistory.value.pop()
      }
      ElMessage.success('查询成功')
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error('查询指标失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  if (chartData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const dataStr = JSON.stringify(chartData.value, null, 2)
  const blob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `metrics_${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('数据已导出')
}

const handleRefresh = () => {
  handleQuery()
}

onMounted(() => {
  fetchServerList()
})
</script>

<template>
  <div class="metrics-page">
    <el-card class="query-card">
      <template #header>
        <div class="card-header">
          <span>指标查询</span>
          <div class="header-actions">
            <el-button :icon="Refresh" @click="handleRefresh">刷新</el-button>
            <el-button :icon="Download" @click="handleExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-form :model="queryForm" inline>
        <el-form-item label="服务器">
          <el-select v-model="queryForm.serverId" placeholder="选择服务器" style="width: 200px">
            <el-option
              v-for="item in serverOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="指标">
          <el-select v-model="queryForm.metric" placeholder="选择指标" style="width: 200px" filterable allow-create>
            <el-option
              v-for="item in metricOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-select v-model="queryForm.timeRange" style="width: 150px">
            <el-option
              v-for="item in timeRangeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery" :loading="loading">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="result-row">
      <el-col :xs="24" :md="16">
        <el-card>
          <template #header>
            <span>查询结果</span>
          </template>
          <v-chart v-if="chartData.length > 0" :option="chartOption" autoresize style="height: 400px" />
          <el-empty v-else description="请选择服务器和指标进行查询" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card>
          <template #header>
            <span>查询历史</span>
          </template>
          <div class="history-list">
            <div
              v-for="item in queryHistory"
              :key="item.id"
              class="history-item"
            >
              <div class="history-query">{{ item.query }}</div>
              <div class="history-time">{{ item.time }}</div>
            </div>
            <div v-if="queryHistory.length === 0" class="history-empty">
              暂无查询历史
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.metrics-page {
  padding: 0;
}

.query-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.result-row {
  margin-top: 20px;
}

.history-list {
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.history-item:hover {
  background-color: #f5f7fa;
}

.history-query {
  font-size: 14px;
  color: #303133;
  word-break: break-all;
}

.history-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.history-empty {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}
</style>
