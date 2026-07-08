import { createRouter, createWebHistory } from 'vue-router'

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
    meta: { title: '训练' }
  },
  {
    path: '/train/plan/:id',
    name: 'TrainPlan',
    component: () => import('@/views/train/PlanDetail.vue'),
    meta: { title: '训练计划', hideTabBar: true }
  },
  {
    path: '/course',
    name: 'Course',
    component: () => import('@/views/course/Index.vue'),
    meta: { title: '课程' }
  },
  {
    path: '/course/:id',
    name: 'CourseDetail',
    component: () => import('@/views/course/Detail.vue'),
    meta: { title: '课程详情', hideTabBar: true }
  },
  {
    path: '/diet',
    name: 'Diet',
    component: () => import('@/views/diet/Index.vue'),
    meta: { title: '饮食' }
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('@/views/community/Index.vue'),
    meta: { title: '社区' }
  },
  {
    path: '/community/post/:id',
    name: 'PostDetail',
    component: () => import('@/views/community/PostDetail.vue'),
    meta: { title: '动态详情', hideTabBar: true }
  },
  {
    path: '/live',
    name: 'Live',
    component: () => import('@/views/live/Index.vue'),
    meta: { title: '直播' }
  },
  {
    path: '/live/room/:id',
    name: 'LiveRoom',
    component: () => import('@/views/live/Room.vue'),
    meta: { title: '直播间', hideTabBar: true }
  },
  {
    path: '/shop',
    name: 'Shop',
    component: () => import('@/views/shop/Index.vue'),
    meta: { title: '商城' }
  },
  {
    path: '/shop/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/shop/ProductDetail.vue'),
    meta: { title: '商品详情', hideTabBar: true }
  },
  {
    path: '/mine',
    name: 'Mine',
    component: () => import('@/views/mine/Index.vue'),
    meta: { title: '我的' }
  },
  {
    path: '/mine/settings',
    name: 'Settings',
    component: () => import('@/views/mine/Settings.vue'),
    meta: { title: '设置', hideTabBar: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Index.vue'),
    meta: { title: '登录', hideTabBar: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/Index.vue'),
    meta: { title: '注册', hideTabBar: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - UP 健身` : 'UP 健身'
  next()
})

export default router
