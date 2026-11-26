import request from './request'

export function getWallet() {
  return request.get('/user/wallet')
}

export function topUpWallet(requestId, amount) {
  return request.put(`/user/wallet/top-ups/${requestId}`, { amount })
}
