<template>
  <div class="login-page">
    <div class="login-logo">
      <van-icon name="user-circle-o" size="80" color="#FF6B35" />
      <h2>UP 健身</h2>
    </div>
    <div class="login-form">
      <van-field v-model="phone" placeholder="请输入手机号" type="tel" />
      <van-field v-model="password" placeholder="请输入密码" type="password" />
      <van-button round block type="primary" color="linear-gradient(135deg, #FF6B35, #FF8C5A)" size="large" @click="handleLogin">登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { login } from '@/api/user'
const phone = ref('')
const password = ref('')
const router = useRouter()
async function handleLogin() {
  if (!phone.value || !password.value) { showToast('请输入手机号和密码'); return }
  try {
    await login({ phone: phone.value, password: password.value })
    showToast('登录成功')
    router.replace('/')
  } catch (e) {
    showToast('登录失败')
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; flex-direction: column; align-items: center; padding-top: 80px; background: var(--color-bg); }
.login-logo { text-align: center; margin-bottom: 40px; }
.login-logo h2 { margin-top: 12px; font-size: 24px; color: var(--color-text); }
.login-form { width: 320px; }
.login-form :deep(.van-button) { margin-top: 20px; }
</style>