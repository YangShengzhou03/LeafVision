<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getSettings } from '@/api'

const userStore = useUserStore()

const watermarkEnabled = ref(false)
const currentTime = ref(new Date())

const watermarkContent = computed(() => {
  if (!watermarkEnabled.value) return []
  const email = userStore.userInfo?.email || userStore.username || 'user'
  const time = currentTime.value.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
  return [email, time]
})

let updateTimer = null

const handleWatermarkChange = (event) => {
  watermarkEnabled.value = event.detail.enabled
}

const fetchWatermarkSetting = async () => {
  try {
    const res = await getSettings()
    if (res.code === 200 && res.data) {
      watermarkEnabled.value = res.data.watermarkEnabled === true || res.data.watermarkEnabled === 'true'
    }
  } catch (error) {
    console.error('Failed to fetch watermark setting:', error)
  }
}

onMounted(() => {
  fetchWatermarkSetting()
  window.addEventListener('watermark-change', handleWatermarkChange)
  
  updateTimer = setInterval(() => {
    currentTime.value = new Date()
  }, 60000)
})

onUnmounted(() => {
  window.removeEventListener('watermark-change', handleWatermarkChange)
  if (updateTimer) {
    clearInterval(updateTimer)
  }
})
</script>

<template>
  <el-watermark
    v-if="watermarkEnabled"
    :content="watermarkContent"
    :font="{ fontSize: 14, color: 'rgba(0, 0, 0, 0.15)' }"
    :gap="[100, 100]"
    :rotate="-20"
  >
    <router-view />
  </el-watermark>
  <router-view v-else />
</template>
