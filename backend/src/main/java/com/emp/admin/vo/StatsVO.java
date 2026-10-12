package com.emp.admin.vo;

import lombok.Data;

/**
 * 首页统计数据
 */
@Data
public class StatsVO {

    /** 员工总数 */
    private Long employeeCount;

    /** 在职人数 */
    private Long onJobCount;

    /** 部门数量 */
    private Long departmentCount;

    /** 本月入职人数 */
    private Long monthHireCount;

    /** 待审批的请假单数量 */
    private Long pendingLeaveCount;
}
