<template>
  <main class="diet-page page-container">
    <header class="diet-header">
      <h1>今日饮食</h1>
      <section class="intake" aria-labelledby="intake-label">
        <div class="intake-summary">
          <h2 id="intake-label">今日摄入</h2>
          <span><strong>{{ consumedCalories }}</strong> 千卡</span>
        </div>
        <div class="intake-goal">
          <div class="goal-heading">
            <span>{{ goalStatus === 'loading' ? '正在读取目标' : goalStatus === 'error' ? '目标读取失败' : `目标 ${calorieGoal} 千卡` }}</span>
            <button ref="editButton" class="edit-goal" type="button" :aria-expanded="editingGoal" aria-controls="goal-editor" @click="goalStatus === 'error' ? loadGoal() : openGoalEditor()" :disabled="goalStatus === 'loading'">
              <van-icon name="edit" aria-hidden="true" /> {{ goalStatus === 'error' ? '重新加载' : '修改目标' }}
            </button>
          </div>
          <div class="goal-progress">
            <progress :value="intakePercent" max="100" aria-label="今日摄入进度"></progress>
            <span>{{ intakePercent }}%</span>
          </div>
        </div>
        <form v-show="editingGoal" id="goal-editor" class="goal-editor" novalidate @submit.prevent="saveGoal" @keydown.esc.prevent="closeGoalEditor">
          <label for="goal-input">每日目标热量（千卡）</label>
          <div class="goal-controls">
            <input
              id="goal-input" ref="goalInput" v-model="goalDraft" type="text" inputmode="numeric"
              required :aria-invalid="Boolean(goalError)" :aria-describedby="goalError ? 'goal-error' : undefined"
              @input="goalError = ''"
            />
            <button class="save-goal" type="submit" :disabled="savingGoal">{{ savingGoal ? '正在保存' : '保存' }}</button>
            <button type="button" :disabled="savingGoal" @click="closeGoalEditor">取消</button>
          </div>
          <p v-if="goalError" id="goal-error" role="alert">{{ goalError }}</p>
        </form>
      </section>
    </header>

    <section aria-labelledby="recipes-title">
      <div class="recipe-toolbar">
        <h2 id="recipes-title">推荐食谱</h2>
        <div class="meal-filters" role="group" aria-label="按餐别筛选">
          <button
            v-for="type in mealTypes" :key="type" type="button"
            :aria-pressed="activeType === type" @click="activeType = type"
          >{{ type === 'all' ? '全部' : mealTypeLabel(type) }}</button>
        </div>
      </div>

      <div v-if="status === 'loading'" class="diet-state" role="status"><van-loading>正在加载食谱</van-loading></div>
      <van-empty v-else-if="status === 'error'" description="食谱加载失败">
        <van-button plain round size="small" @click="loadFoods">重新加载</van-button>
      </van-empty>
      <van-empty v-else-if="!visibleFoods.length" description="暂无推荐食谱" />
      <div v-else class="food-grid">
        <router-link v-for="food in visibleFoods" :key="food.id" :to="`/diet/recipe/${food.id}`" class="food-card" :class="{ 'food-card--text': !food.cover }">
          <img v-if="food.cover" class="food-cover" :src="food.cover" :alt="food.name" width="756" height="574" @error="useLocalCover(food)" />
          <div class="food-info">
            <span v-if="food.meal" class="food-type">{{ mealTypeLabel(food.meal) }}</span>
            <h3>{{ food.name }}</h3>
            <p class="food-energy"><strong>{{ food.calories }}</strong> 千卡</p>
            <dl class="food-macros">
              <template v-for="macro in nutrients" :key="macro.key">
                <div v-if="food[macro.key] != null"><dt>{{ macro.label }}</dt><dd>{{ food[macro.key] }} <small>g</small></dd></div>
              </template>
            </dl>
          </div>
        </router-link>
      </div>
    </section>
  </main>
</template>

<script>
export default { name: 'RecipeLibrary' }
</script>

<script setup>
import { computed, nextTick, onActivated, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserInfo } from '@/api/user'
import { getDietRecommend, mealTypeLabel, nutrients, setCalorieGoal } from '@/api/diet'
import { useUserStore } from '@/stores/user'
import { recipeCover } from '@/utils/cardCovers'

const userStore = useUserStore()
const router = useRouter()
const goalStatus = ref('loading')
const savingGoal = ref(false)
const calorieGoal = ref(0)
const editingGoal = ref(false)
const goalDraft = ref('')
const goalError = ref('')
const goalInput = ref(null)
const editButton = ref(null)
// Intake will come from the signed-in user's daily food log.
const consumedCalories = ref(0)
const intakePercent = computed(() => calorieGoal.value > 0
  ? Math.min(100, Math.round(consumedCalories.value / calorieGoal.value * 100))
  : 0)

