<template>
  <div class="community-page page-container">
    <header class="feed-header">
      <div>
        <p class="eyebrow">TRAINING COMMUNITY</p>
        <h1>训练社区</h1>
        <p class="feed-subtitle">记录每一次坚持，和同频的人一起进步</p>
      </div>
      <span v-if="store.status === 'ready'" class="post-count">
        <strong>{{ store.posts.length }}</strong><span>条动态</span>
      </span>
    </header>
    <div class="feed-controls" role="group" aria-label="动态排序">
      <button
        v-for="option in sortOptions"
        :key="option"
        type="button"
        :aria-pressed="sort === option"
        @click="sort = option"
      >{{ option }}</button>
    </div>
    <div v-if="!store.posts.length && (store.status === 'idle' || store.status === 'loading')" class="feed-state">
      <van-loading>加载动态中</van-loading>
    </div>
    <van-empty v-else-if="store.status === 'error'" description="社区动态加载失败">
      <van-button size="small" round @click="store.loadPosts()">重新加载</van-button>
    </van-empty>
    <van-empty v-else-if="!store.posts.length" description="暂无训练动态" />
    <div v-else class="post-list">
      <article v-for="post in sortedPosts" :key="post.id" class="post-card">
        <header class="post-author">
          <img v-if="post.user?.avatar" class="avatar" :src="post.user.avatar" alt="" />
          <span v-else class="avatar" aria-hidden="true">{{ (post.user?.name || '健').slice(0, 1) }}</span>
          <div>
            <strong>{{ post.user?.name || '健身达人' }}</strong>
            <span class="post-time">{{ formatPostTime(post.createTime || post.time) }}</span>
          </div>
        </header>
        <router-link class="post-body" :to="{ name: 'PostDetail', params: { id: post.id } }">
          {{ post.content }}
        </router-link>
        <footer class="post-actions">
          <button type="button" aria-label="点赞" :aria-pressed="post.liked" @click="store.toggleLike(post)">
            <van-icon :name="post.liked ? 'good-job' : 'good-job-o'" /> {{ post.likes }}
          </button>
          <span aria-label="评论数"><van-icon name="chat-o" /> {{ post.comments }}</span>
          <button class="share" type="button" @click="share(post)">
            <van-icon name="share-o" /> 分享
          </button>
        </footer>
      </article>
    </div>
  </div>
</template>

<script>
export default { name: 'CommunityFeed' }
</script>

<script setup>
import { computed, onActivated, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { formatPostTime, useCommunityStore } from '@/stores/community'

const store = useCommunityStore()
const router = useRouter()
const sortOptions = ['最新发布', '最多点赞']
const sort = ref(sortOptions[0])
const sortedPosts = computed(() => sort.value === '最多点赞'
  ? [...store.posts].sort((a, b) => b.likes - a.likes)
  : store.posts)

async function share(post) {
  const { href } = router.resolve({ name: 'PostDetail', params: { id: post.id } })
  const url = new URL(href, window.location.origin).href
  try {
    if (navigator.share) {
      await navigator.share({ text: post.content, url })
    } else {
      await navigator.clipboard.writeText(url)
      showToast('动态链接已复制')
    }
  } catch (error) {
    if (error.name !== 'AbortError') showToast('分享失败，请稍后重试')
  }
}

onActivated(store.loadPosts)
</script>

<style scoped>
.community-page {
  max-width: 1680px;
  margin: 0 auto;
  padding: 28px 28px calc(var(--tab-bar-height) + 36px);
}
.feed-header, .post-author, .post-actions, .feed-controls {
  display: flex;
  align-items: center;
}
.feed-header { justify-content: space-between; max-width: 1680px; margin: 0 auto; padding: 28px 0 20px; }
.eyebrow { margin-bottom: 7px; color: var(--color-primary-dark); font-size: 10px; font-weight: 800; letter-spacing: .12em; }
.feed-header h1 { font-family: 'Barlow Condensed', sans-serif; font-size: 42px; line-height: .9; }
.feed-subtitle { margin-top: 8px; color: var(--color-text-secondary); font-size: 12px; }
.post-count { display: flex; flex-direction: column; align-items: flex-end; color: var(--color-text-secondary); font-size: 11px; }
.post-count strong {
  font: 600 40px/.8 'Barlow Condensed', sans-serif;
  color: var(--color-primary-dark);
}
.feed-controls { gap: 10px; max-width: 1680px; margin: 0 auto; padding: 4px 0 18px; }
.feed-controls button {
  padding: 9px 18px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  color: var(--color-text-secondary);
  background: white;
  font-weight: 600;
  cursor: pointer;
}
.feed-controls button[aria-pressed="true"] {
  background: var(--color-primary-soft);
  border-color: var(--color-primary);
  color: #11664e;
}
.post-list { display: grid; grid-template-columns: minmax(0, 1fr); gap: 12px; }
.post-card {
  --tint: #effbf6;
  --ink: #176d56;
  --edge: #d9eee7;
  display: grid;
  grid-template-columns: clamp(148px, 15vw, 210px) minmax(0, 1fr) auto;
  align-items: center;
  gap: clamp(16px, 2.5vw, 40px);
  min-width: 0;
  padding: 20px clamp(18px, 2vw, 28px);
  border: 1px solid var(--edge);
  border-radius: 20px;
  background: linear-gradient(145deg, var(--tint), #fff);
  box-shadow: var(--shadow-sm);
}
.post-card:nth-child(3n + 2) { --tint: #eff9fe; --ink: #326b88; --edge: #d7eaf5; }
.post-card:nth-child(3n) { --tint: #fff5f1; --ink: #97563e; --edge: #f5ded5; }
.post-author { gap: 12px; overflow-wrap: anywhere; }
.avatar {
  display: grid;
  place-items: center;
  flex: 0 0 42px;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: var(--edge);
  color: var(--ink);
  font-weight: 700;
  object-fit: cover;
}
.post-time { display: block; color: var(--color-text-secondary); font-size: 12px; }
.post-body {
  display: block;
  color: var(--color-text);
  max-width: 70ch;
  font-size: clamp(16px, 1.2vw, 19px);
  line-height: 1.65;
  white-space: pre-line;
  overflow-wrap: anywhere;
}
.post-body:hover { color: var(--ink); }
.post-actions {
  gap: 16px;
  padding-left: 16px;
  border-left: 1px solid var(--edge);
  color: var(--color-text-secondary);
  font-size: 13px;
}
.post-actions > * { display: inline-flex; align-items: center; gap: 6px; }
.post-actions button {
  min-height: 38px;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
}
.post-actions button:hover, .post-actions button[aria-pressed="true"] { color: #11664e; }
.post-actions .van-icon { font-size: 18px; }
.share { margin-left: auto; }
@media (max-width: 760px) {
  .community-page { padding: 0 16px calc(var(--tab-bar-height) + 36px); }
  .feed-header { padding: 24px 0 18px; }
  .feed-controls { padding-right: 0; padding-left: 0; }
  .post-card { grid-template-columns: minmax(0, 1fr); gap: 12px; padding: 16px; }
  .post-actions { border-left: 0; border-top: 1px solid var(--edge); padding: 6px 0 0; }
}
</style>
