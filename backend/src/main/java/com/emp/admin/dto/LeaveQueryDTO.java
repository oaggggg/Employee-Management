package com.emp.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 请假单查询条件
 * <p>
 * employeeId / employeeIds 都由后端根据当前登录用户的角色自动填充，
 * 前端传了也不生效，避免越权查看别人的请假单。
 */
@Data
public class LeaveQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 20;

    /** 申请人（员工 id）：普通员工查自己的时候用 */
    private Long employeeId;

    /** 申请人 id 集合：部门负责人审批时限定本部门员工 */
    private List<Long> employeeIds;

    /** 状态：0 待审批，1 已通过，2 已驳回，3 已撤销 */
    private Integer status;

    /**
     * 是否排除已撤销的单子
     * <p>
     * 审批列表（管理员 / 部门负责人）固定为 true：员工撤销掉的假单不需要再给审批人看。
     * 由后端强制设置，前端传什么都会被覆盖。
     */
    private Boolean hideCancelled;

    /** 申请人姓名，模糊查询（管理员审批列表用） */
    private String employeeName;
}
