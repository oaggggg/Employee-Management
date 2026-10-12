<template>
  <div class="profile">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <span class="card-title">账号与员工资料</span>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ text(profile.username) }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ text(profile.realName) }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <!-- role: ADMIN 管理员 / MANAGER 部门负责人 / EMPLOYEE 普通员工 -->
          <el-tag :type="roleTagType(profile.role)">{{ roleLabel(profile.role) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="工号">{{ text(profile.empNo) }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ text(profile.deptName) }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ text(profile.position) }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ text(profile.phone) }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ text(profile.email) }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ text(profile.hireDate) }}</el-descriptions-item>
        <el-descriptions-item label="员工状态">
          <!-- employeeStatus: 1 在职 / 0 离职，未关联员工时为 null -->
          <span v-if="profile.employeeStatus === null || profile.employeeStatus === undefined">-</span>
          <el-tag v-else :type="profile.employeeStatus === 1 ? 'success' : 'info'">
            {{ profile.employeeStatus === 1 ? '在职' : '离职' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="password-card" shadow="never">
      <template #header>
        <span class="card-title">修改密码</span>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="password-form">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定修改</el-button>
          <el-button @click="handleResetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="tip">修改成功后当前登录状态会失效，需要重新登录。</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfile, updatePassword } from '@/api/profile'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const profile = ref({})

const formRef = ref(null)
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 两次密码必须一致
const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

// 空值统一显示成 -
const text = (value) => (value === null || value === undefined || value === '' ? '-' : value)

// 角色中文名：ADMIN 管理员 / MANAGER 部门负责人 / EMPLOYEE 普通员工
const roleLabel = (role) => {
  const map = { ADMIN: '管理员', MANAGER: '部门负责人', EMPLOYEE: '普通员工' }
  return map[role] || role || '-'
}

// 角色标签颜色：管理员 danger / 部门负责人 warning / 普通员工 info
const roleTagType = (role) => {
  const map = { ADMIN: 'danger', MANAGER: 'warning', EMPLOYEE: 'info' }
  return map[role] || 'info'
}

// 加载当前登录用户的资料
const loadProfile = async () => {
  loading.value = true
  try {
    profile.value = (await getProfile()) || {}
  } finally {
    loading.value = false
  }
}

const handleResetForm = () => {
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
  formRef.value?.clearValidate()
}

// 提交修改密码：成功后 token 已失效，清空登录态并回登录页
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await updatePassword({
        oldPassword: form.oldPassword,
        newPassword: form.newPassword
      })
      userStore.clearLogin()
      ElMessage.success('密码修改成功，请重新登录')
      router.push('/login')
    } catch (e) {
      // 错误提示由响应拦截器统一处理
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.card-title {
  font-weight: bold;
}

.password-card {
  margin-top: 20px;
}

.password-form {
  max-width: 460px;
}

.tip {
  font-size: 12px;
  color: #909399;
}
</style>
