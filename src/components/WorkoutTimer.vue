<template>
  <section v-if="progress.timer" class="workout-clock" aria-label="本次训练计时">
    <div class="readout">
      <span class="clock-state" role="status">{{ progress.timer.running ? '本次训练中' : '本次训练已暂停' }}</span>
      <strong role="timer" aria-live="off">{{ clock }}</strong>
    </div>
    <button type="button" :disabled="busy" @click="change(progress.timer.running ? 'pause' : 'resume')">
      {{ progress.timer.running ? '暂停' : '继续' }}
    </button>
    <button type="button" class="stop" :disabled="busy" @click="change('stop')">停止训练</button>
  </section>
  <button v-else type="button" class="start-button" :disabled="busy" @click="change('start')">
    <van-icon name="play" aria-hidden="true" />{{ busy ? '正在开始' : '开始训练' }}
  </button>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { showToast } from 'vant'
import { changeWorkoutTimer } from '@/api/train'
import { useUserStore, workoutSavedKey } from '@/stores/user'

const props = defineProps({
  planId: { type: Number, required: true },
  progress: { type: Object, required: true }
})
const emit = defineEmits(['update:progress'])
const user = useUserStore()
const busy = ref(false)
const elapsed = ref(0)
let startId
watch(() => props.progress.timer, (timer, previous, onCleanup) => {
  elapsed.value = timer?.elapsedMs || 0
  if (!timer?.running) return
  const start = performance.now()
  const interval = setInterval(() => {
    elapsed.value = timer.elapsedMs + performance.now() - start
  }, 200)
  onCleanup(() => clearInterval(interval))
}, { immediate: true })

const clock = computed(() => {
  const seconds = Math.floor(elapsed.value / 1000)
  return [Math.floor(seconds / 3600), Math.floor(seconds / 60) % 60, seconds % 60]
    .map(part => String(part).padStart(2, '0')).join(':')
})

async function change(action) {
  if (busy.value) return
  const session = user.getSessionToken()
  busy.value = true
  try {
    // Reuse the start ID if the response was lost after the server committed.
    if (action === 'start' && !startId) {
      const bytes = crypto.getRandomValues(new Uint8Array(16))
      bytes[6] = (bytes[6] & 15) | 64
      bytes[8] = (bytes[8] & 63) | 128
      const hex = Array.from(bytes, byte => byte.toString(16).padStart(2, '0')).join('')
      startId = hex.replace(/^(........)(....)(....)(....)(............)$/, '$1-$2-$3-$4-$5')
    }
    const id = props.progress.timer?.id || startId
    const { data } = await changeWorkoutTimer(props.planId, id, action)
    if (session !== user.getSessionToken()) return
    emit('update:progress', data)
    startId = null
    if (action === 'stop' && !data.timer) {
      user.refreshStats()
      localStorage.setItem(workoutSavedKey, id)
      showToast('本次训练时长已保存')
    }
  } catch {
    // Keep the controls available for retry. The request interceptor displays the error.
  } finally {
    busy.value = false
  }
}
</script>

<style scoped>
.start-button, .workout-clock button { min-height: 46px; padding: 0 22px; border: 1px solid #173f39; border-radius: 12px; background: #173f39; color: #fff; cursor: pointer; font-weight: 700; }
.start-button { display: inline-flex; align-items: center; justify-content: center; gap: 7px; }
.workout-clock { display: flex; align-items: center; gap: 10px; padding: 8px 10px 8px 18px; border-radius: 16px; color: #f6fff9; background: #173f39; }
.readout { margin-right: auto; }
.clock-state { display: block; color: #bcebd2; font-size: 12px; }
.readout strong { display: block; font: 600 30px/1.1 'Barlow Condensed', sans-serif; font-variant-numeric: tabular-nums; }
.workout-clock button { min-height: 44px; padding: 0 12px; color: #173f39; background: #d7f1df; }
.workout-clock .stop { background: transparent; border-color: #98b7a8; color: white; }
button:disabled { opacity: .65; cursor: wait; }
@media (max-width: 680px) {
  .workout-clock { grid-column: 1 / -1; grid-row: 1; }
  .start-button { padding-inline: 4px; }
}
</style>
