package com.emp.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emp.admin.common.BusinessException;
import com.emp.admin.common.DataScopeHelper;
import com.emp.admin.common.PageResult;
import com.emp.admin.common.ResultCode;
import com.emp.admin.context.UserContext;
import com.emp.admin.dto.DepartmentQueryDTO;
import com.emp.admin.entity.Department;
import com.emp.admin.entity.Employee;
import com.emp.admin.mapper.DepartmentMapper;
import com.emp.admin.mapper.EmployeeMapper;
import com.emp.admin.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 部门业务实现
 */
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    /** 删除部门前要检查该部门下有没有员工，所以这里用到了员工 Mapper */
    private final EmployeeMapper employeeMapper;

    /** 数据权限：部门负责人查部门下拉时只返回自己所在的部门 */
    private final DataScopeHelper dataScopeHelper;

    @Override
    public PageResult<Department> pageQuery(DepartmentQueryDTO query) {
        Page<Department> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        // 第一个参数是条件：名称不为空才拼 like 条件
        wrapper.like(StringUtils.hasText(query.getName()), Department::getName, query.getName())
                .orderByDesc(Department::getId);
        return PageResult.of(page(page, wrapper));
    }

    @Override
    public List<Department> listAll() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<Department>()
                .orderByAsc(Department::getId);
        // 部门负责人只能拿到自己所在的部门（新增员工时选部门用，反正也只能填这一个）
        if (UserContext.isManager()) {
            wrapper.eq(Department::getId, dataScopeHelper.requireDeptId());
        }
        return list(wrapper);
    }

    @Override
    public void saveDepartment(Department department) {
        checkCodeUnique(department.getCode(), null);
        if (department.getStatus() == null) {
            department.setStatus(1);
        }
        save(department);
    }

    @Override
    public void updateDepartment(Department department) {
        if (department.getId() == null || getById(department.getId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "部门不存在");
        }
        checkCodeUnique(department.getCode(), department.getId());
        updateById(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDepartment(Long id) {
        Department department = getById(id);
        if (department == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "部门不存在");
        }
        // 业务规则：部门下还有员工时不允许删除，避免出现"孤儿"员工
        Long employeeCount = employeeMapper.selectCount(
                new LambdaQueryWrapper<Employee>().eq(Employee::getDeptId, id));
        if (employeeCount != null && employeeCount > 0) {
            throw new BusinessException(ResultCode.DEPT_HAS_EMPLOYEE);
        }
        // 和员工一样的原因：逻辑删除后数据行还在，部门编码的唯一索引会拦住以后重名新增，
        // 所以先把编码改名腾出来（DEV -> DEV_del_1），再逻辑删除。
        Department release = new Department();
        release.setId(id);
        release.setCode(department.getCode() + "_del_" + id);
        updateById(release);

        removeById(id);
    }

    /** 校验部门编码不能重复；修改时要排除自己 */
    private void checkCodeUnique(String code, Long excludeId) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<Department>()
                .eq(Department::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Department::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultCode.DATA_EXISTS, "部门编码已存在");
        }
    }
}
