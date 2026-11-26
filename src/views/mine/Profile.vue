<template>
  <div class="page-container profile-page">

    <!-- 头像区 -->
    <button class="avatar-section" type="button" :disabled="!ready || saving || readingAvatar" @click="avatarInput.click()">
      <span class="avatar-wrapper">
        <img v-if="form.avatar" :src="form.avatar" alt="当前头像" />
        <van-icon v-else name="user-o" size="42" color="#397253" />
      </span>
      <span class="avatar-hint">点击更换头像</span>
    </button>
    <input ref="avatarInput" type="file" accept="image/png,image/jpeg,image/webp" hidden @change="selectAvatar" />

    <!-- 信息表单 -->
    <div class="info-form">
      <van-field
        v-model="form.nickname"
        label="昵称"
        placeholder="请输入昵称"
        maxlength="50"
        :readonly="!editing || saving"
        :border="false"
      />
      <van-field
        v-model="form.phone"
        label="手机号"
        placeholder="请输入手机号"
        type="tel"
        maxlength="11"
        :readonly="!editing || saving"
        :border="false"
      />
      <van-cell
        tag="button"
        type="button"
        title="性别"
        :value="genderLabel || '请选择性别'"
        class="picker-row"
        :class="{ unset: !genderLabel }"
        :disabled="!ready || saving"
        :border="false"
        is-link
        @click="openGenderPicker"
      />
      <van-cell
        tag="button"
        type="button"
        title="生日"
        :value="form.birthday || '请选择生日'"
        class="picker-row"
        :class="{ unset: !form.birthday }"
        :disabled="!ready || saving"
        :border="false"
        is-link
        @click="openDatePicker"
      />
      <van-field
        v-model="form.bio"
        label="个性签名"
        placeholder="介绍一下自己"
        type="textarea"
        rows="2"
        autosize
        :readonly="!editing || saving"
        :border="false"
      />
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <van-button
        v-if="!editing"
        type="primary"
        block
        round
        :loading="loading"
        @click="ready ? startEdit() : loadProfile()"
      >
        {{ ready ? '编辑资料' : '重新加载资料' }}
      </van-button>
      <template v-else>
        <van-button type="primary" block round :loading="saving" :disabled="readingAvatar" @click="saveProfile">保存</van-button>
        <van-button block round plain class="cancel-btn" :disabled="saving || readingAvatar" @click="cancelEdit">取消</van-button>
      </template>
    </div>

    <!-- 性别选择器 -->
    <van-action-sheet
      v-model:show="showGenderPicker"
      :actions="genderOptions"
      title="选择性别"
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
import { computed, ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ActionSheet as VanActionSheet, DatePicker as VanDatePicker, Popup as VanPopup, showToast } from 'vant'
import { getUserInfo, updateUserInfo } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()
const ready = ref(false)
const loading = ref(false)
const avatarInput = ref(null)
const readingAvatar = ref(false)
let profileSession = ''

const editing = ref(false)
const saving = ref(false)
const showGenderPicker = ref(false)
const showDatePicker = ref(false)

const genderOptions = [
  { name: '男', value: 1 },
  { name: '女', value: 2 },
  { name: '保密', value: 0 }
]
const emptyForm = { nickname: '', phone: '', bio: '', gender: undefined, birthday: '', avatar: '' }
const form = reactive({ ...emptyForm })

// 备份原始数据，用于取消编辑时恢复
const backup = reactive({ ...form })

const genderLabel = computed(() => genderOptions.find(option => option.value === form.gender)?.name)

const currentDate = ref([])
const minDate = new Date(1900, 0, 1)
const maxDate = new Date()

async function loadProfile() {
  const session = userStore.getSessionToken()
  profileSession = ''
  ready.value = false
  editing.value = false
  showGenderPicker.value = false
  showDatePicker.value = false
  Object.assign(form, emptyForm)
  if (!session) {
    router.replace({ path: '/login', query: { redirect: '/mine/profile' } })
    return
  }
  loading.value = true
  try {
    const { data } = await getUserInfo()
    if (session !== userStore.getSessionToken()) return
    const cached = userStore.userInfo
    for (const key of Object.keys(emptyForm)) form[key] = data[key] ?? emptyForm[key]
    // 旧版本的签名只在本机，保留同一账号的草稿，供用户保存。
    if (data.bio == null && cached?.id === data.id) form.bio = cached.bio || ''
    userStore.setUserInfo({ ...data, bio: form.bio })
    profileSession = session
    ready.value = true
  } catch {
    // 由请求拦截器提示错误，可在页面重试。
  } finally {
    if (session === userStore.token) loading.value = false
  }
}

