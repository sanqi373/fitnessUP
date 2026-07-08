/**
 * 本地用户数据库（基于 localStorage 模拟 SQLite）
 * 存储账号密码信息，支持注册、登录验证
 */
const DB_KEY = 'fitness_users'

function getUsers() {
  try {
    return JSON.parse(localStorage.getItem(DB_KEY) || '[]')
  } catch {
    return []
  }
}

function saveUsers(users) {
  localStorage.setItem(DB_KEY, JSON.stringify(users))
}

/**
 * 注册新用户
 * @param {string} phone 11位手机号
 * @param {string} password 6位以上密码
 * @returns {{ success: boolean, message: string }}
 */
export function dbRegister(phone, password) {
  const users = getUsers()
  if (users.find(u => u.phone === phone)) {
    return { success: false, message: '该手机号已注册' }
  }
  users.push({ phone, password, createdAt: Date.now() })
  saveUsers(users)
  return { success: true, message: '注册成功' }
}

/**
 * 登录验证
 * @param {string} phone
 * @param {string} password
 * @returns {{ success: boolean, message: string }}
 */
export function dbLogin(phone, password) {
  const users = getUsers()
  const user = users.find(u => u.phone === phone)
  if (!user) {
    return { success: false, message: '账号不存在' }
  }
  if (user.password !== password) {
    return { success: false, message: '账号密码不匹配' }
  }
  return { success: true, message: '登录成功' }
}
