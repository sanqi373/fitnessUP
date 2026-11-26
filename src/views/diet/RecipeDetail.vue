<template>
  <main class="recipe-detail">
    <article v-if="food" class="recipe">
      <CardCover class="cover" :src="food.image" :fallback="recipeCover(food)" />
      <section class="recipe-info">
        <span class="meal">{{ mealTypeLabel(food.mealType) }}</span>
        <h1>{{ food.name }}</h1>
        <p v-if="food.description" class="description">{{ food.description }}</p>
        <h2>营养成分</h2>
        <dl>
          <div><dt>热量</dt><dd>{{ food.calories }} <small>千卡</small></dd></div>
          <div v-for="macro in nutrients.filter(m => food[m.key] != null)" :key="macro.key">
            <dt>{{ macro.label }}</dt><dd>{{ food[macro.key] }} <small>g</small></dd>
          </div>
        </dl>
        <PlanSaveButton kind="diet" :id="food.id" />
      </section>
    </article>
    <van-empty v-else-if="error" :description="error">
      <van-button plain round size="small" @click="load">重新加载</van-button>
    </van-empty>
    <div v-else class="loading" role="status"><van-loading>正在加载食谱</van-loading></div>
  </main>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getRecipe, mealTypeLabel, nutrients } from '@/api/diet'
import { recipeCover } from '@/utils/cardCovers'
import CardCover from '@/components/CardCover.vue'
import PlanSaveButton from '@/components/PlanSaveButton.vue'
import { useUserStore } from '@/stores/user'

const props = defineProps({ id: { type: String, required: true } })
const store = useUserStore()
const food = ref(null)
const error = ref('')
let revision = 0
async function load() {
  const current = ++revision
  const session = store.getSessionToken()
  if (!session) return
  food.value = null
  error.value = ''
  try {
    const { data } = await getRecipe(props.id)
    if (current === revision && session === store.getSessionToken()) food.value = data
  } catch (e) {
    if (current === revision) error.value = e.response?.status === 404 ? '食谱不存在' : '食谱加载失败'
  }
}
watch([() => props.id, () => store.token], load, { immediate: true })

</script>

<style scoped>
.recipe { display: grid; grid-template-columns: 1fr 1fr; align-items: center; max-width: 980px; margin: clamp(24px, 5vw, 56px) auto; padding-inline: 24px; gap: clamp(24px, 4vw, 44px); }
.cover { width: 100%; aspect-ratio: 1.16; object-fit: cover; border-radius: var(--radius-lg); }
.recipe-info { min-width: 0; }
.meal { color: #39664d; font-weight: 700; }
h1 { margin: 5px 0 12px; font-size: clamp(29px, 4vw, 42px); line-height: 1.2; }
.description { color: var(--color-text-secondary); }
h2 { margin-top: 22px; font-size: 17px; }
dl { display: flex; flex-wrap: wrap; gap: 14px 24px; margin: 10px 0 24px; padding-block: 14px; border-block: 1px solid #d0e3d9; }
dt { color: #526d60; font-size: 13px; }
dd { font-size: 23px; font-weight: 700; font-variant-numeric: tabular-nums; }
small { font-size: 12px; font-weight: 500; }
.loading { padding-top: 60px; text-align: center; }
@media (max-width: 650px) {
  .recipe { display: flex; flex-direction: column; align-items: stretch; padding: 0 16px 32px; gap: 18px; margin: 18px auto 0; }
  .cover { aspect-ratio: 1.8; border-radius: var(--radius-md); }
}
</style>
