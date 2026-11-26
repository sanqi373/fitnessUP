<template>
  <main class="journal">
    <div class="sheet">
      <header class="mast">
        <h1>身体数据</h1>
        <label class="date-control">记录日期
          <input type="date" aria-label="记录日期" :value="day" min="1000-01-01" :max="today"
            :disabled="saving" @change="pickDate" />
        </label>
      </header>

      <div v-if="phase === 'loading'" class="status" role="status">正在读取记录…</div>
      <div v-else-if="phase === 'error'" class="status" role="alert">
        记录读取失败。<button type="button" class="text-button" @click="loadMonth()">重新加载</button>
      </div>
      <template v-else>
        <div class="layout">
          <form class="entry" @submit.prevent="save">
            <fieldset :disabled="saving">
              <section v-for="group in groups" :key="group.title" class="measure-group">
                <h2><van-icon :name="group.icon" aria-hidden="true" />{{ group.title }}</h2>
                <div class="fields">
                  <label v-for="field in group.fields" :key="field.key" class="metric">
                    <span>{{ field.name }}</span>
                    <span class="number-box">
                      <input v-model="draft[field.key]" type="number" inputmode="decimal" step="0.1"
                        :min="field.min" :max="field.max" :aria-label="field.name" placeholder="—" />
                      <small>{{ field.unit }}</small>
                    </span>
                  </label>
                </div>
              </section>
            </fieldset>
            <div class="save-row">
              <span>{{ dirty ? '有未保存的修改' : activeRecord ? '当日记录已保存' : '可只填写已测量的项目' }}</span>
              <button class="primary" type="submit" :disabled="saving || !dirty || conflict">{{ saving ? '正在保存…' : '保存记录' }}</button>
            </div>
            <p v-if="error" class="error" role="alert">{{ error }}
              <button v-if="conflict" type="button" class="text-button" @click="reloadConflict">重新加载</button>
            </p>
          </form>

          <aside class="history">
            <h2>历史记录</h2>
            <div class="month-nav">
              <button aria-label="上个月" :disabled="saving || month === '1000-01'" @click="moveMonth(-1)">‹</button>
              <strong>{{ Number(month.slice(0,4)) }} 年 {{ Number(month.slice(5)) }} 月</strong>
              <button aria-label="下个月" :disabled="saving || month >= today.slice(0,7)" @click="moveMonth(1)">›</button>
            </div>
            <p v-if="!records.length" class="empty">本月还没有记录。保存后，可在这里按日期回看。</p>
            <ul v-else>
              <li v-for="row in records" :key="row.date">
                <button :class="{ selected: row.date === day }" :aria-current="row.date === day ? 'date' : undefined"
                  :disabled="saving" @click="changeDay(row.date)">
                  <strong>{{ Number(row.date.slice(5,7)) }} 月 {{ Number(row.date.slice(8)) }} 日</strong>
                  <span>{{ summary(row) }}</span>
                  <van-icon name="arrow" aria-hidden="true" />
                </button>
              </li>
            </ul>
          </aside>
        </div>
      </template>
    </div>
  </main>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'

