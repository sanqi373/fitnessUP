import request from './request'

export function getTrainPlans() {
  return request.get('/train/plans')
}

export function getTrainPlanDetail(id) {
  return request.get(`/train/plans/${id}`)
}

export function changeWorkoutTimer(planId, sessionId, action) {
  return request.post(`/train/plans/${planId}/timer`, { sessionId, action })
}
