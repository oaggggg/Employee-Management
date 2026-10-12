<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="title">员工管理系统</div>
      <div class="subtitle">欢迎登录</div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable>
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="tip">
        <div>测试账号：</div>
        <div>管理员：admin / 123456</div>
        <div>部门负责人：E1003 / 123456</div>
        <div>普通员工：E1001 / 123456</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

// 用户名、密码都必填
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  // 先做表单校验，校验通过才会进到登录请求
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(form)
      ElMessage.success('登录成功')
      // 三种角色登录后统一进首页，首页内容再按角色区分
      router.push(userStore.homePath)
    } catch (e) {
      // 错误提示已在 axios 响应拦截器里统一处理
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #1f3a5f 0%, #304156 100%);
}

.login-card {
  width: 400px;
  padding: 10px 20px;
}

.title {
  text-align: center;
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}

.subtitle {
  margin: 8px 0 24px;
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.login-btn {
  width: 100%;
}

.tip {
  text-align: center;
  font-size: 12px;
  color: #909399;
}
</style>
