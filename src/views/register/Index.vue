<template>
  <div class="register-page">
    <!-- 返回按钮 -->
    <van-nav-bar left-text="返回登录" left-arrow @click-left="router.replace({ name: 'Login', query: route.query })" />

    <div class="register-logo">
      <van-icon name="user-circle-o" size="80" color="#24C789" />
      <h2>注册账号</h2>
      <p class="register-desc">注册后开启你的健身之旅</p>
    </div>

    <div class="register-form">
      <van-field
        v-model="phone"
        placeholder="请输入11位手机号"
        type="tel"
        inputmode="numeric"
        autocomplete="tel"
      >
        <template #button>
          <span class="field-warning" role="status">{{ phoneHint }}</span>
        </template>
      </van-field>
      <van-field
        ref="codeField"
        v-model="verificationCode"
        placeholder="请输入6位数字验证码"
        aria-label="验证码"
        inputmode="numeric"
        autocomplete="one-time-code"
        maxlength="6"
      >
        <template #button>
          <button class="code-button" type="button">获取验证码</button>
        </template>
      </van-field>
      <van-field
        v-model="password"
        placeholder="设置密码（至少6位）"
        :type="passwordVisible ? 'text' : 'password'"
        autocomplete="new-password"
      >
        <template #button>
          <span v-if="password" class="strength" :class="'strength-' + strength" role="status">强度：{{ ['低','中','高'][strength] }}</span>
          <button class="visibility-button" type="button" aria-label="显示密码" :aria-pressed="passwordVisible"
            @mousedown.prevent @click="passwordVisible = !passwordVisible">
            <van-icon :name="passwordVisible ? 'eye-o' : 'closed-eye'" aria-hidden="true" />
          </button>
        </template>
      </van-field>
      <p class="password-help" :class="{ 'field-warning': password && passwordError }">
        {{ password && passwordError || '可使用数字、字母和符号' }}
      </p>
      <van-field
        v-model="confirmPassword"
        placeholder="请再次输入密码"
        :type="confirmPasswordVisible ? 'text' : 'password'"
        autocomplete="new-password"
      >
        <template #button>
          <span class="field-warning" role="status">{{ confirmPassword && confirmPassword !== password ? '两次密码不一致' : '' }}</span>
          <button class="visibility-button" type="button" aria-label="显示确认密码" :aria-pressed="confirmPasswordVisible"
            @mousedown.prevent @click="confirmPasswordVisible = !confirmPasswordVisible">
            <van-icon :name="confirmPasswordVisible ? 'eye-o' : 'closed-eye'" aria-hidden="true" />
          </button>
        </template>
      </van-field>
      <van-button
        round
        block
        type="primary"
        size="large"
        class="register-submit"
        :loading="loading"
        @click="handleRegister"
      >
        注册
      </van-button>

      <div class="register-extra">
        已有账号？<router-link class="register-login" :to="{ name: 'Login', query: route.query }" replace>立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { register } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const phone = ref('')
const validPhone = computed(() => /^1\d{10}$/.test(phone.value))
const phoneTouched = ref(false)
const phoneHint = computed(() => {
  if (!phoneTouched.value || validPhone.value) return ''
  if (phone.value.length !== 11) return `已输入 ${phone.value.length}/11 位`
  return '手机号格式有误'
})
const verificationCode = ref('')
const codeField = ref(null)
const password = ref('')
const confirmPassword = ref('')
const passwordVisible = ref(false)
const confirmPasswordVisible = ref(false)
const loading = ref(false)

const passwordError = computed(() => {
  if ([...password.value].length < 6) return '密码至少需要 6 位'
  if (!/^[A-Za-z0-9\p{P}\p{S}]+$/u.test(password.value)) return '请使用英文字母、数字或符号'
  if (new TextEncoder().encode(password.value).length > 72) return '密码过长，最多支持 72 字节'
  return ''
})
const strength = computed(() => {
  if (passwordError.value) return 0
  const categories = [/[0-9]/, /[a-zA-Z]/, /[\p{P}\p{S}]/u]
  return categories.filter(category => category.test(password.value)).length - 1
})

watch(phone, () => {
  verificationCode.value = ''
  phoneTouched.value = true
})

function handleRegister() {
  if (loading.value) return
  phoneTouched.value = true
  if (!phone.value) {
    showToast('请输入手机号')
    return
  }
  if (!validPhone.value) {
    showToast('请输入正确的11位手机号')
    return
  }
  if (!/^\d{6}$/.test(verificationCode.value)) {
    showToast('请输入6位数字验证码')
    codeField.value?.focus()
    return
  }
  if (!password.value) {
    showToast('请设置密码')
    return
  }
  if (passwordError.value) {
    showToast(passwordError.value)
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
      router.replace({ name: 'Home' })
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
.register-submit {
  margin-top: 24px;
}
.visibility-button {
  display: inline-grid;
  place-items: center;
  width: 36px;
  height: 32px;
  vertical-align: middle;
  border: 0;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 20px;
  cursor: pointer;
}
.register-form :deep(input::-ms-reveal) { display: none; }
.code-button {
  padding: 5px 12px;
  border: 1px solid #278265;
  border-radius: 8px;
  background: #fff;
  color: #176349;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
}
.password-help {
  padding: 2px 16px 8px;
  background: #fff;
  color: var(--color-text-secondary);
  font-size: 12px;
}
.field-warning, .strength-0 { color: #ae342e; }
.strength-1 { color: #875709; }
.strength-2 { color: #196449; }
.field-warning, .strength { font-size: 12px; }

.register-extra {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--color-text-light);
}
.register-login {
  color: var(--color-primary-dark);
  cursor: pointer;
}
</style>
