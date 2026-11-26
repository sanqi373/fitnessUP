<template>
  <main class="plan-detail">
    <template v-if="plan">
      <header class="plan-hero">
        <div class="plan-photo">
          <CardCover :src="plan.cover || plan.image" :fallback="trainingCover(plan)" />
        </div>
        <div class="plan-brief">
          <p class="plan-category">
            <span>{{ plan.typeName }}</span><span v-if="plan.level">{{ plan.level }}</span>
          </p>
          <h1>{{ plan.name }}</h1>
          <p v-if="plan.desc" class="plan-description">{{ plan.desc }}</p>
          <dl class="plan-metrics">
            <div v-if="plan.duration != null">
              <dt>计划时长</dt><dd>{{ plan.duration }} <small>分钟</small></dd>
            </div>
            <div v-if="plan.calories != null">
              <dt>预计消耗</dt><dd>{{ plan.calories }} <small>千卡</small></dd>
            </div>
          </dl>
        </div>
      </header>

      <dl class="training-summary">
        <dt><van-icon name="clock-o" aria-hidden="true" />已训练时长</dt>
        <dd class="trained-duration" aria-live="polite" aria-atomic="true">
          <span v-for="part in durationParts(plan.progress.trainedSeconds)" :key="part.unit">
            {{ part.value }} <small>{{ part.unit }}</small>
          </span>
        </dd>
      </dl>

      <section class="routine" aria-labelledby="routine-title">
        <header class="routine-heading">
          <h2 id="routine-title">动作安排</h2><span>共 {{ exercises.length }} 个动作</span>
        </header>
        <ol v-if="exercises.length" class="exercise-list">
          <li v-for="(exercise, index) in exercises" :key="index">
            <span class="exercise-number" aria-hidden="true">{{ String(index + 1).padStart(2, '0') }}</span>
            <div class="exercise-work">
              <h3>{{ exercise.name }}</h3>
              <p v-if="exercise.sets || exercise.reps || exercise.duration">
                <span v-if="exercise.sets">{{ exercise.sets }} 组</span>
                <span v-if="exercise.reps && (exercise.reps > 1 || !exercise.duration)">{{ exercise.reps }} 次</span>
                <span v-if="exercise.duration">{{ formatDuration(exercise.duration) }}</span>
              </p>
            </div>
            <span v-if="exercise.rest" class="exercise-rest">
              <van-icon name="clock-o" aria-hidden="true" />休息 {{ formatDuration(exercise.rest) }}
            </span>
          </li>
        </ol>
        <p v-else class="routine-empty">暂无动作明细</p>
      </section>
    </template>
    <van-empty v-else-if="error" :description="error">
      <van-button plain round size="small" @click="load">重新加载</van-button>
    </van-empty>
    <div v-else class="loading" role="status"><van-loading>正在加载训练计划</van-loading></div>

    <footer class="action-bar">
      <div class="plan-actions" :class="{ timing: plan?.progress.timer }">
        <router-link to="/mine/training-plan" class="collection-link">
          <van-icon name="orders-o" aria-hidden="true" />查看我的训练计划
        </router-link>
        <template v-if="plan">
          <PlanSaveButton kind="training" :id="plan.id" :show-collection-link="false" />
          <WorkoutTimer :key="plan.id" :plan-id="plan.id" v-model:progress="plan.progress" />
        </template>
      </div>
    </footer>
  </main>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { getTrainPlanDetail } from '@/api/train'
import WorkoutTimer from '@/components/WorkoutTimer.vue'
import { durationParts } from '@/utils/workoutTime'
import CardCover from '@/components/CardCover.vue'
import { trainingCover } from '@/utils/cardCovers'
import PlanSaveButton from '@/components/PlanSaveButton.vue'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const error = ref('')
let revision = 0
const props = defineProps({ id: { type: String, required: true } })
const plan = ref(null)
const exercises = computed(() => Array.isArray(plan.value?.exercises) ? plan.value.exercises : [])

