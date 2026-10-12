import request from './request'

// 登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 退出登录
export function logout() {
  return request.post('/auth/logout')
}

// 获取当前登录用户信息
export function getInfo() {
  return request.get('/auth/info')
}
