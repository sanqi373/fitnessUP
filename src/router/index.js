import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo } from '@/api/user'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/Index.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/train',
    name: 'Train',
    component: () => import('@/views/train/Index.vue'),
    meta: { title: '训练', backFallback: '/home' }
  },
  {
    path: '/train/plan/:id',
    name: 'TrainPlan',
    props: true,
    component: () => import('@/views/train/PlanDetail.vue'),
    meta: { title: '训练计划', backFallback: '/train', hideTabBar: true, showNavTitle: true }
  },
  {
    path: '/course/:id?',
    redirect: '/train'
  },
  {
    path: '/mine/courses',
    redirect: '/train'
  },
  {
    path: '/diet',
    name: 'Diet',
    component: () => import('@/views/diet/Index.vue'),
    meta: { title: '饮食', backFallback: '/home', activeTab: '/home' }
  },
  {
    path: '/diet/recipe/:id',
    name: 'RecipeDetail',
    props: true,
    component: () => import('@/views/diet/RecipeDetail.vue'),
    meta: { title: '食谱详情', backFallback: '/diet', hideTabBar: true, showNavTitle: true }
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('@/views/community/Index.vue'),
    meta: { title: '社区', backFallback: '/home' }
  },
  {
    path: '/community/post/:id',
    name: 'PostDetail',
    component: () => import('@/views/community/PostDetail.vue'),
    meta: { title: '动态详情', backFallback: '/community', hideTabBar: true, showNavTitle: true }
  },
  {
    path: '/live',
    name: 'Live',
    component: () => import('@/views/live/Index.vue'),
    meta: { title: '直播', backFallback: '/home' }
  },
  {
    path: '/live/room/:id',
    name: 'LiveRoom',
    component: () => import('@/views/live/Room.vue'),
    meta: { title: '直播间', backFallback: '/live', hideTabBar: true, showNavTitle: true }
  },
  {
    path: '/shop',
    name: 'Shop',
    component: () => import('@/views/shop/Index.vue'),
    meta: { title: '商场', backFallback: '/mine', hideTabBar: true }
  },
  {
    path: '/shop/cart',
    name: 'Cart',
    component: () => import('@/views/shop/Cart.vue'),
    meta: { title: '购物车', backFallback: '/shop', hideTabBar: true }
  },
  {
    path: '/shop/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/shop/ProductDetail.vue'),
    meta: { title: '商品详情', backFallback: '/shop', hideTabBar: true, showNavTitle: true }
  },
  {
    path: '/mine',
    name: 'Mine',
    component: () => import('@/views/mine/Index.vue'),
    meta: { title: '我的', backFallback: '/home' }
  },
  {
    path: '/mine/training-plan',
    component: () => import('@/views/mine/SavedPlans.vue'),
    props: { kind: 'training' },
    meta: { title: '我的训练计划', backFallback: '/mine', hideTabBar: true }
  },
  {
    path: '/mine/diet-plan',
    component: () => import('@/views/mine/SavedPlans.vue'),
    props: { kind: 'diet' },
    meta: { title: '我的饮食计划', backFallback: '/mine', hideTabBar: true }
  },
  {
    path: '/mine/body-data',
    component: () => import('@/views/mine/BodyData.vue'),
    meta: { title: '身体数据', backFallback: '/mine', hideTabBar: true }
  },
  {
    path: '/mine/wallet',
    name: 'Wallet',
    component: () => import('@/views/mine/Wallet.vue'),
    meta: { title: '我的钱包', backFallback: '/mine', hideTabBar: true, showNavTitle: true }
  },
  {
    path: '/mine/settings',
    name: 'Settings',
    component: () => import('@/views/mine/Settings.vue'),
    meta: { title: '设置', backFallback: '/mine', hideTabBar: true, showNavTitle: true }
  },
  {
    path: '/mine/profile',
    name: 'Profile',
    component: () => import('@/views/mine/Profile.vue'),
    meta: { title: '个人信息', backFallback: '/mine', hideTabBar: true, showNavTitle: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Index.vue'),
    meta: { title: '登录', hideTabBar: true, guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/Index.vue'),
    meta: { title: '注册', hideTabBar: true, guest: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { top: 0 }
  }
})

router.beforeEach(async to => {
  const store = useUserStore()
  const session = store.getSessionToken()
  if (session && session !== store.verifiedToken) {
    try {
      await getUserInfo({ silentAuthFailure: true })
      if (store.getSessionToken() === session) store.verifiedToken = session
    } catch {
      // Only a verified session may open a business page.
    }
  }
  const active = !!store.token && store.token === store.verifiedToken
  if (!active && !to.meta.guest) return { name: 'Login', replace: true }
  if (active && to.meta.guest) return { name: 'Home', replace: true }
})

router.afterEach((to, from, failure) => {
  if (!failure) document.title = to.meta.title ? `${to.meta.title} - UP 健身` : 'UP 健身'
})

export default router
