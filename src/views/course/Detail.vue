<template>
  <div class="detail-page" v-if="course">
    <van-nav-bar title="课程详情" left-text="返回" left-arrow @click-left="$router.back()" />
    <div class="course-hero">
      <div class="hero-cover">
        <van-icon name="photo-o" size="48" color="#ddd" />
      </div>
      <h2 class="hero-title">{{ course.title }}</h2>
      <div class="hero-meta">
        <span>{{ course.coach.name }}</span>
        <span>{{ course.difficulty }}</span>
        <span>{{ course.totalLessons }}课时</span>
      </div>
      <div class="hero-stats">
        <van-rate :model-value="Math.floor(course.rating)" size="14" color="#FF6B35" readonly />
        <span>{{ course.enrolled > 9999 ? (course.enrolled/10000).toFixed(1)+'w' : course.enrolled }}人已学</span>
      </div>
    </div>
    <div class="course-desc card">
      <h3>课程简介</h3>
      <p>{{ course.desc }}</p>
    </div>
    <div class="bottom-bar safe-area-bottom">
      <span class="price" v-if="course.price">¥{{ course.price }}</span>
      <span class="price price--free" v-else>免费</span>
      <van-button round type="primary" color="linear-gradient(135deg, #FF6B35, #FF8C5A)">立即加入</van-button>
    </div>
  </div>
  <van-loading v-else class="loading" />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCourseDetail } from '@/api/course'
const route = useRoute()
const course = ref(null)
onMounted(async () => {
  const res = await getCourseDetail(route.params.id)
  course.value = res.data
})
</script>

<style scoped>
.detail-page { min-height: 100vh; background: var(--color-bg); padding-bottom: 70px; }
.course-hero { background: #fff; padding: 20px 16px; text-align: center; }
.hero-cover { width: 100%; height: 180px; background: #f5f5f5; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; margin-bottom: 16px; }
.hero-title { font-size: 20px; margin-bottom: 10px; }
.hero-meta { display: flex; justify-content: center; gap: 16px; font-size: 13px; color: var(--color-text-light); margin-bottom: 8px; }
.hero-stats { display: flex; justify-content: center; gap: 8px; font-size: 12px; color: var(--color-text-light); }
.course-desc { margin: 12px 16px; }
.course-desc h3 { font-size: 16px; margin-bottom: 8px; }
.course-desc p { font-size: 14px; line-height: 1.7; color: var(--color-text-secondary); }
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; padding: 10px 16px; background: #fff; display: flex; align-items: center; gap: 12px; justify-content: flex-end; }
.price { font-size: 18px; font-weight: 700; color: var(--color-primary); }
.price--free { color: var(--color-success); }
.loading { display: flex; justify-content: center; padding-top: 100px; }
</style>
