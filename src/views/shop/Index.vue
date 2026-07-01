<template>
  <div class="shop-page page-container">
    <van-search v-model="keyword" placeholder="搜索健身装备" shape="round" />
    <div class="product-grid">
      <div v-for="item in products" :key="item.id" class="product-card">
        <div class="product-cover">
          <van-icon name="photo-o" size="36" color="#ddd" />
        </div>
        <div class="product-name ellipsis-2">{{ item.name }}</div>
        <div class="product-price">¥{{ item.price }}</div>
      </div>
    </div>
    <van-empty v-if="products.length === 0" description="暂无商品" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
const keyword = ref('')
const products = ref([
  { id: 1, name: 'UP 专业瑜伽垫', price: 99, image: '' },
  { id: 2, name: '弹力带套装', price: 39.9, image: '' },
  { id: 3, name: '智能计数跳绳', price: 69, image: '' },
  { id: 4, name: 'UP 运动水壶 750ml', price: 49, image: '' },
  { id: 5, name: '健身手套防滑款', price: 59, image: '' },
  { id: 6, name: '泡沫轴肌肉放松', price: 79, image: '' }
])
</script>

<style scoped>
.product-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 12px 16px;
}
.product-card {
  background: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: 10px;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
}
.product-cover {
  width: 100%;
  height: 140px;
  background: #f5f5f5;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
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
  color: #EE0A24;
}
</style>
