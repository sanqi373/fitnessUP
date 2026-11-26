import request from './request'

export function getCommunityPosts(params) {
  return request.get('/community/posts', { params })
}
