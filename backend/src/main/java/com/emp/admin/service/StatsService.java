package com.emp.admin.service;

import com.emp.admin.vo.StatsVO;

/**
 * 首页统计业务接口
 */
public interface StatsService {

    /** 首页四个统计数字 */
    StatsVO overview();
}
