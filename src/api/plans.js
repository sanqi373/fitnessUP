import request from './request'

export function listSavedPlans(kind) {
  return request.get(`/user/plans/${kind}`)
}

export function isPlanSaved(kind, id) {
  return request.get(`/user/plans/${kind}/${id}`)
}

export function addToPlan(kind, id) {
  return request.put(`/user/plans/${kind}/${id}`)
}

export function removeFromPlan(kind, id) {
  return request.delete(`/user/plans/${kind}/${id}`)
}