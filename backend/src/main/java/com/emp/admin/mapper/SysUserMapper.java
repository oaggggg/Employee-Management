package com.emp.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.emp.admin.dto.UserQueryDTO;
import com.emp.admin.entity.SysUser;
import com.emp.admin.vo.UserVO;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper
 * <p>
 * 继承 BaseMapper 就自动拥有了单表的增删改查方法，不用自己写 SQL。
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 账号分页查询（关联出员工姓名、工号、部门），SQL 写在 SysUserMapper.xml 里 */
    IPage<UserVO> selectUserPage(IPage<UserVO> page, @Param("query") UserQueryDTO query);
}
