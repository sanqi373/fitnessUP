<template>
  <div class="home-page">
    <header class="home-top">
      <div class="home-intro">
        <p class="eyebrow">UP FITNESS</p>
        <h1>{{ userStore.isLogin ? (userStore.userInfo?.nickname || '训练者') : greeting }}</h1>
        <p class="home-subtitle">今天也给自己一点向前的力量</p>
      </div>
      <time class="home-clock" :datetime="now.toISOString()" :aria-label="todayLabel + ' ' + timeLabel">
        <span class="clock-face" aria-hidden="true">
          <span class="clock-digits">{{ timeLabel.slice(0, 5) }}</span>
          <span class="clock-seconds" :style="{ '--sweep': now.getSeconds() * 6 + 'deg' }">{{ timeLabel.slice(-2) }}</span>
        </span>
        <span class="clock-date">{{ todayLabel }}</span>
      </time>
      <button class="avatar-button" aria-label="打开个人资料" @click="handleAvatarClick"><van-icon name="user-o" size="20" /></button>
    </header>

    <section class="hero-panel">
      <div class="hero-art" aria-hidden="true">
        <div class="track-lanes"></div>
        <img v-for="(slide, index) in heroSlides" :key="slide.src" :src="slide.src" alt=""
          class="hero-athlete" :class="{ 'is-current': index === activeSlide }"
          :style="{ objectPosition: slide.position }" />
      </div>
      <div class="slide-controls" role="group" aria-label="训练图片轮播">
        <button v-for="(slide, index) in heroSlides" :key="slide.src" type="button" class="slide-dot"
          :aria-label="'查看' + slide.name + '图片'" :aria-pressed="activeSlide === index"
          @click="selectSlide(index)"></button>
        <button type="button" class="slide-play" :aria-label="playing ? '暂停轮播' : '播放轮播'"
          @click="playing = !playing"><span aria-hidden="true">{{ playing ? 'Ⅱ' : '▶' }}</span></button>
      </div>
      <div class="hero-content"><span class="hero-label"><i></i> 今日训练准备就绪</span><h2>把今天<br /><strong>练出来</strong></h2><p>一组高质量训练，足以让平凡的一天向前一步。</p><button class="hero-cta" @click="$router.push('/train')">开始训练 <van-icon name="arrow" /></button></div>
      <div class="hero-score">
        <div class="score-ring" :style="{ '--progress': Math.min(streakDays, 7) / 7 + 'turn' }">
          <div class="score-face"><strong>{{ streakDays }}</strong><span>连续打卡天数</span></div>
        </div>
        <small>连续打卡目标 <b>7 天</b></small>
        <button type="button" class="check-in" :disabled="checkingIn || userStore.stats.checkedInToday" @click="checkIn">
          <van-icon :name="userStore.stats.checkedInToday ? 'success' : 'calendar-o'" aria-hidden="true" />
          {{ userStore.stats.checkedInToday ? '今日已打卡' : checkingIn ? '打卡中…' : '今日打卡' }}
        </button>
      </div>
    </section>

    <dl class="metric-strip" aria-label="训练统计">
      <div><dt>今日训练时长</dt><dd class="training-duration"><span v-for="part in durationParts(userStore.stats.todayTrainSeconds || 0)" :key="part.unit">{{ part.value }} <small>{{ part.unit }}</small></span></dd></div>
      <div><dt>今日消耗热量</dt><dd>{{ metricFormat.format(userStore.stats.todayCalories || 0) }} <small>千卡</small></dd></div>
      <div><dt>训练天数</dt><dd>{{ metricFormat.format(userStore.stats.totalTrainDays || 0) }} <small>天</small></dd></div>
    </dl>

    <section class="content-section">
      <div class="section-heading">
        <div><p class="eyebrow">NEXT SESSION</p><h3>下一组，练什么</h3></div>
        <router-link to="/train">全部计划 <van-icon name="arrow" /></router-link>
      </div>
      <div class="recommendation-rail">
        <router-link v-for="plan in trainPlans.slice(0, 3)" :key="plan.id" :to="`/train/plan/${plan.id}`" class="recommendation-card">
          <CardCover class="recommendation-cover" :src="plan.cover || plan.image" :fallback="trainingCover(plan)" />
          <div class="recommendation-body">
            <div class="recommendation-card__top"><span>{{ plan.typeName }}</span></div>
            <h4>{{ plan.name }}</h4>
            <p>{{ plan.desc }}</p>
            <footer>
              <span><van-icon name="clock-o" /> {{ plan.duration }} 分钟</span>
              <span><van-icon name="fire-o" /> {{ plan.calories }} 千卡</span>
            </footer>
          </div>
        </router-link>
      </div>
    </section>

    <section class="content-section" aria-labelledby="meal-preview-title">
      <div class="section-heading">
        <div>
          <p class="eyebrow">推荐食谱</p>
          <h3 id="meal-preview-title">下一餐，吃什么</h3>
        </div>
        <router-link to="/diet">更多饮食 <van-icon name="arrow" aria-hidden="true" /></router-link>
      </div>
      <div v-if="mealPreview.length" class="recommendation-rail">
        <router-link v-for="food in mealPreview" :key="food.id" :to="`/diet/recipe/${food.id}`" class="recommendation-card meal-card">
          <CardCover class="recommendation-cover" :src="food.image" :fallback="recipeCover(food)" />
          <div class="recommendation-body">
            <div class="recommendation-card__top">
              <span>{{ mealTypeLabel(food.type || food.mealType) }}</span>
              <span v-if="food.calories != null" class="meal-energy"><b>{{ food.calories }}</b> 千卡</span>
            </div>
            <h4>{{ food.name }}</h4>
            <footer>
              <span v-if="food.protein != null">蛋白质 {{ food.protein }} g</span>
              <span v-if="food.carbs != null">碳水 {{ food.carbs }} g</span>
              <span v-if="food.fat != null">脂肪 {{ food.fat }} g</span>
            </footer>
          </div>
        </router-link>
      </div>
      <p v-else class="meal-status" role="status">{{ mealStatus }}</p>
    </section>

    <section class="content-section community-section" aria-labelledby="field-notes-title">
      <div class="section-heading">
        <div><p class="eyebrow">FIELD NOTES</p><h3 id="field-notes-title">训练现场</h3></div>
        <router-link to="/community">去动态 <van-icon name="arrow" aria-hidden="true" /></router-link>
      </div>
      <div v-if="posts.length" class="post-grid">
        <router-link v-for="post in posts.slice(0, 2)" :key="post.id"
          :to="{ name: 'PostDetail', params: { id: post.id } }" class="post-preview">
          <div class="preview-author">
            <img v-if="post.user?.avatar" class="preview-avatar" :src="post.user.avatar" alt="" />
            <span v-else class="preview-avatar" aria-hidden="true">{{ (post.user?.name || '健').slice(0, 1) }}</span>
            <div>
              <strong>{{ post.user?.name || '健身达人' }}</strong>
              <span class="preview-time">{{ formatPostTime(post.createTime || post.time) }}</span>
            </div>
          </div>
          <p class="preview-excerpt">{{ post.content }}</p>
          <div class="preview-footer">
            <span><van-icon name="good-job-o" /> {{ post.likes }} 赞</span>
            <span><van-icon name="chat-o" /> {{ post.comments }} 评论</span>
            <van-icon class="preview-arrow" name="arrow" aria-hidden="true" />
          </div>
        </router-link>
      </div>
      <p v-else class="meal-status" role="status">{{ postStatus }}</p>
    </section>
  </div>
