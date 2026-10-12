package com.emp.admin.dto;

import lombok.Data;

/**
 * 员工列表的查询条件
 * <p>
 * 三个条件都是可选的，前端传了就作为筛选条件拼进 SQL，没传就不拼。
 */
@Data
public class EmployeeQueryDTO {

    /** 第几页，默认第一页 */
    private Integer pageNum = 1;

    /** 每页条数，默认 10 条 */
    private Integer pageSize = 20;

    /** 姓名，模糊查询 */
    private String name;

    /** 部门 id */
    private Long deptId;

    /** 状态：1 在职，0 离职 */
    private Integer status;
}
