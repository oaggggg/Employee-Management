import request from './request'

// 操作日志分页（仅管理员）
// params: { pageNum, pageSize, username, module, startDate, endDate }
export function getLogPage(params) {
  return request.get('/logs/page', { params })
}
