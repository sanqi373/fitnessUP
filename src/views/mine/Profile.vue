<template>
  <div class="page-container profile-page">
    <van-nav-bar title="个人信息" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- 头像区 -->
    <div class="avatar-section">
      <div class="avatar-wrapper">
        <van-icon name="user-circle-o" size="72" color="#fff" />
      </div>
      <span class="avatar-hint">点击更换头像</span>
    </div>

    <!-- 信息表单 -->
    <div class="info-form">
      <van-field
        v-model="form.nickname"
        label="昵称"
        placeholder="请输入昵称"
        :readonly="!editing"
        :border="false"
      />
      <van-field
        v-model="form.phone"
        label="手机号"
        placeholder="请输入手机号"
        :readonly="!editing"
        :border="false"
      />
      <van-field
        v-model="form.bio"
        label="个性签名"
        placeholder="介绍一下自己"
        type="textarea"
        rows="2"
        autosize
        :readonly="!editing"
        :border="false"
      />
      <van-field
        v-model="form.gender"
        label="性别"
        placeholder="请选择性别"
        :readonly="!editing"
        :border="false"
        is-link
        @click="editing && (showGenderPicker = true)"
      />
      <van-field
        v-model="form.birthday"
        label="生日"
        placeholder="请选择生日"
        :readonly="!editing"
        :border="false"
        is-link
        @click="editing && (showDatePicker = true)"
      />
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <van-button
        v-if="!editing"
        type="primary"
        block
        round
        @click="startEdit"
      >
        编辑资料
      </van-button>
      <template v-else>
        <van-button type="primary" block round @click="saveProfile">保存</van-button>
        <van-button block round plain class="cancel-btn" @click="cancelEdit">取消</van-button>
      </template>
    </div>

    <!-- 性别选择器 -->
    <van-action-sheet
      v-model:show="showGenderPicker"
      :actions="genderOptions"
      cancel-text="取消"
      @select="onGenderSelect"
    />

    <!-- 日期选择器 -->
    <van-popup v-model:show="showDatePicker" position="bottom" round>
      <van-date-picker
        v-model="currentDate"
        title="选择生日"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const editing = ref(false)
const showGenderPicker = ref(false)
const showDatePicker = ref(false)

// 初始化表单数据
const form = reactive({
  nickname: userStore.userInfo?.nickname || '',
  phone: userStore.userInfo?.phone || '',
  bio: userStore.userInfo?.bio || '',
  gender: userStore.userInfo?.gender || '',
  birthday: userStore.userInfo?.birthday || ''
})

// 备份原始数据，用于取消编辑时恢复
const backup = reactive({ ...form })

const genderOptions = [
  { name: '男', value: '男' },
  { name: '女', value: '女' },
  { name: '保密', value: '保密' }
]

const currentDate = ref(form.birthday ? form.birthday.split('-').map(Number) : [2000, 1, 1])
const minDate = new Date(1950, 0, 1)
const maxDate = new Date()

function startEdit() {
  Object.assign(backup, form)
  editing.value = true
}

function cancelEdit() {
  Object.assign(form, backup)
  editing.value = false
}

function saveProfile() {
  if (!form.nickname.trim()) {
    showToast('昵称不能为空')
    return
  }
  // 更新 store
  userStore.setUserInfo({
    ...userStore.userInfo,
    nickname: form.nickname,
    phone: form.phone,
    bio: form.bio,
    gender: form.gender,
    birthday: form.birthday
  })
  showToast('保存成功')
  editing.value = false
}

function onGenderSelect(action) {
  form.gender = action.value
  showGenderPicker.value = false
}

function onDateConfirm({ selectedValues }) {
  const [year, month, day] = selectedValues
  form.birthday = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  showDatePicker.value = false
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--color-bg, #f7f8fa);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 0 20px;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
}

.avatar-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 8px;
}

.info-form {
  margin: 12px 16px;
  border-radius: 8px;
  overflow: hidden;
}

.action-bar {
  padding: 24px 16px;
}

.cancel-btn {
  margin-top: 12px;
}
</style>
