/**
 * Mock 拦截器 —— 开发环境下拦截 API 请求返回模拟数据
 * 后端就绪后删除此文件即可切换到真实接口
 */
import { mockUser, mockAchievements } from './user'
import { mockTrainPlans } from './train'
import { mockCourses } from './course'
import { mockPosts } from './community'
import { mockDietRecommend } from './diet'

// 模拟网络延迟
function delay(data, ms = 300) {
  return new Promise(resolve => setTimeout(() => resolve(data), ms + Math.random() * 200))
}

// 标准响应格式
function success(data, message = 'success') {
  return { code: 200, data, message }
}

export function setupMock(axiosInstance) {
  axiosInstance.interceptors.request.use(async config => {
    const { url, method } = config

    // 登录
    if (url === '/auth/login' && method === 'post') {
      const data = JSON.parse(config.data)
      if (data.phone && data.password) {
        return delay(success({ token: 'mock_token_' + Date.now(), userInfo: mockUser }), 500)
      }
      return Promise.reject({ code: 400, message: '手机号或密码错误' })
    }

    // 获取用户信息
    if (url === '/user/info' && method === 'get') {
      return delay(success(mockUser))
    }

    // 训练计划列表
    if (url === '/train/plans' && method === 'get') {
      return delay(success({ list: mockTrainPlans, total: mockTrainPlans.length }))
    }

    // 训练计划详情
    if (url?.startsWith('/train/plans/') && method === 'get') {
      const id = parseInt(url.split('/').pop())
      const plan = mockTrainPlans.find(p => p.id === id)
      return delay(success(plan || null))
    }

    // 课程列表
    if (url === '/courses' && method === 'get') {
      return delay(success({ list: mockCourses, total: mockCourses.length }))
    }

    // 课程详情
    if (url?.startsWith('/courses/') && !url.includes('comments') && method === 'get') {
      const parts = url.split('/')
      const id = parseInt(parts[parts.length - 1])
      const course = mockCourses.find(c => c.id === id)
      return delay(success(course || null))
    }

    // 社区动态
    if (url === '/community/posts' && method === 'get') {
      return delay(success({ list: mockPosts, total: mockPosts.length }))
    }

    // 成就列表
    if (url === '/user/achievements' && method === 'get') {
      return delay(success(mockAchievements))
    }

    // 饮食推荐
    if (url === '/diet/recommend' && method === 'get') {
      return delay(success(mockDietRecommend))
    }

    // 未匹配的接口，放行到真实请求（会 404）
    return config
  }, error => Promise.reject(error))
}
