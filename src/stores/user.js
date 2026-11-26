import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { checkInToday, getUserStats } from '@/api/user'

export const workoutSavedKey = 'fitnessUP:workoutSaved'

const emptyStats = { totalTrainDays: 0, totalTrainMinutes: 0, totalCalories: 0, todayTrainSeconds: 0, todayCalories: 0, streakDays: 0 }

function expiryTime(token) {
  const parts = token?.split('.')
  if (parts?.length !== 3 || parts.some(part => !/^[A-Za-z0-9_-]+$/.test(part))) return 0
  try {
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')))
    return Number.isFinite(payload.exp) ? payload.exp * 1000 : 0
  } catch {
    return 0
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const verifiedToken = ref('')
  const expiresAt = computed(() => expiryTime(token.value))
  const userInfo = ref(null)
  const isLogin = computed(() => !!token.value)

  // 成长等级
  const level = computed(() => isLogin.value ? userInfo.value?.level ?? 1 : 1)
  const exp = computed(() => isLogin.value ? userInfo.value?.exp ?? 0 : 0)
  const nextLevelExp = computed(() => level.value * 100)

  // 训练统计
  const stats = ref({ ...emptyStats })
  let statsRevision = 0

  async function checkIn() {
    const session = getSessionToken()
    if (!session) return
    const { data } = await checkInToday()
    if (session !== getSessionToken()) return
    ++statsRevision
    stats.value = data
    localStorage.setItem(workoutSavedKey, 'check-in:' + data.checkInDate + ':' + Date.now())
  }

  async function refreshStats() {
    const session = getSessionToken()
    if (!session) return
    const revision = ++statsRevision
    try {
      const { data } = await getUserStats()
      if (revision === statsRevision && session === getSessionToken()) stats.value = data
    } catch {
      // Keep the last confirmed totals. The request interceptor reports the error.
    }
  }

  function setToken(val) {
    token.value = val
    verifiedToken.value = val
    localStorage.setItem('token', val)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    verifiedToken.value = ''
    userInfo.value = null
    stats.value = { ...emptyStats }
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function getSessionToken() {
    const stored = localStorage.getItem('token')
    if (expiryTime(stored) <= Date.now()) {
      if (stored !== null || token.value || localStorage.getItem('userInfo') !== null) logout()
      return ''
    }
    if (stored !== token.value) {
      token.value = stored
      verifiedToken.value = ''
      try {
        userInfo.value = JSON.parse(localStorage.getItem('userInfo'))
      } catch {
        userInfo.value = null
      }
      stats.value = { ...emptyStats, ...userInfo.value?.stats }
    }
    return stored
  }

  getSessionToken()
  return {
    token, verifiedToken, expiresAt, userInfo, isLogin, level, exp, nextLevelExp, stats,
    setToken, setUserInfo, logout, getSessionToken, refreshStats, checkIn
  }
})
