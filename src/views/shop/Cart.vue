<template>
  <main class="cart">
    <header class="heading"><h1>购物车</h1><span v-if="shop">{{ itemCount }} 件商品</span></header>
    <p v-if="failed">购物车加载失败 <button @click="load">重新加载</button></p>
    <p v-else-if="!shop" role="status">正在读取购物车…</p>
    <template v-else>
      <p v-if="notice" class="notice" role="status">{{ notice }}</p>
      <div v-if="rows.length" class="cart-layout">
        <section aria-label="购物车商品" class="items">
          <header class="toolbar">
            <label><input type="checkbox" :checked="allSelected" :indeterminate="!!selected.length && !allSelected"
              :disabled="busy" @change="toggleAll" />全选</label>
            <button class="remove" :disabled="busy || !selected.length" @click="apply('remove')">移除所选</button>
          </header>
          <label v-for="line in rows" :key="line.id" class="item">
            <input v-model="picked" type="checkbox" :value="line.id" :disabled="busy" :aria-label="'选择' + line.product.name" />
            <img :src="line.product.image" alt="" />
            <span class="item-text"><strong>{{ line.product.name }}</strong><span>{{ price(line.product.priceCents) }} × {{ line.quantity }}</span></span>
            <b>{{ price(line.product.priceCents * line.quantity) }}</b>
          </label>
        </section>
        <section class="checkout" aria-labelledby="checkout-title">
          <h2 id="checkout-title">结算</h2>
          <p>已选 {{ selectedCount }} 件</p>
          <div class="total"><span>合计</span><strong>{{ price(total) }}</strong></div>
          <p>模拟钱包余额 <b>¥{{ shop.balance }}</b></p>
          <button class="buy" type="button" :disabled="busy || !selected.length" @click="apply('purchase')">使用余额购买</button>
          <router-link to="/mine/wallet">查看我的钱包</router-link>
        </section>
      </div>
      <div v-else class="empty">
        <van-icon name="shopping-cart-o" size="42" />
        <h2>购物车是空的</h2>
        <router-link to="/shop" replace>去商场逛逛</router-link>
      </div>
    </template>
  </main>
</template>

<script setup>
import { computed, ref } from 'vue'
import { price, useShop } from '@/api/shop'

const { shop, failed, busy, load, update } = useShop()
const picked = ref([])
const notice = ref('')
const rows = computed(() => {
  if (!shop.value) return []
  const products = new Map(shop.value.products.map(product => [product.id, product]))
  return shop.value.cart.map(line => ({ ...line, product: products.get(line.productId) }))
})
const itemCount = computed(() => rows.value.reduce((sum, line) => sum + line.quantity, 0))
const selected = computed(() => rows.value.filter(line => picked.value.includes(line.id)))
const selectedCount = computed(() => selected.value.reduce((sum, line) => sum + line.quantity, 0))
const total = computed(() => selected.value.reduce((sum, line) => sum + line.product.priceCents * line.quantity, 0))
const allSelected = computed(() => !!rows.value.length && selected.value.length === rows.value.length)
function toggleAll() {
  picked.value = allSelected.value ? [] : rows.value.map(line => line.id)
}
async function apply(operation) {
  if (busy.value || !selected.value.length) return
  notice.value = ''
  const items = selected.value.map(({ id, quantity }) => ({ id, quantity }))
  const payment = price(total.value)
  if (await update(operation, { items })) {
    notice.value = operation === 'purchase' ? `购买成功，模拟钱包已支付 ${payment}` : '已移除所选商品'
  }
  picked.value = []
}
</script>

<style scoped>
.cart { max-width: 1080px; margin: auto; padding: clamp(22px, 4vw, 48px) 16px 48px; }
.heading, .toolbar, .total { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.heading { margin-bottom: 18px; }
.heading h1 { font-size: clamp(28px, 4vw, 38px); }
.heading span, .item-text span { color: #516e61; }
.cart-layout { display: grid; grid-template-columns: minmax(0, 1.8fr) minmax(220px, 1fr); gap: 20px; align-items: start; }
.items { border-radius: 16px; padding: 4px clamp(12px, 2vw, 22px); background: white; border: 1px solid var(--color-border); }
.toolbar { min-height: 48px; }
.toolbar label { display: flex; align-items: center; gap: 9px; cursor: pointer; }
.cart input { width: 18px; height: 18px; accent-color: #206748; cursor: pointer; }
.cart button { cursor: pointer; }
.cart button:disabled { opacity: .5; cursor: default; }
.remove { padding: 8px; background: transparent; border: 0; color: #8b422e; }
.item { display: grid; grid-template-columns: auto clamp(48px, 6vw, 80px) minmax(0, 1fr) auto; align-items: center; gap: clamp(8px, 1.6vw, 16px); padding: 16px 0; border-top: 1px solid var(--color-divider); cursor: pointer; }
.item img { width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: 9px; }
.item-text { display: grid; gap: 6px; overflow-wrap: anywhere; }
.item-text span { font-size: 13px; }
.checkout { padding: 24px; border-radius: 18px; background: #194b40; color: #fff; }
.checkout h2 { font-size: 22px; }
.checkout p { color: #d6e9dd; margin-top: 8px; }
.total { border-top: 1px dashed #719789; padding-top: 16px; margin-top: 16px; }
.total strong { font: 600 34px/1.2 'Barlow Condensed', sans-serif; }
.buy { width: 100%; min-height: 46px; margin: 18px 0 10px; border: 0; border-radius: 12px; background: #caf1d9; color: #144736; font-weight: 700; }
.checkout a { text-decoration: underline; }
.empty { padding: 52px 12px; text-align: center; }
.empty h2 { margin: 10px 0; font-size: 20px; }
.empty a, .notice { color: #236145; }
.notice { margin-bottom: 16px; }
@media (max-width: 680px) { .cart-layout { grid-template-columns: minmax(0, 1fr); } }
</style>
