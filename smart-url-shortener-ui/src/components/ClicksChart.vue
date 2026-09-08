<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  data: { type: Array, required: true }
})

const wrap = ref(null)
const width = ref(640)
let observer = null

onMounted(() => {
  observer = new ResizeObserver((entries) => {
    for (const entry of entries) {
      const w = entry.contentRect.width
      if (w > 0) width.value = w
    }
  })
  if (wrap.value) observer.observe(wrap.value)
})

onBeforeUnmount(() => observer?.disconnect())

const CHART_H = 200
const PAD = { top: 14, right: 14, bottom: 28, left: 34 }

const hoverIndex = ref(null)

/* ---------- Data ---------- */
const points = computed(() =>
  (props.data || []).map((d) => ({
    date: typeof d.date === 'string' ? d.date : String(d.date),
    clicks: Number(d.clicks) || 0
  }))
)

const total = computed(() =>
  points.value.reduce((sum, p) => sum + p.clicks, 0)
)

const rangeLabel = computed(() => {
  const pts = points.value
  if (pts.length < 2) return ''
  return `${formatDay(pts[0].date)} – ${formatDay(pts[pts.length - 1].date)}`
})

/* ---------- Scales ---------- */
const niceMax = computed(() => {
  const raw = Math.max(1, ...points.value.map((p) => p.clicks))
  if (raw <= 4) return 4
  if (raw <= 10) return 10
  if (raw <= 25) return 25
  if (raw <= 50) return 50
  const pow = 10 ** Math.floor(Math.log10(raw))
  return Math.ceil(raw / (pow / 2)) * (pow / 2)
})

const innerW = computed(() => Math.max(40, width.value - PAD.left - PAD.right))
const innerH = CHART_H - PAD.top - PAD.bottom

function yFor(clicks) {
  return CHART_H - PAD.bottom - (clicks / niceMax.value) * innerH
}

const yTicks = computed(() => {
  const steps = 4
  return Array.from({ length: steps + 1 }, (_, i) => ({
    value: Math.round((niceMax.value / steps) * (steps - i)),
    y: PAD.top + (innerH / steps) * i
  }))
})

/* ---------- Bars ---------- */
const bars = computed(() => {
  const pts = points.value
  if (!pts.length) return []
  const slot = innerW.value / pts.length
  const barW = Math.min(Math.max(slot * 0.62, 3), 22)
  const r = Math.min(3, barW / 2)

  return pts.map((p, i) => {
    const h = (p.clicks / niceMax.value) * innerH
    const x = PAD.left + slot * i + (slot - barW) / 2
    return {
      i,
      x,
      slotX: PAD.left + slot * i,
      slotW: slot,
      w: barW,
      h: Math.max(h, p.clicks > 0 ? 2 : 0),
      y: yFor(p.clicks),
      path: barPath(x, yFor(p.clicks), barW, Math.max(h, p.clicks > 0 ? 2 : 0), r),
      clicks: p.clicks,
      date: p.date
    }
  })
})

/* Top-rounded bar shape (flat bottom on the baseline) */
function barPath(x, y, w, h, r) {
  if (h <= 0) return ''
  const rr = Math.min(r, w / 2, h)
  return [
    `M${x},${y + h}`,
    `L${x},${y + rr}`,
    `Q${x},${y} ${x + rr},${y}`,
    `L${x + w - rr},${y}`,
    `Q${x + w},${y} ${x + w},${y + rr}`,
    `L${x + w},${y + h}`,
    'Z'
  ].join(' ')
}

/* ---------- X labels: short numeric dd/m format (e.g. 09/9) ---------- */
function formatDay(dateStr) {
  const d = new Date(`${dateStr}T00:00:00Z`)
  if (Number.isNaN(d.getTime())) return dateStr
  const day = String(d.getUTCDate()).padStart(2, '0')
  const month = d.getUTCMonth() + 1
  return `${day}/${month}`
}

const xLabels = computed(() => {
  const pts = points.value
  if (!pts.length) return []
  const slot = innerW.value / pts.length
  const maxLabels = Math.max(2, Math.min(6, Math.floor(innerW.value / 70)))
  const step = Math.max(1, Math.ceil(pts.length / maxLabels))

  const labels = []
  for (let i = 0; i < pts.length; i += step) {
    labels.push({ x: PAD.left + slot * i + slot / 2, label: formatDay(pts[i].date) })
  }
  const lastCenter = PAD.left + slot * (pts.length - 1) + slot / 2
  if (!labels.some((l) => Math.abs(l.x - lastCenter) < slot / 2)) {
    labels.push({ x: lastCenter, label: formatDay(pts[pts.length - 1].date) })
  }
  return labels
})

/* ---------- Tooltip ---------- */
const gradientId = `clicks-grad-${Math.random().toString(36).slice(2, 8)}`

const tooltip = computed(() => {
  const bar = hoverIndex.value != null ? bars.value[hoverIndex.value] : null
  if (!bar) return null
  return {
    x: Math.min(Math.max(bar.x + bar.w / 2, 44), width.value - 44),
    y: bar.y,
    flip: bar.y < 52,
    label: formatDay(bar.date),
    clicks: bar.clicks
  }
})
</script>

