<template>
  <div class="plan-save">
    <button type="button" :class="{ joined: saved }" :disabled="busy" @click="activate">
      <van-loading v-if="busy" size="16" color="currentColor" />
      <van-icon v-else :name="saved ? 'success' : 'plus'" aria-hidden="true" />
      <span>{{ label }}</span>
    </button>
    <router-link v-if="showCollectionLink && saved" :to="`/mine/${kind}-plan`">查看我的{{ title }} <van-icon name="arrow" /></router-link>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { addToPlan, isPlanSaved, removeFromPlan } from '@/api/plans'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  kind: { type: String, required: true },
  id: { type: Number, required: true },
  showCollectionLink: { type: Boolean, default: true }
})
const store = useUserStore()
const route = useRoute()
const router = useRouter()
const saved = ref(false)
const phase = ref('loading')
const busy = computed(() => ['loading', 'saving'].includes(phase.value))
const title = computed(() => props.kind === 'training' ? '训练计划' : '饮食计划')
const label = computed(() => {
  if (phase.value === 'error') return '重新读取计划状态'
  if (phase.value === 'loading') return '正在读取计划'
  if (phase.value === 'saving') return '正在保存'
  return saved.value ? `已添加 · 移出${title.value}` : `添加至${title.value}`
})

let revision = 0
async function load() {
  const current = ++revision
  const session = store.getSessionToken()
  saved.value = false
  phase.value = session ? 'loading' : 'ready'
  if (!session) return
  try {
    const { data } = await isPlanSaved(props.kind, props.id)
    if (current === revision && session === store.getSessionToken()) {
      saved.value = data
      phase.value = 'ready'
    }
  } catch {
    if (current === revision) phase.value = 'error'
  }
}
watch(() => [props.kind, props.id, store.token], load, { immediate: true })

async function activate() {
  const session = store.getSessionToken()
  if (!session) return router.push({ path: '/login', query: { redirect: route.path } })
  if (phase.value === 'error') return load()
  if (busy.value) return
  const current = revision
  phase.value = 'saving'
  try {
    const change = saved.value ? removeFromPlan : addToPlan
    const { data } = await change(props.kind, props.id)
    if (current !== revision || session !== store.getSessionToken()) return
    saved.value = data
    phase.value = 'ready'
    showToast((data ? '已添加至' : '已移出') + title.value)
  } catch {
    if (current === revision) await load()
  }
}
</script>

<style scoped>
.plan-save { display: grid; gap: 5px; justify-items: stretch; }
.plan-save button { display: inline-flex; gap: 8px; align-items: center; justify-content: center; width: 100%; min-height: 46px; padding: 8px 16px; border: 1px solid #206a50; border-radius: 13px; background: #206a50; color: white; font-weight: 700; cursor: pointer; }
.plan-save button.joined { background: #e8f5ed; color: #245c45; border-color: #b5d8c4; }
.plan-save button:disabled { cursor: wait; }
.plan-save a { justify-self: center; padding: 4px 8px; color: #245c45; font-size: 13px; }
</style>
