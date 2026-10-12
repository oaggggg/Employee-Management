# 企业员工管理系统

一个前后端分离的后台管理系统，系统分三种角色：**管理员、部门负责人、普通员工**，权限从高到低逐级放开；所有写操作都会留下操作日志，管理员可以查看谁在什么时候做了什么。

## 功能

- **登录认证**：账号密码登录、退出登录、获取当前用户；连续输错密码 5 次锁定 10 分钟
- **角色权限**：三种角色（管理员 / 部门负责人 / 普通员工），接口级鉴权 + 部门数据隔离：管理员管全公司，部门负责人只管自己所在的部门，普通员工只管自己的请假和个人信息。
- **部门管理**：增删改查、分页、按名称模糊搜索；部门编码唯一；部门下有员工时禁止删除
- **员工管理**：增删改查、分页、姓名 / 部门 / 状态组合查询；工号唯一；新增员工时可同时开通登录账号；员工离职或删除时自动禁用并踢出其账号
- **账号管理**：账号分页查询、启用 / 禁用、重置密码、删除；一个员工只能绑定一个账号。账号在「员工管理」里开通：新增员工时勾选"同时开通登录账号"，或对已有员工点「开通账号」
- **操作日志**：所有写操作（新增、修改、删除、审批、重置密码等）自动留痕，记录操作人、姓名、模块、操作、结果和失败原因
- **个人中心**：查看自己的账号信息与员工资料、修改密码
- **请假审批**：员工提交、查看、撤销自己的请假；管理员和部门负责人审批（通过 / 驳回），状态流转为 待审批 → 已通过 / 已驳回 / 已撤销
- **首页统计**：员工总数、在职人数、部门数量、本月入职人数、待审批请假数量 —— 管理员看全公司，部门负责人看本部门
- **通用能力**：统一响应结果、全局异常处理、参数校验、登录与权限拦截、跨域、逻辑删除、字段自动填充

## 技术栈

后端使用 JDK 17、Spring Boot 3、MyBatis-Plus 3、MySQL 8、Redis 7、JWT 0.12、Spring Security Crypto 6（BCrypt 密码加密）、
Hibernate Validator 8、HikariCP 5，构建工具为 Maven 3；
前端使用 Vue 3、Element Plus 2、Vue Router 4、Pinia 2、Axios 1，构建工具为 Vite 5。

## 项目结构

```
emp-admin
├── backend                          # 后端 Spring Boot 工程
│   └── src/main
│       ├── java/com/emp/admin
│       │   ├── annotation           # @RequireAdmin / @RequireManager 权限注解、@LogOperation 日志注解
│       │   ├── aspect               # 操作日志切面（AOP 统一留痕）
│       │   ├── common               # 统一返回结果、状态码、分页结果、异常、枚举常量
│       │   ├── config               # 分页插件、字段自动填充、拦截器与跨域、启动初始化账号
│       │   ├── interceptor          # 登录校验 + 角色权限校验拦截器
│       │   ├── context              # ThreadLocal 保存当前登录用户与角色
│       │   ├── util                 # JWT 工具类
│       │   ├── entity / mapper      # 实体与 Mapper（复杂查询写在 XML 里）
│       │   ├── dto / vo             # 接收参数对象 / 返回对象
│       │   ├── service / impl       # 登录、部门、员工、账号、请假、日志业务逻辑
│       │   └── controller           # 接口层
│       └── resources
│           ├── application.yml      # 数据源、Redis、MyBatis-Plus、JWT 配置
│           └── mapper               # MyBatis XML（动态条件、关联查询、in 查询）
├── frontend                         # 前端 Vue3 工程
│   └── src
│       ├── api                      # axios 实例、拦截器、各模块接口
│       ├── router                   # 路由与登录 + 角色守卫
│       ├── store                    # Pinia：token、用户信息、角色
│       ├── layout                   # 侧边菜单（按角色渲染）+ 顶部栏
│       └── views                    # 登录、首页、部门、员工、账号、操作日志、个人中心、我的请假、请假审批
└── sql/emp_manage.sql               # 建库建表脚本 + 示例数据（5 个部门、12 名员工、3 条请假单）
```

## 快速启动

### 1. 环境准备

JDK 17、Maven 3、MySQL 8、Redis、Node.js 18 以上。