watch(() => userStore.token, loadProfile, { immediate: true })

async function selectAvatar(event) {
  const [file] = event.target.files
  event.target.value = ''
  if (!file || !ready.value || saving.value || readingAvatar.value) return
  const session = userStore.getSessionToken()
  if (!session || session !== profileSession) return
  readingAvatar.value = true
  try {
    const bitmap = await createImageBitmap(file)
    try {
      if (session !== userStore.getSessionToken()) return
      const canvas = document.createElement('canvas')
      canvas.width = canvas.height = 192
      const side = Math.min(bitmap.width, bitmap.height)
      canvas.getContext('2d').drawImage(bitmap, (bitmap.width - side) / 2, (bitmap.height - side) / 2,
        side, side, 0, 0, canvas.width, canvas.height)
      startEdit()
      form.avatar = canvas.toDataURL('image/png')
    } finally {
      bitmap.close()
    }
  } catch {
    showToast('图片读取失败，请重新选择')
  } finally {
    readingAvatar.value = false
  }
}

function startEdit() {
  if (editing.value) return
  Object.assign(backup, form)
  editing.value = true
}

function cancelEdit() {
  Object.assign(form, backup)
  editing.value = false
}

async function saveProfile() {
  if (!ready.value || saving.value || readingAvatar.value) return
  if (!form.nickname.trim()) {
    showToast('昵称不能为空')
    return
  }
  const session = userStore.getSessionToken()
  if (!session || session !== profileSession) return
  saving.value = true
  try {
    const { data } = await updateUserInfo({
      nickname: form.nickname.trim(),
      phone: form.phone.trim(),
      bio: form.bio,
      avatar: form.avatar === backup.avatar ? undefined : form.avatar,
      gender: form.gender,
      birthday: form.birthday || undefined
    })
    if (userStore.getSessionToken() !== session) return
    for (const key of Object.keys(emptyForm)) form[key] = data[key] ?? emptyForm[key]
    userStore.setUserInfo(data)
    showToast('保存成功')
    editing.value = false
  } catch {
    // 请求拦截器显示错误，保留编辑内容供重试。
  } finally {
    saving.value = false
  }
}

function openGenderPicker() {
  startEdit()
  showGenderPicker.value = true
}

function openDatePicker() {
  startEdit()
  currentDate.value = (form.birthday || '2000-01-01').split('-')
  showDatePicker.value = true
}

function onGenderSelect(action) {
  form.gender = action.value
  showGenderPicker.value = false
}

function onDateConfirm({ selectedValues }) {
  form.birthday = selectedValues.join('-')
  showDatePicker.value = false
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--color-bg, #f7f8fa);
}

.avatar-section {
  width: 100%;
  border: 0;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 0 20px;
  background: linear-gradient(110deg, #eaf7d9, #d7f2dc 48%, #ecfaf3);
}

.avatar-wrapper {
  overflow: hidden;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f4ffe7, #ade6bd);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-hint {
  font-size: 12px;
  color: #476b57;
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

.picker-row {
  appearance: none;
  border: 0;
  border-radius: 0;
  text-align: left;
  cursor: pointer;
}

.picker-row:focus-visible {
  outline-offset: -2px;
}

.picker-row :deep(.van-cell__title) {
  flex: none;
  width: var(--van-field-label-width);
  margin-right: var(--van-field-label-margin-right);
}

.picker-row :deep(.van-cell__value) {
  color: var(--color-text);
  text-align: left;
}

.picker-row.unset :deep(.van-cell__value) {
  color: var(--van-field-placeholder-text-color, var(--van-text-color-3));
}

.cancel-btn {
  margin-top: 12px;
}
</style>
