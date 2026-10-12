<template>
  <div class="employee">
    <el-card shadow="never">
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="query" class="search-bar">
        <el-form-item label="姓名">
          <el-input v-model="query.name" placeholder="请输入姓名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="query.deptId" placeholder="请选择部门" clearable style="width: 180px">
            <el-option v-for="dept in deptList" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 右上角新增按钮：管理员和部门负责人均可见 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增员工
        </el-button>
      </div>

      <!-- 员工表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column align="center" header-align="center" prop="empNo" label="工号" width="110" />
        <el-table-column align="center" header-align="center" prop="name" label="姓名" width="100" />
        <el-table-column align="center" header-align="center" label="性别" width="80">
          <template #default="{ row }">
            <!-- gender: 1 男 / 2 女 -->
            <el-tag :type="row.gender === 1 ? 'primary' : 'danger'">
              {{ row.gender === 1 ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" header-align="center" prop="age" label="年龄" width="70" />
        <el-table-column align="center" header-align="center" prop="phone" label="手机号" width="130" />
        <el-table-column align="center" header-align="center" prop="deptName" label="部门" width="120" />
        <el-table-column align="center" header-align="center" prop="position" label="职位" width="120" />
        <el-table-column align="center" header-align="center" prop="salary" label="薪资" width="100" />
        <el-table-column align="center" header-align="center" prop="hireDate" label="入职日期" width="120" />
        <el-table-column align="center" header-align="center" label="状态" width="90">
          <template #default="{ row }">
            <!-- status: 1 在职 / 0 离职 -->
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" header-align="center" label="登录账号" width="120">
          <template #default="{ row }">
            <!-- accountUsername：该员工已开通的登录账号，未开通为 null -->
            <span v-if="row.accountUsername">{{ row.accountUsername }}</span>
            <span v-else class="unopened">未开通</span>
          </template>
        </el-table-column>
        <el-table-column align="center" header-align="center" label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <!-- 管理员和部门负责人均可编辑/开通账号/删除，后端按部门限制范围 -->
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <!-- 未开通账号的员工才会显示开通入口 -->
            <el-button v-if="!row.accountUsername" type="success" link @click="handleOpenAccount(row)">
              开通账号
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="loadData"
      />
    </el-card>

    <!-- 新增 / 编辑共用一个弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工号" prop="empNo">
              <!-- 编辑时工号不允许修改 -->
              <el-input v-model="form.empNo" placeholder="请输入工号" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="18" :max="65" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-select v-model="form.deptId" placeholder="请选择部门" style="width: 100%">
                <el-option v-for="dept in deptList" :key="dept.id" :label="dept.name" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="form.position" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资" prop="salary">
              <el-input-number v-model="form.salary" :min="0" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker
                v-model="form.hireDate"
                type="date"
                placeholder="请选择入职日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">在职</el-radio>
                <el-radio :value="0">离职</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <!-- 已开通账号的员工只做只读提示，不再显示开通选项 -->
          <el-col :span="24" v-if="form.accountUsername">
            <el-form-item label="登录账号">
              <span class="opened-tip">已开通账号：{{ form.accountUsername }}</span>
            </el-form-item>
          </el-col>
          <template v-else>
            <el-col :span="12">
              <el-form-item label="开通账号">
                <el-switch v-model="form.openAccount" active-text="同时开通登录账号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="账号角色">
                <el-select v-model="form.accountRole" :disabled="!form.openAccount" style="width: 100%">
                  <el-option label="普通员工" value="EMPLOYEE" />
                  <el-option label="部门负责人" value="MANAGER" />
                  <el-option label="管理员" value="ADMIN" />
                </el-select>
              </el-form-item>
            </el-col>
          </template>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 单独给已有员工开通登录账号 -->
    <el-dialog v-model="accountDialogVisible" title="开通登录账号" width="480px">
      <el-form ref="accountFormRef" :model="accountForm" :rules="accountRules" label-width="80px">
        <el-form-item label="员工">
          <span>{{ accountForm.realName }}（{{ accountForm.empNo }}）</span>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="accountForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="accountForm.role" style="width: 100%">
            <el-option label="普通员工" value="EMPLOYEE" />
            <el-option label="部门负责人" value="MANAGER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="accountDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="accountSubmitLoading" @click="handleAccountSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getEmployeePage,
  getEmployeeById,
  addEmployee,
  updateEmployee,
  deleteEmployee
} from '@/api/employee'
import { getDepartmentList } from '@/api/department'
import { addUser } from '@/api/user'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const deptList = ref([])
const total = ref(0)

// 查询条件 + 分页参数
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  name: '',
  deptId: '',
  status: ''
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增员工')
const formRef = ref(null)
const form = reactive({
  id: null,
  empNo: '',
  name: '',
  gender: 1,
  age: 25,
  phone: '',
  email: '',
  deptId: '',
  position: '',
  salary: 0,
  hireDate: '',
  status: 1,
  // 同时开通登录账号相关
  openAccount: true,
  accountRole: 'EMPLOYEE',
  // 该员工已开通的登录账号，未开通为 null
  accountUsername: null
})

// 开通账号弹窗
const accountDialogVisible = ref(false)
const accountSubmitLoading = ref(false)
const accountFormRef = ref(null)
const accountForm = reactive({
  employeeId: null,
  realName: '',
  empNo: '',
  username: '',
  role: 'EMPLOYEE'
})

const accountRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 是否编辑状态（编辑时工号只读）
const isEdit = computed(() => !!form.id)

// 编辑前的员工状态，用来判断有没有从在职改成离职
const originalStatus = ref(1)

// 手机号用正则校验
const rules = {
  empNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

// 加载分页数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await getEmployeePage(query)
    tableData.value = data?.records || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

// 部门下拉数据（搜索栏和表单共用）
const loadDeptList = async () => {
  deptList.value = (await getDepartmentList()) || []
}

const handleSearch = () => {
  query.pageNum = 1
  loadData()
}

const handleReset = () => {
  query.name = ''
  query.deptId = ''
  query.status = ''
  query.pageNum = 1
  loadData()
}

// 每页条数变化后回到第一页
const handleSizeChange = () => {
  query.pageNum = 1
  loadData()
}

// 新增：清空表单
const handleAdd = () => {
  dialogTitle.value = '新增员工'
  resetForm()
  dialogVisible.value = true
}

// 编辑：先查详情再回显
const handleEdit = async (row) => {
  dialogTitle.value = '编辑员工'
  const data = await getEmployeeById(row.id)
  resetForm()
  form.id = data.id
  form.empNo = data.empNo
  form.name = data.name
  form.gender = data.gender
  form.age = data.age
  form.phone = data.phone
  form.email = data.email
  form.deptId = data.deptId
  form.position = data.position
  form.salary = data.salary
  form.hireDate = data.hireDate
  form.status = data.status
  // 已开通账号的员工只显示只读提示；未开通的编辑时默认不勾选开通
  form.accountUsername = data.accountUsername || null
  form.openAccount = false
  form.accountRole = 'EMPLOYEE'
  originalStatus.value = data.status
  dialogVisible.value = true
}

// 重置表单，并清掉上一次的校验红字
const resetForm = () => {
  form.id = null
  form.empNo = ''
  form.name = ''
  form.gender = 1
  form.age = 25
  form.phone = ''
  form.email = ''
  form.deptId = ''
  form.position = ''
  form.salary = 0
  form.hireDate = ''
  form.status = 1
  form.openAccount = true
  form.accountRole = 'EMPLOYEE'
  form.accountUsername = null
  originalStatus.value = 1
  formRef.value?.clearValidate()
}

// 提交：有 id 是修改，没有是新增
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = {
        empNo: form.empNo,
        name: form.name,
        gender: form.gender,
        age: form.age,
        phone: form.phone,
        email: form.email,
        deptId: form.deptId,
        position: form.position,
        salary: form.salary,
        hireDate: form.hireDate,
        status: form.status
      }
      // 已开通账号的员工不再提交开通参数，避免重复开通
      if (!form.accountUsername) {
        data.openAccount = form.openAccount
        data.accountRole = form.accountRole
      }
      // 判断是不是从在职改成了离职，用来决定提交后要不要提示
      const toResign = isEdit.value && originalStatus.value === 1 && form.status === 0
      if (form.id) {
        await updateEmployee(form.id, data)
        ElMessage.success('修改成功')
      } else {
        await addEmployee(data)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      // 改为离职后后端会自动禁用该员工的登录账号
      if (toResign) {
        ElMessage.warning('该员工登录账号已被禁用')
      }
      loadData()
    } catch (e) {
      // 错误提示由响应拦截器统一处理
    } finally {
      submitLoading.value = false
    }
  })
}

