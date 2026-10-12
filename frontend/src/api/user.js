import request from './request'

// 账号分页列表，params: { pageNum, pageSize, username, role }
export function getUserPage(params) {
  return request.get('/users/page', { params })
}

// 新增账号，data: { username, realName, role, employeeId, password }
export function addUser(data) {
  return request.post('/users', data)
}

// 启用 / 禁用账号，status: 1 启用 / 0 禁用（禁用后该账号会被立即踢下线）
export function updateUserStatus(id, status) {
  return request.put(`/users/${id}/status`, { status })
}

// 重置密码
export function resetUserPassword(id, password) {
  return request.put(`/users/${id}/password`, { password })
}

// 删除账号
export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}
