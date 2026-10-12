package com.emp.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emp.admin.common.PageResult;
import com.emp.admin.dto.LogQueryDTO;
import com.emp.admin.entity.OperationLog;
import com.emp.admin.vo.OperationLogVO;

/**
 * 操作日志业务接口
 */
public interface OperationLogService extends IService<OperationLog> {

    /** 操作日志分页查询（管理员查看） */
    PageResult<OperationLogVO> pageQuery(LogQueryDTO query);
}
