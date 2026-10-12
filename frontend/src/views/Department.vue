<template>
  <div class="department">
    <el-card shadow="never">
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="query" class="search-bar">
        <el-form-item label="部门名称">
          <el-input v-model="query.name" placeholder="请输入部门名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 右上角新增按钮：仅管理员可新增部门 -->
      <div class="toolbar">
        <el-button v-if="userStore.isAdmin" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增部门
        </el-button>
      </div>

      <!-- 部门表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column align="center" header-align="center" prop="id" label="ID" width="80" />
        <el-table-column align="center" header-align="center" prop="name" label="部门名称" min-width="140" />
        <el-table-column align="center" header-align="center" prop="code" label="部门编码" min-width="120" />
        <el-table-column align="center" header-align="center" prop="leader" label="负责人" min-width="100" />
        <el-table-column align="center" header-align="center" prop="phone" label="电话" min-width="130" />
        <el-table-column align="center" header-align="center" label="状态" width="90">
          <template #default="{ row }">
            <!-- status: 1 启用 / 0 禁用 -->
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" header-align="center" prop="createTime" label="创建时间" min-width="170" />
        <el-table-column align="center" header-align="center" label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <!-- 管理员可编辑/删除，部门负责人只读，用 - 占位 -->
            <template v-if="userStore.isAdmin">
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            </template>
            <span v-else class="text-muted">-</span>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDepartmentPage,
  addDepartment,
  updateDepartment,
  deleteDepartment
} from '@/api/department'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

// 查询条件 + 分页参数
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  name: ''
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增部门')
const formRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  code: '',
  leader: '',
  phone: '',
  status: 1
})

// 名称、编码必填
const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入部门编码', trigger: 'blur' }]
}

// 加载分页数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await getDepartmentPage(query)
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
  query.name = ''
  query.pageNum = 1
  loadData()
}

// 每页条数变化后回到第一页
const handleSizeChange = () => {
  query.pageNum = 1
  loadData()
}

// 打开弹窗并清空表单（新增）
const handleAdd = () => {
  dialogTitle.value = '新增部门'
  resetForm()
  dialogVisible.value = true
}

// 打开弹窗并回显数据（编辑）
const handleEdit = (row) => {
  dialogTitle.value = '编辑部门'
  form.id = row.id
  form.name = row.name
  form.code = row.code
  form.leader = row.leader
  form.phone = row.phone
  form.status = row.status
  dialogVisible.value = true
}

// 重置表单，顺便清掉上一次的校验红字
const resetForm = () => {
  form.id = null
  form.name = ''
  form.code = ''
  form.leader = ''
  form.phone = ''
  form.status = 1
  formRef.value?.clearValidate()
}

// 提交：有 id 是修改，没有是新增
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = {
        name: form.name,
        code: form.code,
        leader: form.leader,
        phone: form.phone,
        status: form.status
      }
      if (form.id) {
        await updateDepartment(form.id, data)
        ElMessage.success('修改成功')
      } else {
        await addDepartment(data)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) {
      // 错误提示由响应拦截器统一处理
    } finally {
      submitLoading.value = false
    }
  })
}

// 删除前二次确认
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除部门「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deleteDepartment(row.id)
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

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.text-muted {
  color: #909399;
}
</style>
