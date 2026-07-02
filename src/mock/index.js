/**
 * Mock 拦截器 —— 替换 axios adapter 拦截请求返回模拟数据
 * 后端就绪后注释 main.js 中 setupMock 调用即可切换到真实接口
 */
import { mockUser, mockAchievements } from './user'
import { mockTrainPlans } from './train'
import { mockCourses } from './course'
import { mockPosts } from './community'
import { mockDietRecommend } from './diet'

function delay(ms = 300) {
  return new Promise(resolve => setTimeout(resolve, ms + Math.random() * 200))
}

function success(data, message = 'success') {
  return { code: 200, data, message }
}

function createResponse(config, data, status = 200) {
  return {
    status,
    statusText: status === 200 ? 'OK' : 'Error',
    data,
    headers: { 'content-type': 'application/json' },
    config,
    request: {}
  }
}

export function setupMock(axiosInstance) {
  const originalAdapter = axiosInstance.defaults.adapter

  axiosInstance.defaults.adapter = async (config) => {
    const { url, method, data } = config
    const parsedData = data ? JSON.parse(data) : {}

    // ========== 用户模块 ==========
    // 登录
    if (url === '/auth/login' && method === 'post') {
      await delay(500)
      if (parsedData.phone && parsedData.password) {
        return createResponse(config, success({ token: 'mock_token_' + Date.now(), userInfo: mockUser }))
      }
      return Promise.reject({
        config,
        response: createResponse(config, { code: 400, message: '手机号或密码错误' }, 400)
      })
    }

    // 注册
    if (url === '/auth/register' && method === 'post') {
      await delay(500)
      return createResponse(config, success({ token: 'mock_token_' + Date.now(), userInfo: mockUser }))
    }

    // 获取用户信息
    if (url === '/user/info' && method === 'get') {
      await delay()
      return createResponse(config, success(mockUser))
    }

    // 更新用户信息
    if (url === '/user/info' && method === 'put') {
      await delay()
      return createResponse(config, success({ ...mockUser, ...parsedData }))
    }

    // 用户统计
    if (url === '/user/stats' && method === 'get') {
      await delay()
      return createResponse(config, success(mockUser.stats))
    }

    // 成就列表
    if (url === '/user/achievements' && method === 'get') {
      await delay()
      return createResponse(config, success(mockAchievements))
    }

    // ========== 训练模块 ==========
    // 训练计划列表
    if (url === '/train/plans' && method === 'get') {
      await delay()
      return createResponse(config, success({ list: mockTrainPlans, total: mockTrainPlans.length }))
    }

    // 训练计划详情
    if (url?.startsWith('/train/plans/') && method === 'get') {
      const id = parseInt(url.split('/').pop())
      const plan = mockTrainPlans.find(p => p.id === id)
      await delay()
      return createResponse(config, success(plan || null))
    }

    // 开始训练
    if (url === '/train/start' && method === 'post') {
      await delay()
      return createResponse(config, success({ sessionId: 'session_' + Date.now(), startTime: new Date().toISOString() }))
    }

    // 结束训练
    if (url === '/train/finish' && method === 'post') {
      await delay()
      return createResponse(config, success({ calories: 320, duration: 45, expGained: 15 }))
    }

    // 训练历史
    if (url === '/train/history' && method === 'get') {
      await delay()
      return createResponse(config, success({
        list: [
          { id: 1, planName: '上肢力量突破', date: '2026-06-30', duration: 45, calories: 350 },
          { id: 2, planName: '核心燃脂 HIIT', date: '2026-06-29', duration: 30, calories: 400 },
          { id: 3, planName: '下肢塑形计划', date: '2026-06-28', duration: 40, calories: 300 }
        ],
        total: 3
      }))
    }

    // ========== 课程模块 ==========
    // 课程列表
    if (url === '/courses' && method === 'get') {
      await delay()
      return createResponse(config, success({ list: mockCourses, total: mockCourses.length }))
    }

    // 课程详情
    if (url?.startsWith('/courses/') && !url.includes('comments') && method === 'get') {
      const parts = url.split('/')
      const id = parseInt(parts[parts.length - 1])
      const course = mockCourses.find(c => c.id === id)
      await delay()
      return createResponse(config, success(course || null))
    }

    // 课程评论
    if (url?.includes('/comments') && method === 'get') {
      await delay()
      return createResponse(config, success({
        list: [
          { id: 1, user: { name: '用户A' }, content: '课程很棒，讲解详细', time: '2026-06-30', likes: 12 },
          { id: 2, user: { name: '用户B' }, content: '坚持了一周，效果明显', time: '2026-06-29', likes: 8 }
        ],
        total: 2
      }))
    }

    // ========== 社区模块 ==========
    // 社区动态
    if (url === '/community/posts' && method === 'get') {
      await delay()
      return createResponse(config, success({ list: mockPosts, total: mockPosts.length }))
    }

    // ========== 饮食模块 ==========
    // 饮食推荐
    if (url === '/diet/recommend' && method === 'get') {
      await delay()
      return createResponse(config, success(mockDietRecommend))
    }

    // ========== 其他未匹配接口 ==========
    // 兜底：所有未匹配的 API 请求返回 404 模拟，避免走真实后端
    if (url.startsWith('/')) {
      await delay(100)
      return Promise.reject({
        config,
        response: createResponse(config, { code: 404, message: '接口未实现（Mock 模式）' }, 404)
      })
    }

    // 非 API 请求（如静态资源）走原始 adapter
    return originalAdapter(config)
  }
}
