import request from './request'

// 个人中心：当前登录账号 + 关联员工资料
export function getProfile() {
  return request.get('/profile')
}

// 修改密码，成功后后端会让当前 token 失效，前端需要重新登录
export function updatePassword(data) {
  return request.put('/profile/password', data)
}
