<template>
  <el-container class="layout">
    <!-- 左侧菜单 -->
    <el-aside width="220px" class="aside">
      <div class="logo">员工管理系统</div>
      <!-- 菜单按角色渲染：管理员 7 项、部门负责人 7 项、普通员工 3 项 -->
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="page-title">{{ route.meta.title }}</div>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            欢迎，{{ userStore.userInfo.realName || userStore.userInfo.username || '用户' }}
            <!-- 角色标签：管理员红色，部门负责人橙色，普通员工灰色 -->
            <el-tag :type="roleTag.type" size="small">
              {{ roleTag.label }}
            </el-tag>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前路由路径，用来高亮对应的菜单项
const activeMenu = computed(() => route.path)

// 管理员菜单（7 项，没有「我的请假」）
const adminMenus = [
  { path: '/home', title: '首页', icon: 'House' },
  { path: '/department', title: '部门管理', icon: 'OfficeBuilding' },
  { path: '/employee', title: '员工管理', icon: 'User' },
  { path: '/user', title: '账号管理', icon: 'Avatar' },
  { path: '/log', title: '操作日志', icon: 'Document' },
  { path: '/leave-audit', title: '请假审批', icon: 'DocumentChecked' },
  { path: '/profile', title: '个人中心', icon: 'Setting' }
]

// 部门负责人菜单（7 项）：本部门相关的员工 / 账号 / 日志 + 审批 + 自己的请假
const managerMenus = [
  { path: '/home', title: '首页', icon: 'House' },
  { path: '/employee', title: '员工管理', icon: 'User' },
  { path: '/user', title: '账号管理', icon: 'Avatar' },
  { path: '/log', title: '操作日志', icon: 'Document' },
  { path: '/leave-audit', title: '请假审批', icon: 'DocumentChecked' },
  { path: '/leave', title: '我的请假', icon: 'Calendar' },
  { path: '/profile', title: '个人中心', icon: 'Setting' }
]

// 普通员工菜单（3 项）
const employeeMenus = [
  { path: '/home', title: '首页', icon: 'House' },
  { path: '/leave', title: '我的请假', icon: 'Calendar' },
  { path: '/profile', title: '个人中心', icon: 'Setting' }
]

// 菜单按角色渲染
const menus = computed(() => {
  if (userStore.isAdmin) return adminMenus
  return userStore.isManager ? managerMenus : employeeMenus
})

// 顶栏角色标签：管理员 danger / 部门负责人 warning / 普通员工 info
const roleTag = computed(() => {
  const map = {
    ADMIN: { type: 'danger', label: '管理员' },
    MANAGER: { type: 'warning', label: '部门负责人' },
    EMPLOYEE: { type: 'info', label: '普通员工' }
  }
  return map[userStore.userInfo.role] || { type: 'info', label: '普通员工' }
})

// 刷新页面后一般能从 localStorage 回填 userInfo，这里兜底再拉一次
onMounted(() => {
  if (!userStore.userInfo.username) {
    userStore.getInfo().catch(() => {})
  }
})

// 下拉菜单只有"退出登录"一项
const handleCommand = async (command) => {
  if (command !== 'logout') return
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3648;
}

.aside :deep(.el-menu) {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.page-title {
  font-size: 16px;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #409eff;
  outline: none;
}

.main {
  background-color: #f0f2f5;
}
</style>
