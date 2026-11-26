export function durationParts(seconds) {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor(seconds / 60) % 60
  const rest = seconds % 60
  const parts = []
  if (hours) parts.push({ value: hours, unit: '小时' })
  if (minutes) parts.push({ value: minutes, unit: '分' })
  if (rest || !parts.length) parts.push({ value: rest, unit: '秒' })
  return parts
}
