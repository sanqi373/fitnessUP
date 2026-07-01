import request from './request'

export function getCourses(params) {
  return request.get('/courses', { params })
}

export function getCourseDetail(id) {
  return request.get(`/courses/${id}`)
}

export function getCourseComments(id, params) {
  return request.get(`/courses/${id}/comments`, { params })
}
