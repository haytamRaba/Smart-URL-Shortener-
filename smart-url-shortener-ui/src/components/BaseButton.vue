<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (v) => ['primary', 'secondary', 'ghost', 'danger'].includes(v)
  },
  size: {
    type: String,
    default: 'md',
    validator: (v) => ['sm', 'md', 'lg'].includes(v)
  },
  disabled: Boolean,
  loading: Boolean,
  icon: Boolean
})

defineEmits(['click'])
</script>

<template>
  <button
    :class="[
      'base-btn',
      `base-btn--${variant}`,
      `base-btn--${size}`,
      { 'base-btn--icon': icon, 'base-btn--loading': loading }
    ]"
    :disabled="disabled || loading"
    @click="$emit('click', $event)"
  >
    <span v-if="loading" class="base-btn__spinner" />
    <slot v-else />
  </button>
</template>

<style scoped>
.base-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-family: inherit;
  font-weight: 500;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  user-select: none;
  position: relative;
  overflow: hidden;
}

.base-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

/* Sizes */
.base-btn--sm {
  padding: 0.5rem 1rem;
  font-size: 0.8125rem;
  border-radius: 8px;
}

.base-btn--md {
  padding: 0.625rem 1.25rem;
  font-size: 0.875rem;
}

.base-btn--lg {
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  border-radius: 14px;
}

/* Icon button */
.base-btn--icon {
  padding: 0.5rem;
  border-radius: 10px;
}

.base-btn--icon.base-btn--sm { padding: 0.375rem; }
.base-btn--icon.base-btn--lg { padding: 0.625rem; }

/* Variants */
.base-btn--primary {
  background: var(--color-primary);
  color: white;
}
.base-btn--primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px var(--color-primary-shadow);
}
.base-btn--primary:active:not(:disabled) {
  transform: translateY(0);
}

.base-btn--secondary {
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}
.base-btn--secondary:hover:not(:disabled) {
  background: var(--color-surface-hover);
  border-color: var(--color-border-hover);
}

.base-btn--ghost {
  background: transparent;
  color: var(--color-text-secondary);
}
.base-btn--ghost:hover:not(:disabled) {
  background: var(--color-surface);
  color: var(--color-text);
}

.base-btn--danger {
  background: var(--color-danger);
  color: white;
}
.base-btn--danger:hover:not(:disabled) {
  background: var(--color-danger-hover);
  transform: translateY(-1px);
}

/* Disabled */
.base-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Loading spinner */
.base-btn__spinner {
  width: 1em;
  height: 1em;
  border: 2px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
