import { onMounted, ref } from 'vue'
import request from './request'

export function useShop() {
  const shop = ref(null)
  const busy = ref(false)
  const failed = ref(false)

  async function load() {
    failed.value = false
    try {
      shop.value = (await request.get('/shop')).data
    } catch {
      failed.value = true
    }
  }

  async function update(path, body) {
    if (busy.value) return false
    busy.value = true
    try {
      shop.value = (await request.post('/shop/cart/' + path, body)).data
      return true
    } catch {
      await load()
      return false
    } finally {
      busy.value = false
    }
  }

  onMounted(load)
  return { shop, busy, failed, load, update }
}

export function price(cents) {
  return '¥' + (cents / 100).toFixed(2)
}
