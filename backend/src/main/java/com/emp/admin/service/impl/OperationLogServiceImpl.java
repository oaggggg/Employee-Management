package com.emp.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emp.admin.common.DataScopeHelper;
import com.emp.admin.common.PageResult;
import com.emp.admin.context.UserContext;
import com.emp.admin.dto.LogQueryDTO;
import com.emp.admin.entity.OperationLog;
import com.emp.admin.mapper.OperationLogMapper;
import com.emp.admin.service.OperationLogService;
import com.emp.admin.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 操作日志业务实现
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    private final DataScopeHelper dataScopeHelper;

    @Override
    public PageResult<OperationLogVO> pageQuery(LogQueryDTO query) {
        // 数据权限：管理员看全部，部门负责人只看本部门成员的日志
        if (UserContext.isManager()) {
            query.setUserIds(dataScopeHelper.orEmptyPlaceholder(
                    dataScopeHelper.userIdsOfDept(dataScopeHelper.requireDeptId())));
        }
        Page<OperationLogVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectLogPage(page, query));
    }
}
