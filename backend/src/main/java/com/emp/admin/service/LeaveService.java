package com.emp.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emp.admin.common.PageResult;
import com.emp.admin.dto.LeaveApplyDTO;
import com.emp.admin.dto.LeaveApproveDTO;
import com.emp.admin.dto.LeaveQueryDTO;
import com.emp.admin.entity.Leave;
import com.emp.admin.vo.LeaveVO;

/**
 * 请假业务接口
 */
public interface LeaveService extends IService<Leave> {

    /** 提交请假申请（当前登录用户必须是绑定了员工的账号） */
    void apply(LeaveApplyDTO dto);

    /** 我的请假分页（只查自己的，employeeId 由后端填充，防止越权看别人的） */
    PageResult<LeaveVO> pageMy(LeaveQueryDTO query);

    /** 撤销申请（只能撤自己的，且必须是待审批状态） */
    void cancel(Long id);

    /** 全部请假单分页（管理员审批列表） */
    PageResult<LeaveVO> pageQuery(LeaveQueryDTO query);

    /** 审批（通过 / 驳回，只能在待审批状态下操作） */
    void approve(Long id, LeaveApproveDTO dto);
}
