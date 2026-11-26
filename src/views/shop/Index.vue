<template>
  <div class="shop-page page-container page-container--no-tab">
    <header class="shop-heading">
      <h1 class="shop-title">商场</h1>
      <router-link to="/shop/cart" class="cart-link">
        <van-icon name="shopping-cart-o" />购物车 <span v-if="count">{{ count }}</span>
      </router-link>
    </header>
    <van-search v-model="keyword" placeholder="搜索健身装备" shape="round" />
    <p v-if="failed" class="shop-state">商品加载失败 <button @click="load">重新加载</button></p>
    <p v-else-if="!shop" class="shop-state" role="status">正在加载商品…</p>
    <div v-else class="product-grid">
      <div v-for="item in filteredProducts" :key="item.id" class="product-card">
        <img class="product-cover" :src="item.image" :alt="item.name" width="960" height="640" />
        <div class="product-name ellipsis-2">{{ item.name }}</div>
        <div class="product-actions">
          <strong class="product-price">{{ price(item.priceCents) }}</strong>
          <button type="button" :disabled="busy" :aria-label="'将' + item.name + '加入购物车'" @click="add(item)">加入购物车</button>
        </div>
      </div>
    </div>
    <van-empty v-if="shop && !failed && !filteredProducts.length" description="未找到相关商品" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { showToast } from 'vant'
import { useShop, price } from '@/api/shop'
const { shop, busy, failed, load, update } = useShop()
const count = computed(() => shop.value?.cart.reduce((sum, line) => sum + line.quantity, 0) || 0)
const keyword = ref('')
const filteredProducts = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return shop.value?.products.filter(p => p.name.toLowerCase().includes(kw)) || []
})
async function add(item) {
  if (await update('items/' + item.id)) showToast('已加入购物车')
}
</script>

<style scoped>
.shop-page { max-width: 1680px; margin-inline: auto; }
.shop-heading, .product-actions { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.shop-heading { padding: 20px 16px 10px; }
.cart-link, .product-actions button { display: inline-flex; align-items: center; gap: 6px; min-height: 42px; padding: 8px 14px; border: 1px solid #bcd7ca; border-radius: 12px; background: #e7f6ec; color: #1c593e; font-weight: 700; cursor: pointer; }
.cart-link span { min-width: 20px; text-align: center; border-radius: 12px; background: #1c593e; color: white; }
.product-actions { flex-wrap: wrap; }
.product-actions button { min-height: 36px; margin-left: auto; }
.shop-state { padding: 24px 16px; }
.shop-title {
  font-size: 32px;
  line-height: 1.2;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  padding: 12px 16px;
}
.product-card {
  background: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: 10px;
  box-shadow: var(--shadow-sm);
}
.product-cover {
  width: 100%;
  height: auto;
  aspect-ratio: 3 / 2;
  object-fit: contain;
  border-radius: var(--radius-sm);
  display: block;
  margin-bottom: 8px;
}
.product-name {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 6px;
}
.product-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-primary-dark);
}
@media (max-width: 760px) {
  .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
