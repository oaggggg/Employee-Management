package com.emp.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emp.admin.common.PageResult;
import com.emp.admin.dto.ChangePasswordDTO;
import com.emp.admin.dto.CreateUserDTO;
import com.emp.admin.dto.LoginDTO;
import com.emp.admin.dto.UserQueryDTO;
import com.emp.admin.entity.SysUser;
import com.emp.admin.vo.LoginVO;
import com.emp.admin.vo.ProfileVO;
import com.emp.admin.vo.UserInfoVO;
import com.emp.admin.vo.UserVO;

/**
 * 用户/登录/账号业务接口
 */
public interface SysUserService extends IService<SysUser> {

    /** 登录，成功返回 token 和用户信息 */
    LoginVO login(LoginDTO loginDTO);

    /** 获取当前登录用户信息 */
    UserInfoVO getCurrentUserInfo();

    /** 退出登录 */
    void logout();

    // ==================== 账号管理（管理员） ====================

    /** 账号分页查询 */
    PageResult<UserVO> pageQuery(UserQueryDTO query);

    /** 开通登录账号，返回新账号 id */
    Long createUser(CreateUserDTO dto);

    /** 启用 / 禁用账号（禁用后立即踢下线） */
    void updateStatus(Long id, Integer status);

    /** 重置密码（重置后立即踢下线） */
    void resetPassword(Long id, String password);

    /** 删除账号（逻辑删除） */
    void removeUser(Long id);

    // ==================== 个人中心 ====================

    /** 查看自己的账号信息与员工资料 */
    ProfileVO getProfile();

    /** 修改自己的密码（改完当前登录态失效，需要重新登录） */
    void changePassword(ChangePasswordDTO dto);

    // ==================== 供员工模块调用（账号与员工状态联动） ====================

    /** 员工离职 / 被删除时，禁用并踢出其登录账号 */
    void disableByEmployeeId(Long employeeId);

    /** 员工复职时，重新启用其登录账号 */
    void enableByEmployeeId(Long employeeId);

    /** 该员工是否已开通登录账号 */
    boolean existsByEmployeeId(Long employeeId);

    /** 查该员工已开通的登录账号，没开通返回 null */
    String getAccountUsernameByEmployeeId(Long employeeId);

    /** 让某个账号的登录态立即失效（Redis 里的 token 删掉） */
    void kickOut(Long userId);
}
