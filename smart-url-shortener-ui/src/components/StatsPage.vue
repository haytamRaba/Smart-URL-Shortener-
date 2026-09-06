<script setup>
import { ref, onMounted } from 'vue'
import { getUrlStats } from '../services/urlApi.js'
import BaseButton from './BaseButton.vue'
import CopyButton from './CopyButton.vue'
import ShareButtons from './ShareButtons.vue'

const props = defineProps({
  shortUrl: { type: String, required: true }
})

const emit = defineEmits(['close'])

const stats = ref(null)
const loading = ref(true)
const error = ref('')
const notAvailable = ref(false)

onMounted(fetchStats)

async function fetchStats() {
  loading.value = true
  error.value = ''
  notAvailable.value = false

  try {
    const data = await getUrlStats(props.shortUrl)
    stats.value = data
  } catch (err) {
    console.error('Stats error:', err)
    if (err.message.includes('404') || err.message.includes('Not Found')) {
      notAvailable.value = true
    } else {
      error.value = err.message
    }
  } finally {
    loading.value = false
  }
}

function formatNumber(n) {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1).replace(/\.0$/, '') + 'M'
  if (n >= 1_000) return (n / 1_000).toFixed(1).replace(/\.0$/, '') + 'K'
  return String(n ?? 0)
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

function formatDateTime(dateStr) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
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
          <h2>Stats Coming Soon</h2>
          <p>Click tracking will be available once the backend adds the stats endpoint.</p>
          <div class="stats-page__coming-soon-info">
            <code>GET /api/v1/urls/stats</code>
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

      <!-- Stats -->
      <div v-else-if="stats" class="stats-page__content">
        <!-- Hero counter -->
        <div class="stats-page__hero">
          <div class="stats-page__counter">
            <span class="stats-page__counter-number">
              {{ formatNumber(stats.totalClicks ?? stats.clickCount ?? stats.clicks ?? 0) }}
            </span>
            <span class="stats-page__counter-label">total clicks</span>
          </div>
        </div>

        <!-- Unique visitors -->
        <div v-if="stats.uniqueVisitors != null && stats.uniqueVisitors !== undefined" class="stats-page__unique">
          <div class="stats-page__unique-card">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
              <circle cx="9" cy="7" r="4" />
              <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
              <path d="M16 3.13a4 4 0 0 1 0 7.75" />
            </svg>
            <div>
              <span class="stats-page__unique-label">Unique visitors</span>
              <span class="stats-page__unique-value">{{ formatNumber(stats.uniqueVisitors) }}</span>
            </div>
          </div>
        </div>

        <!-- Link info card -->
        <div class="stats-page__card">
          <div class="stats-page__card-row">
            <span class="stats-page__card-label">Short URL</span>
            <div class="stats-page__card-value">
              <a :href="shortUrl" target="_blank" rel="noopener noreferrer" class="stats-page__link">
                {{ shortUrl }}
              </a>
            </div>
          </div>
          <div class="stats-page__card-divider" />
          <div class="stats-page__card-row">
            <span class="stats-page__card-label">Original URL</span>
            <div class="stats-page__card-value stats-page__card-value--truncate" :title="stats.originalUrl">
              {{ stats.originalUrl || '—' }}
            </div>
          </div>
        </div>

        <!-- Details grid -->
        <div class="stats-page__grid">
          <div class="stats-page__detail">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect width="18" height="18" x="3" y="4" rx="2" ry="2" />
              <line x1="16" x2="16" y1="2" y2="6" />
              <line x1="8" x2="8" y1="2" y2="6" />
              <line x1="3" x2="21" y1="10" y2="10" />
            </svg>
            <div>
              <span class="stats-page__detail-label">Created</span>
              <span class="stats-page__detail-value">{{ formatDate(stats.createdAt ?? stats.created) }}</span>
            </div>
          </div>

          <div class="stats-page__detail" v-if="stats.lastClickedAt || stats.lastClick">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
            <div>
              <span class="stats-page__detail-label">Last clicked</span>
              <span class="stats-page__detail-value">{{ formatDateTime(stats.lastClickedAt ?? stats.lastClick) }}</span>
            </div>
          </div>

          <div class="stats-page__detail" v-if="stats.referrer || stats.referer">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M15 3h6v6" />
              <path d="M10 14 21 3" />
              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
            </svg>
            <div>
              <span class="stats-page__detail-label">Top referrer</span>
              <span class="stats-page__detail-value">{{ stats.referrer || stats.referer }}</span>
            </div>
          </div>

          <div class="stats-page__detail" v-if="stats.browser">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10" />
              <line x1="2" x2="22" y1="12" y2="12" />
              <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
            </svg>
            <div>
              <span class="stats-page__detail-label">Browser</span>
              <span class="stats-page__detail-value">{{ stats.browser }}</span>
            </div>
          </div>

          <div class="stats-page__detail" v-if="stats.country">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z" />
              <circle cx="12" cy="10" r="3" />
            </svg>
            <div>
              <span class="stats-page__detail-label">Country</span>
              <span class="stats-page__detail-value">{{ stats.country }}</span>
            </div>
          </div>
        </div>

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
  max-width: 640px;
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
  max-width: 640px;
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

/* Coming soon */
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

.stats-page__coming-soon-info {
  margin-top: 0.5rem;
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
  gap: 1.5rem;
}

/* Hero counter */
.stats-page__hero {
  text-align: center;
  padding: 2.5rem 1rem;
  background: linear-gradient(135deg, var(--color-primary-ring), transparent);
  border: 1px solid var(--color-primary-ring);
  border-radius: 20px;
}

.stats-page__counter {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stats-page__counter-number {
  font-size: 4rem;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
  letter-spacing: -0.04em;
}

.stats-page__counter-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

/* Card */
.stats-page__card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
}

.stats-page__card-row {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.5rem 0;
}

.stats-page__card-label {
  font-size: 0.6875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-text-tertiary);
}

.stats-page__card-value {
  font-size: 0.9375rem;
  color: var(--color-text);
}

.stats-page__card-value--truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stats-page__card-divider {
  height: 1px;
  background: var(--color-border);
  margin: 0.25rem 0;
}

.stats-page__link {
  color: var(--color-primary);
  font-weight: 600;
  text-decoration: none;
  word-break: break-all;
}

.stats-page__link:hover {
  text-decoration: underline;
}

/* Details grid */
.stats-page__grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
}

.stats-page__detail {
  display: flex;
  align-items: flex-start;
  gap: 0.625rem;
  padding: 1rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 14px;
}

.stats-page__detail svg {
  flex-shrink: 0;
  color: var(--color-text-tertiary);
  margin-top: 0.125rem;
}

.stats-page__detail-label {
  display: block;
  font-size: 0.6875rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-text-tertiary);
}

.stats-page__detail-value {
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text);
  margin-top: 0.125rem;
}

/* Unique visitors card */
.stats-page__unique {
  display: flex;
  justify-content: center;
}

.stats-page__unique-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 14px;
}

.stats-page__unique-card svg {
  color: var(--color-primary);
  flex-shrink: 0;
}

.stats-page__unique-label {
  display: block;
  font-size: 0.6875rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-text-tertiary);
}

.stats-page__unique-value {
  display: block;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-text);
  margin-top: 0.125rem;
}

/* Actions */
.stats-page__actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
  padding-top: 0.5rem;
}

@media (max-width: 480px) {
  .stats-page__grid {
    grid-template-columns: 1fr;
  }

  .stats-page__counter-number {
    font-size: 3rem;
  }
}
</style>
