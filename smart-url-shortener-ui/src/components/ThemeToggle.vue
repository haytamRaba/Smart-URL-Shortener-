<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const theme = ref('system')

const themes = [
  { id: 'light', label: 'Light' },
  { id: 'dark', label: 'Dark' },
  { id: 'system', label: 'System' }
]

onMounted(() => {
  theme.value = localStorage.getItem('theme') || 'system'
  applyTheme(theme.value)
})

watch(theme, (val) => {
  localStorage.setItem('theme', val)
  applyTheme(val)
})

function applyTheme(val) {
  if (val === 'dark') {
    document.documentElement.classList.add('dark')
    document.documentElement.classList.remove('light')
  } else if (val === 'light') {
    document.documentElement.classList.add('light')
    document.documentElement.classList.remove('dark')
  } else {
    document.documentElement.classList.remove('dark', 'light')
  }
}

function cycle() {
  const order = ['system', 'light', 'dark']
  const idx = order.indexOf(theme.value)
  theme.value = order[(idx + 1) % order.length]
}
</script>

<template>
  <button class="theme-toggle" :title="`Theme: ${theme}`" @click="cycle">
    <!-- Sun (light) -->
    <svg v-if="theme === 'light'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2" />
      <path d="M12 20v2" />
      <path d="m4.93 4.93 1.41 1.41" />
      <path d="m17.66 17.66 1.41 1.41" />
      <path d="M2 12h2" />
      <path d="M20 12h2" />
      <path d="m6.34 17.66-1.41 1.41" />
      <path d="m19.07 4.93-1.41 1.41" />
    </svg>
    <!-- Moon (dark) -->
    <svg v-else-if="theme === 'dark'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z" />
    </svg>
    <!-- Monitor (system) -->
    <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <rect width="20" height="14" x="2" y="3" rx="2" />
      <line x1="8" x2="16" y1="21" y2="21" />
      <line x1="12" x2="12" y1="17" y2="21" />
    </svg>
  </button>
</template>

<style scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: var(--color-surface);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.theme-toggle:hover {
  background: var(--color-surface-hover);
  color: var(--color-text);
  border-color: var(--color-border-hover);
  transform: scale(1.05);
}

.theme-toggle:active {
  transform: scale(0.95);
}
</style>
