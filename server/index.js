const express = require('express')
const mysql = require('mysql2/promise')
const cors = require('cors')

const app = express()
app.use(cors())
app.use(express.json())

// MySQL 连接池
const pool = mysql.createPool({
  host: 'localhost',
  port: 3307,
  user: 'root',
  password: 'MsDic373204.',
  database: 'fitness_up',
  waitForConnections: true,
  connectionLimit: 10
})

// ==================== 用户认证 ====================

// 注册
app.post('/auth/register', async (req, res) => {
  const { phone, password } = req.body
  if (!/^1\d{10}$/.test(phone)) {
    return res.status(400).json({ code: 400, message: '请输入正确的11位手机号' })
  }
  if (!password || password.length < 6) {
    return res.status(400).json({ code: 400, message: '密码至少6位，支持数字和字母' })
  }
  try {
    await pool.execute('INSERT INTO users (phone, password) VALUES (?, ?)', [phone, password])
    res.json({
      code: 200, message: '注册成功',
      data: {
        token: 'token_' + Date.now(),
        userInfo: { phone: phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') }
      }
    })
  } catch (err) {
    if (err.code === 'ER_DUP_ENTRY') {
      return res.status(400).json({ code: 400, message: '该手机号已注册' })
    }
    console.error('注册错误:', err)
    res.status(500).json({ code: 500, message: '服务器错误' })
  }
})

// 登录
app.post('/auth/login', async (req, res) => {
  const { phone, password } = req.body
  if (!phone || !password) {
    return res.status(400).json({ code: 400, message: '请输入手机号和密码' })
  }
  try {
    const [rows] = await pool.execute('SELECT * FROM users WHERE phone = ?', [phone])
    if (rows.length === 0) {
      return res.status(400).json({ code: 400, message: '账号不存在' })
    }
    if (rows[0].password !== password) {
      return res.status(400).json({ code: 400, message: '账号密码不匹配' })
    }
    res.json({
      code: 200, message: '登录成功',
      data: {
        token: 'token_' + Date.now(),
        userInfo: {
          phone: rows[0].phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2'),
          nickname: rows[0].nickname || ''
        }
      }
    })
  } catch (err) {
    console.error('登录错误:', err)
    res.status(500).json({ code: 500, message: '服务器错误' })
  }
})

// ==================== 用户信息 ====================

app.get('/user/info', (req, res) => {
  res.json({ code: 200, message: 'success', data: mockUser })
})

app.put('/user/info', (req, res) => {
  res.json({ code: 200, message: 'success', data: req.body })
})

app.get('/user/stats', (req, res) => {
  res.json({ code: 200, message: 'success', data: mockUser.stats })
})

app.get('/user/achievements', (req, res) => {
  res.json({ code: 200, message: 'success', data: mockAchievements })
})

// ==================== 训练模块 ====================

app.get('/train/plans', (req, res) => {
  res.json({ code: 200, message: 'success', data: { list: mockTrainPlans, total: mockTrainPlans.length } })
})

app.get('/train/plans/:id', (req, res) => {
  const plan = mockTrainPlans.find(p => p.id === parseInt(req.params.id))
  res.json({ code: 200, message: 'success', data: plan || null })
})

app.post('/train/start', (req, res) => {
  res.json({ code: 200, message: 'success', data: { sessionId: 'session_' + Date.now(), startTime: new Date().toISOString() } })
})

app.post('/train/finish', (req, res) => {
  res.json({ code: 200, message: 'success', data: { calories: 320, duration: 45, expGained: 15 } })
})

app.get('/train/history', (req, res) => {
  res.json({
    code: 200, message: 'success', data: {
      list: [
        { id: 1, planName: '上肢力量突破', date: '2026-06-30', duration: 45, calories: 350 },
        { id: 2, planName: '核心燃脂 HIIT', date: '2026-06-29', duration: 30, calories: 400 },
        { id: 3, planName: '下肢塑形计划', date: '2026-06-28', duration: 40, calories: 300 }
      ],
      total: 3
    }
  })
})

// ==================== 课程模块 ====================

app.get('/courses', (req, res) => {
  res.json({ code: 200, message: 'success', data: { list: mockCourses, total: mockCourses.length } })
})

app.get('/courses/:id', (req, res) => {
  const course = mockCourses.find(c => c.id === parseInt(req.params.id))
  res.json({ code: 200, message: 'success', data: course || null })
})

app.get('/courses/:id/comments', (req, res) => {
  res.json({
    code: 200, message: 'success', data: {
      list: [
        { id: 1, user: { name: '用户A' }, content: '课程很棒，讲解详细', time: '2026-06-30', likes: 12 },
        { id: 2, user: { name: '用户B' }, content: '坚持了一周，效果明显', time: '2026-06-29', likes: 8 }
      ],
      total: 2
    }
  })
})

// ==================== 社区模块 ====================

app.get('/community/posts', (req, res) => {
  res.json({ code: 200, message: 'success', data: { list: mockPosts, total: mockPosts.length } })
})

// ==================== 饮食模块 ====================

app.get('/diet/recommend', (req, res) => {
  res.json({ code: 200, message: 'success', data: mockDietRecommend })
})

// ==================== 兜底 404 ====================

app.use((req, res) => {
  res.status(404).json({ code: 404, message: '接口未实现' })
})

app.listen(3001, () => {
  console.log('Server running on http://localhost:3001')
})

// ==================== Mock 数据 ====================

const mockUser = {
  id: 1, nickname: '健身达人', avatar: '', phone: '138****8888',
  level: 5, exp: 230, bio: '自律给我自由',
  stats: { totalTrainDays: 86, totalTrainMinutes: 4320, totalCalories: 28600, streakDays: 7 }
}

const mockAchievements = [
  { id: 1, name: '初出茅庐', desc: '完成首次训练', icon: 'medal-o', unlocked: true },
  { id: 2, name: '坚持一周', desc: '连续训练7天', icon: 'fire-o', unlocked: true },
  { id: 3, name: '百炼成钢', desc: '累计训练100天', icon: 'shield-o', unlocked: false },
  { id: 4, name: '卡路里杀手', desc: '累计消耗50000大卡', icon: 'flame-o', unlocked: false },
  { id: 5, name: '全能战士', desc: '完成所有训练类别', icon: 'star-o', unlocked: false },
  { id: 6, name: '社交达人', desc: '发帖获100赞', icon: 'good-job-o', unlocked: false }
]

const mockTrainPlans = [
  {
    id: 1, name: '上肢力量突破', type: 'strength', level: '中级', duration: 45, calories: 350, cover: '',
    desc: '针对胸、背、肩、手臂的综合力量训练',
    exercises: [
      { name: '俯卧撑', sets: 4, reps: 15, rest: 60, video: '' },
      { name: '哑铃弯举', sets: 4, reps: 12, rest: 45, video: '' },
      { name: '哑铃飞鸟', sets: 3, reps: 12, rest: 45, video: '' },
      { name: '三头臂屈伸', sets: 3, reps: 15, rest: 45, video: '' }
    ]
  },
  {
    id: 2, name: '核心燃脂 HIIT', type: 'hiit', level: '高级', duration: 30, calories: 400, cover: '',
    desc: '高强度间歇训练，快速燃脂塑形',
    exercises: [
      { name: '开合跳', sets: 4, reps: 40, rest: 30, video: '' },
      { name: '波比跳', sets: 4, reps: 12, rest: 45, video: '' },
      { name: '登山跑', sets: 3, reps: 30, rest: 30, video: '' },
      { name: '平板支撑', sets: 3, reps: 1, rest: 30, duration: 60, video: '' }
    ]
  },
  {
    id: 3, name: '下肢塑形计划', type: 'strength', level: '初级', duration: 40, calories: 300, cover: '',
    desc: '深蹲、箭步蹲等下肢训练，打造完美腿型',
    exercises: [
      { name: '自重深蹲', sets: 4, reps: 20, rest: 60, video: '' },
      { name: '箭步蹲', sets: 3, reps: 15, rest: 45, video: '' },
      { name: '臀桥', sets: 4, reps: 15, rest: 45, video: '' },
      { name: '提踵', sets: 3, reps: 25, rest: 30, video: '' }
    ]
  },
  {
    id: 4, name: '晨间瑜伽拉伸', type: 'yoga', level: '初级', duration: 20, calories: 120, cover: '',
    desc: '唤醒身体的清晨瑜伽序列',
    exercises: [
      { name: '山式站立', sets: 1, reps: 1, rest: 0, duration: 60, video: '' },
      { name: '下犬式', sets: 1, reps: 1, rest: 0, duration: 60, video: '' },
      { name: '猫牛式', sets: 1, reps: 10, rest: 0, video: '' },
      { name: '婴儿式', sets: 1, reps: 1, rest: 0, duration: 120, video: '' }
    ]
  },
  {
    id: 5, name: '5 公里跑步计划', type: 'cardio', level: '中级', duration: 35, calories: 380, cover: '',
    desc: '从热身到冲刺的完整跑步训练',
    exercises: [
      { name: '慢跑热身', duration: 300, video: '' },
      { name: '匀速跑', duration: 1200, video: '' },
      { name: '间歇冲刺', sets: 5, reps: 1, rest: 90, duration: 60, video: '' },
      { name: '慢跑放松', duration: 300, video: '' }
    ]
  }
]

const mockCourses = [
  { id: 1, title: '30天马甲线挑战', cover: '', coach: { name: '陈教练', avatar: '' }, difficulty: '中级', duration: '30天', totalLessons: 15, enrolled: 12800, rating: 4.8, price: 0, category: '腹肌', desc: '专为久坐一族设计，每天15分钟，30天练出马甲线' },
  { id: 2, title: 'HIIT 高效燃脂', cover: '', coach: { name: '李教练', avatar: '' }, difficulty: '高级', duration: '21天', totalLessons: 10, enrolled: 9600, rating: 4.7, price: 0, category: '燃脂', desc: '高强度间歇训练，每次20分钟，燃脂效果持续48小时' },
  { id: 3, title: '新手入门指南', cover: '', coach: { name: '王教练', avatar: '' }, difficulty: '初级', duration: '14天', totalLessons: 7, enrolled: 21500, rating: 4.9, price: 0, category: '综合', desc: '从零开始，系统讲解健身基础知识与动作要领' },
  { id: 4, title: '瑜伽身心平衡', cover: '', coach: { name: '张教练', avatar: '' }, difficulty: '初级', duration: '28天', totalLessons: 12, enrolled: 7800, rating: 4.6, price: 29.9, category: '瑜伽', desc: '缓解压力、改善体态的系统瑜伽课程' },
  { id: 5, title: '增肌塑形计划', cover: '', coach: { name: '刘教练', avatar: '' }, difficulty: '高级', duration: '60天', totalLessons: 24, enrolled: 5400, rating: 4.8, price: 49.9, category: '增肌', desc: '科学分化训练，胸背腿肩臂每周循环' },
  { id: 6, title: '办公室拉伸放松', cover: '', coach: { name: '赵教练', avatar: '' }, difficulty: '初级', duration: '7天', totalLessons: 5, enrolled: 18600, rating: 4.5, price: 0, category: '拉伸', desc: '每天5分钟，告别肩颈酸痛' }
]

const mockPosts = [
  { id: 1, user: { name: '健身老王', avatar: '' }, content: '坚持晨跑第 30 天！配速终于进 5 分钟了，这种感觉太爽了', images: [], likes: 238, comments: 42, time: '2 小时前', liked: false },
  { id: 2, user: { name: '瑜伽小美', avatar: '' }, content: '分享一套睡前拉伸序列，5个动作缓解一天的疲劳~', images: [], likes: 156, comments: 28, time: '5 小时前', liked: true },
  { id: 3, user: { name: '铁馆阿强', avatar: '' }, content: '新 PR！深蹲 120kg 5x5 完成 三个月前还只能蹲 80，进步看得见', images: [], likes: 412, comments: 67, time: '昨天', liked: false },
  { id: 4, user: { name: 'UP小助手', avatar: '' }, content: '本周六晚上 8 点，陈教练将带来核心力量训练直播课，免费参加', images: [], likes: 89, comments: 15, time: '昨天', liked: false }
]

const mockDietRecommend = [
  { id: 1, name: '鸡胸肉沙拉', calories: 320, protein: 35, fat: 8, carbs: 15, type: 'lunch', image: '' },
  { id: 2, name: '全麦三明治', calories: 280, protein: 20, fat: 6, carbs: 35, type: 'breakfast', image: '' },
  { id: 3, name: '三文鱼糙米饭', calories: 450, protein: 38, fat: 12, carbs: 40, type: 'lunch', image: '' },
  { id: 4, name: '蛋白奶昔', calories: 180, protein: 30, fat: 3, carbs: 10, type: 'snack', image: '' },
  { id: 5, name: '蔬菜牛肉汤', calories: 350, protein: 28, fat: 10, carbs: 25, type: 'dinner', image: '' },
  { id: 6, name: '水果酸奶碗', calories: 200, protein: 12, fat: 4, carbs: 30, type: 'breakfast', image: '' }
]
