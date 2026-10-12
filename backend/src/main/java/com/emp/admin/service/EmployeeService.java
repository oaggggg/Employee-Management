package com.emp.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emp.admin.common.PageResult;
import com.emp.admin.dto.EmployeeQueryDTO;
import com.emp.admin.entity.Employee;
import com.emp.admin.vo.EmployeeVO;

/**
 * 员工业务接口
 */
public interface EmployeeService extends IService<Employee> {

    /** 分页 + 条件查询（带部门名称） */
    PageResult<EmployeeVO> pageQuery(EmployeeQueryDTO query);

    /** 查询员工详情 */
    EmployeeVO getDetail(Long id);

    void saveEmployee(Employee employee);

    void updateEmployee(Employee employee);

    void removeEmployee(Long id);
}
