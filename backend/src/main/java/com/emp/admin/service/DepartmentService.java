package com.emp.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emp.admin.common.PageResult;
import com.emp.admin.dto.DepartmentQueryDTO;
import com.emp.admin.entity.Department;

import java.util.List;

/**
 * 部门业务接口
 */
public interface DepartmentService extends IService<Department> {

    /** 分页查询 */
    PageResult<Department> pageQuery(DepartmentQueryDTO query);

    /** 查询所有部门（给员工表单的下拉框用） */
    List<Department> listAll();

    void saveDepartment(Department department);

    void updateDepartment(Department department);

    void removeDepartment(Long id);
}
