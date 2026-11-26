<template>
  <nav class="tab-bar safe-area-bottom" aria-label="主导航">
    <router-link
      v-for="item in tabs"
      :key="item.path"
      :to="{ path: item.path, state: { tabEntry: item.path }, force: route.fullPath === item.path }"
      :replace="route.fullPath === item.path"
      class="tab-bar__item"
      :class="{ 'tab-bar__item--active': activeTab === item.path }"
    >
      <span class="tab-bar__icon"><van-icon :name="item.icon" size="21" /></span>
      <span>{{ item.label }}</span>
    </router-link>
  </nav>
</template>
<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
const tabs = [
  { path: '/home', label: '首页', icon: 'home-o' },
  { path: '/train', label: '训练', icon: 'bars' },
  { path: '/community', label: '社区', icon: 'chat-o' },
  { path: '/mine', label: '我的', icon: 'user-o' }
]
const route = useRoute()
const activeTab = computed(() => route.meta.activeTab || route.path)
</script>
<style scoped>
.tab-bar { position: fixed; left: 14px; right: 14px; bottom: 12px; z-index: 999; display: flex; align-items: center; height: var(--tab-bar-height); padding: 5px; background: rgba(255,255,255,.92); border: 1px solid var(--color-border); border-radius: 22px; box-shadow: var(--shadow-lg); backdrop-filter: blur(18px); }
.tab-bar__item { flex: 1; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px; color: var(--color-text-light); cursor: pointer; transition: color .2s ease, transform .2s ease; }
.tab-bar__item:active { transform: scale(.94); } .tab-bar__item--active { color: var(--color-primary-dark); font-weight: 700; }
.tab-bar__icon { width: 36px; height: 28px; display: flex; align-items: center; justify-content: center; border-radius: 12px; } .tab-bar__item--active .tab-bar__icon { background: var(--color-primary-soft); }
.tab-bar__item span { font-size: 12px; }
</style>
