import request from './request'

export function getTrainPlans() {
  return request.get('/train/plans')
}

export function getTrainPlanDetail(id) {
  return request.get(`/train/plans/${id}`)
}

export function startTrain(data) {
  return request.post('/train/start', data)
}

export function finishTrain(data) {
  return request.post('/train/finish', data)
}

export function getTrainHistory() {
  return request.get('/train/history')
}
