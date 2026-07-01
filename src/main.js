import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import request from './api/request'
import { setupMock } from './mock'

// Vant 组件手动注册
import {
  Button, Icon, NavBar, Tab, Tabs, Rate, Progress,
  Search, Field, Cell, CellGroup, Empty, Loading, Toast, Image, Swipe, SwipeItem
} from 'vant'
import 'vant/lib/index.css'

const app = createApp(App)
app.use(Button)
app.use(Icon)
app.use(NavBar)
app.use(Tab)
app.use(Tabs)
app.use(Rate)
app.use(Progress)
app.use(Search)
app.use(Field)
app.use(Cell)
app.use(CellGroup)
app.use(Empty)
app.use(Loading)
app.use(Toast)
app.use(Image)
app.use(Swipe)
app.use(SwipeItem)

// 开发环境启用 Mock 数据（后端就绪后注释此行）
setupMock(request)

// 全局样式
import './styles/global.css'
import './styles/variables.css'

app.use(createPinia())
app.use(router)
app.mount('#app')
