import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const isLogin = computed(() => !!token.value)

  // 成长等级
  const level = ref(userInfo.value?.level || 1)
  const exp = ref(userInfo.value?.exp || 0)
  const nextLevelExp = computed(() => level.value * 100)

  // 训练统计
  const stats = ref(userInfo.value?.stats || {
    totalTrainDays: 0,
    totalTrainMinutes: 0,
    totalCalories: 0,
    streakDays: 0
  })

  function setToken(val) {
    token.value = val
    localStorage.setItem('token', val)
  }

  function setUserInfo(info) {
    userInfo.value = info
    if (info) {
      level.value = info.level || 1
      exp.value = info.exp || 0
      stats.value = info.stats || stats.value
    }
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function addExp(val) {
    exp.value += val
    while (exp.value >= nextLevelExp.value) {
      exp.value -= nextLevelExp.value
      level.value++
    }
  }

  return {
    token, userInfo, isLogin, level, exp, nextLevelExp, stats,
    setToken, setUserInfo, logout, addExp
  }
})