</template>

<script>
export default { name: 'FitnessHome' }
</script>

<script setup>
import { ref, computed, onActivated, onDeactivated, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTrainPlans } from '@/api/train'
import { durationParts } from '@/utils/workoutTime'
import { getDietRecommend, mealTypeLabel } from '@/api/diet'
import { formatPostTime, useCommunityStore } from '@/stores/community'
import CardCover from '@/components/CardCover.vue'
import { recipeCover, trainingCover } from '@/utils/cardCovers'
const router = useRouter()
const userStore = useUserStore()
const trainPlans = ref([])
const mealPreview = ref([])
const mealStatus = ref('正在加载推荐食谱…')
const community = useCommunityStore()
const posts = computed(() => community.posts)
const postStatus = computed(() => ({
  ready: '暂无训练动态',
  error: '训练动态加载失败'
}[community.status] || '正在加载训练动态…'))
const now = ref(new Date())
const heroSlides = [
  { src: '/training/distance-run.webp', name: '户外跑步', position: '52% 42%' },
  { src: '/training/dumbbell.webp', name: '哑铃训练', position: '55% 35%' },
  { src: '/training/hip-mobility.webp', name: '髋部拉伸', position: '50% 50%' },
  { src: '/training/single-leg.webp', name: '弓步训练', position: '43% 54%' }
]
const activeSlide = ref(0)
const motionPreference = window.matchMedia('(prefers-reduced-motion: reduce)')
const playing = ref(!motionPreference.matches)
const pageVisible = ref(false)
function syncMotionPreference() { playing.value = !motionPreference.matches }
function syncPageVisibility() {
  pageVisible.value = !document.hidden
  if (pageVisible.value) userStore.refreshStats()
}
const checkingIn = ref(false)
async function checkIn() {
  if (checkingIn.value || userStore.stats.checkedInToday) return
  checkingIn.value = true
  try {
    await userStore.checkIn()
  } catch {
    // The request interceptor reports failures; the same day can be retried.
  } finally {
    checkingIn.value = false
  }
}
function selectSlide(index) {
  activeSlide.value = index
  playing.value = false
}
watch([playing, pageVisible], ([autoplay, visible], previous, onCleanup) => {
  if (!autoplay || !visible) return
  const timer = window.setInterval(() => {
    activeSlide.value = (activeSlide.value + 1) % heroSlides.length
  }, 4500)
  onCleanup(() => window.clearInterval(timer))
}, { immediate: true })
onActivated(() => {
  syncPageVisibility()
  updateClock()
  community.loadPosts()
  loadRecommendations()
  motionPreference.addEventListener('change', syncMotionPreference)
  document.addEventListener('visibilitychange', syncPageVisibility)
})
onDeactivated(() => {
  pageVisible.value = false
  window.clearTimeout(clockTimer)
  motionPreference.removeEventListener('change', syncMotionPreference)
  document.removeEventListener('visibilitychange', syncPageVisibility)
})
const dateFormat = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' })
const timeFormat = new Intl.DateTimeFormat('en-GB', { hour: '2-digit', minute: '2-digit', second: '2-digit', hourCycle: 'h23' })
const todayLabel = computed(() => dateFormat.format(now.value))
const timeLabel = computed(() => timeFormat.format(now.value))
const greeting = computed(() => {
  const hour = now.value.getHours()
  return hour < 12 ? '早上好' : hour < 18 ? '下午好' : '晚上好'
})
let clockTimer
function updateClock() {
  now.value = new Date()
  clockTimer = window.setTimeout(updateClock, 1000 - now.value.getMilliseconds())
}
const typeNameMap = { strength: '力量', hiit: 'HIIT', yoga: '瑜伽', cardio: '有氧' }
const metricFormat = new Intl.NumberFormat('zh-CN')
const streakDays = computed(() => Math.max(0, userStore.stats.streakDays || 0))
function handleAvatarClick() { router.push(userStore.isLogin ? '/mine/profile' : '/login') }
let catalogRevision = 0
async function loadRecommendations() {
  const current = ++catalogRevision
  if (!mealPreview.value.length) mealStatus.value = '正在加载推荐食谱…'
  const [foods, plans] = await Promise.allSettled([getDietRecommend(), getTrainPlans()])
  if (current !== catalogRevision) return
  if (foods.status === 'fulfilled') {
    const { data } = foods.value
    mealPreview.value = (data ?? []).slice(0, 3)
    mealStatus.value = '暂无推荐食谱'
  } else {
    mealStatus.value = '推荐食谱加载失败'
  }
  if (plans.status === 'fulfilled') {
    trainPlans.value = plans.value.data.list.map(plan => ({ ...plan, typeName: typeNameMap[plan.type] || plan.type }))
  }
}
</script>

