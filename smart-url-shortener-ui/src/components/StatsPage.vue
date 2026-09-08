<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { getUrlStats } from '../services/urlApi.js'
import BaseButton from './BaseButton.vue'
import CopyButton from './CopyButton.vue'
import ShareButtons from './ShareButtons.vue'
import ClicksChart from './ClicksChart.vue'

const props = defineProps({
  shortUrl: { type: String, required: true }
})

const emit = defineEmits(['close'])

const stats = ref(null)
const loading = ref(true)
const refreshing = ref(false)
const error = ref('')
const notAvailable = ref(false)

const POLL_INTERVAL_MS = 15000
let pollTimer = null

onMounted(() => {
  fetchStats()
  pollTimer = setInterval(poll, POLL_INTERVAL_MS)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})

/* Background refresh — keeps the chart/KPIs live without a full reload */
function poll() {
  if (document.hidden) return
  if (loading.value || notAvailable.value || error.value) return
  fetchStats(true)
}

async function fetchStats(silent = false) {
  if (silent) refreshing.value = true
  else loading.value = true
  error.value = ''
  notAvailable.value = false

  try {
    const data = await getUrlStats(props.shortUrl)
    stats.value = data
  } catch (err) {
    console.error('Stats error:', err)
    if (err.message.includes('404') || err.message.includes('Not Found')) {
      notAvailable.value = true
    } else if (!silent) {
      error.value = err.message
    }
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const totalClicks = computed(() => Number(stats.value?.totalClicks ?? 0))
const uniqueVisitors = computed(() => Number(stats.value?.uniqueVisitors ?? 0))
const dailyClicks = computed(() =>
  Array.isArray(stats.value?.dailyClicks) ? stats.value.dailyClicks : []
)

const chartHasData = computed(() => dailyClicks.value.some((d) => Number(d.clicks) > 0))

const clicks30 = computed(() =>
  dailyClicks.value.reduce((sum, d) => sum + (Number(d.clicks) || 0), 0)
)

const activeDays = computed(
  () => dailyClicks.value.filter((d) => Number(d.clicks) > 0).length
)

const avgPerActiveDay = computed(() =>
  activeDays.value > 0 ? Math.round((clicks30.value / activeDays.value) * 10) / 10 : 0
)

function formatNumber(n) {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1).replace(/\.0$/, '') + 'M'
  if (n >= 1_000) return (n / 1_000).toFixed(1).replace(/\.0$/, '') + 'K'
  return String(n ?? 0)
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

function formatDateTime(dateStr) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<template>
  <div class="stats-page">
    <header class="stats-page__header">
      <div class="stats-page__header-inner">
        <div class="stats-page__logo">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
          </svg>
          <span>LinkSnip</span>
        </div>
        <BaseButton variant="ghost" size="sm" @click="emit('close')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" x2="6" y1="6" y2="18" />
            <line x1="6" x2="18" y1="6" y2="18" />
          </svg>
          Close
        </BaseButton>
      </div>
    </header>

    <main class="stats-page__main">
      <!-- Loading -->
      <div v-if="loading" class="stats-page__state">
        <div class="stats-page__spinner" />
        <p>Loading stats...</p>
      </div>

      <!-- Not available -->
      <div v-else-if="notAvailable" class="stats-page__state">
        <div class="stats-page__coming-soon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--color-primary)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 20V10" />
            <path d="M12 20V4" />
            <path d="M6 20v-6" />
          </svg>
          <h2>No stats yet</h2>
          <p>Stats for this link are not available. It may not exist or hasn't been clicked yet.</p>
          <div class="stats-page__coming-soon-info">
            <code>GET /api/v1/urls/{shortCode}/statistics</code>
          </div>
        </div>
        <BaseButton variant="secondary" size="md" @click="fetchStats">Retry</BaseButton>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="stats-page__state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--color-danger)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10" />
          <line x1="15" y1="9" x2="9" y2="15" />
          <line x1="9" y1="9" x2="15" y2="15" />
        </svg>
        <p class="stats-page__error-text">{{ error }}</p>
        <BaseButton variant="secondary" size="md" @click="fetchStats">Try again</BaseButton>
      </div>

      <!-- Content -->
      <div v-else-if="stats" class="stats-page__content">
        <div class="live-badge" :class="{ 'live-badge--refreshing': refreshing }">
          <span class="live-badge__dot" />
        </div>

        <!-- Section: Link overview -->
        <section class="stats-section">
          <h4 class="stats-section__title">Link</h4>
          <div class="stats-section__card">
            <div class="link-rows">
              <div class="link-rows__row">
                <span class="link-rows__label">Short URL</span>
                <a :href="shortUrl" target="_blank" rel="noopener noreferrer" class="link-rows__value link-rows__value--link">
                  {{ shortUrl }}
                </a>
              </div>
              <div class="link-rows__divider" />
              <div class="link-rows__row">
                <span class="link-rows__label">Original URL</span>
                <span class="link-rows__value link-rows__value--truncate" :title="stats.originalUrl">
                  {{ stats.originalUrl || '—' }}
                </span>
              </div>
              <div class="link-rows__divider" />
              <div class="link-rows__row">
                <span class="link-rows__label">Created</span>
                <span class="link-rows__value">{{ formatDate(stats.createdAt) }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- Section: KPI cards -->
        <section class="stats-section">
          <h4 class="stats-section__title">Overview</h4>
          <div class="kpi-grid">
            <div class="kpi kpi--primary">
              <span class="kpi__value kpi__value--primary">{{ formatNumber(totalClicks) }}</span>
              <span class="kpi__label">Total clicks</span>
            </div>
            <div class="kpi">
              <span class="kpi__value">{{ formatNumber(uniqueVisitors) }}</span>
              <span class="kpi__label">Unique visitors</span>
            </div>
            <div class="kpi">
              <span class="kpi__value">{{ formatNumber(clicks30) }}</span>
              <span class="kpi__label">Clicks · 30d</span>
            </div>
            <div class="kpi">
              <span class="kpi__value">{{ formatNumber(avgPerActiveDay) }}</span>
              <span class="kpi__label">Avg / active day</span>
            </div>
          </div>
        </section>

        <!-- Section: Chart -->
        <section class="stats-section">
          <h4 class="stats-section__title">Activity</h4>
          <div class="stats-section__card">
            <ClicksChart v-if="chartHasData" :data="dailyClicks" />
            <div v-else class="chart-placeholder">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 20V10" /><path d="M12 20V4" /><path d="M6 20v-6" />
              </svg>
              <span>No clicks in the last 30 days — share your link to see activity here.</span>
            </div>
          </div>
        </section>

        <!-- Section: Recent activity -->
        <section v-if="stats.lastClickAt" class="stats-section">
          <h4 class="stats-section__title">Latest</h4>
          <div class="stats-section__card">
            <div class="latest-row">
              <div class="latest-row__icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
              </div>
              <div>
                <span class="latest-row__label">Last clicked</span>
                <span class="latest-row__value">{{ formatDateTime(stats.lastClickAt) }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- Actions -->
        <div class="stats-page__actions">
          <CopyButton :text="shortUrl" />
          <ShareButtons :url="shortUrl" title="Check out this link!" />
          <BaseButton variant="ghost" size="md" @click="fetchStats">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10" />
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
            </svg>
            Refresh
          </BaseButton>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.stats-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--color-bg);
}

/* Header */
.stats-page__header {
  width: 100%;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface);
}

.stats-page__header-inner {
  max-width: 720px;
  margin: 0 auto;
  padding: 0.875rem 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stats-page__logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-text);
}

