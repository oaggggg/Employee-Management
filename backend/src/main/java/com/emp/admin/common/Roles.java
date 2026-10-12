package com.emp.admin.common;

/**
 * 角色常量
 * <p>
 * 数据库里直接存字符串，好处是打开表就能看懂数据，不用再去查字典表。
 * 权限从高到低：ADMIN（管理员） &gt; MANAGER（部门负责人） &gt; EMPLOYEE（普通员工）
 */
public class Roles {

    /** 管理员：最高权限，可以管理部门、员工、账号、查看操作日志，并审批所有部门的请假 */
    public static final String ADMIN = "ADMIN";

    /** 部门负责人：可以审批本部门员工的请假，其余权限和普通员工一样 */
    public static final String MANAGER = "MANAGER";

    /** 普通员工：只能提交/查看自己的请假，以及维护个人信息 */
    public static final String EMPLOYEE = "EMPLOYEE";

    private Roles() {
    }
}
