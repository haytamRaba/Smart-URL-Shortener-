<script setup>
import { ref, computed, nextTick } from 'vue'
import { createShortUrl } from '../services/urlApi.js'
import BaseButton from './BaseButton.vue'
import CopyButton from './CopyButton.vue'
import ShareButtons from './ShareButtons.vue'
import ThemeToggle from './ThemeToggle.vue'
import SuggestionChips from './SuggestionChips.vue'
import StatsDisplay from './StatsDisplay.vue'
import UrlHistory from './UrlHistory.vue'

const originalUrl = ref('')
const result = ref(null)
const error = ref('')
const loading = ref(false)
const historyRef = ref(null)

const suggestions = [
  { label: 'GitHub', url: 'https://github.com', icon: 'link' },
  { label: 'YouTube', url: 'https://youtube.com', icon: 'trending' },
  { label: 'Twitter / X', url: 'https://x.com', icon: 'link' },
  { label: 'Medium', url: 'https://medium.com', icon: 'link' },
  { label: 'Product Hunt', url: 'https://producthunt.com', icon: 'trending' },
]

const shortUrlDisplay = computed(() => result.value?.shortUrl || '')

async function handleSubmit() {
  if (!originalUrl.value) return
  error.value = ''
  result.value = null
  loading.value = true

  try {
    const res = await createShortUrl(originalUrl.value)
    result.value = res

    // Add to history
    await nextTick()
    historyRef.value?.addToHistory({
      shortUrl: res.shortUrl,
      originalUrl: res.originalUrl ?? originalUrl.value,
      clickCount: res.clickCount ?? 0
    })
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

function handleSuggestion(url) {
  originalUrl.value = url
}

function reset() {
  result.value = null
  originalUrl.value = ''
  error.value = ''
}
</script>

<template>
  <div class="app">
    <header class="header">
      <div class="header__inner">
        <div class="header__logo">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
          </svg>
          <span>LinkSnip</span>
        </div>
        <ThemeToggle />
      </div>
    </header>

    <main class="main">
      <div class="hero">
        <h1 class="hero__title">
          Shorten your links,
          <span class="hero__accent">amplify your reach.</span>
        </h1>
        <p class="hero__subtitle">
          Transform long, unwieldy URLs into clean, shareable links in seconds.
        </p>
      </div>

      <div class="card">
        <form class="input-group" @submit.prevent="handleSubmit">
          <div class="input-wrapper">
            <svg class="input-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
            </svg>
            <input
              v-model="originalUrl"
              type="url"
              class="input"
              placeholder="Paste your long URL here..."
              required
            />
          </div>
          <BaseButton
            type="submit"
            variant="primary"
            size="lg"
            :loading="loading"
            :disabled="!originalUrl"
          >
            {{ loading ? 'Shortening...' : 'Shorten' }}
          </BaseButton>
        </form>

        <SuggestionChips
          :suggestions="suggestions"
          @select="handleSuggestion"
        />
      </div>

      <Transition name="fade">
        <div v-if="error" class="error-card">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <line x1="15" y1="9" x2="9" y2="15" />
            <line x1="9" y1="9" x2="15" y2="15" />
          </svg>
          <span>{{ error }}</span>
          <BaseButton variant="ghost" size="sm" @click="error = ''">
            Dismiss
          </BaseButton>
        </div>
      </Transition>

      <Transition name="result">
        <div v-if="result" class="result-card">
          <div class="result-card__header">
            <div class="result-card__badge">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 6 9 17l-5-5" />
              </svg>
            </div>
            <div>
              <h3 class="result-card__title">Your shortened link is ready!</h3>
              <p class="result-card__subtitle">Share it anywhere — it's live now.</p>
            </div>
          </div>

          <div class="result-card__url">
            <a
              :href="shortUrlDisplay"
              target="_blank"
              rel="noopener noreferrer"
              class="result-card__link"
            >
              {{ shortUrlDisplay }}
            </a>
          </div>

          <!-- Stats -->
          <StatsDisplay :short-url="shortUrlDisplay" />

          <div class="result-card__actions">
            <CopyButton :text="shortUrlDisplay" />
            <ShareButtons :url="shortUrlDisplay" title="Check out this link!" />
            <BaseButton variant="ghost" size="md" @click="reset">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="1 4 1 10 7 10" />
                <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" />
              </svg>
              New link
            </BaseButton>
          </div>
        </div>
      </Transition>

      <!-- History -->
      <!-- History -->
      <section class="history-section">
        <UrlHistory ref="historyRef" />
      </section>
    </main>

    <footer class="footer">
      <p>Stats · Clean · Fast</p>
    </footer>
  </div>
</template>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header {
  width: 100%;
  padding: 1.25rem 2rem;
  display: flex;
  justify-content: center;
}

.header__inner {
  width: 100%;
  max-width: 720px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header__logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.02em;
}

.header__logo svg {
  color: var(--color-primary);
}

.main {
  flex: 1;
  width: 100%;
  max-width: 720px;
  padding: 2rem 1.5rem 4rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.hero {
  text-align: center;
  margin-bottom: 0.5rem;
}

.hero__title {
  font-size: clamp(1.75rem, 5vw, 2.5rem);
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: -0.03em;
  color: var(--color-text);
  margin: 0;
}

.hero__accent {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-hover));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero__subtitle {
  margin-top: 0.75rem;
  font-size: 1.0625rem;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.input-group {
  display: flex;
  gap: 0.75rem;
}

.input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 1rem;
  color: var(--color-text-tertiary);
  pointer-events: none;
}

.input {
  width: 100%;
  padding: 0.875rem 1rem 0.875rem 2.75rem;
  font-size: 0.9375rem;
  font-family: inherit;
  color: var(--color-text);
  background: var(--color-bg);
  border: 1.5px solid var(--color-border);
  border-radius: 14px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.input::placeholder {
  color: var(--color-text-tertiary);
}

.input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-ring);
}

.error-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  background: var(--color-danger-bg);
  border: 1px solid var(--color-danger-border);
  border-radius: 14px;
  color: var(--color-danger);
  font-size: 0.875rem;
}

.error-card svg {
  flex-shrink: 0;
}

.error-card span {
  flex: 1;
}

.result-card {
  background: var(--color-surface);
  border: 1.5px solid var(--color-primary);
  border-radius: 20px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  box-shadow: 0 4px 20px var(--color-primary-shadow);
}

.result-card__header {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.result-card__badge {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.result-card__title {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text);
}

.result-card__subtitle {
  margin: 0.125rem 0 0;
  font-size: 0.8125rem;
  color: var(--color-text-secondary);
}

.result-card__url {
  padding: 0.75rem 1rem;
  background: var(--color-bg);
  border-radius: 12px;
  border: 1px solid var(--color-border);
}

.result-card__link {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-primary);
  text-decoration: none;
  word-break: break-all;
}

.result-card__link:hover {
  text-decoration: underline;
}

.result-card__actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.footer {
  padding: 2rem;
  text-align: center;
  font-size: 0.8125rem;
  color: var(--color-text-tertiary);
}

.history-section {
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border);
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.result-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.result-leave-active {
  transition: all 0.25s ease;
}
.result-enter-from {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}
.result-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 540px) {
  .input-group {
    flex-direction: column;
  }

  .result-card__actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
