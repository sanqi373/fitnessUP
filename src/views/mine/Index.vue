<template>
  <div class="mine-page page-container">
    <!-- 个人信息卡片 -->
    <div class="profile-card" @click="handleProfileClick">
      <div class="profile-avatar">
        <img v-if="userStore.isLogin && userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" alt="头像" />
        <van-icon v-else name="user-o" size="32" />
      </div>
      <div class="profile-info">
        <div class="profile-name">{{ userStore.isLogin ? (userStore.userInfo?.nickname || '健身达人') : '未登录' }}</div>
        <div class="profile-bio">{{ userStore.isLogin ? (userStore.userInfo?.bio || '自律给我自由') : '点击登录，开启健身之旅' }}</div>
      </div>
      <van-icon name="arrow" color="#527d66" />
    </div>

    <!-- 成长等级 -->
    <div class="level-card card">
      <div class="level-header">
        <strong>成长等级<span v-if="growthReady"> Lv.{{ userStore.level }}</span></strong>
        <span v-if="growthReady" class="level-exp">{{ userStore.exp }} / {{ userStore.nextLevelExp }} 经验</span>
        <button v-else-if="growthStatus === 'error'" type="button" class="growth-retry" @click="loadGrowth">重新加载经验</button>
        <span v-else class="level-exp">正在读取经验…</span>
      </div>
      <van-progress
        v-if="growthReady"
        role="progressbar" aria-label="本级经验" :aria-valuenow="userStore.exp"
        :aria-valuemax="userStore.nextLevelExp" aria-valuemin="0"
        :percentage="progress" :show-pivot="false" :stroke-width="8"
        color="linear-gradient(90deg, #b9e9aa, #7ed6a8)" track-color="#e4f1e6"
      />
      <div class="achievements">
        <div v-for="ach in achievements" :key="ach.id" class="ach-item" :class="{ locked: !ach.unlocked }">
          <van-icon :name="ach.icon" size="24" :color="ach.unlocked ? '#24C789' : '#B9CCC4'" />
          <span>{{ ach.name }}</span>
        </div>
      </div>
    </div>

    <!-- 训练数据 -->
    <div class="stats-row">
      <div class="stat-card card">
        <button v-if="balanceFailed" type="button" class="growth-retry" @click="loadBalance">重新读取余额</button>
        <div v-else class="stat-num" aria-live="polite">{{ balance === null ? '—' : '¥' + balance }}</div>
        <div class="stat-label">钱包余额</div>
      </div>
      <div class="stat-card card">
        <div class="stat-num">{{ userStore.stats.streakDays }}</div>
        <div class="stat-label">连续打卡</div>
      </div>
      <div class="stat-card card">
        <div class="stat-num">{{ cumulativeTime }}</div>
        <div class="stat-label">累计时长</div>
      </div>
      <div class="stat-card card">
        <div class="stat-num">{{ userStore.stats.totalCalories || 0 }} <small>千卡</small></div>
        <div class="stat-label">累计消耗热量</div>
      </div>
    </div>

    <!-- 功能入口 -->
    <div class="menu-section">
      <van-cell title="训练计划" icon="bars" is-link to="/mine/training-plan" />
      <van-cell title="饮食计划" icon="orders-o" is-link to="/mine/diet-plan" />
      <van-cell title="身体数据" icon="chart-trending-o" is-link to="/mine/body-data" />
      <van-cell title="我的钱包" icon="balance-o" is-link to="/mine/wallet" />
      <van-cell title="商场" icon="shopping-cart-o" is-link to="/shop" />
    </div>
    <div class="menu-section">
      <van-cell title="设置" icon="setting-o" is-link to="/mine/settings" />
      <van-cell title="关于 UP" icon="info-o" is-link />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAchievements, getUserInfo } from '@/api/user'
import { getWallet } from '@/api/wallet'
import { durationParts } from '@/utils/workoutTime'

