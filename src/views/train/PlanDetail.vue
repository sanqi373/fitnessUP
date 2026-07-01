<template>
  <div class="plan-detail" v-if="plan">
    <!-- 返回按钮 -->
    <van-nav-bar title="训练详情" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- 训练头部 -->
    <div class="plan-hero">
      <div class="plan-hero__tag" :class="`tag--${plan.type}`">{{ plan.typeName }}</div>
      <h2 class="plan-hero__name">{{ plan.name }}</h2>
      <p class="plan-hero__desc">{{ plan.desc }}</p>
      <div class="plan-hero__meta">
        <div class="meta-item"><van-icon name="clock-o" /><span>{{ plan.duration }}分钟</span></div>
        <div class="meta-item"><van-icon name="fire-o" /><span>{{ plan.calories }}千卡</span></div>
        <div class="meta-item"><van-icon name="label-o" /><span>{{ plan.level }}</span></div>
      </div>
    </div>

    <!-- 动作列表 -->
    <div class="exercise-section">
      <h3 class="section-title">动作列表（{{ plan.exercises?.length || 0 }}）</h3>
      <div
        v-for="(ex, idx) in plan.exercises"
        :key="idx"
        class="exercise-card card"
      >
        <div class="exercise-index">{{ idx + 1 }}</div>
        <div class="exercise-info">
          <h4>{{ ex.name }}</h4>
          <div class="exercise-params">
            <template v-if="ex.sets">
              <span>{{ ex.sets }}组</span>
              <span v-if="ex.reps > 1">{{ ex.reps }}次</span>
              <span v-else>{{ ex.duration }}秒</span>
            </template>
            <template v-else>
              <span>{{ ex.duration }}秒</span>
            </template>
            <span v-if="ex.rest">休息 {{ ex.rest }}秒</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 开始训练按钮 -->
    <div class="bottom-bar safe-area-bottom">
      <van-button
        round
        block
        type="primary"
        color="linear-gradient(135deg, #FF6B35, #FF8C5A)"
        size="large"
        @click="startTraining"
      >开始训练</van-button>
    </div>
  </div>

  <van-loading v-else class="loading" />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTrainPlanDetail } from '@/api/train'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()
const plan = ref(null)

const typeNameMap = { strength: '力量', hiit: 'HIIT', yoga: '瑜伽', cardio: '有氧' }

onMounted(async () => {
  const res = await getTrainPlanDetail(route.params.id)
  if (res.data) {
    plan.value = { ...res.data, typeName: typeNameMap[res.data.type] || res.data.type }
  }
})

function startTraining() {
  showToast('训练即将开始')
  // TODO: 跳转到训练进行页面
}
</script>

<style scoped>
.plan-detail { min-height: 100vh; background: var(--color-bg); padding-bottom: 80px; }
.plan-hero {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  padding: 24px 16px;
  color: #fff;
}
.plan-hero__tag {
  display: inline-block;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: var(--radius-round);
  margin-bottom: 12px;
  color: #fff;
  background: rgba(255,255,255,0.25);
}
.tag--strength { background: rgba(255,255,255,0.25); }
.plan-hero__name { font-size: 24px; font-weight: 700; margin-bottom: 8px; }
.plan-hero__desc { font-size: 14px; opacity: 0.85; line-height: 1.5; margin-bottom: 16px; }
.plan-hero__meta { display: flex; gap: 24px; }
.meta-item { display: flex; align-items: center; gap: 4px; font-size: 13px; }

.exercise-section { padding: 20px 16px; }
.section-title { font-size: 16px; margin-bottom: 12px; }
.exercise-card { display: flex; align-items: center; gap: 14px; margin: 0 0 10px; }
.exercise-index {
  width: 32px; height: 32px;
  background: var(--color-primary);
  color: #fff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 600; flex-shrink: 0;
}
.exercise-info h4 { font-size: 15px; margin-bottom: 4px; }
.exercise-params { display: flex; gap: 10px; font-size: 12px; color: var(--color-text-light); }
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; padding: 12px 16px; background: var(--color-bg-white); }
.loading { display: flex; justify-content: center; padding-top: 100px; }
</style>