.stats-page__logo svg {
  color: var(--color-primary);
}

/* Main */
.stats-page__main {
  flex: 1;
  width: 100%;
  max-width: 720px;
  padding: 2rem 1.5rem 4rem;
}

/* States */
.stats-page__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1.25rem;
  padding: 4rem 1rem;
  text-align: center;
  color: var(--color-text-secondary);
}

.stats-page__coming-soon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}

.stats-page__coming-soon h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
  margin: 0;
}

.stats-page__coming-soon p {
  font-size: 0.9375rem;
  color: var(--color-text-secondary);
  max-width: 400px;
  line-height: 1.5;
}

.stats-page__coming-soon-info code {
  display: inline-block;
  padding: 0.5rem 1rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 0.8125rem;
  font-family: 'SF Mono', 'Fira Code', monospace;
  color: var(--color-primary);
}

.stats-page__spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.stats-page__error-text {
  color: var(--color-danger);
  font-weight: 500;
}

/* Content */
.stats-page__content {
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
}

/* Live badge */
.live-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  align-self: flex-start;
  font-size: 0.6875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-tertiary);
}

.live-badge__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--color-primary);
  animation: live-pulse 2s ease-in-out infinite;
}

.live-badge--refreshing .live-badge__dot {
  animation-duration: 0.6s;
}

@keyframes live-pulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 0 0 var(--color-primary-ring); }
  50% { opacity: 0.5; box-shadow: 0 0 0 4px var(--color-primary-ring); }
}

/* Sections */
.stats-section {
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
}

.stats-section__title {
  margin: 0;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-text-tertiary);
}

.stats-section__card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: 1.25rem;
  box-shadow: var(--shadow-sm);
}

/* Link rows */
.link-rows {
  display: flex;
  flex-direction: column;
}

.link-rows__row {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.5rem 0;
}

.link-rows__label {
  font-size: 0.6875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-text-tertiary);
}

.link-rows__value {
  font-size: 0.9375rem;
  color: var(--color-text);
  word-break: break-all;
}

.link-rows__value--link {
  color: var(--color-primary);
  font-weight: 600;
  text-decoration: none;
}

.link-rows__value--link:hover {
  text-decoration: underline;
}

.link-rows__value--truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-rows__divider {
  height: 1px;
  background: var(--color-border);
}

/* KPI grid */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.75rem;
}

.kpi {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  padding: 1.125rem 0.75rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  text-align: center;
}

.kpi--primary {
  background: linear-gradient(135deg, var(--color-primary-ring), transparent);
  border-color: var(--color-primary-ring);
}

.kpi__value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1;
  letter-spacing: -0.02em;
}

.kpi__value--primary {
  font-size: 1.875rem;
  color: var(--color-primary);
}

.kpi__label {
  font-size: 0.6875rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-tertiary);
}

/* Chart placeholder */
.chart-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.625rem;
  padding: 2rem 1rem;
  text-align: center;
  color: var(--color-text-tertiary);
  font-size: 0.8125rem;
  line-height: 1.5;
  max-width: 360px;
  margin: 0 auto;
}

/* Latest row */
.latest-row {
  display: flex;
  align-items: center;
  gap: 0.875rem;
}

.latest-row__icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--color-primary-ring);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.latest-row__label {
  display: block;
  font-size: 0.6875rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-tertiary);
}

.latest-row__value {
  display: block;
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-text);
}

/* Actions */
.stats-page__actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
  padding-top: 0.25rem;
}

@media (max-width: 640px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
