<template>
  <div class="leave">
    <el-card shadow="never">
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="query" class="search-bar">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 右上角提交按钮 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          提交请假申请
        </el-button>
      </div>

      <!-- 我的请假表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column align="center" header-align="center" prop="leaveTypeName" label="请假类型" width="100" />
        <el-table-column align="center" header-align="center" prop="startDate" label="开始日期" width="120" />
        <el-table-column align="center" header-align="center" prop="endDate" label="结束日期" width="120" />
        <el-table-column align="center" header-align="center" prop="days" label="天数" width="80" />
        <el-table-column align="center" header-align="center" prop="reason" label="事由" min-width="160" show-overflow-tooltip />
        <el-table-column align="center" header-align="center" label="状态" width="100">
          <template #default="{ row }">
            <!-- status: 0 待审批 / 1 已通过 / 2 已驳回 / 3 已撤销 -->
            <el-tag :type="statusTagType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" header-align="center" label="审批人" width="110">
          <template #default="{ row }">{{ row.approveUserName || '-' }}</template>
        </el-table-column>
        <el-table-column align="center" header-align="center" label="审批意见" min-width="140">
          <template #default="{ row }">{{ row.approveRemark || '-' }}</template>
        </el-table-column>
        <el-table-column align="center" header-align="center" prop="createTime" label="提交时间" min-width="170" />
        <el-table-column align="center" header-align="center" label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <!-- 只有待审批状态才能撤销 -->
            <el-button v-if="row.status === 0" type="danger" link @click="handleCancel(row)">撤销</el-button>
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

    <!-- 提交请假申请弹窗 -->
    <el-dialog v-model="dialogVisible" title="提交请假申请" width="520px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="请假日期" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="请假事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入请假事由" />
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
import { addLeave, getMyLeavePage, cancelLeave } from '@/api/leave'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

// 请假类型：1 事假 / 2 病假 / 3 年假
const typeOptions = [
  { label: '事假', value: 1 },
  { label: '病假', value: 2 },
  { label: '年假', value: 3 }
]

// 请假状态：0 待审批 / 1 已通过 / 2 已驳回 / 3 已撤销
const statusOptions = [
  { label: '待审批', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已驳回', value: 2 },
  { label: '已撤销', value: 3 }
]

// 状态对应的标签颜色
const statusTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || 'info'
}

// 查询条件 + 分页参数
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  status: ''
})

// 弹窗相关
const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  leaveType: '',
  // 日期范围，提交时拆成 startDate / endDate
  dateRange: [],
  reason: ''
})

const rules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  dateRange: [{ type: 'array', required: true, message: '请选择请假日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假事由', trigger: 'blur' }]
}

// 加载分页数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await getMyLeavePage(query)
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
  query.status = ''
  query.pageNum = 1
  loadData()
}

// 每页条数变化后回到第一页
const handleSizeChange = () => {
  query.pageNum = 1
  loadData()
}

// 打开提交弹窗
const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

// 重置表单，顺便清掉上一次的校验红字
const resetForm = () => {
  form.leaveType = ''
  form.dateRange = []
  form.reason = ''
  formRef.value?.clearValidate()
}

// 提交申请：把日期范围拆成 startDate / endDate
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await addLeave({
        leaveType: form.leaveType,
        startDate: form.dateRange[0],
        endDate: form.dateRange[1],
        reason: form.reason
      })
      ElMessage.success('提交成功，等待审批')
      dialogVisible.value = false
      loadData()
    } catch (e) {
      // 错误提示由响应拦截器统一处理
    } finally {
      submitLoading.value = false
    }
  })
}

// 撤销前二次确认
const handleCancel = (row) => {
  ElMessageBox.confirm('确定要撤销这条请假申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await cancelLeave(row.id)
      ElMessage.success('撤销成功')
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
