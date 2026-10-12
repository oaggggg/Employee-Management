-- =====================================================================
--  企业员工管理系统 数据库初始化脚本
--  数据库：MySQL 8.0
--  字符集：utf8mb4（支持 emoji，比 utf8 更完整）
--
--  执行方式（二选一）：
--    1) MySQL 命令行里执行：  source sql/emp_manage.sql
--    2) Windows 终端执行：
--       mysql --default-character-set=utf8mb4 -u root -p -e "source sql/emp_manage.sql"
--
--  说明：
--   1) 脚本会删除并重建这四张表，示例数据会重置
--   2) 管理员账号 admin / 123456 以及演示用的员工账号，都由后端首次启动时自动创建
--      （密码是 BCrypt 密文，交给程序生成更可靠）
-- =====================================================================

CREATE DATABASE IF NOT EXISTS emp_manage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE emp_manage;

DROP TABLE IF EXISTS sys_operation_log;
DROP TABLE IF EXISTS sys_leave;
DROP TABLE IF EXISTS sys_employee;
DROP TABLE IF EXISTS sys_department;
DROP TABLE IF EXISTS sys_user;

-- ---------------------------------------------------------------------
-- 1. 系统用户表（登录账号）
--    employee_id 关联员工表：一个员工最多一个登录账号（唯一索引，允许多个 NULL）
--    role 区分角色：ADMIN 管理员可以管部门/员工/账号/审批，EMPLOYEE 普通员工只能请假和看个人信息
-- ---------------------------------------------------------------------
CREATE TABLE sys_user
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    username    VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密后的密文）',
    real_name   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    role        VARCHAR(20)  NOT NULL DEFAULT 'EMPLOYEE' COMMENT '角色：ADMIN管理员 EMPLOYEE普通员工',
    employee_id BIGINT       DEFAULT NULL COMMENT '关联的员工id，为 NULL 表示不绑定员工',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_employee_id (employee_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户表';

-- ---------------------------------------------------------------------
-- 2. 部门表
-- ---------------------------------------------------------------------
CREATE TABLE sys_department
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    name        VARCHAR(50)  NOT NULL COMMENT '部门名称',
    code        VARCHAR(50)  NOT NULL COMMENT '部门编码',
    leader      VARCHAR(50)  DEFAULT NULL COMMENT '部门负责人',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='部门表';

-- ---------------------------------------------------------------------
-- 3. 员工表
--    dept_id 是"逻辑外键"：数据库里没有建外键约束，靠代码保证数据正确
--    （互联网项目里常见做法：不建物理外键，避免影响写入性能和维护）
-- ---------------------------------------------------------------------
CREATE TABLE sys_employee
(
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键id',
    emp_no      VARCHAR(30)    NOT NULL COMMENT '工号',
    name        VARCHAR(50)    NOT NULL COMMENT '姓名',
    gender      TINYINT        DEFAULT 1 COMMENT '性别：1男 2女',
    age         INT            DEFAULT NULL COMMENT '年龄',
    phone       VARCHAR(20)    DEFAULT NULL COMMENT '手机号',
    email       VARCHAR(50)    DEFAULT NULL COMMENT '邮箱',
    dept_id     BIGINT         DEFAULT NULL COMMENT '所属部门id',
    position    VARCHAR(50)    DEFAULT NULL COMMENT '职位',
    salary      DECIMAL(10, 2) DEFAULT NULL COMMENT '薪资',
    hire_date   DATE           DEFAULT NULL COMMENT '入职日期',
    status      TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1在职 0离职',
    create_time DATETIME       DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME       DEFAULT NULL COMMENT '更新时间',
    deleted     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_emp_no (emp_no),
    KEY idx_dept_id (dept_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='员工表';

-- ---------------------------------------------------------------------
-- 4. 请假单表（员工提交 → 管理员审批）
--    状态流转：0 待审批 → 1 已通过 / 2 已驳回，或由本人撤销为 3 已撤销
-- ---------------------------------------------------------------------
CREATE TABLE sys_leave
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    employee_id     BIGINT       NOT NULL COMMENT '申请人（员工id）',
    leave_type      TINYINT      NOT NULL COMMENT '请假类型：1事假 2病假 3年假',
    start_date      DATE         NOT NULL COMMENT '开始日期',
    end_date        DATE         NOT NULL COMMENT '结束日期',
    days            INT          NOT NULL COMMENT '请假天数',
    reason          VARCHAR(255) NOT NULL COMMENT '请假事由',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0待审批 1已通过 2已驳回 3已撤销',
    approve_user_id BIGINT       DEFAULT NULL COMMENT '审批人（系统用户id）',
    approve_time    DATETIME     DEFAULT NULL COMMENT '审批时间',
    approve_remark  VARCHAR(255) DEFAULT NULL COMMENT '审批意见',
    create_time     DATETIME     DEFAULT NULL COMMENT '提交时间',
    update_time     DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_employee_id (employee_id),
    KEY idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='请假单表';

-- ---------------------------------------------------------------------
-- 5. 操作日志表（审计留痕）
--    所有写操作（新增/修改/删除/审批/重置密码等）都会被 AOP 切面记录一条，
--    管理员可以在「操作日志」页面查看谁在什么时候做了什么。
--    username 存的是操作时的快照：就算账号以后被删除，痕迹也还在。
-- ---------------------------------------------------------------------
CREATE TABLE sys_operation_log
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    user_id     BIGINT       DEFAULT NULL COMMENT '操作人（系统用户id）',
    username    VARCHAR(50)  DEFAULT NULL COMMENT '操作人用户名（快照）',
    module      VARCHAR(50)  DEFAULT NULL COMMENT '所属模块：部门管理/员工管理/账号管理/请假管理/个人中心',
    operation   VARCHAR(50)  DEFAULT NULL COMMENT '操作类型：新增/修改/删除/审批/启用禁用/重置密码等',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '结果：1成功 0失败',
    error_msg   VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    create_time DATETIME     DEFAULT NULL COMMENT '操作时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_module (module),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='操作日志表';

-- ---------------------------------------------------------------------
-- 5. 初始化部门数据
-- ---------------------------------------------------------------------
INSERT INTO sys_department (id, name, code, leader, phone, status, create_time, update_time)
VALUES (1, '研发部', 'DEV', '王强', '010-88880001', 1, NOW(), NOW()),
       (2, '产品部', 'PRODUCT', '赵敏', '010-88880002', 1, NOW(), NOW()),
       (3, '人事部', 'HR', '杨帆', '010-88880003', 1, NOW(), NOW()),
       (4, '财务部', 'FINANCE', '周涛', '010-88880004', 1, NOW(), NOW()),
       (5, '市场部', 'MARKET', '徐磊', '010-88880005', 1, NOW(), NOW());

-- ---------------------------------------------------------------------
-- 6. 初始化员工数据（12 条）
--    最后两条入职日期用 CURDATE()，这样首页"本月入职"统计总能显示出数字
-- ---------------------------------------------------------------------
INSERT INTO sys_employee (emp_no, name, gender, age, phone, email, dept_id, position, salary, hire_date, status,
                          create_time, update_time)
VALUES ('E1001', '张伟', 1, 28, '13800138001', 'zhangwei@emp.com', 1, 'Java开发工程师', 12000.00, '2021-03-15', 1, NOW(), NOW()),
       ('E1002', '李娜', 2, 26, '13800138002', 'lina@emp.com', 1, '前端开发工程师', 11000.00, '2022-07-01', 1, NOW(), NOW()),
       ('E1003', '王强', 1, 32, '13800138003', 'wangqiang@emp.com', 1, '技术经理', 20000.00, '2019-05-20', 1, NOW(), NOW()),
       ('E1004', '赵敏', 2, 30, '13800138004', 'zhaomin@emp.com', 2, '产品经理', 18000.00, '2020-09-10', 1, NOW(), NOW()),
       ('E1005', '刘洋', 1, 27, '13800138005', 'liuyang@emp.com', 2, '产品助理', 9000.00, '2023-02-06', 1, NOW(), NOW()),
       ('E1006', '陈静', 2, 29, '13800138006', 'chenjing@emp.com', 3, '人事专员', 8000.00, '2021-11-01', 1, NOW(), NOW()),
       ('E1007', '杨帆', 1, 35, '13800138007', 'yangfan@emp.com', 3, '人事经理', 15000.00, '2018-04-02', 1, NOW(), NOW()),
       ('E1008', '周涛', 1, 31, '13800138008', 'zhoutao@emp.com', 4, '财务主管', 16000.00, '2020-06-15', 1, NOW(), NOW()),
       ('E1009', '吴倩', 2, 25, '13800138009', 'wuqian@emp.com', 4, '会计', 8500.00, '2023-08-21', 0, NOW(), NOW()),
       ('E1010', '徐磊', 1, 33, '13800138010', 'xulei@emp.com', 5, '市场经理', 17000.00, '2019-12-09', 1, NOW(), NOW()),
       ('E1011', '孙悦', 2, 24, '13800138011', 'sunyue@emp.com', 5, '市场专员', 7500.00, CURDATE(), 1, NOW(), NOW()),
       ('E1012', '马超', 1, 26, '13800138012', 'machao@emp.com', 1, '测试工程师', 10000.00, CURDATE(), 1, NOW(), NOW());

-- ---------------------------------------------------------------------
-- 7. 初始化请假单数据（3 条，覆盖 待审批 / 已驳回 两种状态）
--    日期用 CURDATE() 相对计算，保证任何时候导入都合理
-- ---------------------------------------------------------------------
INSERT INTO sys_leave (employee_id, leave_type, start_date, end_date, days, reason, status, approve_time, approve_remark,
                       create_time, update_time)
VALUES (1, 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY), 2, '家中有事，需要回老家一趟', 0, NULL, NULL, NOW(), NOW()),
       (2, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, '感冒发烧，需要休息两天', 0, NULL, NULL, NOW(), NOW()),
       (4, 3, DATE_ADD(CURDATE(), INTERVAL -5 DAY), DATE_ADD(CURDATE(), INTERVAL -3 DAY), 3, '年假出行，已提前安排好工作', 2, NOW(), '项目上线期间，暂不批假', NOW(), NOW());

-- ---------------------------------------------------------------------
-- 8. 快速自检
-- ---------------------------------------------------------------------
SELECT (SELECT COUNT(*) FROM sys_department) AS 部门数,
       (SELECT COUNT(*) FROM sys_employee)   AS 员工数,
       (SELECT COUNT(*) FROM sys_leave)      AS 请假单数,
       (SELECT COUNT(*) FROM sys_operation_log) AS 操作日志数,
       '初始化完成；登录账号由后端首次启动时自动创建（admin/123456，另有演示员工账号）' AS 提示;
