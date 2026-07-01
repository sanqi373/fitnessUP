<template>
  <div class="com-page page-container">
    <div class="post-list">
      <div
        v-for="post in posts"
        :key="post.id"
        class="post-card card"
      >
        <div class="post-header">
          <van-icon name="user-circle-o" size="40" color="#ddd" />
          <div class="post-info">
            <div class="post-name">{{ post.user.name }}</div>
            <div class="post-time">{{ post.time }}</div>
          </div>
          <van-icon name="ellipsis" color="#999" size="18" />
        </div>
        <div class="post-body">{{ post.content }}</div>
        <div class="post-actions">
          <div class="action" :class="{ liked: post.liked }" @click="post.liked = !post.liked">
            <van-icon :name="post.liked ? 'good-job' : 'good-job-o'" />
            <span>{{ post.likes + (post.liked ? 1 : 0) }}</span>
          </div>
          <div class="action">
            <van-icon name="chat-o" /><span>{{ post.comments }}</span>
          </div>
          <div class="action">
            <van-icon name="share-o" /><span>分享</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { mockPosts } from '@/mock/community'
const posts = ref(mockPosts)
</script>

<style scoped>
.post-list { padding: 12px 16px; }
.post-card { margin: 0 0 12px; }
.post-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.post-info { flex: 1; }
.post-name { font-size: 15px; font-weight: 500; }
.post-time { font-size: 12px; color: var(--color-text-light); }
.post-body { font-size: 15px; line-height: 1.65; margin-bottom: 12px; color: var(--color-text); }
.post-actions { display: flex; justify-content: space-around; padding-top: 10px; border-top: 1px solid var(--color-divider); }
.action {
  display: flex; align-items: center; gap: 4px;
  font-size: 13px; color: var(--color-text-light); cursor: pointer;
}
.action.liked { color: var(--color-primary); }
</style>
