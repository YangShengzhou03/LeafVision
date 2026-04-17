<script setup>
import { ref, onMounted, computed } from 'vue'
import { Download, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent])

const queryForm = ref({ metric: 'up', serverId: null, timeRange: '1h' })

const metricOptions = [
  { label: '服务状态', value: 'up' },
  { label: 'CPU 使用率', value: 'process_cpu_seconds_total' },
  { label: '内存使用', value: 'process_resident_memory_bytes' },
  { label: '请求总数', value: 'http_requests_total' }
]

const serverOptions = ref([])
const loading = ref(false)
const chartData = ref([])

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis', backgroundColor: '#252525', borderColor: '#404040', textStyle: { color: '#ccc' } },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: chartData.value.map(item => item.time),
    axisLine: { lineStyle: { color: '#333' } },
    axisLabel: { color: '#888' }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#222' } },
    axisLabel: { color: '#888' }
  },
  series: [{
    type: 'line',
    smooth: true,
    symbol: 'none',
    areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(99, 102, 241, 0.25)' }, { offset: 1, color: 'rgba(99, 102, 241, 0.02)' }] } },
    lineStyle: { color: '#6366f1', width: 1.5 },
    data: chartData.value.map(item => item.value)
  }]
}))

const timeRangeOptions = [
  { label: '1小时', value: '1h' },
  { label: '6小时', value: '6h' },
  { label: '24小时', value: '24h' },
  { label: '7天', value: '7d' }
]

const fetchServerList = async () => {
  const res = await api.getServerList()
  if (res.code === 200 && res.data) {
    serverOptions.value = res.data.map(s => ({ label: s.name, value: s.id }))
    if (serverOptions.value.length > 0) queryForm.value.serverId = serverOptions.value[0].value
  }
}

const handleQuery = async () => {
  if (!queryForm.value.serverId) return ElMessage.warning('请选择服务器')
  loading.value = true
  try {
    const res = await api.queryMetrics(queryForm.value)
    if (res.code === 200) {
      const data = res.data || {}
      if (data.values?.length) {
        chartData.value = data.values.map(item => ({
          time: new Date(item[0] * 1000).toLocaleTimeString(),
          value: parseFloat(item[1])
        }))
      }
      ElMessage.success('查询成功')
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  if (!chartData.value.length) return ElMessage.warning('无数据')
  const blob = new Blob([JSON.stringify(chartData.value, null, 2)], { type: 'application/json' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `metrics_${Date.now()}.json`
  a.click()
  ElMessage.success('已导出')
}

onMounted(() => fetchServerList())
</script>

<template>
  <div class="page-container">
    <div class="panel-card">
      <div class="panel-header"><span>查询条件</span></div>
      <el-form :inline="true" class="query-form">
        <el-form-item label="服务器">
          <el-select v-model="queryForm.serverId" placeholder="选择服务器" size="small">
            <el-option v-for="item in serverOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="指标">
          <el-select v-model="queryForm.metric" size="small">
            <el-option v-for="item in metricOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-select v-model="queryForm.timeRange" size="small">
            <el-option v-for="item in timeRangeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="handleQuery" :loading="loading"><el-icon><Search /></el-icon> 查询</el-button>
          <el-button size="small" @click="handleExport" :disabled="!chartData.length"><el-icon><Download /></el-icon> 导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="panel-card">
      <div class="panel-header"><span>查询结果</span></div>
      <v-chart v-if="chartData.length > 0" :option="chartOption" autoresize style="height: 380px" />
      <div v-else class="empty-tip">请先查询数据</div>
    </div>
  </div>
</template>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }

.panel-card {
  background: #1e1e1e;
  border: 1px solid #2a2a2a;
  border-radius: 3px;
  padding: 16px;
}

.panel-header {
  font-size: 13px;
  font-weight: 500;
  color: #bbb;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #2a2a2a;
}

.query-form :deep(.el-form-item__label) { color: #888; font-size: 13px; }

.empty-tip {
  text-align: center;
  color: #555;
  padding: 60px 0;
  font-size: 14px;
}
</style>
