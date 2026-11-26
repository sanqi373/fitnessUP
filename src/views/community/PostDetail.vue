<template>
  <main class="detail-page">
    <article v-if="post" class="post-detail">
      <header>
        <strong>{{ post.user?.name || '健身达人' }}</strong>
        <span>{{ formatPostTime(post.createTime || post.time) }}</span>
      </header>
      <p>{{ post.content }}</p>
      <footer>
        <button type="button" aria-label="点赞" :aria-pressed="post.liked" @click="store.toggleLike(post)">
          <van-icon :name="post.liked ? 'good-job' : 'good-job-o'" /> {{ post.likes }}
        </button>
        <span><van-icon name="chat-o" /> {{ post.comments }} 条评论</span>
      </footer>
    </article>
    <van-loading v-else-if="store.status === 'idle' || store.status === 'loading'">加载动态中</van-loading>
    <van-empty v-else-if="store.status === 'error'" description="动态加载失败">
      <van-button size="small" round @click="store.loadPosts()">重新加载</van-button>
    </van-empty>
    <van-empty v-else description="这条动态已不存在">
      <router-link to="/community">查看社区动态</router-link>
    </van-empty>
  </main>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { formatPostTime, useCommunityStore } from '@/stores/community'

const store = useCommunityStore()
const route = useRoute()
const post = computed(() => store.posts.find(item => String(item.id) === route.params.id))


onMounted(() => {
  if (!post.value) store.loadPosts()
})
</script>

<style scoped>
.detail-page { min-height: 100vh; }
.post-detail {
  max-width: 76ch;
  margin: 24px auto;
  padding: clamp(20px, 4vw, 36px);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  background: linear-gradient(145deg, #effbf6, white);
  box-shadow: var(--shadow-sm);
}
.post-detail header { display: grid; gap: 3px; }
.post-detail header span, footer { color: var(--color-text-secondary); font-size: 13px; }
.post-detail p { margin: 24px 0; font-size: 18px; line-height: 1.75; white-space: pre-line; overflow-wrap: anywhere; }
footer { display: flex; align-items: center; gap: 24px; padding-top: 12px; border-top: 1px solid var(--color-border); }
footer button { border: 0; padding: 9px 0; background: transparent; color: inherit; cursor: pointer; }
footer button[aria-pressed="true"] { color: #11664e; }
@media (max-width: 760px) { .post-detail { margin: 16px; } }
</style>
