export const mockTrainPlans = [
  {
    id: 1,
    name: '上肢力量突破',
    type: 'strength',
    level: '中级',
    duration: 45,
    calories: 350,
    cover: '',
    desc: '针对胸、背、肩、手臂的综合力量训练',
    exercises: [
      { name: '俯卧撑', sets: 4, reps: 15, rest: 60, video: '' },
      { name: '哑铃弯举', sets: 4, reps: 12, rest: 45, video: '' },
      { name: '哑铃飞鸟', sets: 3, reps: 12, rest: 45, video: '' },
      { name: '三头臂屈伸', sets: 3, reps: 15, rest: 45, video: '' }
    ]
  },
  {
    id: 2,
    name: '核心燃脂 HIIT',
    type: 'hiit',
    level: '高级',
    duration: 30,
    calories: 400,
    cover: '',
    desc: '高强度间歇训练，快速燃脂塑形',
    exercises: [
      { name: '开合跳', sets: 4, reps: 40, rest: 30, video: '' },
      { name: '波比跳', sets: 4, reps: 12, rest: 45, video: '' },
      { name: '登山跑', sets: 3, reps: 30, rest: 30, video: '' },
      { name: '平板支撑', sets: 3, reps: 1, rest: 30, duration: 60, video: '' }
    ]
  },
  {
    id: 3,
    name: '下肢塑形计划',
    type: 'strength',
    level: '初级',
    duration: 40,
    calories: 300,
    cover: '',
    desc: '深蹲、箭步蹲等下肢训练，打造完美腿型',
    exercises: [
      { name: '自重深蹲', sets: 4, reps: 20, rest: 60, video: '' },
      { name: '箭步蹲', sets: 3, reps: 15, rest: 45, video: '' },
      { name: '臀桥', sets: 4, reps: 15, rest: 45, video: '' },
      { name: '提踵', sets: 3, reps: 25, rest: 30, video: '' }
    ]
  },
  {
    id: 4,
    name: '晨间瑜伽拉伸',
    type: 'yoga',
    level: '初级',
    duration: 20,
    calories: 120,
    cover: '',
    desc: '唤醒身体的清晨瑜伽序列',
    exercises: [
      { name: '山式站立', sets: 1, reps: 1, rest: 0, duration: 60, video: '' },
      { name: '下犬式', sets: 1, reps: 1, rest: 0, duration: 60, video: '' },
      { name: '猫牛式', sets: 1, reps: 10, rest: 0, video: '' },
      { name: '婴儿式', sets: 1, reps: 1, rest: 0, duration: 120, video: '' }
    ]
  },
  {
    id: 5,
    name: '5 公里跑步计划',
    type: 'cardio',
    level: '中级',
    duration: 35,
    calories: 380,
    cover: '',
    desc: '从热身到冲刺的完整跑步训练',
    exercises: [
      { name: '慢跑热身', duration: 300, video: '' },
      { name: '匀速跑', duration: 1200, video: '' },
      { name: '间歇冲刺', sets: 5, reps: 1, rest: 90, duration: 60, video: '' },
      { name: '慢跑放松', duration: 300, video: '' }
    ]
  }
]
