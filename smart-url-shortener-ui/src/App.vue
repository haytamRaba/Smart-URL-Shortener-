<script setup>
import { ref, computed, onMounted } from 'vue'
import './style.css'
import UrlShortener from './components/UrlShortener.vue'
import StatsPage from './components/StatsPage.vue'

const statsUrl = ref('')

function checkRoute() {
  const params = new URLSearchParams(window.location.search)
  statsUrl.value = params.get('stats') || ''
}

onMounted(() => {
  checkRoute()
  window.addEventListener('popstate', checkRoute)
})

const isStatsView = computed(() => !!statsUrl.value)

function closeStats() {
  // Remove the stats param and go back to main view
  const url = new URL(window.location.href)
  url.searchParams.delete('stats')
  window.history.pushState({}, '', url.toString())
  statsUrl.value = ''
}
</script>

<template>
  <StatsPage
    v-if="isStatsView"
    :short-url="statsUrl"
    @close="closeStats"
  />
  <UrlShortener v-else />
</template>