function parseGoal(value) {
  const digits = value?.trim()
  const goal = Number(digits)
  return /^\d+$/.test(digits) && Number.isSafeInteger(goal) ? goal : null
}

let goalRevision = 0
async function loadGoal() {
  const current = ++goalRevision
  const session = userStore.getSessionToken()
  const active = () => current === goalRevision && session === userStore.getSessionToken()
  editingGoal.value = false
  savingGoal.value = false
  calorieGoal.value = 0
  goalStatus.value = session ? 'loading' : 'ready'
  if (!session) return
  try {
    let { data } = await getUserInfo()
    if (!active()) return
    const legacyKey = `fitnessUP:diet:calorieGoal:${data.id}`
    const legacy = parseGoal(localStorage.getItem(legacyKey))
    if (data.calorieGoal == null && legacy != null) {
      data = (await setCalorieGoal(legacy, true)).data
      if (!active()) return
    }
    if (data.calorieGoal != null) localStorage.removeItem(legacyKey)
    calorieGoal.value = data.calorieGoal ?? 0
    goalStatus.value = 'ready'
  } catch {
    if (active()) goalStatus.value = 'error'
  }
}
onActivated(loadGoal)

async function openGoalEditor() {
  if (!userStore.getSessionToken()) return router.push({ path: '/login', query: { redirect: '/diet' } })
  if (!editingGoal.value) {
    goalDraft.value = String(calorieGoal.value)
    goalError.value = ''
    editingGoal.value = true
  }
  await nextTick()
  goalInput.value.focus()
  goalInput.value.select()
}

async function closeGoalEditor() {
  editingGoal.value = false
  await nextTick()
  editButton.value.focus()
}

async function saveGoal() {
  if (savingGoal.value) return
  const session = userStore.getSessionToken()
  if (!session) return openGoalEditor()
  const goal = parseGoal(goalDraft.value)
  if (goal === null) {
    goalError.value = '请输入 0 或正整数'
    goalInput.value.focus()
    return
  }
  savingGoal.value = true
  try {
    const { data } = await setCalorieGoal(goal)
    if (session !== userStore.getSessionToken()) return
    calorieGoal.value = data.calorieGoal
    await closeGoalEditor()
  } catch {
    if (session === userStore.getSessionToken()) goalError.value = '保存失败，请重试'
  } finally {
    if (session === userStore.getSessionToken()) savingGoal.value = false
  }
}

const mealTypes = ['all', 'breakfast', 'lunch', 'dinner', 'snack']

const foods = ref([])
const activeType = ref('all')
const status = ref('loading')
const visibleFoods = computed(() => activeType.value === 'all'
  ? foods.value
  : foods.value.filter(food => food.meal === activeType.value))

function useLocalCover(food) {
  const local = recipeCover(food)
  food.cover = food.cover !== local ? local : ''
}

let foodsRevision = 0
async function loadFoods() {
  const current = ++foodsRevision
  if (!foods.value.length) status.value = 'loading'
  try {
    const { data } = await getDietRecommend()
    if (current !== foodsRevision) return
    foods.value = (data ?? []).map(food => ({
      ...food, meal: food.type || food.mealType, cover: food.image || recipeCover(food)
    }))
    status.value = 'ready'
  } catch {
    if (current === foodsRevision) status.value = 'error'
  }
}
onActivated(loadFoods)
</script>

