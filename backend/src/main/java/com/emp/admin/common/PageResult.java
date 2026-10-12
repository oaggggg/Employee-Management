package com.emp.admin.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页结果
 * <p>
 * 为什么不直接把 MyBatis-Plus 的 Page 返回给前端？
 * 因为 Page 里有一堆前端用不到的字段（orders、optimizeCountSql...），
 * 而且以后换分页插件时前端不用跟着改，接口更稳定。
 *
 * @param <T> 列表数据类型
 */
@Data
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> records;

    /** 总条数 */
    private Long total;

    /** 当前第几页 */
    private Long pageNum;

    /** 每页多少条 */
    private Long pageSize;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        return result;
    }
}