<style scoped>
.home-page { width: 100%; max-width: 1680px; min-height: 100vh; margin: 0 auto; padding: 28px 32px 128px; }
.home-top {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  grid-template-areas: 'intro clock profile';
  align-items: center;
  gap: 16px;
  margin-bottom: 22px;
}
.home-intro { grid-area: intro; min-width: 0; overflow-wrap: anywhere; }
.eyebrow { margin-bottom: 7px; color: var(--color-primary-dark); font-size: 10px; font-weight: 800; letter-spacing: .12em; }
.home-top h1 { font-family: 'Barlow Condensed', sans-serif; font-size: 40px; line-height: .95; letter-spacing: -.02em; }
.home-subtitle { margin-top: 8px; color: var(--color-text-secondary); font-size: 12px; }
.avatar-button { grid-area: profile; place-self: start end; width: 44px; height: 44px; border: 1px solid var(--color-border); border-radius: 15px; color: var(--color-primary-dark); background: #fff; box-shadow: var(--shadow-sm); cursor: pointer; }
.home-clock { grid-area: clock; justify-self: center; display: grid; justify-items: center; gap: 6px; color: #185e51; font-variant-numeric: tabular-nums; }
.clock-face { display: flex; align-items: center; gap: 10px; }
.clock-digits { font: 700 clamp(46px, 4vw, 64px)/.92 'Barlow Condensed', 'Arial Narrow', sans-serif; font-variant-numeric: tabular-nums; letter-spacing: -.035em; transform: skewX(-6deg); }
.clock-seconds {
  position: relative;
  display: grid;
  place-items: center;
  width: 36px;
  aspect-ratio: 1;
  font: 700 18px/1 'Barlow Condensed', sans-serif;
  font-variant-numeric: tabular-nums;
  color: #176851;
}
.clock-seconds::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: conic-gradient(#249875 var(--sweep), #cee8dd 0);
  mask-image: radial-gradient(circle closest-side, transparent calc(100% - 3px), #000 calc(100% - 2px));
  -webkit-mask-image: radial-gradient(circle closest-side, transparent calc(100% - 3px), #000 calc(100% - 2px));
}
.clock-date { display: flex; align-items: center; gap: 8px; font-size: 12px; color: var(--color-text-secondary); }
.clock-date::before, .clock-date::after { content: ''; width: 16px; height: 1px; background: #abd3c2; }
@media (max-width: 620px) {
  .home-top { grid-template-columns: minmax(0, 1fr) auto; grid-template-areas: 'intro profile' 'clock clock'; row-gap: 12px; }
}
.hero-panel {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  min-height: clamp(284px, 23vw, 344px);
  padding: clamp(22px, 3vw, 44px);
  border-radius: var(--radius-lg);
  background: linear-gradient(108deg, #e6faf0 8%, #dcf4e6 42%, #a7d9c0 77%, #bbdfcf);
  box-shadow: var(--shadow-md);
}
.hero-art { position: absolute; inset: 0; z-index: -1; overflow: hidden; pointer-events: none; background: inherit; }
.hero-art::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, #e6faf0 4%, transparent 31%);
}
.track-lanes {
  position: absolute;
  width: 92%;
  height: 175%;
  left: 23%;
  top: -62%;
  transform: rotate(-14deg);
  border-radius: 48%;
  border: 2px solid #f5fff5;
  box-shadow: 0 0 0 12px #74ae93, 0 0 0 14px #e2f5df, 0 0 0 26px #69a68e,
    0 0 0 28px #e2f5df, 0 0 0 40px #609d86, 0 0 0 42px #e2f5df, 0 0 0 54px #51947e;
}
.hero-athlete {
  position: absolute;
  left: 55%;
  top: 0;
  width: 54%;
  max-width: none;
  height: 100%;
  object-fit: cover;
  transform: translateX(-50%);
  opacity: 0;
  transition: opacity .8s ease-in-out;
  filter: saturate(.87);
  mask-image: linear-gradient(90deg, transparent, black 25%, black 74%, transparent);
}
.hero-athlete.is-current { opacity: 1; }
.slide-controls {
  position: absolute;
  left: 55%;
  bottom: 12px;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  padding: 0 5px;
  border-radius: 20px;
  background: #143d32de;
}
.slide-controls button { display: grid; place-items: center; width: 24px; height: 28px; border: 0; background: transparent; color: #fff; cursor: pointer; }
.slide-dot::after { content: ''; width: 6px; height: 6px; border-radius: 6px; background: #b3c9bf; }
.slide-dot[aria-pressed="true"]::after { width: 18px; background: #dcf5aa; }
.slide-play { font-size: 13px; }
.hero-content { max-width: 380px; }
.hero-label { display: flex; align-items: center; gap: 7px; color: #17694f; font-size: 11px; font-weight: 750; }
.hero-label i { width: 7px; height: 7px; border-radius: 50%; background: #249778; }
.hero-content h2 { margin: 14px 0 8px; color: #193b32; font-size: clamp(38px, 3.7vw, 58px); line-height: 1; letter-spacing: -.045em; }
.hero-content h2 strong { color: #187e61; }
.hero-content p { color: #405e51; font-size: 12px; line-height: 1.65; }
.hero-cta { display: inline-flex; align-items: center; gap: 13px; margin-top: 18px; padding: 11px 16px; border: 0; border-radius: 12px; color: #fff; background: #176c54; font-weight: 750; cursor: pointer; }
.hero-score { display: grid; justify-items: center; gap: 10px; }
.check-in { display: inline-flex; align-items: center; justify-content: center; gap: 6px; padding: 7px 14px; min-height: 38px; white-space: nowrap; border-radius: 12px; border: 1px solid #216349; background: #216349; color: white; font-size: 13px; font-weight: 700; cursor: pointer; }
.check-in:disabled { background: #e2f1e5; border-color: #b5d3bf; color: #315b44; cursor: default; }
.score-ring {
  width: clamp(116px, 11vw, 164px);
  aspect-ratio: 1;
  border-radius: 50%;
  position: relative;
  display: grid;
  place-items: center;
  background: radial-gradient(circle at 37% 26%, #325f52, #163d32 75%);
  box-shadow: 0 7px 18px #234f412b, inset 0 0 0 2px #315d4d;
}
.score-ring::before, .score-ring::after { content: ''; position: absolute; border-radius: 50%; }
.score-ring::before {
  inset: 4%;
  background: repeating-conic-gradient(#c4dece 0deg 1deg, transparent 1deg 6deg);
  mask-image: radial-gradient(closest-side, transparent 95%, black 96%);
}
.score-ring::after {
  inset: 10%;
  background: conic-gradient(#a8ecc5, #e5f5ae var(--progress), #52786a var(--progress));
  mask-image: repeating-conic-gradient(transparent 0deg 1.5deg, black 1.5deg calc(1turn / 7 - 1.5deg), transparent calc(1turn / 7 - 1.5deg) calc(1turn / 7));
}
.score-face { z-index: 1; display: grid; place-content: center; justify-items: center; gap: 5px; width: 70%; height: 70%; border-radius: 50%; background: #20483b; color: #fff; }
.score-face strong { font: 700 clamp(32px, 3.2vw, 52px)/1 'Barlow Condensed', sans-serif; font-variant-numeric: tabular-nums; }
.score-face span { color: #d2e8dc; font-size: 10px; }
.hero-score small { color: #284e40; font-size: 12px; }
.metric-strip { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); margin: 14px 0 8px; padding: 14px 0; border-block: 1px solid var(--color-border); }
.metric-strip > div { display: grid; align-content: center; justify-items: center; gap: 6px; min-height: 62px; padding: 0 4px; }
.metric-strip > div + div { border-left: 1px solid var(--color-border); }
.metric-strip dt { color: #506c61; font-size: 13px; line-height: 1.4; }
.metric-strip dd { text-align: center; font: 700 clamp(22px, 2.3vw, 34px)/1.1 'Barlow Condensed', sans-serif; font-variant-numeric: tabular-nums; overflow-wrap: anywhere; }
.metric-strip dd small { color: #516b60; font: 500 12px/1.2 'DM Sans', sans-serif; white-space: nowrap; }
@media (max-width: 700px) {
  .hero-panel { min-height: 266px; padding: 20px; }
  .hero-content { max-width: 170px; }
  .hero-athlete { left: 58%; width: 56%; }
  .score-ring { width: 96px; }
}
@media (max-width: 480px) {
  .hero-panel { min-height: 286px; padding: 18px; }
  .hero-content { max-width: 52%; grid-column: 1 / -1; grid-row: 1; }
  .hero-content p { max-width: 13em; }
  .hero-art::after { background: linear-gradient(90deg, #e6faf0 8%, #e6faf0db 34%, transparent 56%); }
  .hero-athlete { left: 76%; width: 68%; height: 78%; mask-image: linear-gradient(90deg, transparent, black 33%), linear-gradient(0deg, transparent, black 24%); mask-composite: intersect; }
  .slide-controls { left: auto; right: 10px; top: 9px; bottom: auto; transform: none; }
  .hero-score { grid-column: 2; grid-row: 1; align-self: end; gap: 3px; }
  .score-ring { width: 84px; }
  .score-face { gap: 0; }
  .score-face strong { font-size: 25px; }
  .score-face span { font-size: 8px; }
  .hero-score small { font-size: 10px; }
  .metric-strip dd small { font-size: 11px; }
}
.content-section { margin-top: 28px; }
.training-duration { display: flex; flex-wrap: wrap; justify-content: center; gap: 0 5px; }
.training-duration span { white-space: nowrap; }
.section-heading { display: flex; align-items: end; justify-content: space-between; margin-bottom: 13px; }
.section-heading h3 { font-family: 'Barlow Condensed'; font-size: 27px; line-height: .9; }
.section-heading :is(button, a) { display: flex; align-items: center; gap: 4px; border: 0; color: var(--color-text-secondary); background: transparent; font-size: 11px; cursor: pointer; }
.recommendation-rail { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.recommendation-card { display: flex; flex-direction: column; min-width: 0; overflow: hidden; border: 1px solid #d9f0e8; border-radius: 17px; background: linear-gradient(145deg, #effcf6, #fff); box-shadow: var(--shadow-sm); }
.recommendation-cover { width: 100%; aspect-ratio: 2.25; object-fit: cover; }
.recommendation-body { display: flex; flex: 1; flex-direction: column; padding: 16px 18px; }
.recommendation-card:nth-child(3n + 2) { border-color: #d5eaf5; background: linear-gradient(145deg, #eef9ff, #fff); }
.recommendation-card:nth-child(3n) { border-color: #f7dfd6; background: linear-gradient(145deg, #fff5f1, #fff); }
.recommendation-card__top { display: flex; justify-content: space-between; color: var(--color-primary-dark); font-size: 11px; font-weight: 800; }
.recommendation-card:nth-child(3n + 2) .recommendation-card__top { color: #4c9bc3; }
.recommendation-card:nth-child(3n) .recommendation-card__top { color: #e77960; }
.recommendation-card__top b { font: 700 24px/1 'Barlow Condensed', sans-serif; }
.recommendation-card h4 { margin: 7px 0 5px; font-family: 'Barlow Condensed', sans-serif; font-size: 24px; line-height: 1.15; }
.recommendation-card p { color: var(--color-text-secondary); font-size: 12px; line-height: 1.5; }
.recommendation-card footer { display: flex; flex-wrap: wrap; gap: 5px 14px; margin-top: auto; padding-top: 12px; color: var(--color-text-secondary); font-size: 11px; }
.recommendation-card footer span { display: flex; align-items: center; gap: 4px; }
.meal-card .meal-energy { display: inline-flex; align-items: baseline; gap: 4px; color: var(--color-text-secondary); }
.meal-card .meal-energy b { color: inherit; font-variant-numeric: tabular-nums; }
.meal-card footer { flex-wrap: wrap; gap: 6px 14px; margin-top: auto; padding-top: 14px; color: var(--color-text-secondary); }
.meal-status { padding: 12px 0; color: var(--color-text-secondary); font-size: 13px; }
.post-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.post-preview {
  --tint: #effaf4;
  --edge: #d4e8dd;
  --ink: #27674d;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 190px;
  padding: clamp(18px, 2vw, 28px);
  border: 1px solid var(--edge);
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--tint), #fff 85%);
  box-shadow: var(--shadow-sm);
}
.post-preview:nth-child(even) { --tint: #edf7fc; --edge: #d5e6ef; --ink: #336982; }
.post-preview:is(:hover, :focus-visible) { border-color: var(--ink); }
.preview-author { display: flex; align-items: center; gap: 12px; overflow-wrap: anywhere; }
.preview-author strong { font-size: 16px; }
.preview-avatar { display: grid; flex: none; place-items: center; width: 46px; height: 46px; border-radius: 50%; object-fit: cover; color: var(--ink); background: var(--edge); font-size: 20px; font-weight: 750; }
.preview-time { display: block; color: var(--color-text-secondary); font-size: 12px; }
.preview-excerpt { display: -webkit-box; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 3; overflow-wrap: anywhere; font-size: clamp(16px, 1.25vw, 20px); line-height: 1.55; }
.preview-footer { display: flex; align-items: center; gap: 18px; margin-top: auto; padding-top: 12px; border-top: 1px solid var(--edge); color: var(--color-text-secondary); font-size: 13px; }
.preview-footer span { display: inline-flex; align-items: center; gap: 5px; }
.preview-arrow { display: grid; place-items: center; margin-left: auto; width: 28px; height: 28px; border-radius: 50%; color: var(--ink); background: var(--tint); }
@media (max-width: 620px) { .post-grid { grid-template-columns: minmax(0, 1fr); gap: 12px; } .post-preview { min-height: 176px; } }
@media (max-width: 960px) { .home-page { max-width: 100%; padding: 24px 22px 128px; } }
@media (max-width: 620px) { .home-page { padding: 24px 16px 118px; } .recommendation-rail { display: flex; gap: 12px; overflow-x: auto; margin: 0 -16px; padding: 0 16px 7px; scrollbar-width: none; } .recommendation-rail::-webkit-scrollbar { display: none; } .recommendation-card { flex: 0 0 min(78vw, 320px); } }
</style>
