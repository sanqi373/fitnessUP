<template>
  <div class="tab-bar safe-area-bottom">
    <div
      v-for="item in tabs"
      :key="item.path"
      class="tab-bar__item"
      :class="{ 'tab-bar__item--active': currentPath === item.path }"
      @click="navigate(item.path)"
    >
      <div class="tab-bar__icon">
        <van-icon :name="currentPath === item.path ? item.activeIcon : item.icon" size="22" />
      </div>
      <span class="tab-bar__label">{{ item.label }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const tabs = [
  { path: '/home', label: '首页', icon: 'home-o', activeIcon: 'home-o' },
  { path: '/train', label: '训练', icon: 'bars', activeIcon: 'bars' },
  { path: '/community', label: '社区', icon: 'chat-o', activeIcon: 'chat-o' },
  { path: '/shop', label: '商城', icon: 'shopping-cart-o', activeIcon: 'shopping-cart-o' },
  { path: '/mine', label: '我的', icon: 'user-o', activeIcon: 'user-o' }
]

const router = useRouter()
const route = useRoute()
const currentPath = computed(() => route.path)

function navigate(path) {
  if (currentPath.value !== path) {
    router.push(path)
  }
}
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  height: var(--tab-bar-height);
  background: var(--color-bg-white);
  border-top: 1px solid var(--color-border);
}

.tab-bar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  cursor: pointer;
  transition: color 0.2s;
  color: var(--color-text-light);
}

.tab-bar__item--active {
  color: var(--color-primary);
}

.tab-bar__icon {
  position: relative;
  line-height: 1;
  margin-bottom: 2px;
}

.tab-bar__label {
  font-size: 10px;
  line-height: 1;
}
</style>
