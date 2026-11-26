<template>
  <main class="wallet">
    <section class="balance-card" aria-labelledby="balance-title">
      <h1 id="balance-title"><van-icon name="balance-o" aria-hidden="true" />钱包余额</h1>
      <p v-if="balance !== null" class="balance" aria-live="polite"><small>¥</small>{{ balance }}</p>
      <p v-else role="status">{{ loadFailed ? '余额读取失败' : '正在读取余额…' }}</p>
      <button v-if="loadFailed" type="button" class="open-button" @click="load">重新加载</button>
      <button v-else ref="openButton" class="open-button" type="button" :disabled="balance === null"
        :aria-expanded="open" aria-controls="top-up" @click="openForm">充值</button>
    </section>

    <form v-if="open" id="top-up" class="top-up" @submit.prevent="submit">
      <fieldset :disabled="saving">
        <legend>模拟充值</legend>
        <p class="hint">充值金额将计入当前账号的模拟余额。</p>
        <div class="amounts" role="group" aria-label="充值金额">
          <button v-for="value in presets" :key="value" type="button" :aria-pressed="choice === value"
            @click="choose(value)">{{ value }} <small>元</small></button>
          <button type="button" :aria-pressed="choice === 'custom'" @click="choose('custom')">自定义</button>
        </div>
        <div v-if="choice === 'custom'" class="custom-amount">
          <label for="wallet-amount">自定义金额（元）</label>
          <input id="wallet-amount" ref="customInput" v-model.trim="custom" type="text" inputmode="decimal"
            placeholder="请输入金额" autocomplete="off" :aria-invalid="invalid" aria-describedby="amount-hint" />
          <p id="amount-hint" :class="{ invalid }">请输入大于 0 的金额，最多两位小数，整数部分最多 12 位。</p>
        </div>
        <div class="actions">
          <button class="confirm" type="submit" :disabled="!valid">{{ saving ? '正在充值…' : '确认充值' }}</button>
          <button type="button" class="cancel" @click="closeForm">取消</button>
        </div>
      </fieldset>
    </form>
    <p v-if="receipt" class="receipt" role="status"><van-icon name="passed" aria-hidden="true" />{{ receipt }}</p>
  </main>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { getWallet, topUpWallet } from '@/api/wallet'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const presets = ['100', '200', '500', '1000']
const balance = ref(null)
const loadFailed = ref(false)
const open = ref(false)
const choice = ref(presets[0])
const custom = ref('')
const customInput = ref(null)
const openButton = ref(null)
const saving = ref(false)
const receipt = ref('')
const amount = computed(() => choice.value === 'custom' ? custom.value : choice.value)
const valid = computed(() => /^\d{1,12}(\.\d{1,2})?$/.test(amount.value) && Number(amount.value) > 0)
const invalid = computed(() => custom.value !== '' && !valid.value)
let pending

async function load() {
  const session = user.getSessionToken()
  loadFailed.value = false
  try {
    const { data } = await getWallet()
    if (session === user.getSessionToken()) balance.value = data.balance
  } catch {
    loadFailed.value = true
  }
}
onMounted(load)

async function openForm() {
  if (open.value) return
  receipt.value = ''
  open.value = true
  await nextTick()
  document.querySelector('#top-up button[aria-pressed="true"]')?.focus()
}

async function choose(value) {
  choice.value = value
  if (value === 'custom') {
    await nextTick()
    customInput.value?.focus()
  }
}

async function closeForm() {
  open.value = false
  await nextTick()
  openButton.value?.focus()
}

async function submit() {
  if (saving.value || !valid.value) return
  const session = user.getSessionToken()
  const value = Number(amount.value).toFixed(2)
  if (pending?.amount !== value) {
    const bytes = crypto.getRandomValues(new Uint8Array(16))
    const id = Array.from(bytes, byte => byte.toString(16).padStart(2, '0')).join('')
    pending = { id, amount: value }
  }
  saving.value = true
  try {
    const { data } = await topUpWallet(pending.id, value)
    if (session !== user.getSessionToken()) return
    balance.value = data.balance
    receipt.value = `模拟充值成功，已到账 ¥${value}`
    pending = null
    choice.value = presets[0]
    custom.value = ''
    await closeForm()
  } catch {
    // Retain the request ID so retrying a lost response credits the wallet only once.
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.wallet { max-width: 680px; margin: 0 auto; padding: clamp(24px, 5vw, 58px) 18px 52px; color: #193c34; }
.balance-card { position: relative; isolation: isolate; overflow: hidden; padding: clamp(24px, 5vw, 42px); border-radius: 22px; background: #154b40; color: #fff; }
.balance-card::after { content: ''; position: absolute; z-index: -1; width: 52%; height: 165%; right: -27%; top: -17%; border: 14px double #4a7968; border-radius: 48%; transform: rotate(26deg); }
.balance-card h1 { display: flex; align-items: center; gap: 8px; font-size: 17px; color: #d8eee2; }
.balance { margin: 14px 0 20px; font: 600 clamp(42px, 7vw, 68px)/1.1 'Barlow Condensed', sans-serif; font-variant-numeric: tabular-nums; overflow-wrap: anywhere; }
.balance small { font-size: .52em; margin-right: 6px; }
.wallet button { cursor: pointer; min-height: 44px; border-radius: 10px; font-weight: 700; }
.open-button { padding: 0 28px; color: #164c40; background: #ccf0db; border: 0; }
.top-up { margin-top: 24px; padding: clamp(18px, 4vw, 32px); background: white; border: 1px solid var(--color-border); border-radius: 16px; }
fieldset { min-width: 0; padding: 0; border: 0; }
legend { font-size: 22px; font-weight: 700; }
.hint, .custom-amount p { color: #536e62; }
.amounts { display: grid; grid-template-columns: repeat(auto-fit, minmax(85px, 1fr)); gap: 8px; margin: 18px 0; }
.amounts button { padding: 4px 8px; border: 1px solid #bdd3c6; background: #fff; color: #214c3a; }
.amounts button[aria-pressed="true"] { background: #e4f4e9; border-color: #28734d; box-shadow: inset 0 0 0 1px #28734d; }
.amounts small { font-weight: 500; }
.custom-amount label { display: block; font-weight: 700; }
.custom-amount input { width: 100%; margin: 6px 0; padding: 12px; border: 1px solid #aac6b6; border-radius: 10px; font-size: 16px; }
.custom-amount p { font-size: 12px; }
.custom-amount .invalid { color: #a13521; }
.actions { display: flex; gap: 10px; margin-top: 22px; }
.confirm { flex: 1; border: 0; background: #195941; color: white; }
.cancel { padding: 0 16px; border: 1px solid #c7d9ce; background: white; color: #284a39; }
.wallet button:disabled { opacity: .55; cursor: default; }
.receipt { display: flex; align-items: center; gap: 8px; margin: 18px 4px; color: #245e41; }
</style>
