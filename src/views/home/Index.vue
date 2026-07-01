<template>
  <div class="home-page">
    <!-- 顶部 -->
    <div class="home-header">
      <div class="home-header__greeting">
        <div class="home-header__avatar">
          <van-icon name="user-circle-o" size="40" color="#FF6B35" />
        </div>
        <div>
          <div class="home-header__hi">{{ greeting }}</div>
          <div class="home-header__slogan">每一次训练都算数</div>
        </div>
      </div>
      <div class="home-header__stats">
        <div class="stat-item">
          <span class="stat-value">{{ userStore.stats.streakDays }}</span>
          <span class="stat-label">连续天数</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ userStore.stats.totalTrainMinutes }}</span>
          <span class="stat-label">训练分钟</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ formatCalories(userStore.stats.totalCalories) }}</span>
          <span class="stat-label">消耗千卡</span>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-actions">
      <div class="action-item" @click="$router.push('/train')">
        <div class="action-icon action-icon--train">
          <van-icon name="bars" size="22" />
        </div>
        <span>开始训练</span>
      </div>
      <div class="action-item" @click="$router.push('/course')">
        <div class="action-icon action-icon--course">
          <van-icon name="play-circle-o" size="22" />
        </div>
        <span>精选课程</span>
      </div>
      <div class="action-item" @click="$router.push('/diet')">
        <div class="action-icon action-icon--diet">
          <van-icon name="balance-o" size="22" />
        </div>
        <span>饮食记录</span>
      </div>
      <div class="action-item" @click="$router.push('/live')">
        <div class="action-icon action-icon--live">
          <van-icon name="video-o" size="22" />
        </div>
        <span>直播课</span>
      </div>
    </div>

    <!-- 今日推荐训练 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">今日推荐训练</h3>
        <span class="section-more" @click="$router.push('/train')">更多 ></span>
      </div>
      <div class="train-scroll">
        <div
          v-for="plan in trainPlans.slice(0, 3)"
          :key="plan.id"
          class="train-card"
          @click="$router.push(`/train/plan/${plan.id}`)"
        >
          <div class="train-card__info">
            <div class="train-card__tag" :class="`tag--${plan.type}`">{{ plan.typeName }}</div>
            <h4 class="train-card__name">{{ plan.name }}</h4>
            <div class="train-card__meta">
              <span><van-icon name="clock-o" /> {{ plan.duration }}分钟</span>
              <span><van-icon name="fire-o" /> {{ plan.calories }}千卡</span>
            </div>
          </div>
          <div class="train-card__level">{{ plan.level }}</div>
        </div>
      </div>
    </div>

    <!-- 推荐课程 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">热门课程</h3>
        <span class="section-more" @click="$router.push('/course')">更多 ></span>
      </div>
      <div class="course-grid">
        <div
          v-for="course in courses.slice(0, 4)"
          :key="course.id"
          class="course-card"
          @click="$router.push(`/course/${course.id}`)"
        >
          <div class="course-card__cover">
            <van-icon name="photo-o" size="32" color="#ccc" />
          </div>
          <div class="course-card__body">
            <h4 class="course-card__title ellipsis-2">{{ course.title }}</h4>
            <div class="course-card__meta">
              <span>{{ course.difficulty }}</span>
              <span>{{ course.enrolled / 10000 }}万人练</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 社区动态 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">社区动态</h3>
        <span class="section-more" @click="$router.push('/community')">更多 ></span>
      </div>
      <div class="post-list">
        <div
          v-for="post in posts.slice(0, 2)"
          :key="post.id"
          class="post-card card"
          @click="$router.push(`/community/post/${post.id}`)"
        >
          <div class="post-header">
            <van-icon name="user-circle-o" size="36" color="#ccc" />
            <div class="post-user">
              <span class="post-name">{{ post.user.name }}</span>
              <span class="post-time">{{ post.time }}</span>
            </div>
          </div>
          <div class="post-content ellipsis-2">{{ post.content }}</div>
          <div class="post-actions">
            <span><van-icon name="good-job-o" /> {{ post.likes }}</span>
            <span><van-icon name="chat-o" /> {{ post.comments }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTrainPlans } from '@/api/train'
