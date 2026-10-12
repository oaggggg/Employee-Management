<template>
  <div class="leave-audit">
    <el-card shadow="never">
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="query" class="search-bar">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="员工姓名">
          <el-input v-model="query.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 请假申请表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column align="center" header-align="center" prop="employeeName" label="申请人" width="100" />
        <el-table-column align="center" header-align="center" prop="empNo" label="工号" width="110" />
        <el-table-column align="center" header-align="center" prop="deptName" label="部门" width="120" />
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
        <el-table-column align="center" header-align="center" prop="createTime" label="提交时间" min-width="170" />
        <el-table-column align="center" header-align="center" label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <!-- 只有待审批状态才能审批 -->
            <template v-if="row.status === 0">
              <el-button type="success" link @click="handleAudit(row, 1)">通过</el-button>
              <el-button type="danger" link @click="handleAudit(row, 2)">驳回</el-button>
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

    <!-- 审批弹窗 -->
    <el-dialog v-model="dialogVisible" title="请假审批" width="600px">
      <!-- 顶部只读展示这条申请 -->
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="申请人">{{ current.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ current.empNo }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ current.deptName }}</el-descriptions-item>
        <el-descriptions-item label="请假类型">{{ current.leaveTypeName }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ current.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ current.endDate }}</el-descriptions-item>
        <el-descriptions-item label="天数">{{ current.days }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ current.createTime }}</el-descriptions-item>
        <el-descriptions-item label="事由" :span="2">{{ current.reason }}</el-descriptions-item>
      </el-descriptions>

      <el-form class="audit-form" :model="auditForm" :rules="rules" ref="auditFormRef" label-width="90px">
        <el-form-item label="审批结果" prop="status">
          <el-radio-group v-model="auditForm.status">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见" prop="approveRemark">
          <el-input v-model="auditForm.approveRemark" type="textarea" :rows="3" placeholder="请输入审批意见" />
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
import { ElMessage } from 'element-plus'
import { getLeavePage, approveLeave } from '@/api/leave'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

// 状态筛选选项：后端审批列表已排除员工撤销的请假单，故不再提供「已撤销」筛选项
// （status 3 已撤销仍可能出现在列表数据中，statusTagType 保留其标签色）
const statusOptions = [
  { label: '待审批', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已驳回', value: 2 }
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
  status: '',
  employeeName: ''
})

// 审批弹窗
const dialogVisible = ref(false)
const auditFormRef = ref(null)
const current = ref({})
const auditForm = reactive({
  id: null,
  status: 1,
  approveRemark: ''
})

const rules = {
  status: [{ required: true, message: '请选择审批结果', trigger: 'change' }]
}

// 加载分页数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await getLeavePage(query)
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
  query.employeeName = ''
  query.pageNum = 1
  loadData()
}

// 每页条数变化后回到第一页
const handleSizeChange = () => {
  query.pageNum = 1
  loadData()
}

// 打开审批弹窗，默认选中点进来的那个结果
const handleAudit = (row, status) => {
  current.value = row
  auditForm.id = row.id
  auditForm.status = status
  auditForm.approveRemark = ''
  auditFormRef.value?.clearValidate()
  dialogVisible.value = true
}

// 提交审批
const handleSubmit = () => {
  auditFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await approveLeave(auditForm.id, {
        status: auditForm.status,
        approveRemark: auditForm.approveRemark
      })
      ElMessage.success(auditForm.status === 1 ? '已通过' : '已驳回')
      dialogVisible.value = false
      loadData()
    } catch (e) {
      // 错误提示由响应拦截器统一处理
    } finally {
      submitLoading.value = false
    }
  })
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

.audit-form {
  margin-top: 20px;
}

.text-muted {
  color: #909399;
}
</style>
