<template>
  <header v-if="active && route.meta.backFallback && router.options.history.state.tabEntry !== route.path" class="page-navigation">
    <div class="page-navigation__inner">
      <button type="button" class="page-back" @click="goBack">
        <van-icon name="arrow-left" aria-hidden="true" /> 返回
      </button>
      <strong v-if="route.meta.showNavTitle" class="page-navigation__title">{{ route.meta.title }}</strong>
    </div>
  </header>
  <router-view v-if="route.meta.guest || active" v-slot="{ Component }" :key="route.meta.guest ? 'guest' : store.verifiedToken">
    <keep-alive :include="['FitnessHome', 'TrainingLibrary', 'RecipeLibrary', 'CommunityFeed']">
      <component :is="Component" />
    </keep-alive>
  </router-view>
  <tab-bar v-if="active && route.matched.length && !route.meta.hideTabBar" />
</template>
<script setup>
import { computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TabBar from './components/TabBar.vue'
import { useUserStore, workoutSavedKey } from '@/stores/user'
const store = useUserStore()
const route = useRoute()
const router = useRouter()
const active = computed(() => !!store.token && store.verifiedToken === store.token)

function goBack() {
  const previous = router.options.history.state.back
  const target = previous && router.resolve(previous)
  if (target?.matched.length && !target.meta.guest) router.back()
  else router.replace(route.meta.backFallback)
}

function checkSession() {
  store.getSessionToken()
}
function syncSession(event) {
  if (event.key === 'token' || event.key === null) checkSession()
  if (event.key === workoutSavedKey && active.value) store.refreshStats()
}
watch(() => store.stats.nextCheckInAt, (next, previous, onCleanup) => {
  if (!next) return
  const timer = window.setTimeout(() => store.refreshStats(), Math.max(1000, Date.parse(next) - Date.now() + 100))
  onCleanup(() => window.clearTimeout(timer))
})
watch(() => store.token, token => {
  if (!route.matched.length) return
  if (!token && !route.meta.guest) {
    router.replace({ name: 'Login' })
  } else if (token && token !== store.verifiedToken) {
    router.replace({ path: route.path, query: route.query, hash: route.hash, force: true })
  }
})
watch(() => store.expiresAt, (expiry, previous, onCleanup) => {
  if (!expiry) return
  let timer
  function schedule() {
    const remaining = expiry - Date.now()
    if (remaining <= 0) checkSession()
    else timer = setTimeout(schedule, Math.min(remaining, 2147483647))
  }
  schedule()
  onCleanup(() => clearTimeout(timer))
}, { immediate: true })
onMounted(() => {
  window.addEventListener('storage', syncSession)
  window.addEventListener('pageshow', checkSession)
  window.addEventListener('focus', checkSession)
})
onUnmounted(() => {
  window.removeEventListener('storage', syncSession)
  window.removeEventListener('pageshow', checkSession)
  window.removeEventListener('focus', checkSession)
})
</script>
<style scoped>
.page-navigation { border-bottom: 1px solid var(--color-border); background: var(--color-surface-raised); }
.page-navigation__inner { position: relative; display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; max-width: 1680px; margin: 0 auto; padding: 0 clamp(12px, 2vw, 28px); }
.page-back { justify-self: start; display: inline-flex; align-items: center; gap: 5px; min-height: 46px; padding: 0 8px; border: 0; background: transparent; color: #22694f; font-weight: 700; cursor: pointer; }
.page-back:hover { text-decoration: underline; }
.page-navigation__title { grid-column: 2; font-size: 16px; }
</style>
<style>html, body { margin: 0; padding: 0; width: 100%; min-height: 100%; overflow-x: hidden; } #app { width: 100%; min-height: 100vh; color: var(--color-text); }</style>
