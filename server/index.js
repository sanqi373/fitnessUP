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
      code: 200,
      message: '注册成功',
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
      code: 200,
      message: '登录成功',
      data: {
        token: 'token_' + Date.now(),
        userInfo: {
          phone: rows[0].phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2'),
          nickname: rows[0].nickname || ''
        }
      }
    })
  } catch (err) {
    require('fs').appendFileSync('error.log', new Date().toISOString() + ' 登录错误: ' + err.message + '\n' + err.stack + '\n\n')
    res.status(500).json({ code: 500, message: '服务器错误: ' + err.message })
  }
})

// 获取用户信息
app.get('/user/info', async (req, res) => {
  res.json({ code: 200, message: 'success', data: {} })
})

// 更新用户信息
app.put('/user/info', async (req, res) => {
  res.json({ code: 200, message: 'success', data: req.body })
})

app.listen(3001, () => {
  console.log('Server running on http://localhost:3001')
})
