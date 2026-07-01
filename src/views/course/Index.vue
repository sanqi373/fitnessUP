<template>
  <div class="course-page page-container">
    <div class="category-tabs">
      <div
        v-for="cat in categories"
        :key="cat.key"
        class="category-tab"
        :class="{ active: activeCategory === cat.key }"
        @click="activeCategory = cat.key"
      >{{ cat.label }}</div>
    </div>

    <div class="course-list">
      <div
        v-for="course in filteredCourses"
        :key="course.id"
        class="course-card"
        @click="$router.push(`/course/${course.id}`)"
      >
        <div class="course-cover">
          <van-icon name="photo-o" size="36" color="#ddd" />
          <div class="course-badge">{{ course.difficulty }}</div>
        </div>
        <div class="course-body">
          <h4 class="course-title ellipsis-2">{{ course.title }}</h4>
          <div class="course-coach">{{ course.coach.name }}</div>
          <div class="course-meta">
            <span>{{ course.totalLessons }}课时</span>
            <span>{{ course.enrolled > 9999 ? (course.enrolled/10000).toFixed(1) + 'w' : course.enrolled }}人学习</span>
          </div>
          <div class="course-footer">
            <van-rate :model-value="Math.floor(course.rating)" size="12" color="#FF6B35" readonly />
            <span v-if="course.price > 0" class="course-price">¥{{ course.price }}</span>
            <span v-else class="course-price course-price--free">免费</span>
          </div>
        </div>
      </div>
      <van-empty v-if="filteredCourses.length === 0" description="暂无课程" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCourses } from '@/api/course'

const categories = [
  { key: 'all', label: '全部' },
  { key: '腹肌', label: '腹肌' },
  { key: '燃脂', label: '燃脂' },
  { key: '瑜伽', label: '瑜伽' },
  { key: '增肌', label: '增肌' },
  { key: '拉伸', label: '拉伸' },
  { key: '综合', label: '综合' }
]
const activeCategory = ref('all')
const courses = ref([])

const filteredCourses = computed(() => {
  if (activeCategory.value === 'all') return courses.value
  return courses.value.filter(c => c.category === activeCategory.value)
})

onMounted(async () => {
  const res = await getCourses()
  courses.value = res.data.list || []
})
</script>

<style scoped>
.category-tabs {
  display: flex; gap: 8px; padding: 12px 16px;
  background: var(--color-bg-white); overflow-x: auto;
  position: sticky; top: 0; z-index: 10;
}
.category-tab {
  flex-shrink: 0; padding: 6px 16px; border-radius: var(--radius-round);
  font-size: 13px; background: var(--color-bg);
  color: var(--color-text-secondary); cursor: pointer;
}
.category-tab.active { background: var(--color-primary); color: #fff; }
.course-list { padding: 12px 16px; overflow-y: auto; }
.course-card {
  display: flex; gap: 12px;
  background: var(--color-bg-white); border-radius: var(--radius-md);
  padding: 12px; margin-bottom: 10px; box-shadow: var(--shadow-sm);
  cursor: pointer;
}
.course-cover {
  width: 120px; height: 90px; border-radius: var(--radius-sm);
  background: #f5f5f5; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden;
}
.course-badge {
  position: absolute; top: 4px; left: 4px;
  font-size: 10px; color: #fff; background: rgba(0,0,0,0.5);
  padding: 1px 6px; border-radius: var(--radius-sm);
}
.course-body { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.course-title { font-size: 14px; font-weight: 500; margin-bottom: 4px; }
.course-coach { font-size: 12px; color: var(--color-text-light); margin-bottom: 4px; }
.course-meta { font-size: 11px; color: var(--color-text-light); margin-bottom: 6px; display: flex; gap: 12px; }
.course-footer { display: flex; align-items: center; justify-content: space-between; margin-top: auto; }
.course-price { font-size: 14px; font-weight: 600; color: var(--color-primary); }
.course-price--free { color: var(--color-success); }
</style>
