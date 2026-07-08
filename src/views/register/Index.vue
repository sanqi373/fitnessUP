<template>
  <div class="register-page">
    <!-- 返回按钮 -->
    <van-nav-bar left-text="返回" left-arrow @click-left="$router.back()" />

    <div class="register-logo">
      <van-icon name="user-circle-o" size="80" color="#FF6B35" />
      <h2>注册账号</h2>
      <p class="register-desc">注册后开启你的健身之旅</p>
    </div>

    <div class="register-form">
      <van-field
        v-model="phone"
        placeholder="请输入11位手机号"
        type="tel"
        maxlength="11"
      />
      <van-field
        v-model="password"
        placeholder="请设置密码（6位以上数字或字母）"
        type="password"
      />
      <van-field
        v-model="confirmPassword"
        placeholder="请再次输入密码"
        type="password"
      />
      <van-button
        round
        block
        type="primary"
        color="linear-gradient(135deg, #FF6B35, #FF8C5A)"
        size="large"
        :loading="loading"
        @click="handleRegister"
      >
        注册
      </van-button>

      <div class="register-extra">
        已有账号？<span class="register-login" @click="$router.back()">立即登录</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { register } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const phone = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)

function handleRegister() {
  if (!phone.value) {
    showToast('请输入手机号')
    return
  }
  if (!/^1\d{10}$/.test(phone.value)) {
    showToast('请输入正确的11位手机号')
    return
  }
  if (!password.value) {
    showToast('请设置密码')
    return
  }
  if (password.value.length < 6) {
    showToast('密码不能少于6位')
    return
  }
  if (!/^[a-zA-Z0-9]+$/.test(password.value)) {
    showToast('密码只能包含数字和字母')
    return
  }
  if (password.value !== confirmPassword.value) {
    showToast('两次输入的密码不一致')
    return
  }

  loading.value = true
  register({ phone: phone.value, password: password.value })
    .then(res => {
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data.userInfo)
      showToast('注册成功')
      router.replace('/mine')
    })
    .catch(() => {
      // 错误消息由 request 拦截器统一 toast
    })
    .finally(() => {
      loading.value = false
    })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: var(--color-bg);
}

.register-logo {
  text-align: center;
  padding: 40px 0 30px;
}
.register-logo h2 {
  margin-top: 12px;
  font-size: 24px;
  color: var(--color-text);
}
.register-desc {
  margin-top: 8px;
  font-size: 14px;
  color: var(--color-text-light);
}

.register-form {
  padding: 0 32px;
}
.register-form :deep(.van-button) {
  margin-top: 24px;
}

.register-extra {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--color-text-light);
}
.register-login {
  color: var(--color-primary);
  cursor: pointer;
}
</style>
