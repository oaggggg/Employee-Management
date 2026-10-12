package com.emp.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.emp.admin.dto.LogQueryDTO;
import com.emp.admin.entity.OperationLog;
import com.emp.admin.vo.OperationLogVO;
import org.apache.ibatis.annotations.Param;

/**
 * 操作日志 Mapper
 */
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /** 日志分页查询（关联出操作人姓名），SQL 写在 OperationLogMapper.xml 里 */
    IPage<OperationLogVO> selectLogPage(IPage<OperationLogVO> page, @Param("query") LogQueryDTO query);
}
