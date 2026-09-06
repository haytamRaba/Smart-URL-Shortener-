<script setup>
import { ref, onMounted, watch } from 'vue'
import { getUrlStats } from '../services/urlApi.js'

const props = defineProps({
  shortUrl: { type: String, required: true }
})

const stats = ref(null)
const loading = ref(false)
const error = ref('')
const expanded = ref(false)

async function fetchStats() {
  if (!props.shortUrl) return
  loading.value = true
  error.value = ''

  try {
    stats.value = await getUrlStats(props.shortUrl)
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

onMounted(fetchStats)
watch(() => props.shortUrl, fetchStats)

function formatNumber(n) {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1).replace(/\.0$/, '') + 'M'
  if (n >= 1_000) return (n / 1_000).toFixed(1).replace(/\.0$/, '') + 'K'
  return String(n)
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>

<template>
  <div class="stats">
    <button class="stats__toggle" @click="expanded = !expanded">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M18 20V10" />
        <path d="M12 20V4" />
        <path d="M6 20v-6" />
      </svg>
      <span>Stats</span>
      <svg
        class="stats__chevron"
        :class="{ 'stats__chevron--open': expanded }"
        width="14"
        height="14"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="m6 9 6 6 6-6" />
      </svg>
    </button>

    <Transition name="expand">
      <div v-if="expanded" class="stats__panel">
        <!-- Loading -->
        <div v-if="loading" class="stats__loading">
          <div class="stats__spinner" />
          <span>Loading stats...</span>
        </div>

        <!-- Error -->
        <div v-else-if="error" class="stats__error">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <span>Could not load stats</span>
        </div>

        <!-- Stats content -->
        <div v-else-if="stats" class="stats__content">
          <!-- Click counter -->
          <div class="stats__clicks">
            <div class="stats__clicks-number">
              {{ formatNumber(stats.clickCount ?? stats.clicks ?? stats.totalClicks ?? 0) }}
            </div>
            <div class="stats__clicks-label">total clicks</div>
          </div>

          <!-- Meta info -->
          <div class="stats__meta">
            <div class="stats__meta-item">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect width="18" height="18" x="3" y="4" rx="2" ry="2" />
                <line x1="16" x2="16" y1="2" y2="6" />
                <line x1="8" x2="8" y1="2" y2="6" />
                <line x1="3" x2="21" y1="10" y2="10" />
              </svg>
              <span>Created {{ formatDate(stats.createdAt ?? stats.created) }}</span>
            </div>
            <div class="stats__meta-item" v-if="stats.lastClickedAt || stats.lastClick">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
              <span>Last click {{ formatDate(stats.lastClickedAt ?? stats.lastClick) }}</span>
            </div>
            <div class="stats__meta-item" v-if="stats.originalUrl">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
                <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
              </svg>
              <span class="stats__meta-url">{{ stats.originalUrl }}</span>
            </div>
          </div>

          <!-- Refresh -->
          <button class="stats__refresh" @click="fetchStats">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10" />
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
            </svg>
            Refresh
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.stats {
  width: 100%;
}

.stats__toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.625rem 0.875rem;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 0.8125rem;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.stats__toggle:hover {
  background: var(--color-surface-hover);
  border-color: var(--color-border-hover);
  color: var(--color-text);
}

.stats__toggle span {
  flex: 1;
  text-align: left;
}

.stats__chevron {
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.stats__chevron--open {
  transform: rotate(180deg);
}

.stats__panel {
  overflow: hidden;
}

.stats__loading,
.stats__error {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1.5rem;
  font-size: 0.8125rem;
  color: var(--color-text-tertiary);
}

.stats__error {
  color: var(--color-danger);
}

.stats__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.stats__content {
  padding: 1rem 0.25rem 0.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.stats__clicks {
  text-align: center;
  padding: 1rem;
  background: linear-gradient(135deg, var(--color-primary-ring), transparent);
  border-radius: 14px;
  border: 1px solid var(--color-primary-ring);
}

.stats__clicks-number {
  font-size: 2.25rem;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
  letter-spacing: -0.03em;
}

.stats__clicks-label {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.stats__meta {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.stats__meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8125rem;
  color: var(--color-text-secondary);
}

.stats__meta-item svg {
  flex-shrink: 0;
  color: var(--color-text-tertiary);
}

.stats__meta-url {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.stats__refresh {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  align-self: flex-end;
  padding: 0.375rem 0.75rem;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--color-text-tertiary);
  font-size: 0.75rem;
  font-family: inherit;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.stats__refresh:hover {
  background: var(--color-surface-hover);
  color: var(--color-text);
}

/* Expand transition */
.expand-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.expand-leave-active {
  transition: all 0.2s ease;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-8px);
}

.expand-enter-to,
.expand-leave-from {
  max-height: 400px;
}
</style>
