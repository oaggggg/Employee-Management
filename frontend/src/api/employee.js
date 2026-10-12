import request from './request'

// 员工分页列表，params: { pageNum, pageSize, name, deptId, status }
export function getEmployeePage(params) {
  return request.get('/employees/page', { params })
}

// 员工详情（编辑时回显用）
export function getEmployeeById(id) {
  return request.get(`/employees/${id}`)
}

// 新增员工
export function addEmployee(data) {
  return request.post('/employees', data)
}

// 修改员工
export function updateEmployee(id, data) {
  return request.put(`/employees/${id}`, data)
}

// 删除员工
export function deleteEmployee(id) {
  return request.delete(`/employees/${id}`)
}
