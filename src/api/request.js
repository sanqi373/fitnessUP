import axios from 'axios'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  // Spring Boot exposes the API under /api. Vite proxies this path to port 8080.
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = useUserStore().getSessionToken()
    if (token && !config.url.startsWith('/auth/')) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

function handleUnauthorized(config, message) {
  const sent = config.headers?.Authorization
  if (!sent) {
    if (!config.silentAuthFailure) showToast(message || '请先登录')
    return
  }
  if (sent === `Bearer ${localStorage.getItem('token')}`) {
    useUserStore().logout()
    if (!config.silentAuthFailure) showToast('登录已失效，请重新登录')
  }
}

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { data } = response
    if (data.code === 0 || data.code === 200) {
      return data
    }
    // Token 过期
    if (data.code === 401) {
      handleUnauthorized(response.config, data.message)
      return Promise.reject(data)
    }
    showToast(data.message || '请求失败')
    return Promise.reject(data)
  },
  error => {
    if (error.code === 'ECONNABORTED') {
      showToast('请求超时，请重试')
    } else if (!error.response) {
      showToast('网络异常，请检查网络')
    } else {
      const { status, data: resData } = error.response
      if (status === 401) {
        handleUnauthorized(error.config, resData?.message)
      } else if (resData?.message) {
        showToast(resData.message)
      } else {
        const msgMap = { 400: '请求参数错误', 403: '无权限访问', 404: '资源不存在', 500: '服务器错误' }
        showToast(msgMap[status] || '请求失败')
      }
    }
    return Promise.reject(error)
  }
)

export default request