### 2. 初始化数据库

```bash
mysql --default-character-set=utf8mb4 -u root -p -e "source sql/emp_manage.sql"
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

数据库连接、Redis 地址在 `backend/src/main/resources/application.yml` 中按本机情况修改。
也可以先 `mvn clean package -DskipTests` 打包，再 `java -jar target/emp-admin-1.0.0.jar` 启动。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端已配置代理，`/api` 请求会转发到后端。

## 账号与地址

| 项目 | 值 |
| --- | --- |
| 前端地址 | http://localhost:5173 |
| 后端地址 | http://localhost:8080 |
| 管理员账号 | admin / 123456 |
| 部门负责人账号 | E1003 / 123456（王强，研发部） |
| 普通员工账号 | E1001 / 123456（张伟，研发部） |

三个账号都由后端首次启动时自动创建，不需要手工往数据库里插数据：

- `admin`：管理员，登录后能看到全部菜单
- `E1003`：研发部负责人，登录后能审批研发部员工的请假
- `E1001`：普通员工，只能提交和查看自己的请假

另外，每个部门的负责人都会按部门表里的负责人姓名自动开通一个账号（用户名是工号，初始密码同样是 123456）。

## 接口说明

所有接口统一返回 `{ code, message, data }`，业务结果看 `code`，200 表示成功。
除登录接口外，其余接口都需要在请求头携带 `Authorization: Bearer <token>`。
标记「仅管理员」的接口只有管理员能访问，标记「管理层」的接口管理员和部门负责人可以访问，其它角色访问会返回 `403`。

### 认证

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录，返回 token 与用户信息 |
| POST | `/api/auth/logout` | 退出登录 |
| GET | `/api/auth/info` | 获取当前登录用户 |

### 部门管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/departments/page` | 分页查询，支持按名称模糊搜索 |
| GET | `/api/departments/list` | 部门列表 |
| POST | `/api/departments` | 新增部门 |
| PUT | `/api/departments/{id}` | 修改部门 |
| DELETE | `/api/departments/{id}` | 删除部门 |

### 员工管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/employees/page` | 分页查询，支持姓名、部门、状态组合条件 |
| GET | `/api/employees/{id}` | 员工详情，含已开通的登录账号 |
| POST | `/api/employees` | 新增员工，可同时开通登录账号 |
| PUT | `/api/employees/{id}` | 修改员工，改为离职会自动禁用其账号 |
| DELETE | `/api/employees/{id}` | 删除员工，同时禁用其账号 |

### 账号管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/users/page` | 分页查询 |
| POST | `/api/users` | 开通账号 |
| PUT | `/api/users/{id}/status` | 启用 / 禁用账号 |
| PUT | `/api/users/{id}/password` | 重置密码 |
| DELETE | `/api/users/{id}` | 删除账号 |

### 操作日志

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/logs/page` | 分页查询，支持按操作人、模块、日期区间筛选 |

### 个人中心

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/profile` | 查看自己的账号信息与员工资料 |
| PUT | `/api/profile/password` | 修改密码 |

### 请假

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/leaves` | 提交请假申请 |
| GET | `/api/leaves/my` | 我的请假列表，支持按状态筛选 |
| PUT | `/api/leaves/{id}/cancel` | 撤销申请 |

### 请假审批

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/leaves/page` | 请假单列表：管理员看全部，部门负责人只看本部门 |
| PUT | `/api/leaves/{id}/approve` | 审批：1 通过 / 2 驳回 |

### 统计

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/stats/overview` | 管理员返回全公司，部门负责人返回本部门，普通员工只统计自己 |

### 响应格式

```json
{ "code": 200, "message": "操作成功", "data": { "records": [], "total": 12, "pageNum": 1, "pageSize": 10 } }
```

状态码：`200` 成功、`400` 参数错误、`401` 未登录或登录过期、`403` 没有操作权限、`1001` 用户名或密码错误、
`1002` 密码错误次数过多已锁定、`1003` 账号已禁用、`1004` 数据已存在、`1005` 部门下有员工不能删除、
`1006` 请假单当前状态不允许该操作、`1007` 该员工已开通登录账号、`1008` 原密码不正确、`1009` 当前账号未关联员工、`1010` 结束日期不能早于开始日期。