import { getCourses } from '@/api/course'

const userStore = useUserStore()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const trainPlans = ref([])
const courses = ref([])
const posts = ref([])

function formatCalories(val) {
  if (val >= 10000) return (val / 10000).toFixed(1) + 'w'
  return val
}

const typeNameMap = {
  strength: '力量', hiit: 'HIIT', yoga: '瑜伽', cardio: '有氧'
}

onMounted(async () => {
  try {
    const [trainRes, courseRes] = await Promise.all([
      getTrainPlans(),
      getCourses()
    ])
    trainPlans.value = (trainRes.data.list || []).map(p => ({
      ...p, typeName: typeNameMap[p.type] || p.type
    }))
    courses.value = courseRes.data.list || []

    // 动态数据（可后续对接真实接口）
    const { mockPosts } = await import('@/mock/community')
    posts.value = mockPosts
  } catch (e) {
    console.error('首页数据加载失败', e)
  }
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  padding-bottom: calc(var(--tab-bar-height) + 20px);
  background: var(--color-bg);
}

/* Header */
.home-header {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  padding: 24px 16px 20px;
  color: #fff;
}
.home-header__greeting {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.home-header__avatar {
  width: 44px;
  height: 44px;
  background: rgba(255,255,255,0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.home-header__hi {
  font-size: 18px;
  font-weight: 600;
}
.home-header__slogan {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 2px;
}
.home-header__stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: rgba(255,255,255,0.15);
  border-radius: var(--radius-md);
  padding: 12px 0;
}
.stat-item {
  text-align: center;
  flex: 1;
}
.stat-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
}
.stat-label {
  font-size: 11px;
  opacity: 0.75;
}
.stat-divider {
  width: 1px;
  height: 28px;
  background: rgba(255,255,255,0.3);
}

/* Quick Actions */
.quick-actions {
  display: flex;
  justify-content: space-around;
  padding: 16px;
  background: var(--color-bg-white);
  margin: -8px 12px 12px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  position: relative;
  z-index: 1;
}
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
}
.action-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.action-icon--train { background: #FF6B35; }
.action-icon--course { background: #1989FA; }
.action-icon--diet { background: #07C160; }
.action-icon--live { background: #EE0A24; }

/* Section */
.section {
  margin-top: 16px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 10px;
}
.section-title {
  font-size: 17px;
  font-weight: 600;
}
.section-more {
  font-size: 13px;
  color: var(--color-text-light);
  cursor: pointer;
}

/* Train Cards */
.train-scroll {
  padding: 0 16px;
  display: flex;
  gap: 12px;
  overflow-x: auto;
}
.train-card {
  min-width: 200px;
  background: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: 16px;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  flex-shrink: 0;
}
.train-card__tag {
  display: inline-block;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: var(--radius-round);
  margin-bottom: 8px;
  color: #fff;
}
.tag--strength { background: #FF6B35; }
.tag--hiit { background: #EE0A24; }
.tag--yoga { background: #07C160; }
.tag--cardio { background: #1989FA; }
.train-card__name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}
.train-card__meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--color-text-light);
}
.train-card__level {
  margin-top: 10px;
  font-size: 12px;
  color: var(--color-primary);
}

/* Course Grid */
.course-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 0 16px;
}
.course-card {
  background: var(--color-bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
}
.course-card__cover {
  width: 100%;
  height: 110px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}
.course-card__body {
  padding: 10px;
}
.course-card__title {
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
  margin-bottom: 6px;
}
.course-card__meta {
  font-size: 11px;
  color: var(--color-text-light);
  display: flex;
  justify-content: space-between;
}

/* Post List */
.post-list {
  padding: 0 16px;
}
.post-card {
  margin: 0 0 10px;
  cursor: pointer;
}
.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.post-user { display: flex; flex-direction: column; }
.post-name { font-size: 14px; font-weight: 500; }
.post-time { font-size: 11px; color: var(--color-text-light); }
.post-content { font-size: 14px; line-height: 1.6; margin-bottom: 10px; color: var(--color-text-secondary); }
.post-actions {
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: var(--color-text-light);
}
</style>
