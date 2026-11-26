<template>
  <div class="page-container">
    <van-cell title="个人信息" is-link @click="$router.push('/mine/profile')" />
    <van-cell title="隐私设置" is-link />
    <van-cell title="通知管理" is-link />
    <van-cell title="清除缓存" is-link />
    <van-cell title="退出登录" is-link class="logout-cell" @click="handleLogout" />
  </div>
</template>

<script setup>
import { showConfirmDialog, showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

function handleLogout() {
  showConfirmDialog({ title: '确认退出', message: '确定要退出登录吗？' })
    .then(() => {
      userStore.logout()
      showToast('已退出登录')
    })
    .catch(() => {})
}
</script>

<style scoped>
.logout-cell { margin-top: 16px; }
</style>
