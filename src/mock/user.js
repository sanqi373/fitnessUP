/**
 * Mock 数据 —— 用户模块
 */
export const mockUser = {
  id: 1,
  nickname: '健身达人',
  avatar: '',
  phone: '138****8888',
  level: 5,
  exp: 230,
  bio: '自律给我自由',
  stats: {
    totalTrainDays: 86,
    totalTrainMinutes: 4320,
    totalCalories: 28600,
    streakDays: 7
  }
}

export const mockAchievements = [
  { id: 1, name: '初出茅庐', desc: '完成首次训练', icon: 'medal-o', unlocked: true },
  { id: 2, name: '坚持一周', desc: '连续训练7天', icon: 'fire-o', unlocked: true },
  { id: 3, name: '百炼成钢', desc: '累计训练100天', icon: 'shield-o', unlocked: false },
  { id: 4, name: '卡路里杀手', desc: '累计消耗50000大卡', icon: 'flame-o', unlocked: false },
  { id: 5, name: '全能战士', desc: '完成所有训练类别', icon: 'star-o', unlocked: false },
  { id: 6, name: '社交达人', desc: '发帖获100赞', icon: 'good-job-o', unlocked: false }
]
