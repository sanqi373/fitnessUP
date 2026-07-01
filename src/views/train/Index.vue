<template>
  <div class="train-page page-container">
    <!-- 分类筛选 -->
    <div class="category-tabs">
      <div
        v-for="cat in categories"
        :key="cat.key"
        class="category-tab"
        :class="{ active: activeCategory === cat.key }"
        @click="activeCategory = cat.key"
      >{{ cat.label }}</div>
    </div>

    <!-- 训练列表 -->
    <div class="plan-list">
      <div
        v-for="plan in filteredPlans"
        :key="plan.id"
        class="plan-card card"
        @click="$router.push(`/train/plan/${plan.id}`)"
      >
        <div class="plan-card__header">
          <div class="plan-card__tag" :class="`tag--${plan.type}`">{{ plan.typeName }}</div>
          <span class="plan-card__level">{{ plan.level }}</span>
        </div>
        <h4 class="plan-card__name">{{ plan.name }}</h4>
        <p class="plan-card__desc">{{ plan.desc }}</p>
        <div class="plan-card__footer">
          <span><van-icon name="clock-o" /> {{ plan.duration }}分钟</span>
          <span><van-icon name="fire-o" /> {{ plan.calories }}千卡</span>
          <span>{{ plan.exercises?.length || 0 }}个动作</span>
        </div>
      </div>

      <van-empty v-if="filteredPlans.length === 0" description="暂无训练计划" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTrainPlans } from '@/api/train'

const categories = [
  { key: 'all', label: '全部' },
  { key: 'strength', label: '力量' },
  { key: 'hiit', label: 'HIIT' },
  { key: 'yoga', label: '瑜伽' },
  { key: 'cardio', label: '有氧' }
]

const activeCategory = ref('all')
const plans = ref([])

const typeNameMap = { strength: '力量', hiit: 'HIIT', yoga: '瑜伽', cardio: '有氧' }

const filteredPlans = computed(() => {
  if (activeCategory.value === 'all') return plans.value
  return plans.value.filter(p => p.type === activeCategory.value)
})

onMounted(async () => {
  const res = await getTrainPlans()
  plans.value = (res.data.list || []).map(p => ({
    ...p, typeName: typeNameMap[p.type] || p.type
  }))
})
</script>

<style scoped>
.category-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  overflow-x: auto;
  background: var(--color-bg-white);
  position: sticky;
  top: 0;
  z-index: 10;
}
.category-tab {
  flex-shrink: 0;
  padding: 6px 16px;
  border-radius: var(--radius-round);
  font-size: 13px;
  background: var(--color-bg);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.category-tab.active {
  background: var(--color-primary);
  color: #fff;
}
.plan-list { padding: 12px 16px; }
.plan-card { margin: 0 0 12px; cursor: pointer; }
.plan-card__header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.plan-card__tag {
  display: inline-block;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: var(--radius-round);
  color: #fff;
}
.tag--strength { background: #FF6B35; }
.tag--hiit { background: #EE0A24; }
.tag--yoga { background: #07C160; }
.tag--cardio { background: #1989FA; }
.plan-card__level { font-size: 12px; color: var(--color-primary); }
.plan-card__name { font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.plan-card__desc { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 10px; line-height: 1.5; }
.plan-card__footer {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--color-text-light);
  padding-top: 10px;
  border-top: 1px solid var(--color-divider);
}
</style>
