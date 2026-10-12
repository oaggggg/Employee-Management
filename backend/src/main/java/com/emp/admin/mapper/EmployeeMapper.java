package com.emp.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.emp.admin.dto.EmployeeQueryDTO;
import com.emp.admin.entity.Employee;
import com.emp.admin.vo.EmployeeVO;
import org.apache.ibatis.annotations.Param;

/**
 * 员工 Mapper
 * <p>
 * 单表的增删改查用 BaseMapper 自带的方法；
 * 需要 join 部门表的列表查询、统计查询，就自己在 XML 里写 SQL。
 */
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 员工分页查询（关联部门表）
     * <p>
     * 第一个参数传 IPage，MyBatis-Plus 的分页插件会自动在 SQL 后面拼 LIMIT，
     * 并自动执行 count 语句，结果写回 page 对象。
     */
    IPage<EmployeeVO> selectEmployeePage(IPage<EmployeeVO> page, @Param("query") EmployeeQueryDTO query);
}