const groups = [
  { title: '身体测量', icon: 'chart-trending-o', fields: [
    { key: 'weight', name: '体重', unit: 'kg', min: 0.1, max: 1000 },
    { key: 'height', name: '身高', unit: 'cm', min: 0.1, max: 300 },
    { key: 'bodyFat', name: '体脂率', unit: '%', min: 0.1, max: 100 },
    { key: 'waist', name: '腰围', unit: 'cm', min: 0.1, max: 500 },
    { key: 'basalMetabolism', name: '基础代谢', unit: 'kcal/天', min: 0.1, max: 15000 }
  ] },
  { title: '每日摄入', icon: 'orders-o', fields: [
    { key: 'carbs', name: '碳水化合物', unit: 'g', min: 0, max: 5000 },
    { key: 'protein', name: '蛋白质', unit: 'g', min: 0, max: 5000 },
    { key: 'fat', name: '脂肪', unit: 'g', min: 0, max: 5000 },
    { key: 'water', name: '饮水量', unit: 'mL', min: 0, max: 30000 }
  ] }
]
const fields = groups.flatMap(group => group.fields)
function localDate(date) {
  return [date.getFullYear(), date.getMonth() + 1, date.getDate()].map(n => String(n).padStart(2, '0')).join('-')
}
const today = localDate(new Date())
const day = ref(today)
const month = computed(() => day.value.slice(0, 7))
const records = ref([])
const activeRecord = computed(() => records.value.find(row => row.date === day.value))
const draft = ref({})
const phase = ref('loading')
const saving = ref(false)
const conflict = ref(false)
const error = ref('')
const store = useUserStore()
const router = useRouter()
let version = 0
let owner = ''
const dirty = computed(() => phase.value === 'ready' && fields.some(({ key }) =>
  String(draft.value[key] ?? '') !== String(activeRecord.value?.[key] ?? '')))

function resetDraft() {
  draft.value = Object.fromEntries(fields.map(({ key }) => [key, activeRecord.value?.[key] ?? '']))
  error.value = ''
  conflict.value = false
}
function canLeave() {
  return !saving.value && (!dirty.value || window.confirm('放弃未保存的修改？'))
}
onBeforeRouteLeave(() => !store.token || canLeave())

async function loadMonth() {
  const current = ++version
  const session = store.getSessionToken()
  phase.value = 'loading'
  records.value = []
  owner = ''
  resetDraft()
  if (!session) return router.replace({ path: '/login', query: { redirect: '/mine/body-data' } })
  try {
    const { data } = await request.get('/user/body-records', { params: { month: month.value } })
    if (current !== version || session !== store.getSessionToken()) return
    records.value = data
    owner = session
    resetDraft()
    phase.value = 'ready'
  } catch {
    if (current === version) phase.value = 'error'
  }
}
watch(() => store.token, () => { day.value = today; loadMonth() }, { immediate: true })

