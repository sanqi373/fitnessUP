<template>
  <div class="page-container">
    <van-nav-bar title="设置" left-text="返回" left-arrow @click-left="$router.back()" />
    <van-cell title="个人信息" is-link />
    <van-cell title="隐私设置" is-link />
    <van-cell title="通知管理" is-link />
    <van-cell title="清除缓存" is-link />
    <van-cell title="退出登录" is-link class="logout-cell" />
  </div>
</template>
<style scoped>
.logout-cell { margin-top: 16px; }
</style>