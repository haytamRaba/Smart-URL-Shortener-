<script setup>
import { ref, onMounted } from 'vue'
import CopyButton from './CopyButton.vue'

const emit = defineEmits(['select'])

const history = ref([])

onMounted(() => {
  loadHistory()
})

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
    createdAt: new Date().toISOString()
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

function openStats(shortUrl) {
  const statsPageUrl = `${window.location.origin}${window.location.pathname}?stats=${encodeURIComponent(shortUrl)}`
  window.open(statsPageUrl, '_blank', 'noopener,noreferrer,width=700,height=800')
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
  return `${days}d ago`
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
      <button class="history__clear" @click="clearHistory">Clear all</button>
    </div>

    <TransitionGroup name="list" tag="div" class="history__list">
      <div
        v-for="(item, index) in history"
        :key="item.shortUrl"
        class="history__item"
      >
        <div class="history__item-main">
          <div class="history__item-urls">
            <a
              :href="item.shortUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="history__item-short"
            >
              {{ item.shortUrl }}
            </a>
            <span class="history__item-original" :title="item.originalUrl">
              {{ item.originalUrl }}
            </span>
          </div>

          <div class="history__item-meta">
            <button
              class="history__item-clicks"
              :class="{ 'history__item-clicks--active': item.clickCount > 0 }"
              @click.stop="openStats(item.shortUrl)"
              title="Open stats in new window"
            >
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 20V10" />
                <path d="M12 20V4" />
                <path d="M6 20v-6" />
              </svg>
              {{ item.clickCount }} clicks
              <svg
                class="history__item-clicks-external"
                width="10"
                height="10"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M15 3h6v6" />
                <path d="M10 14 21 3" />
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
              </svg>
            </button>
            <span class="history__item-time">{{ timeAgo(item.createdAt) }}</span>
          </div>
        </div>

        <div class="history__item-actions">
          <CopyButton :text="item.shortUrl" size="sm" />
          <button class="history__item-remove" title="Remove" @click="removeFromHistory(index)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" x2="6" y1="6" y2="18" />
              <line x1="6" x2="18" y1="6" y2="18" />
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

.history__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.history__title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.history__title svg {
  color: var(--color-text-tertiary);
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
  transition: all 0.15s ease;
}

.history__clear:hover {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}

.history__list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.history__item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 14px;
  transition: all 0.2s ease;
}

.history__item:hover {
  border-color: var(--color-border-hover);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.history__item-main {
  flex: 1;
  min-width: 0;
}

.history__item-urls {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.history__item-short {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-primary);
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history__item-short:hover {
  text-decoration: underline;
}

.history__item-original {
  font-size: 0.75rem;
  color: var(--color-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history__item-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: 0.375rem;
}

/* Clicks button */
.history__item-clicks {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  font-weight: 600;
  font-family: inherit;
  color: var(--color-text-tertiary);
  padding: 0.1875rem 0.625rem;
  background: var(--color-surface-hover);
  border: 1.5px solid transparent;
  border-radius: 100px;
  cursor: pointer;
  transition: all 0.2s ease;
  line-height: 1.4;
}

.history__item-clicks:hover {
  background: var(--color-primary-ring);
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.history__item-clicks--active {
  color: var(--color-primary);
  background: var(--color-primary-ring);
}

.history__item-clicks--active:hover {
  background: var(--color-primary);
  color: white;
}

.history__item-clicks-external {
  margin-left: 0.125rem;
  opacity: 0.6;
  transition: opacity 0.15s ease;
}

.history__item-clicks:hover .history__item-clicks-external {
  opacity: 1;
}

.history__item-time {
  font-size: 0.6875rem;
  color: var(--color-text-tertiary);
}

.history__item-actions {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.history__item-remove {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition: all 0.15s ease;
}

.history__item-remove:hover {
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
</style>
