<template>
  <div class="mine-page page-container">
    <!-- 个人信息卡片 -->
    <div class="profile-card">
      <div class="profile-avatar">
        <van-icon name="user-circle-o" size="60" color="#ddd" />
      </div>
      <div class="profile-info">
        <div class="profile-name">{{ userStore.userInfo?.nickname || '未登录' }}</div>
        <div class="profile-bio">{{ userStore.userInfo?.bio || '自律给我自由' }}</div>
      </div>
      <van-icon name="arrow" color="#ccc" />
    </div>

    <!-- 成长等级 -->
    <div class="level-card card">
      <div class="level-header">
        <span>成长等级 Lv.{{ userStore.userInfo?.level || 1 }}</span>
        <span class="level-exp">{{ userStore.userInfo?.exp || 0 }}/1000</span>
      </div>
      <van-progress :percentage="23" stroke-color="#FF6B35" track-color="#f5f5f5" />
      <div class="achievements">
        <div v-for="ach in achievements" :key="ach.id" class="ach-item" :class="{ locked: !ach.unlocked }">
          <van-icon :name="ach.icon" size="24" :color="ach.unlocked ? '#FF6B35' : '#ddd'" />
          <span>{{ ach.name }}</span>
        </div>
      </div>
    </div>

    <!-- 训练数据 -->
    <div class="stats-row">
      <div class="stat-card card">
        <div class="stat-num">{{ userStore.stats.totalTrainDays }}</div>
        <div class="stat-label">训练天数</div>
      </div>
      <div class="stat-card card">
        <div class="stat-num">{{ userStore.stats.streakDays }}</div>
        <div class="stat-label">连续打卡</div>
      </div>
      <div class="stat-card card">
        <div class="stat-num">{{ formatMin(userStore.stats.totalTrainMinutes) }}</div>
        <div class="stat-label">累计时长</div>
      </div>
    </div>

    <!-- 功能入口 -->
    <div class="menu-section">
      <van-cell title="我的课程" icon="play-circle-o" is-link to="/mine/courses" />
      <van-cell title="训练历史" icon="clock-o" is-link to="/mine/history" />
      <van-cell title="身体数据" icon="chart-trending-o" is-link to="/mine/body-data" />
      <van-cell title="饮食记录" icon="balance-o" is-link to="/diet" />
    </div>
    <div class="menu-section">
      <van-cell title="设置" icon="setting-o" is-link to="/mine/settings" />
      <van-cell title="关于 UP" icon="info-o" is-link />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { mockAchievements } from '@/mock/user'

const userStore = useUserStore()
const achievements = ref(mockAchievements)

function formatMin(min) {
  if (min >= 60) return Math.floor(min / 60) + 'h'
  return min + 'min'
}
</script>

<style scoped>
.mine-page { padding-bottom: calc(var(--tab-bar-height) + 20px); }
.profile-card {
  display: flex; align-items: center; gap: 14px;
  padding: 24px 16px; background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff;
}
.profile-avatar { width: 64px; height: 64px; border-radius: 50%; background: rgba(255,255,255,0.3); display: flex; align-items: center; justify-content: center; }
.profile-info { flex: 1; }
.profile-name { font-size: 20px; font-weight: 600; }
.profile-bio { font-size: 13px; opacity: 0.8; margin-top: 4px; }
.level-card { margin: 12px 16px; }
.level-header { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 8px; color: var(--color-text-secondary); }
.level-exp { color: var(--color-text-light); }
.achievements { display: flex; gap: 12px; margin-top: 14px; overflow-x: auto; }
.ach-item { display: flex; flex-direction: column; align-items: center; gap: 4px; font-size: 11px; flex-shrink: 0; min-width: 52px; }
.ach-item.locked { opacity: 0.4; }
.stats-row { display: flex; gap: 8px; padding: 0 16px; margin: 12px 0; }
.stat-card { flex: 1; text-align: center; padding: 14px 8px; }
.stat-num { font-size: 20px; font-weight: 700; color: var(--color-primary); }
.stat-label { font-size: 11px; color: var(--color-text-light); margin-top: 4px; }
.menu-section { margin: 12px 16px 0; border-radius: var(--radius-md); overflow: hidden; }
</style>