const router = useRouter()
const userStore = useUserStore()
const achievements = ref([])
const balance = ref(null)
const balanceFailed = ref(false)
async function loadBalance() {
  const session = userStore.getSessionToken()
  if (!session) return
  balanceFailed.value = false
  try {
    const { data } = await getWallet()
    if (session === userStore.getSessionToken()) balance.value = data.balance
  } catch {
    if (session === userStore.getSessionToken()) balanceFailed.value = true
  }
}
onMounted(loadBalance)
const growthStatus = ref('loading')
const growthReady = computed(() => !userStore.isLogin || growthStatus.value === 'ready')
const progress = computed(() => Math.min(100, Math.max(0, 100 * userStore.exp / userStore.nextLevelExp)))
async function loadGrowth() {
  const session = userStore.getSessionToken()
  if (!session) return
  growthStatus.value = 'loading'
  try {
    const { data } = await getUserInfo({ silentAuthFailure: true })
    if (userStore.getSessionToken() !== session) return
    userStore.setUserInfo({ ...(userStore.userInfo ?? data), level: data.level, exp: data.exp })
    growthStatus.value = 'ready'
  } catch {
    if (userStore.token === session) growthStatus.value = 'error'
  }
}
onMounted(loadGrowth)
onMounted(async () => {
  const session = userStore.getSessionToken()
  userStore.refreshStats()
  const achievementRes = await getAchievements()
  if (session !== userStore.getSessionToken()) return
  achievements.value = achievementRes.data || []
})

function handleProfileClick() {
  if (userStore.isLogin) {
    router.push('/mine/profile')
  } else {
    router.push('/login')
  }
}

const cumulativeTime = computed(() => durationParts(userStore.stats.totalTrainSeconds ?? (userStore.stats.totalTrainMinutes || 0) * 60)
  .map(part => part.value + part.unit).join(' '))
</script>

<style scoped>
.mine-page { padding-bottom: calc(var(--tab-bar-height) + 20px); }
.profile-card {
  display: flex; align-items: center; gap: 14px;
  padding: 24px 16px; background: linear-gradient(110deg, #eaf7d9, #d7f2dc 48%, #ecfaf3);
  color: #244d3a; cursor: pointer;
}
.profile-avatar { width: 64px; height: 64px; flex-shrink: 0; overflow: hidden; border-radius: 50%; background: linear-gradient(135deg, #f4ffe7, #ade6bd); color: #397253; display: grid; place-items: center; border: 2px solid #f8fff5; }
.profile-avatar img { width: 100%; height: 100%; object-fit: cover; }
.profile-info { flex: 1; }
.profile-name { font-size: 20px; font-weight: 600; }
.profile-bio { font-size: 13px; opacity: 0.8; margin-top: 4px; }
.level-card { margin: 12px 16px; background: linear-gradient(110deg, #f1fae8, #f5fcf7 65%, #ebf8ef); }
.level-header { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 8px; color: var(--color-text-secondary); }
.level-exp { color: #476b57; font-variant-numeric: tabular-nums; }
.growth-retry { border: 0; background: transparent; color: #27694d; text-decoration: underline; cursor: pointer; }
.achievements { display: flex; gap: 12px; margin-top: 14px; overflow-x: auto; }
.ach-item { display: flex; flex-direction: column; align-items: center; gap: 4px; font-size: 11px; flex-shrink: 0; min-width: 52px; }
.ach-item.locked { opacity: 0.4; }
.stats-row { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin: 16px; }
.stat-card { display: grid; align-content: center; justify-items: center; margin: 0; text-align: center; padding: 14px 8px; overflow-wrap: anywhere; }
@media (max-width: 600px) { .stats-row { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; } }
.stat-num small { font-size: 12px; }
.stat-num { font-size: 20px; font-weight: 700; color: var(--color-primary-dark); }
.stat-label { font-size: 11px; color: var(--color-text-light); margin-top: 4px; }
.menu-section { margin: 12px 16px 0; border-radius: var(--radius-md); overflow: hidden; }
</style>
