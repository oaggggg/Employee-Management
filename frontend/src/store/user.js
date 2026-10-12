import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getInfo as getInfoApi } from '@/api/auth'

// 从 localStorage 读回上次保存的用户信息（刷新页面后菜单不会闪）
const getStoredUserInfo = () => {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}') || {}
  } catch (e) {
    return {}
  }
}

// 角色 -> 登录后首页：三种角色统一进 /home，首页内部再按角色渲染不同内容
const ROLE_HOME = {
  ADMIN: '/home',
  MANAGER: '/home',
  EMPLOYEE: '/home'
}

// 用户信息 store：token 和 userInfo 都存一份到 localStorage，刷新页面不掉登录
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    // userInfo: { id, username, realName, avatar, role }，role 为 ADMIN / MANAGER / EMPLOYEE
    userInfo: getStoredUserInfo()
  }),

  getters: {
    // 是否管理员
    isAdmin: (state) => state.userInfo.role === 'ADMIN',
    // 是否部门负责人（只能审批本部门员工的请假）
    isManager: (state) => state.userInfo.role === 'MANAGER',
    // 各角色登录后的默认首页，统一为 /home
    homePath: (state) => ROLE_HOME[state.userInfo.role] || '/home'
  },

  actions: {
    // 登录：拿 token 和用户信息
    async login(form) {
      const data = await loginApi(form)
      this.token = data.token
      this.userInfo = data.userInfo || {}
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      return data
    },

    // 获取当前登录用户信息（刷新页面后用它回填）
    async getInfo() {
      const data = await getInfoApi()
      this.userInfo = data || {}
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      return data
    },

    // 只清空本地登录态（修改密码后 token 已被后端作废，不需要再调登出接口）
    clearLogin() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    // 退出登录：先通知后端，无论成功失败都清空本地登录状态
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // 后端登出接口失败不影响前端退出
      }
      this.clearLogin()
    }
  }
})