<style scoped>
.diet-page {
  max-width: 1680px;
  margin-inline: auto;
  padding: 28px 28px calc(var(--tab-bar-height) + 36px);
}
.diet-header { display: grid; justify-items: start; gap: 16px; }
.diet-header h1 { font: 700 42px/1.1 'Barlow Condensed', sans-serif; color: var(--color-text); }
.intake {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 14px clamp(20px, 4vw, 40px);
  width: min(100%, 400px);
  padding: 14px 20px;
  border: 1px solid #d5ebe0;
  border-radius: 17px;
  background: linear-gradient(105deg, #eff9e9, #f7fcfa);
}
.intake-summary h2, .intake-goal { color: #516f62; font-size: 12px; font-weight: 600; }
.intake-summary > span, .food-energy { color: #3f6959; font-size: 12px; }
.intake-summary strong { color: #256449; font: 700 35px/1 'Barlow Condensed', sans-serif; }
.goal-heading, .goal-progress { display: flex; align-items: center; justify-content: space-between; gap: 4px 10px; }
.goal-heading { flex-wrap: wrap; }
.edit-goal { display: inline-flex; align-items: center; gap: 4px; min-height: 30px; padding: 0 8px; border: 1px solid #b7d9c7; border-radius: 8px; background: white; color: #155b40; font-size: 12px; font-weight: 700; cursor: pointer; }
.goal-progress { margin-top: 4px; font-variant-numeric: tabular-nums; }
.intake-goal progress { display: block; appearance: none; flex: 1; min-width: 0; height: 6px; border: 0; border-radius: 8px; overflow: hidden; background: #d8e9de; color: var(--color-primary-dark); }
.intake-goal progress::-webkit-progress-bar { background: #d8e9de; }
.intake-goal progress::-webkit-progress-value { background: var(--color-primary-dark); }
.intake-goal progress::-moz-progress-bar { background: var(--color-primary-dark); }
.goal-editor { grid-column: 1 / -1; padding-top: 12px; border-top: 1px solid #d5ebe0; }
.goal-editor label { color: #3f6959; font-size: 13px; font-weight: 700; }
.goal-controls { display: flex; gap: 8px; margin-top: 6px; }
.goal-controls input { flex: 1; min-width: 0; padding: 7px 10px; border: 1px solid #9fbfad; border-radius: 8px; color: var(--color-text); font-size: 16px; }
.goal-controls button { padding: 7px 12px; border: 1px solid #b7d9c7; border-radius: 8px; background: white; color: #285e48; cursor: pointer; }
.goal-controls .save-goal { border-color: #256449; background: #256449; color: white; }
.goal-editor p { margin-top: 6px; color: #a33728; font-size: 12px; }
.recipe-toolbar { display: grid; justify-items: start; gap: 12px; margin: 26px 0 16px; }
.recipe-toolbar h2 { font-size: 23px; line-height: 1.2; }
.meal-filters { display: flex; flex-wrap: wrap; gap: 8px; }
.meal-filters button { padding: 8px 15px; border: 1px solid var(--color-border); border-radius: 12px; color: #526e65; background: white; cursor: pointer; font-size: 13px; font-weight: 700; }
.meal-filters button[aria-pressed="true"] { border-color: #a2d6bd; color: #155b40; background: #d9f3e5; }
.food-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.food-card {
  display: grid;
  grid-template-columns: 43% minmax(0, 1fr);
  overflow: hidden;
  min-width: 0;
  border: 1px solid #d8eae1;
  border-radius: 20px;
  background: linear-gradient(115deg, #effaf3, white);
  box-shadow: var(--shadow-sm);
}
.food-card:nth-child(3n + 2) { border-color: #dae8ec; background: linear-gradient(115deg, #edf8fa, white); }
.food-card:nth-child(3n) { border-color: #efdfd5; background: linear-gradient(115deg, #fff4ee, white); }
.food-card--text { grid-template-columns: minmax(0, 1fr); }
.food-cover {
  width: 100%; height: 100%; min-height: 0;
  object-fit: cover; aspect-ratio: 1.36;
}
.food-info { display: flex; flex-direction: column; align-items: flex-start; min-width: 0; padding: clamp(16px, 1.55vw, 25px); overflow-wrap: anywhere; }
.food-type { padding: 3px 10px; border-radius: 8px; color: #296248; background: #e3f2e8; font-size: 12px; font-weight: 700; }
.food-info h3 { margin: 12px 0 4px; font-size: clamp(20px, 1.7vw, 28px); line-height: 1.2; }
.food-energy strong { font: 600 24px/1.2 'Barlow Condensed', sans-serif; font-variant-numeric: tabular-nums; }
.food-macros { display: flex; align-self: stretch; flex-wrap: wrap; gap: 6px 16px; margin-top: auto; padding-top: 12px; }
.food-macros dt { font-size: 11px; color: #536c62; }
.food-macros dd { color: #304a40; font-size: 14px; font-weight: 600; font-variant-numeric: tabular-nums; }
.food-macros small { font-size: 11px; font-weight: 400; }
.diet-state { padding: 32px 0; text-align: center; }
@media (max-width: 760px) {
  .diet-page { padding: 24px 16px calc(var(--tab-bar-height) + 36px); }
  .food-grid { grid-template-columns: minmax(0, 1fr); }
  .diet-header h1 { font-size: 36px; }
}
@media (max-width: 540px) {
  .recipe-toolbar { margin-top: 24px; }
  .meal-filters { gap: 6px; }
  .meal-filters button { padding: 7px 12px; }
  .food-card { grid-template-columns: 40% minmax(0, 1fr); }
  .food-card--text { grid-template-columns: minmax(0, 1fr); }
  .food-info { padding: 14px; }
  .food-macros { gap: 5px 10px; }
}
</style>
