<template>
  <div class="diet-page page-container">
    <div class="calorie-card card">
      <div class="calorie-title">今日摄入</div>
      <div class="calorie-value">1,280<span class="calorie-unit">千卡</span></div>
      <van-progress :percentage="60" stroke-color="#FF6B35" track-color="#f5f5f5" />
      <div class="calorie-range">目标 2100 千卡</div>
    </div>

    <h3 class="section-label">推荐食谱</h3>
    <div class="food-grid">
      <div v-for="food in foods" :key="food.id" class="food-card card">
        <div class="food-cover">
          <van-icon name="photo-o" size="28" color="#ddd" />
        </div>
        <div class="food-info">
          <h4>{{ food.name }}</h4>
          <div class="food-macros">
            <span>🔥 {{ food.calories }}千卡</span>
            <span>💪 蛋白{{ food.protein }}g</span>
            <span>🍚 碳水{{ food.carbs }}g</span>
          </div>
          <div class="food-type">{{ typeLabel(food.type) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { mockDietRecommend } from '@/mock/diet'
const foods = ref(mockDietRecommend)
function typeLabel(t) {
  const map = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐', snack: '加餐' }
  return map[t] || t
}
</script>

<style scoped>
.diet-page { padding-bottom: 20px; }
.calorie-card { margin: 16px; text-align: center; }
.calorie-title { font-size: 14px; color: var(--color-text-light); margin-bottom: 8px; }
.calorie-value { font-size: 36px; font-weight: 700; color: var(--color-primary); margin-bottom: 12px; }
.calorie-unit { font-size: 14px; font-weight: 400; }
.calorie-range { font-size: 12px; color: var(--color-text-light); margin-top: 8px; }
.section-label { font-size: 16px; font-weight: 600; padding: 0 16px; margin-bottom: 10px; }
.food-grid { padding: 0 16px; }
.food-card { display: flex; gap: 12px; margin: 0 0 10px; cursor: pointer; }
.food-cover {
  width: 80px; height: 70px; border-radius: var(--radius-sm);
  background: #f5f5f5; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.food-info { flex: 1; }
.food-info h4 { font-size: 14px; margin-bottom: 4px; }
.food-macros { font-size: 11px; color: var(--color-text-light); display: flex; gap: 8px; margin-bottom: 4px; }
.food-type { font-size: 10px; color: var(--color-primary); }
</style>
