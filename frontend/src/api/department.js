import request from './request'

// 部门列表（不分页，员工页面的部门下拉框用它）
export function getDepartmentList() {
  return request.get('/departments/list')
}

// 部门分页列表，params: { pageNum, pageSize, name }
export function getDepartmentPage(params) {
  return request.get('/departments/page', { params })
}

// 新增部门
export function addDepartment(data) {
  return request.post('/departments', data)
}

// 修改部门
export function updateDepartment(id, data) {
  return request.put(`/departments/${id}`, data)
}

// 删除部门
export function deleteDepartment(id) {
  return request.delete(`/departments/${id}`)
}
