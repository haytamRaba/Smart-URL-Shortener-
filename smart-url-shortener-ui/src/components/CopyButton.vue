<script setup>
import { ref } from 'vue'
import { useClipboard } from '@vueuse/core'
import BaseButton from './BaseButton.vue'

const props = defineProps({
  text: { type: String, required: true },
  size: { type: String, default: 'md' }
})

const { copy, copied } = useClipboard()
const justCopied = ref(false)

async function handleCopy() {
  await copy(props.text)
  justCopied.value = true
  setTimeout(() => { justCopied.value = false }, 2000)
}
</script>

<template>
  <BaseButton
    :variant="justCopied ? 'primary' : 'secondary'"
    :size="size"
    @click="handleCopy"
  >
    <svg
      v-if="!justCopied"
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
    >
      <rect width="14" height="14" x="8" y="8" rx="2" ry="2" />
      <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2" />
    </svg>
    <svg
      v-else
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
    >
      <path d="M20 6 9 17l-5-5" />
    </svg>
    {{ justCopied ? 'Copied!' : 'Copy' }}
  </BaseButton>
</template>
