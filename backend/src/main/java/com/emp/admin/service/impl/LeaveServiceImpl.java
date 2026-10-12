package com.emp.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emp.admin.common.BusinessException;
import com.emp.admin.common.DataScopeHelper;
import com.emp.admin.common.LeaveStatus;
import com.emp.admin.common.LeaveType;
import com.emp.admin.common.PageResult;
import com.emp.admin.common.ResultCode;
import com.emp.admin.context.UserContext;
import com.emp.admin.dto.LeaveApplyDTO;
import com.emp.admin.dto.LeaveApproveDTO;
import com.emp.admin.dto.LeaveQueryDTO;
import com.emp.admin.entity.Leave;
import com.emp.admin.mapper.LeaveMapper;
import com.emp.admin.service.LeaveService;
import com.emp.admin.vo.LeaveVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 请假业务实现
 * <p>
 * 状态流转：0 待审批 →（管理员审批）→ 1 已通过 / 2 已驳回；0 待审批 →（本人撤销）→ 3 已撤销
 */
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl extends ServiceImpl<LeaveMapper, Leave> implements LeaveService {

    /** 数据权限统一走 DataScopeHelper，请假模块不再自己写一套 */
    private final DataScopeHelper dataScopeHelper;

    @Override
    public void apply(LeaveApplyDTO dto) {
        Long employeeId = currentEmployeeId();
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException(ResultCode.LEAVE_DATE_ERROR);
        }
        // 天数按日历天数算（含首尾两天）。这里没有扣周末和法定节假日，
        // 真实项目里会接工作日历或考勤规则，属于可以继续完善的点。
        int days = (int) (ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1);

        Leave leave = new Leave();
        leave.setEmployeeId(employeeId);
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setDays(days);
        leave.setReason(dto.getReason());
        leave.setStatus(LeaveStatus.PENDING.getCode());
        save(leave);
    }

    @Override
    public PageResult<LeaveVO> pageMy(LeaveQueryDTO query) {
        // 强制只查自己的请假单，前端就算传了 employeeId 也会被覆盖掉
        query.setEmployeeId(currentEmployeeId());
        return doPage(query);
    }

    @Override
    public void cancel(Long id) {
        Leave leave = getById(id);
        if (leave == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "请假单不存在");
        }
        if (!Objects.equals(leave.getEmployeeId(), currentEmployeeId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能撤销自己的请假申请");
        }
        if (!Objects.equals(leave.getStatus(), LeaveStatus.PENDING.getCode())) {
            throw new BusinessException(ResultCode.LEAVE_STATUS_ERROR);
        }
        Leave update = new Leave();
        update.setId(id);
        update.setStatus(LeaveStatus.CANCELED.getCode());
        updateById(update);
    }

    @Override
    public PageResult<LeaveVO> pageQuery(LeaveQueryDTO query) {
        // 数据权限：管理员看全部、部门负责人看本部门、普通员工只能看自己的
        applyDataScope(query);
        // 审批列表不展示员工已经撤销的请假单（撤销掉的不需要再给人审批，员工自己在"我的请假"里能看到）；
        // 这里强制覆盖，前端传 hideCancelled=false 也不生效
        query.setHideCancelled(true);
        return doPage(query);
    }

    /** 真正执行分页查询，并把类型名、状态名补上 */
    private PageResult<LeaveVO> doPage(LeaveQueryDTO query) {
        Page<LeaveVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<LeaveVO> result = baseMapper.selectLeavePage(page, query);
        // 把类型名和状态名补上，前端直接展示，不用自己维护一份字典
        result.getRecords().forEach(vo -> {
            vo.setLeaveTypeName(LeaveType.nameOf(vo.getLeaveType()));
            vo.setStatusName(LeaveStatus.nameOf(vo.getStatus()));
        });
        return PageResult.of(result);
    }

    /**
     * 列表数据权限控制（统一委托给 DataScopeHelper）
     * <p>
     * 管理员：不加限制，能看全公司
     * 部门负责人：只能看本部门员工的请假单（把本部门员工 id 集合塞进查询条件）
     * 其它角色：只能看自己的
     */
    private void applyDataScope(LeaveQueryDTO query) {
        if (UserContext.isAdmin()) {
            return;
        }
        if (UserContext.isManager()) {
            query.setEmployeeIds(dataScopeHelper.orEmptyPlaceholder(
                    dataScopeHelper.employeeIdsOfDept(dataScopeHelper.requireDeptId())));
            return;
        }
        query.setEmployeeId(currentEmployeeId());
    }

    @Override
    public void approve(Long id, LeaveApproveDTO dto) {
        Leave leave = getById(id);
        if (leave == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "请假单不存在");
        }
        // 数据权限：部门负责人只能审批本部门的请假单
        checkApproveScope(leave);
        // 只有待审批的才能审批，避免重复审批或审批已撤销的单子
        if (!Objects.equals(leave.getStatus(), LeaveStatus.PENDING.getCode())) {
            throw new BusinessException(ResultCode.LEAVE_STATUS_ERROR);
        }
        Leave update = new Leave();
        update.setId(id);
        update.setStatus(dto.getStatus());
        update.setApproveUserId(UserContext.getUserId());
        update.setApproveTime(LocalDateTime.now());
        update.setApproveRemark(dto.getApproveRemark());
        updateById(update);
    }

    /**
     * 取当前登录账号绑定的员工 id
     * <p>
     * 请假单是挂在员工身上的，所以没绑定员工的账号（比如纯管理员账号）不能请假。
     */
    private Long currentEmployeeId() {
        Long employeeId = dataScopeHelper.currentEmployeeIdOrNull();
        if (employeeId == null) {
            throw new BusinessException(ResultCode.NO_EMPLOYEE_BOUND);
        }
        return employeeId;
    }

    /** 部门负责人只能审批本部门员工的请假单，管理员不受限制 */
    private void checkApproveScope(Leave leave) {
        if (UserContext.isAdmin()) {
            return;
        }
        if (!UserContext.isManager()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 复用统一的数据权限校验：请假单的申请人必须属于当前负责人管理的部门
        dataScopeHelper.checkEmployeeInMyDept(leave.getEmployeeId());
    }
}
