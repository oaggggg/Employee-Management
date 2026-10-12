import request from './request'

// 提交请假申请，data: { leaveType, startDate, endDate, reason }
export function addLeave(data) {
  return request.post('/leaves', data)
}

// 我的请假分页，params: { pageNum, pageSize, status }
export function getMyLeavePage(params) {
  return request.get('/leaves/my', { params })
}

// 撤销请假（只有本人且待审批状态才能撤销）
export function cancelLeave(id) {
  return request.put(`/leaves/${id}/cancel`)
}

// 请假审批分页（管理员），params: { pageNum, pageSize, status, employeeName }
export function getLeavePage(params) {
  return request.get('/leaves/page', { params })
}

// 审批，data: { status: 1 通过 / 2 驳回, approveRemark }
export function approveLeave(id, data) {
  return request.put(`/leaves/${id}/approve`, data)
}
