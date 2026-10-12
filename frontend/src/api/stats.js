import request from './request'

// 首页统计概览：员工总数 / 在职人数 / 部门数量 / 本月入职
export function getOverview() {
  return request.get('/stats/overview')
}
