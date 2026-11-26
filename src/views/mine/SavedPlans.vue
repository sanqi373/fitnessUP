<template>
  <main class="collection" :class="{ 'collection-diet': !training }">
    <header>
      <div>
        <h1>我的{{ title }}</h1>
        <p v-if="phase === 'ready'">已添加 {{ items.length }} {{ training ? '项训练' : '份食谱' }}</p>
      </div>
      <router-link class="browse" :to="catalog"><van-icon name="plus" /> {{ browse }}</router-link>
    </header>
    <div v-if="phase === 'loading'" class="state" role="status"><van-loading>正在读取计划</van-loading></div>
    <div v-else-if="phase === 'error'" class="state" role="alert">
      <p>计划读取失败</p><button class="browse" @click="load">重新加载</button>
    </div>
    <div v-else-if="!items.length" class="state">
      <van-icon :name="training ? 'records-o' : 'orders-o'" size="40" color="#477d68" />
      <h2>{{ training ? '还没有添加训练' : '还没有添加食谱' }}</h2>
      <p>打开{{ training ? '训练' : '食谱' }}详情，点击「添加至{{ title }}」。</p>
        <router-link class="browse" :to="catalog">{{ browse }}</router-link>
    </div>
    <ul v-else>
      <li v-for="item in items" :key="item.id">
        <router-link class="detail" :to="training ? `/train/plan/${item.id}` : `/diet/recipe/${item.id}`">
          <div class="thumb">
            <van-icon :name="training ? 'records-o' : 'orders-o'" aria-hidden="true" />
            <CardCover :src="training ? item.cover : item.image" :fallback="training ? trainingCover(item) : recipeCover(item)" />
          </div>
          <div class="item-text">
            <h2>{{ item.name }}</h2>
            <p>
              <span v-if="training">{{ item.duration }} 分钟</span>
              <span v-else>{{ mealTypeLabel(item.mealType) }}</span>
              <span v-if="item.calories != null">{{ item.calories }} 千卡</span>
            </p>
          </div>
        </router-link>
        <button type="button" class="remove" :aria-label="`将${item.name}移出${title}`" :disabled="removing.has(item.id)" @click="remove(item.id)">
          <van-icon name="delete-o" aria-hidden="true" />{{ removing.has(item.id) ? '正在移出' : '移出计划' }}
        </button>
      </li>
    </ul>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { listSavedPlans, removeFromPlan } from '@/api/plans'
import { mealTypeLabel } from '@/api/diet'
import { useUserStore } from '@/stores/user'
import CardCover from '@/components/CardCover.vue'
import { recipeCover, trainingCover } from '@/utils/cardCovers'

const props = defineProps({ kind: { type: String, required: true } })
const route = useRoute()
const router = useRouter()
const store = useUserStore()
const training = computed(() => props.kind === 'training')
const title = computed(() => training.value ? '训练计划' : '饮食计划')
const browse = computed(() => training.value ? '挑选训练' : '挑选食谱')
const catalog = computed(() => training.value ? '/train' : '/diet')
const items = ref([])
const phase = ref('loading')
const removing = ref(new Set())
let revision = 0

async function load() {
  const current = ++revision
  removing.value = new Set()
  items.value = []
  phase.value = 'loading'
  const session = store.getSessionToken()
  if (!session) return router.replace({ path: '/login', query: { redirect: route.path } })
  try {
    const { data } = await listSavedPlans(props.kind)
    if (current !== revision || session !== store.getSessionToken()) return
    items.value = data
    phase.value = 'ready'
  } catch {
    if (current === revision) phase.value = 'error'
  }
}
watch(() => [props.kind, store.token], load, { immediate: true })
onBeforeUnmount(() => { revision++ })

async function remove(id) {
  if (removing.value.has(id)) return
  const session = store.getSessionToken()
  if (!session) return router.replace({ path: '/login', query: { redirect: route.path } })
  const current = revision
  removing.value = new Set(removing.value).add(id)
  try {
    await removeFromPlan(props.kind, id)
    if (current !== revision || session !== store.getSessionToken()) return
    items.value = items.value.filter(item => item.id !== id)
    showToast('已移出' + title.value)
  } catch {
    // The request interceptor displays the error. Keep the item available for retry.
  } finally {
    if (current === revision) {
      const next = new Set(removing.value)
      next.delete(id)
      removing.value = next
    }
  }
}
</script>

<style scoped>
.collection {
  --gutter: clamp(16px, 3.5vw, 64px);
  --banner: url('/training/dumbbell.webp');
  --tint: #193e32;
  display: flex; flex-direction: column;
  min-height: calc(100vh - var(--header-height) - 1px);
  min-height: calc(100dvh - var(--header-height) - 1px);
}
.collection-diet { --banner: url('/recipes/salmon-rice.webp'); --tint: #473a29; }
header {
  display: flex; flex-wrap: wrap; align-items: flex-end; justify-content: space-between;
  gap: 16px; padding: clamp(30px, 5vw, 72px) var(--gutter); color: white;
  background-color: var(--tint);
  background-image: linear-gradient(90deg, var(--tint) 26%, transparent 90%), var(--banner);
  background-position: center, right 48%; background-size: cover, max(75%, 420px) auto; background-repeat: no-repeat;
}
h1 { font-size: clamp(27px, 4vw, 46px); line-height: 1.2; }
header p { margin-top: 8px; }
.browse { display: inline-flex; align-items: center; justify-content: center; gap: 5px; min-height: 40px; padding: 7px 14px; background: #e2f2e9; color: #205b42; font-weight: 700; border: 1px solid #c4dfcf; border-radius: 12px; cursor: pointer; }
ul {
  display: grid; grid-template-columns: repeat(auto-fit, minmax(min(100%, 290px), 1fr));
  gap: clamp(14px, 2vw, 28px); list-style: none;
  padding: clamp(20px, 3vw, 44px) var(--gutter) 40px;
}
li { min-width: 0; display: flex; flex-direction: column; background: white; border: 1px solid var(--color-border); border-radius: 16px; }
.detail { display: flex; flex: 1; flex-direction: column; border-radius: inherit; }
.detail:hover h2 { text-decoration: underline; }
.thumb { position: relative; display: grid; place-items: center; color: #648074; font-size: 32px; height: clamp(150px, 16vw, 260px); background: var(--color-bg-deep); border-radius: 15px 15px 0 0; overflow: hidden; }
.thumb img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
.item-text { padding: 16px 18px 0; min-width: 0; }
h2 { font-size: clamp(18px, 2vw, 24px); overflow-wrap: anywhere; }
.item-text p { display: flex; flex-wrap: wrap; gap: 4px 12px; margin-top: 4px; color: var(--color-text-secondary); }
.remove { display: inline-flex; align-self: flex-end; align-items: center; gap: 6px; min-height: 44px; margin: 0 8px 4px; padding: 0 10px; border: 0; background: transparent; color: #8b4941; cursor: pointer; }
.remove:hover { text-decoration: underline; }
.remove:disabled { opacity: .65; cursor: wait; }
.state { flex: 1; display: grid; align-content: center; justify-items: center; gap: 10px; padding: 48px var(--gutter); text-align: center; }
.state p { color: var(--color-text-secondary); }
@media (min-width: 720px) {
  li:only-child { display: grid; grid-template-columns: minmax(0, 1fr) auto; }
  li:only-child .detail { flex-direction: row; align-items: center; }
  li:only-child .thumb { width: 45%; flex-shrink: 0; border-radius: 15px 0 0 15px; }
  li:only-child .item-text { padding: 20px; }
}
</style>
