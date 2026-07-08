<template>
  <div class="page-container">
    <van-nav-bar title="设置" left-text="返回" left-arrow @click-left="$router.back()" />
    <van-cell title="个人信息" is-link @click="$router.push('/mine/profile')" />
    <van-cell title="隐私设置" is-link />
    <van-cell title="通知管理" is-link />
    <van-cell title="清除缓存" is-link />
    <van-cell title="退出登录" is-link class="logout-cell" @click="handleLogout" />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  showConfirmDialog({ title: '确认退出', message: '确定要退出登录吗？' })
    .then(() => {
      userStore.logout()
      showToast('已退出登录')
      router.replace('/mine')
    })
    .catch(() => {})
}
</script>

<style scoped>
.logout-cell { margin-top: 16px; }
</style>