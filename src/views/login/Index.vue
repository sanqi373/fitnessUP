<template>
  <main class="login-page">
    <div class="login-panel">
      <div class="cover">
        <img src="/training/distance-run.webp" alt="" />
        <span class="brand"><b>UP</b> 健身</span>
      </div>
      <section class="account">
        <h1>登录账号</h1>
        <form @submit.prevent="handleLogin">
          <label>手机号
            <input v-model.trim="phone" type="tel" inputmode="numeric" autocomplete="username"
              placeholder="请输入11位手机号" maxlength="11" pattern="1[0-9]{10}" required />
          </label>
          <label>密码
            <input v-model="password" type="password" autocomplete="current-password"
              placeholder="请输入密码" minlength="6" required />
          </label>
          <button type="submit" :disabled="loading">{{ loading ? '正在登录…' : '登录' }}</button>
        </form>
        <p>还没有账号？ <router-link :to="{ name: 'Register', query: route.query }">立即注册</router-link></p>
      </section>
    </div>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const phone = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (loading.value) return
  loading.value = true
  try {
    const { data } = await login({ phone: phone.value, password: password.value })
    userStore.setToken(data.token)
    userStore.setUserInfo(data.userInfo)
    await router.replace({ name: 'Home' })
  } catch {
    // 接口错误由请求拦截器提示。
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; min-height: 100dvh; display: grid; place-items: center; padding: clamp(18px, 4vw, 64px); }
.login-panel { display: grid; grid-template-columns: 1fr 1fr; width: min(100%, 900px); background: white; border-radius: 22px; overflow: hidden; box-shadow: var(--shadow-md); }
.cover { position: relative; isolation: isolate; min-height: 490px; color: white; background: #235942; }
.cover img { position: absolute; width: 100%; height: 100%; object-fit: cover; z-index: -2; }
.cover::after { position: absolute; content: ''; inset: 0; z-index: -1; background: linear-gradient(165deg, rgba(13, 42, 31, .72), transparent 65%); }
.brand { display: inline-flex; align-items: baseline; gap: 7px; margin: 26px; font-size: 18px; font-weight: 700; }
.brand b { font: italic 700 38px/1 'Barlow Condensed', sans-serif; letter-spacing: -2px; }
.account { align-self: center; padding: clamp(28px, 5vw, 56px); }
h1 { font-size: clamp(27px, 3vw, 36px); margin-bottom: 28px; }
label { display: grid; gap: 7px; font-size: 14px; font-weight: 650; margin-bottom: 18px; }
input { width: 100%; min-height: 46px; padding: 10px 12px; border: 1px solid #c8d9d2; border-radius: 8px; color: var(--color-text); font-size: 16px; }
button { width: 100%; margin-top: 6px; border: 0; border-radius: 8px; padding: 13px; background: #196449; color: white; font-weight: 700; cursor: pointer; }
button:disabled { opacity: .65; }
p { margin-top: 22px; color: var(--color-text-secondary); }
a { color: #196449; font-weight: 700; }
@media (max-width: 650px) {
  .login-panel { max-width: 420px; grid-template-columns: 1fr; }
  .cover { min-height: 145px; }
  .cover img { object-position: center 45%; }
  .brand { margin: 19px; }
  .account { padding: 26px 22px 30px; }
}
</style>
