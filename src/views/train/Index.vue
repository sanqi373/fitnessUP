<template>
  <div class="train-page page-container">
    <header class="train-header"><div><p class="eyebrow">TRAINING LIBRARY</p><h1>训练计划</h1><p>找到今天适合你的节奏</p></div><div class="train-count"><strong>{{ filteredPlans.length }}</strong><span>组计划</span></div></header>
    <div class="category-tabs"><button v-for="cat in categories" :key="cat.key" class="category-tab" :class="{ active: activeCategory === cat.key }" @click="activeCategory = cat.key">{{ cat.label }}</button></div>
    <div class="plan-list">
      <router-link v-for="plan in filteredPlans" :key="plan.id" :to="`/train/plan/${plan.id}`" class="plan-card">
        <CardCover class="plan-cover" :src="plan.cover || plan.image" :fallback="trainingCover(plan)" />
        <div class="plan-info">
          <div class="plan-card__header">
            <span class="plan-card__tag">{{ plan.typeName }}</span>
            <span>{{ plan.level }}</span>
          </div>
          <h2>{{ plan.name }}</h2>
          <p>{{ plan.desc }}</p>
          <div class="plan-card__footer">
            <span><van-icon name="clock-o" /> {{ plan.duration }} 分钟</span>
            <span><van-icon name="fire-o" /> {{ plan.calories }} 千卡</span>
            <span>{{ plan.exercises?.length || 0 }} 个动作</span>
            <van-icon class="plan-arrow" name="arrow" />
          </div>
        </div>
      </router-link>
      <van-empty v-if="filteredPlans.length === 0" description="暂无训练计划" />
    </div>
  </div>
</template>

<script>
export default { name: 'TrainingLibrary' }
</script>

<script setup>
import { ref, computed, onActivated } from 'vue'
import { getTrainPlans } from '@/api/train'
import CardCover from '@/components/CardCover.vue'
import { trainingCover } from '@/utils/cardCovers'
const categories = [{ key: 'all', label: '全部' }, { key: 'strength', label: '力量' }, { key: 'hiit', label: 'HIIT' }, { key: 'yoga', label: '瑜伽' }, { key: 'cardio', label: '有氧' }]
const activeCategory = ref('all'); const plans = ref([]); const typeNameMap = { strength: '力量', hiit: 'HIIT', yoga: '瑜伽', cardio: '有氧' }
const filteredPlans = computed(() => activeCategory.value === 'all' ? plans.value : plans.value.filter(p => p.type === activeCategory.value))
let revision = 0
onActivated(async () => {
  const current = ++revision
  try {
    const { data } = await getTrainPlans()
    if (current === revision) plans.value = data.list.map(p => ({ ...p, typeName: typeNameMap[p.type] || p.type }))
  } catch {
    // The request interceptor displays the error.
  }
})
</script>

<style scoped>
.train-page { width: 100%; min-height: 100vh; background: transparent; }.train-header { display: flex; align-items: end; justify-content: space-between; max-width: 1680px; margin: 0 auto; padding: 28px 28px 20px; }.eyebrow { margin-bottom: 7px; color: var(--color-primary-dark); font-size: 10px; font-weight: 800; letter-spacing: .12em; }.train-header h1 { font-family: 'Barlow Condensed'; font-size: 42px; line-height: .9; }.train-header p:last-child { margin-top: 8px; color: var(--color-text-secondary); font-size: 12px; }.train-count { display: flex; flex-direction: column; align-items: end; color: var(--color-text-secondary); }.train-count strong { color: var(--color-primary-dark); font-family: 'Barlow Condensed'; font-size: 40px; line-height: .8; }.train-count span { margin-top: 6px; font-size: 10px; }.category-tabs { display: flex; gap: 10px; max-width: 1680px; margin: 0 auto; padding: 4px 28px 18px; overflow-x: auto; }.category-tab { flex-shrink: 0; padding: 9px 18px; border: 1px solid var(--color-border); border-radius: 12px; color: var(--color-text-secondary); background: #fff; font-size: 12px; font-weight: 700; cursor: pointer; }.category-tab.active { border-color: var(--color-primary); color: #fff; background: var(--color-primary); box-shadow: 0 7px 16px rgba(50,201,154,.2); }.plan-list { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; max-width: 1680px; margin: 0 auto; padding: 8px 28px 128px; }
.plan-card {
  display: grid;
  grid-template-columns: 42% minmax(0, 1fr);
  min-height: 186px;
  overflow: hidden;
  border: 1px solid #d9eee7;
  border-radius: 20px;
  background: linear-gradient(145deg, #effbf6, #fff);
  box-shadow: var(--shadow-sm);
}
.plan-card:nth-child(3n + 2) { border-color: #d7eaf5; background: linear-gradient(145deg, #eff9fe, #fff); }
.plan-card:nth-child(3n) { border-color: #f5ded5; background: linear-gradient(145deg, #fff5f1, #fff); }
.plan-cover { width: 100%; height: 100%; min-height: 0; aspect-ratio: 1.35; object-fit: cover; }
.plan-info { display: flex; flex-direction: column; min-width: 0; padding: clamp(14px, 1.5vw, 24px); }
.plan-card__header { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; color: var(--color-text-secondary); font-size: 12px; }
.plan-card__tag { padding: 3px 9px; border-radius: 8px; color: #206747; background: #def2e7; font-weight: 700; }
.plan-card h2 { margin: 12px 0 6px; font: 700 clamp(20px, 1.8vw, 29px)/1.15 'Barlow Condensed', sans-serif; }
.plan-card p { color: var(--color-text-secondary); font-size: 12px; line-height: 1.55; }
.plan-card__footer { display: flex; flex-wrap: wrap; align-items: center; gap: 4px 12px; margin-top: auto; padding-top: 14px; color: var(--color-text-secondary); font-size: 11px; }
.plan-card__footer span { display: inline-flex; align-items: center; gap: 4px; }
.plan-arrow { margin-left: auto; color: var(--color-primary-dark); }
@media (max-width: 760px) {
  .train-header { padding: 24px 16px 18px; }
  .category-tabs, .plan-list { padding-right: 16px; padding-left: 16px; }
  .plan-list { grid-template-columns: minmax(0, 1fr); }
}
@media (max-width: 480px) {
  .plan-card { grid-template-columns: minmax(0, 1fr); }
  .plan-cover { height: auto; aspect-ratio: 2.1; }
}
</style>
