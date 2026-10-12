package com.emp.admin.vo;

import com.emp.admin.entity.Employee;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工列表返回对象
 * <p>
 * 继承 Employee 是为了少写重复字段，自己再多加一个 deptName：
 * 员工表里只存了 dept_id，列表要直接显示部门名称，所以在 SQL 里 join 出来了。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeVO extends Employee {

    /** 部门名称（SQL join 查询出来的） */
    private String deptName;

    /** 该员工已开通的登录账号（未开通为 null，SQL join 查询出来的） */
    private String accountUsername;
}
