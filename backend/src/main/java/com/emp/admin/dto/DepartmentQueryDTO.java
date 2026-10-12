package com.emp.admin.dto;

import lombok.Data;

/**
 * 部门列表的查询条件
 */
@Data
public class DepartmentQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 20;

    /** 部门名称，模糊查询 */
    private String name;
}
