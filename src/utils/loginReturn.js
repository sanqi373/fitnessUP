export function loginReturnPath(value, router) {
  if (typeof value !== 'string' || !value.startsWith('/') || value.startsWith('//') || value.includes('\\')) return '/home'
  const target = router.resolve(value)
  return target.matched.length && !target.meta.guest && !target.matched.some(record => record.redirect)
    ? target.fullPath : '/home'
}
