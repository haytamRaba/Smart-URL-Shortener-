<script setup>
import { ref, onMounted } from 'vue'
import CopyButton from './CopyButton.vue'
import { getUrlStats } from '../services/urlApi.js'

const emit = defineEmits(['select'])

const history = ref([])

onMounted(() => {
  loadHistory()
  refreshClickCounts() 
})
async function refreshClickCounts() {
  const results = await Promise.allSettled(
    history.value.map(item => getUrlStats(item.shortUrl))
  )
  results.forEach((res, i) => {
    if (res.status === 'fulfilled') {
      history.value[i].clickCount = res.value.totalClicks ?? 0
    }
  })
  saveHistory()
  }


function loadHistory() {
  try {
    const stored = localStorage.getItem('linksnip_history')
    history.value = stored ? JSON.parse(stored) : []
  } catch {
    history.value = []
  }
}

function saveHistory() {
  localStorage.setItem('linksnip_history', JSON.stringify(history.value))
}

function addToHistory(item) {
  const existing = history.value.findIndex(h => h.shortUrl === item.shortUrl)
  if (existing !== -1) {
    history.value.splice(existing, 1)
  }
  history.value.unshift({
    shortUrl: item.shortUrl,
    originalUrl: item.originalUrl,
    clickCount: item.clickCount ?? 0,
    createdAt: item.createdAt ?? new Date().toISOString()
  })
  if (history.value.length > 20) {
    history.value = history.value.slice(0, 20)
  }
  saveHistory()
}

function updateClicks(shortUrl, count) {
  const item = history.value.find(h => h.shortUrl === shortUrl)
  if (item) {
    item.clickCount = count
    saveHistory()
  }
}

function removeFromHistory(index) {
  history.value.splice(index, 1)
  saveHistory()
}

function clearHistory() {
  history.value = []
  saveHistory()
}

/* Open the in-app stats view in a new browser tab (not a popup window) */
function openStats(shortUrl) {
  const statsPageUrl = `${window.location.origin}${window.location.pathname}?stats=${encodeURIComponent(shortUrl)}`
  window.open(statsPageUrl, '_blank', 'noopener,noreferrer')
}

function hostOf(url) {
  try {
    return new URL(url).host
  } catch {
    return url
  }
}

function timeAgo(dateStr) {
  if (!dateStr) return ''
  const diff = Date.now() - new Date(dateStr).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}d ago`
  const weeks = Math.floor(days / 7)
  return `${weeks}w ago`
}

defineExpose({ addToHistory, updateClicks })
</script>

<template>
  <div class="history" v-if="history.length > 0">
    <div class="history__header">
      <h3 class="history__title">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10" />
          <polyline points="12 6 12 12 16 14" />
        </svg>
        Recent Links
      </h3>
      <span class="history__count">{{ history.length }} link{{ history.length === 1 ? '' : 's' }}</span>
      <button class="history__clear" @click="clearHistory">Clear all</button>
    </div>

    <TransitionGroup name="list" tag="div" class="history__list">
      <div
        v-for="(item, index) in history"
        :key="item.shortUrl"
        class="history__item"
      >
        <!-- Favicon-style avatar from destination host -->
        <a
          :href="item.shortUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="history__avatar"
          :title="hostOf(item.originalUrl)"
        >
          {{ (hostOf(item.originalUrl) || '?').charAt(0).toUpperCase() }}
        </a>

        <div class="history__body">
          <a
            :href="item.shortUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="history__short"
          >
            {{ item.shortUrl }}
          </a>
          <span class="history__original" :title="item.originalUrl">
            {{ hostOf(item.originalUrl) }} · {{ item.originalUrl }}
          </span>

          <div class="history__meta">
            <button
              class="history__stats-btn"
              @click.stop="openStats(item.shortUrl)"
              title="Open stats in new tab"
            >
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 20V10" />
                <path d="M12 20V4" />
                <path d="M6 20v-6" />
              </svg>
              {{ item.clickCount }} click{{ item.clickCount === 1 ? '' : 's' }}
            </button>
            <span class="history__dot">·</span>
            <span class="history__time">{{ timeAgo(item.createdAt) }}</span>
          </div>
        </div>

        <div class="history__actions">
          <button
            class="history__icon-btn"
            title="View stats"
            @click.stop="openStats(item.shortUrl)"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 20V10" />
              <path d="M12 20V4" />
              <path d="M6 20v-6" />
            </svg>
          </button>
          <CopyButton :text="item.shortUrl" size="sm" />
          <button
            class="history__icon-btn history__icon-btn--danger"
            title="Remove"
            @click="removeFromHistory(index)"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            </svg>
          </button>
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.history {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* Header */
.history__header {
  display: flex;
  align-items: center;
  gap: 0.625rem;
}

.history__title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
  margin-right: auto;
}

.history__title svg {
  color: var(--color-text-tertiary);
}

.history__count {
  font-size: 0.6875rem;
  font-weight: 600;
  color: var(--color-text-tertiary);
  padding: 0.125rem 0.5rem;
  background: var(--color-surface-hover);
  border-radius: 100px;
}

.history__clear {
  border: none;
  background: none;
  color: var(--color-text-tertiary);
  font-size: 0.75rem;
  font-family: inherit;
  font-weight: 500;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  transition: all var(--transition-fast);
}

.history__clear:hover {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}

/* List */
.history__list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.history__item {
  display: flex;
  align-items: center;
  gap: 0.875rem;
  padding: 0.875rem 1rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal);
}

.history__item:hover {
  border-color: var(--color-border-hover);
  box-shadow: var(--shadow-sm);
}

/* Avatar */
.history__avatar {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  background: linear-gradient(135deg, var(--color-primary-ring), transparent);
  border: 1px solid var(--color-primary-ring);
  color: var(--color-primary);
  font-size: 0.9375rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  text-transform: uppercase;
}

/* Body */
.history__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.history__short {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-primary);
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: fit-content;
  max-width: 100%;
}

.history__short:hover {
  text-decoration: underline;
}

.history__original {
  font-size: 0.75rem;
  color: var(--color-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Meta row */
.history__meta {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  margin-top: 0.25rem;
}

.history__stats-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.6875rem;
  font-weight: 600;
  font-family: inherit;
  color: var(--color-text-tertiary);
  padding: 0.125rem 0.5rem;
  background: var(--color-surface-hover);
  border: none;
  border-radius: 100px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.history__stats-btn:hover {
  background: var(--color-primary-ring);
  color: var(--color-primary);
}

.history__dot {
  font-size: 0.6875rem;
  color: var(--color-text-tertiary);
}

.history__time {
  font-size: 0.6875rem;
  color: var(--color-text-tertiary);
}

/* Actions */
.history__actions {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  flex-shrink: 0;
}

.history__icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.history__icon-btn:hover {
  background: var(--color-primary-ring);
  color: var(--color-primary);
}

.history__icon-btn--danger:hover {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}

/* List transitions */
.list-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.list-leave-active {
  transition: all 0.2s ease;
}

.list-enter-from {
  opacity: 0;
  transform: translateY(-8px) scale(0.98);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.list-move {
  transition: transform 0.3s ease;
}

@media (max-width: 540px) {
  .history__actions {
    flex-direction: column;
    gap: 0.125rem;
  }
}
</style>