<template>
  <div class="chart">
    <div v-if="!points.length" class="chart__empty">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M18 20V10" /><path d="M12 20V4" /><path d="M6 20v-6" />
      </svg>
      No click data yet
    </div>

    <div v-else ref="wrap" class="chart__wrap">
      <div class="chart__header">
        <span class="chart__title">Clicks per day · last {{ points.length }} days</span>
        <span class="chart__range">{{ rangeLabel }}</span>
      </div>

      <div class="chart__svg-wrap">
        <svg
          class="chart__svg"
          :width="width"
          :height="CHART_H"
          :viewBox="`0 0 ${width} ${CHART_H}`"
          role="img"
          aria-label="Clicks per day"
          @mouseleave="hoverIndex = null"
        >
          <defs>
            <linearGradient :id="gradientId" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="var(--color-primary)" />
              <stop offset="100%" stop-color="var(--color-primary)" stop-opacity="0.25" />
            </linearGradient>
          </defs>

          <!-- Grid + y labels -->
          <g v-for="tick in yTicks" :key="`grid-${tick.y}`">
            <line
              :x1="PAD.left" :x2="width - PAD.right"
              :y1="tick.y" :y2="tick.y"
              class="chart__grid"
              :class="{ 'chart__grid--base': tick.value === 0 }"
            />
            <text :x="PAD.left - 8" :y="tick.y + 3.5" class="chart__tick" text-anchor="end">
              {{ tick.value }}
            </text>
          </g>

          <!-- Hover slot highlight -->
          <rect
            v-if="hoverIndex !== null && bars[hoverIndex]"
            :x="bars[hoverIndex].slotX"
            :y="PAD.top"
            :width="bars[hoverIndex].slotW"
            :height="innerH"
            class="chart__slot"
          />

          <!-- Bars -->
          <g>
            <template v-for="bar in bars" :key="`bar-${bar.i}`">
              <path
                v-if="bar.clicks > 0"
                :d="bar.path"
                :fill="`url(#${gradientId})`"
                :opacity="hoverIndex === null || hoverIndex === bar.i ? 1 : 0.35"
                class="chart__bar"
                :style="{ animationDelay: `${bar.i * 12}ms` }"
              />
              <rect
                v-else
                :x="bar.x" :y="CHART_H - PAD.bottom - 2"
                :width="bar.w" height="2" rx="1"
                class="chart__zero"
                :opacity="hoverIndex === null || hoverIndex === bar.i ? 0.6 : 0.2"
              />
            </template>
          </g>

          <!-- X labels -->
          <text
            v-for="l in xLabels"
            :key="`x-${l.x}-${l.label}`"
            :x="l.x" :y="CHART_H - 8"
            class="chart__tick chart__tick--x"
            text-anchor="middle"
          >{{ l.label }}</text>

          <!-- Hover hit areas (full column height) -->
          <rect
            v-for="bar in bars"
            :key="`hit-${bar.i}`"
            :x="bar.slotX" y="0"
            :width="bar.slotW" :height="CHART_H"
            fill="transparent"
            @mouseenter="hoverIndex = bar.i"
            @mousemove="hoverIndex = bar.i"
          />
        </svg>

        <!-- Tooltip -->
        <Transition name="chart-tip">
          <div
            v-if="tooltip"
            class="chart__tooltip"
            :class="{ 'chart__tooltip--below': tooltip.flip }"
            :style="{ left: `${tooltip.x}px`, top: `${tooltip.y}px` }"
          >
            <span class="chart__tooltip-value">{{ tooltip.clicks }} {{ tooltip.clicks === 1 ? 'click' : 'clicks' }}</span>
            <span class="chart__tooltip-date">{{ tooltip.label }}</span>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chart {
  width: 100%;
}

.chart__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-tertiary);
  font-size: 0.8125rem;
}

.chart__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 0.5rem;
  margin-bottom: 0.625rem;
}

.chart__title {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-text-tertiary);
}

.chart__range {
  font-size: 0.6875rem;
  font-weight: 500;
  color: var(--color-text-tertiary);
  font-variant-numeric: tabular-nums;
}

.chart__svg-wrap {
  position: relative;
}

.chart__svg {
  display: block;
  overflow: visible;
}

.chart__grid {
  stroke: var(--color-border);
  stroke-width: 1;
  stroke-dasharray: 3 4;
}

.chart__grid--base {
  stroke-dasharray: none;
  stroke: var(--color-border-hover);
}

.chart__slot {
  fill: var(--color-surface-hover);
  border-radius: 6px;
  opacity: 0.55;
}

.chart__bar {
  transform-box: fill-box;
  transform-origin: 50% 100%;
  animation: bar-grow 0.5s cubic-bezier(0.4, 0, 0.2, 1) backwards;
  transition: opacity var(--transition-fast);
}

@keyframes bar-grow {
  from { transform: scaleY(0); }
  to { transform: scaleY(1); }
}

.chart__zero {
  fill: var(--color-border-hover);
}

.chart__tick {
  font-size: 10px;
  font-family: var(--font-sans);
  fill: var(--color-text-tertiary);
  font-variant-numeric: tabular-nums;
}

.chart__tick--x {
  font-size: 10.5px;
  font-weight: 500;
}

.chart__tooltip {
  position: absolute;
  transform: translate(-50%, calc(-100% - 8px));
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.0625rem;
  padding: 0.375rem 0.625rem;
  background: var(--color-text);
  border-radius: 8px;
  pointer-events: none;
  white-space: nowrap;
  z-index: 5;
}

.chart__tooltip--below {
  transform: translate(-50%, 12px);
}

.chart__tooltip-value {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--color-bg);
  font-variant-numeric: tabular-nums;
}

.chart__tooltip-date {
  font-size: 0.6875rem;
  color: var(--color-surface);
  opacity: 0.75;
  font-variant-numeric: tabular-nums;
}

.chart-tip-enter-active,
.chart-tip-leave-active {
  transition: opacity 0.12s ease;
}

.chart-tip-enter-from,
.chart-tip-leave-to {
  opacity: 0;
}
</style>