function changeDay(value) {
  if (!value || value === day.value || value > today || !canLeave()) return
  const previous = month.value
  day.value = value
  if (month.value !== previous || phase.value !== 'ready') loadMonth()
  else resetDraft()
}
function pickDate(event) {
  const input = event.target
  if (input.validity.valid) changeDay(input.value)
  input.value = day.value
}
function moveMonth(delta) {
  const [year, current] = month.value.split('-').map(Number)
  const first = localDate(new Date(year, current - 1 + delta, 1))
  changeDay(first.slice(0,7) === today.slice(0,7) ? today : first)
}
function reloadConflict() {
  if (canLeave()) loadMonth()
}
function summary(record) {
  const present = fields.filter(field => record[field.key] != null)
  return present.slice(0, 2).map(f => f.name + ' ' + record[f.key] + ' ' + f.unit).join(' · ')
}
async function save() {
  const session = store.getSessionToken()
  if (!session || session !== owner || saving.value || conflict.value || !dirty.value) return
  const values = Object.fromEntries(fields.map(({ key }) => [key, draft.value[key] === '' ? null : Number(draft.value[key])]))
  if (Object.values(values).every(value => value == null)) {
    error.value = '请至少填写一项数据'
    return
  }
  const current = version
  const date = day.value
  saving.value = true
  error.value = ''
  try {
    const { data: revision } = await request.put('/user/body-records/' + date, { ...values, revision: activeRecord.value?.revision ?? 0 })
    if (current !== version || session !== store.getSessionToken()) return
    records.value = [...records.value.filter(row => row.date !== date), { date, revision, ...values }]
      .sort((a, b) => b.date.localeCompare(a.date))
    resetDraft()
    showToast('记录已保存')
  } catch (err) {
    if (current !== version) return
    conflict.value = err.response?.status === 409
    error.value = err.response?.data?.message || '保存失败，请重试'
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.journal { min-height: 100vh; background: var(--color-bg); }
.sheet { max-width: 1120px; margin: auto; padding: clamp(20px, 4vw, 48px) clamp(16px, 4vw, 44px) 56px; }
.mast { display: flex; align-items: end; justify-content: space-between; flex-wrap: wrap; gap: 14px; margin-bottom: 22px; }
h1 { font-size: clamp(30px, 4.4vw, 44px); line-height: 1.15; letter-spacing: -.04em; }
h2 { font-size: 20px; line-height: 1.4; }
.date-control { display: flex; align-items: center; gap: 10px; color: var(--color-text-secondary); font-size: 13px; }
.date-control input { color: var(--color-text); padding: 8px 10px; border: 1px solid #bdd5ca; background: white; border-radius: 8px; }
.layout { display: grid; grid-template-columns: minmax(0, 1fr) minmax(220px, 30%); gap: clamp(18px, 3vw, 38px); align-items: start; }
.entry { background: white; border: 1px solid var(--color-border); border-radius: 16px; overflow: hidden; }
fieldset { border: 0; min-width: 0; }
.measure-group { padding: 20px clamp(16px, 2.7vw, 30px) 10px; }
.measure-group h2 { display: flex; align-items: center; gap: 10px; }
.measure-group h2 i { font-size: 20px; color: #34785e; }
.fields { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); column-gap: 26px; margin-top: 8px; }
.metric { display: flex; align-items: center; justify-content: space-between; gap: 5px; padding: 8px 0; border-bottom: 1px solid var(--color-divider); }
.number-box { display: flex; align-items: baseline; gap: 4px; padding: 4px 6px; background: #f5faf7; border-radius: 5px; }
.number-box:focus-within { outline: 2px solid #37866b; }
.metric input { width: 6ch; min-width: 0; font-size: 16px; font-weight: 650; font-variant-numeric: tabular-nums; text-align: right; color: var(--color-text); background: transparent; border: 0; outline: 0; appearance: textfield; }
.metric input::-webkit-inner-spin-button { appearance: none; }
.metric small { color: #50675e; font-size: 12px; white-space: nowrap; }
.save-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; background: #edf6f1; padding: 15px clamp(16px, 2.7vw, 30px); margin-top: 15px; }
.save-row span, .empty, .status { color: var(--color-text-secondary); }
button { cursor: pointer; }
button:disabled { cursor: default; opacity: .55; }
.primary { border: 0; border-radius: 9px; background: #196449; color: white; font-weight: 700; padding: 10px 18px; }
.error { color: #963925; padding: 12px 16px; }
.text-button { background: none; border: 0; text-decoration: underline; color: #21634d; }
.history { border-top: 3px solid #609b80; padding-top: 14px; }
.month-nav { display: flex; justify-content: space-between; align-items: center; margin: 10px 0; }
.month-nav button { background: white; border: 1px solid var(--color-border); border-radius: 8px; width: 36px; height: 36px; font-size: 22px; color: #326951; }
.history ul { list-style: none; max-height: 500px; overflow: auto; }
.history li button { display: grid; grid-template-columns: 1fr auto; gap: 3px 8px; width: 100%; text-align: left; color: inherit; background: transparent; border: 0; border-bottom: 1px solid #d8e8df; padding: 11px 12px; }
.history li button.selected { background: #e1f1e8; }
.history li span { grid-column: 1; font-size: 12px; color: #50675e; }
.history li i { grid-column: 2; grid-row: 1 / 3; align-self: center; }
.empty, .status { padding: 15px 0; }
@media (max-width: 850px) { .layout { grid-template-columns: minmax(0, 1fr); } }
@media (max-width: 520px) {
  .fields { grid-template-columns: 1fr; }
  .metric { padding: 5px 0; }
  .mast { align-items: start; }
  .date-control { flex-wrap: wrap; }
}
</style>
