import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 从本地缓存的用户信息里读角色。这里不 import store，
// 避免 router -> store -> api/auth -> api/request -> router 的循环依赖
const getLocalRole = () => {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')?.role || ''
  } catch (e) {
    return ''
  }
}

// 登录后统一进首页，三种角色看到的首页内容不同
const homePath = () => '/home'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    // 布局页：左侧菜单 + 顶部栏 + 内容区
    path: '/',
    component: () => import('@/layout/index.vue'),
    // 默认重定向到首页
    redirect: () => homePath(),
    children: [
      {
        path: 'home',
        name: 'home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', roles: ['ADMIN', 'MANAGER', 'EMPLOYEE'] }
      },
      {
        path: 'department',
        name: 'department',
        component: () => import('@/views/Department.vue'),
        meta: { title: '部门管理', roles: ['ADMIN'] }
      },
      {
        path: 'employee',
        name: 'employee',
        component: () => import('@/views/Employee.vue'),
        meta: { title: '员工管理', roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'user',
        name: 'user',
        component: () => import('@/views/User.vue'),
        meta: { title: '账号管理', roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'log',
        name: 'log',
        component: () => import('@/views/Log.vue'),
        meta: { title: '操作日志', roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'leave-audit',
        name: 'leaveAudit',
        component: () => import('@/views/LeaveAudit.vue'),
        meta: { title: '请假审批', roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'leave',
        name: 'leave',
        component: () => import('@/views/Leave.vue'),
        meta: { title: '我的请假', roles: ['MANAGER', 'EMPLOYEE'] }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', roles: ['ADMIN', 'MANAGER', 'EMPLOYEE'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：登录校验 + 角色权限校验
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')

  // 没登录：只能去登录页
  if (!token) {
    return to.path === '/login' ? next() : next('/login')
  }

  // 有 token 但本地还没缓存用户信息（比如老会话），先补拉一次
  let role = getLocalRole()
  if (!role) {
    try {
      // 动态引入 store，避免循环依赖
      const { useUserStore } = await import('@/store/user')
      const info = await useUserStore().getInfo()
      role = info?.role || ''
    } catch (e) {
      // 拉取失败（token 失效等）已由 axios 拦截器统一处理
    }
    // 拉取过程中 token 被清掉了（登录过期），直接回登录页
    if (!localStorage.getItem('token')) {
      if (to.path === '/login') return next()
      return next('/login')
    }
  }

  const home = homePath()

  // 已登录还想去登录页 → 直接回各自首页
  if (to.path === '/login') {
    return next(home)
  }

  // 角色不在该路由允许的 meta.roles 里 → 回各自首页并提示
  const roles = to.meta.roles
  if (roles && role && !roles.includes(role)) {
    ElMessage.warning('没有权限访问该页面')
    return next(home)
  }

  next()
})

// 切换路由时同步浏览器标签标题
router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 员工管理系统` : '员工管理系统'
})

export default router
