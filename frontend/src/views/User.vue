<template>
  <div class="user">
    <el-card shadow="never">
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="query" class="search-bar">
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="请选择角色" clearable style="width: 150px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="部门负责人" value="MANAGER" />
            <el-option label="普通员工" value="EMPLOYEE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 账号表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column align="center" header-align="center" prop="id" label="ID" width="80" />
        <el-table-column align="center" header-align="center" prop="username" label="用户名" min-width="120" />
        <el-table-column align="center" header-align="center" prop="realName" label="姓名" min-width="100" />
        <el-table-column align="center" header-align="center" label="角色" width="120">
          <template #default="{ row }">
            <!-- role: ADMIN 管理员 / MANAGER 部门负责人 / EMPLOYEE 普通员工 -->
            <el-tag :type="roleTagType(row.role)">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" header-align="center" label="关联员工" min-width="150">
          <template #default="{ row }">
            <span v-if="row.employeeName">{{ row.employeeName }}（{{ row.empNo }}）</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column align="center" header-align="center" label="所属部门" min-width="120">
          <template #default="{ row }">
            <span>{{ row.deptName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" header-align="center" label="状态" width="90">
          <template #default="{ row }">
            <!-- status: 1 启用 / 0 禁用 -->
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" header-align="center" prop="createTime" label="创建时间" min-width="170" />
        <el-table-column align="center" header-align="center" label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
            <!-- 删除对管理员和部门负责人均可见，后端只允许删本部门账号 -->
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserPage,
  updateUserStatus,
  resetUserPassword,
  deleteUser
} from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

// 角色中文名：ADMIN 管理员 / MANAGER 部门负责人 / EMPLOYEE 普通员工
const roleLabel = (role) => {
  const map = { ADMIN: '管理员', MANAGER: '部门负责人', EMPLOYEE: '普通员工' }
  return map[role] || role || '-'
}

// 角色标签颜色：管理员 danger / 部门负责人 warning / 普通员工 info
const roleTagType = (role) => {
  const map = { ADMIN: 'danger', MANAGER: 'warning', EMPLOYEE: 'info' }
  return map[role] || 'info'
}

// 查询条件 + 分页参数
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  username: '',
  role: ''
})

// 加载分页数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await getUserPage(query)
    tableData.value = data?.records || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  loadData()
}

const handleReset = () => {
  query.username = ''
  query.role = ''
  query.pageNum = 1
  loadData()
}

// 每页条数变化后回到第一页
const handleSizeChange = () => {
  query.pageNum = 1
  loadData()
}

// 启用 / 禁用前二次确认
const handleToggleStatus = (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const action = nextStatus === 1 ? '启用' : '禁用'
  const tip = nextStatus === 0 ? '禁用后该账号会被立即踢下线。' : ''
  ElMessageBox.confirm(`确定要${action}账号「${row.username}」吗？${tip}`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await updateUserStatus(row.id, nextStatus)
      ElMessage.success(`${action}成功`)
      loadData()
    })
    .catch(() => {})
}

// 重置密码：二次确认后统一重置为 123456，不需要输入
const handleResetPassword = (row) => {
  ElMessageBox.confirm(`确定把账号「${row.username}」的密码重置为 123456 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await resetUserPassword(row.id, '123456')
      ElMessage.success('密码已重置为 123456')
      loadData()
    })
    .catch(() => {})
}

// 删除前二次确认
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除账号「${row.username}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deleteUser(row.id)
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
  loadData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 4px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.text-muted {
  color: #909399;
}
</style>