// 开通账号：默认用户名填工号
const handleOpenAccount = (row) => {
  accountForm.employeeId = row.id
  accountForm.realName = row.name
  accountForm.empNo = row.empNo
  accountForm.username = row.empNo
  accountForm.role = 'EMPLOYEE'
  accountFormRef.value?.clearValidate()
  accountDialogVisible.value = true
}

// 调用新增账号接口给员工开通登录账号
const handleAccountSubmit = () => {
  accountFormRef.value.validate(async (valid) => {
    if (!valid) return
    accountSubmitLoading.value = true
    try {
      await addUser({
        username: accountForm.username,
        realName: accountForm.realName,
        role: accountForm.role,
        employeeId: accountForm.employeeId
      })
      ElMessage.success('账号开通成功，初始密码 123456')
      accountDialogVisible.value = false
      loadData()
    } catch (e) {
      // 错误提示由响应拦截器统一处理
    } finally {
      accountSubmitLoading.value = false
    }
  })
}

// 删除前二次确认
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除员工「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deleteEmployee(row.id)
      ElMessage.success('删除成功')
      // 删掉当前页最后一条时往前翻一页
      if (tableData.value.length === 1 && query.pageNum > 1) {
        query.pageNum -= 1
      }
      loadData()
    })
    .catch(() => {})
}

onMounted(() => {
  // 部门下拉：管理员拿到全量，部门负责人只拿到自己管理的部门
  loadDeptList()
  loadData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 4px;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

/* 未开通账号的灰色提示 */
.unopened {
  color: #909399;
}

.opened-tip {
  color: #67c23a;
}
</style>
