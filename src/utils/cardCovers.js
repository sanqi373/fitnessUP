const trainingCovers = {
  上肢力量突破: 'upper-body',
  '核心燃脂 HIIT': 'core-hiit',
  下肢塑形计划: 'lower-body',
  晨间瑜伽拉伸: 'morning-yoga',
  '5公里跑步计划': 'distance-run',
  哑铃推拉交替: 'dumbbell',
  臀腿单侧控制: 'single-leg',
  弹力带背部训练: 'resistance-band',
  无跳跃间歇: 'low-impact',
  拳击步伐间歇: 'boxing',
  跳绳节奏间歇: 'jump-rope',
  全身短间歇: 'full-body',
  桌边肩颈瑜伽: 'seated-stretch',
  髋部舒展流动: 'hip-mobility',
  站姿平衡瑜伽: 'balance',
  睡前垫上舒缓: 'mat-stretch',
  公园快走循环: 'park-walk',
  动感单车节奏骑: 'studio-cycle',
  走跑交替入门: 'walk-run',
  室内有氧踏步: 'indoor-cardio'
}

const categoryCovers = { strength: 'upper-body', hiit: 'core-hiit', yoga: 'morning-yoga', cardio: 'distance-run' }

export function trainingCover(plan) {
  const filename = trainingCovers[plan.name] || categoryCovers[plan.type] || 'full-body'
  return `/training/${filename}.webp`
}

const recipeCovers = {
  鸡胸肉沙拉: '/recipes/chicken-salad.webp',
  全麦三明治: '/recipes/wholewheat-sandwich.webp',
  三文鱼糙米饭: '/recipes/salmon-rice.webp',
  蛋白奶昔: '/recipes/protein-shake.webp',
  蔬菜牛肉汤: '/recipes/vegetable-beef-soup.webp',
  水果酸奶碗: '/recipes/fruit-yogurt.webp'
}

export function recipeCover(food) {
  return recipeCovers[food.name] || ''
}