const typeNameMap = { strength: '力量', hiit: 'HIIT', yoga: '瑜伽', cardio: '有氧' }

async function load() {
  const current = ++revision
  const session = store.getSessionToken()
  if (!session) return
  plan.value = null
  error.value = ''
  try {
    const { data } = await getTrainPlanDetail(props.id)
    if (current === revision && session === store.getSessionToken()) {
      plan.value = { ...data, typeName: typeNameMap[data.type] || data.type }
    }
  } catch (e) {
    if (current === revision) error.value = e.response?.status === 404 ? '训练不存在' : '训练加载失败'
  }
}
watch([() => props.id, () => store.token], load, { immediate: true })

function formatDuration(seconds) {
  const minutes = Math.floor(seconds / 60)
  const remainder = seconds % 60
  if (!minutes) return `${seconds} 秒`
  return remainder ? `${minutes} 分 ${remainder} 秒` : `${minutes} 分钟`
}

</script>

<style scoped>
.plan-detail {
  --pine: #173f39;
  --mint: #bcebd2;
  --paper: #fcfefc;
  max-width: 1184px;
  margin: 0 auto;
  padding: clamp(16px, 3vw, 40px) clamp(16px, 3vw, 36px) calc(106px + env(safe-area-inset-bottom));
  color: var(--pine);
}
.plan-hero {
  display: grid;
  grid-template-columns: 1.14fr 1fr;
  overflow: hidden;
  border-radius: 24px;
  background: var(--pine);
  color: var(--paper);
}
.plan-photo { position: relative; min-height: 290px; background: #315951; }
.plan-photo img { position: absolute; width: 100%; height: 100%; object-fit: cover; object-position: center 35%; }
.plan-brief { display: flex; flex-direction: column; align-items: flex-start; padding: clamp(24px, 3vw, 46px); }
.plan-category { display: flex; align-items: center; color: var(--mint); font-size: 14px; font-weight: 700; }
.plan-category span + span { margin-left: 12px; padding-left: 12px; border-left: 1px solid #72958b; line-height: 1; }
.plan-brief h1 { margin: 12px 0 9px; font-size: clamp(30px, 3.4vw, 52px); line-height: 1.12; letter-spacing: -.035em; overflow-wrap: anywhere; }
.plan-description { color: #d8e9df; font-size: 15px; line-height: 1.7; overflow-wrap: anywhere; }
.plan-metrics { display: flex; flex-wrap: wrap; gap: 14px clamp(16px, 2vw, 32px); width: 100%; margin-top: auto; padding-top: 24px; }
.plan-metrics div { border-left: 2px solid #75b59a; padding-left: 12px; }
.plan-metrics dt { font-size: 12px; color: #d8e9df; }
.plan-metrics dd { font: 600 34px/1.15 'Barlow Condensed', sans-serif; font-variant-numeric: tabular-nums; }
.plan-metrics small { font: 500 12px 'DM Sans', sans-serif; }
.training-summary {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 18px;
  margin: clamp(16px, 2vw, 24px) 0 0;
  padding: 16px clamp(16px, 2.5vw, 30px);
  border: 1px solid #ebd6b4;
  border-inline-start: 5px solid #bf652d;
  border-radius: 14px;
  background: #fff1da;
  color: #633917;
}
.training-summary dt { display: flex; flex: 1; align-items: center; gap: 10px; white-space: nowrap; font-size: clamp(16px, 1.4vw, 20px); font-weight: 700; }
.training-summary dt .van-icon { font-size: 25px; }
.training-summary dt::after { content: ''; flex: 1; margin-inline-start: 6px; border-bottom: 2px dotted #dec6a5; }
.trained-duration { display: flex; flex-wrap: wrap; align-items: baseline; gap: 0 12px; font: 650 clamp(36px, 4vw, 56px)/1 'Barlow Condensed', sans-serif; font-variant-numeric: tabular-nums; }
.trained-duration span { white-space: nowrap; }
.trained-duration small { font: 600 14px/1.2 'DM Sans', sans-serif; }
.routine { margin-top: clamp(24px, 3vw, 38px); }
.routine-heading { display: flex; align-items: baseline; gap: 14px; margin-bottom: 12px; }
.routine-heading h2 { font-size: clamp(22px, 2vw, 28px); }
.routine-heading span, .routine-empty { font-size: 14px; color: var(--color-text-secondary); }
.exercise-list { list-style: none; padding: 0 clamp(14px, 2.6vw, 30px); background: var(--paper); border: 1px solid var(--color-border); border-radius: 18px; }
.exercise-list li { display: grid; grid-template-columns: 2.5em minmax(0, 1fr) auto; gap: 12px; align-items: center; padding: 16px 0; }
.exercise-list li + li { border-top: 1px solid var(--color-border); }
.exercise-number { font: 600 28px/1 'Barlow Condensed', sans-serif; font-variant-numeric: tabular-nums; color: #477c67; }
.exercise-work h3 { font-size: 17px; line-height: 1.4; overflow-wrap: anywhere; }
.exercise-work p { display: flex; flex-wrap: wrap; align-items: center; margin-top: 4px; color: #546e63; }
.exercise-work p span + span::before { content: '·'; padding: 0 7px; }
.exercise-rest { display: inline-flex; align-items: center; gap: 5px; color: #546e63; font-size: 13px; }
.action-bar { position: fixed; z-index: 5; inset: auto 0 0; border-top: 1px solid #cfddd5; background: var(--paper); }
.plan-actions { display: flex; align-items: center; gap: 12px; max-width: 1184px; margin: auto; padding: 12px clamp(16px, 3vw, 36px) calc(12px + env(safe-area-inset-bottom)); }
.collection-link { display: inline-flex; align-items: center; justify-content: center; gap: 7px; min-height: 46px; border-radius: 12px; font-weight: 700; }
.collection-link { margin-right: auto; padding: 0 14px; border: 1px solid #c4d9cc; color: var(--pine); }
.plan-actions :deep(.plan-save button) { padding-inline: 16px; background: #eaf5ec; border-color: #bbd6c6; color: var(--pine); }
.plan-actions :deep(.plan-save button.joined) { background: #d5eada; }
.collection-link:hover { background: #edf5ef; }
.loading { padding: 60px 0; text-align: center; }
@media (max-width: 680px) {
  .training-summary { padding: 12px 15px; }
  .training-summary dt::after { display: none; }
  .plan-hero { grid-template-columns: minmax(0, 1fr); border-radius: 18px; }
  .plan-photo { min-height: 0; aspect-ratio: 2.1; }
  .plan-brief { padding: 20px; }
  .plan-metrics { padding-top: 18px; }
  .exercise-list li { grid-template-columns: 24px minmax(0, 1fr) auto; gap: 9px; }
  .exercise-number { font-size: 22px; }
}
@media (max-width: 680px) {
  .plan-detail { padding-bottom: calc(166px + env(safe-area-inset-bottom)); }
  .plan-actions { display: grid; grid-template-columns: minmax(0, 1.25fr) minmax(0, 1fr); gap: 6px 8px; padding-top: 8px; padding-bottom: calc(5px + env(safe-area-inset-bottom)); }
  .collection-link { grid-column: 1 / -1; grid-row: 2; margin: 0; min-height: 44px; background: #f5faf6; }
  .plan-actions :deep(.plan-save button) { padding-inline: 4px; }
  .timing .collection-link { grid-column: 1; }
  .timing .plan-save { grid-column: 2; grid-row: 2; }
  .timing { grid-template-columns: 1.08fr 1fr; }
  .timing .collection-link { padding: 0 3px; }
}
@media (max-width: 360px) {
  .timing .collection-link, .timing :deep(.plan-save button) { gap: 4px; font-size: 13px; }
}
</style>
