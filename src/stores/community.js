import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCommunityPosts } from '@/api/community'

const timeFormat = new Intl.DateTimeFormat('zh-CN', {
  month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
})

export function formatPostTime(value) {
  if (!value) return ''
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : timeFormat.format(date)
}

export const useCommunityStore = defineStore('community', () => {
  const posts = ref([])
  const status = ref('idle')
  const localLikes = new Map()

  async function loadPosts() {
    if (status.value === 'loading') return
    status.value = 'loading'
    try {
      const { data } = await getCommunityPosts()
      posts.value = data.list.map(post => {
        const serverLiked = Boolean(post.liked)
        const liked = localLikes.get(post.id) ?? serverLiked
        return {
          ...post,
          likes: Math.max(0, (Number(post.likes) || 0) + Number(liked) - Number(serverLiked)),
          comments: Number(post.comments) || 0,
          liked
        }
      })
      status.value = 'ready'
    } catch {
      status.value = 'error'
    }
  }

  function toggleLike(post) {
    post.likes = Math.max(0, post.likes + (post.liked ? -1 : 1))
    post.liked = !post.liked
    localLikes.set(post.id, post.liked)
  }

  return { posts, status, loadPosts, toggleLike }
})
