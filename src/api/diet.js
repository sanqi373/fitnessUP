import request from './request'

export const nutrients = [
  { key: 'protein', label: '蛋白质' },
  { key: 'carbs', label: '碳水' },
  { key: 'fat', label: '脂肪' }
]

const mealTypes = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐', snack: '加餐' }

export function mealTypeLabel(type) {
  return mealTypes[type] || type
}

export function getRecipe(id) {
  return request.get(`/diet/recommend/${id}`)
}

export function setCalorieGoal(calories, onlyIfUnset = false) {
  const headers = onlyIfUnset ? { 'If-None-Match': '*' } : {}
  return request.put('/user/calorie-goal', { calories }, { headers })
}

export function getDietRecommend() {
  return request.get('/diet/recommend')
}
