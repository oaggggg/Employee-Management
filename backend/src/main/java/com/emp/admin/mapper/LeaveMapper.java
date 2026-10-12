package com.emp.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.emp.admin.dto.LeaveQueryDTO;
import com.emp.admin.entity.Leave;
import com.emp.admin.vo.LeaveVO;
import org.apache.ibatis.annotations.Param;

/**
 * 请假单 Mapper
 * <p>
 * 请假列表要关联员工、部门和审批人，所以 SQL 写在 XML 里。
 */
public interface LeaveMapper extends BaseMapper<Leave> {

    /**
     * 请假单分页查询
     * <p>
     * 同一个方法既支持"我的请假"（传 employeeId），
     * 也支持管理员的审批列表（不传 employeeId、可传状态和员工姓名）。
     */
    IPage<LeaveVO> selectLeavePage(IPage<LeaveVO> page, @Param("query") LeaveQueryDTO query);
}
