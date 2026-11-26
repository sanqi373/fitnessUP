/**
 * 用户相关 API
 */
import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getUserInfo(config) {
  return request.get('/user/info', config)
}

export function updateUserInfo(data) {
  return request.put('/user/info', data)
}

export function getUserStats() {
  return request.get('/user/stats')
}

export function checkInToday() {
  return request.put('/user/check-in')
}

export function getAchievements() {
  return request.get('/user/achievements')
}
