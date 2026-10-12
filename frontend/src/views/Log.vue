<template>
  <div class="log">
    <el-card shadow="never">
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="query" class="search-bar">
        <el-form-item label="操作人">
          <el-input v-model="query.username" placeholder="请输入操作人用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="query.module" placeholder="请选择模块" clearable style="width: 160px">
            <el-option v-for="item in moduleOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作日志表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column align="center" header-align="center" prop="createTime" label="操作时间" width="180" />
        <el-table-column align="center" header-align="center" prop="username" label="操作人" width="120" />
        <el-table-column align="center" header-align="center" label="姓名" width="100">
          <template #default="{ row }">{{ row.realName || '-' }}</template>
        </el-table-column>
        <el-table-column align="center" header-align="center" label="模块" width="110">
          <template #default="{ row }">{{ row.module || '-' }}</template>
        </el-table-column>
        <el-table-column align="center" header-align="center" label="操作" min-width="130">
          <!-- 操作名用标签展示，没有就显示 - -->
          <template #default="{ row }">
            <el-tag v-if="row.operation" effect="plain">{{ row.operation }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column align="center" header-align="center" label="结果" width="90" fixed="right">
          <template #default="{ row }">
            <!-- status: 1 成功 / 0 失败，失败时悬停显示错误信息 -->
            <el-tag v-if="row.status === 1" type="success">成功</el-tag>
            <el-tooltip v-else :content="row.errorMsg || '无错误信息'" placement="top">
              <el-tag type="danger">失败</el-tag>
            </el-tooltip>
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
import { getLogPage } from '@/api/log'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

// 操作模块下拉
const moduleOptions = ['部门管理', '员工管理', '账号管理', '请假管理', '个人中心']

// 查询条件 + 分页参数（日期范围单独放，请求时再拆成 startDate / endDate）
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  username: '',
  module: ''
})

// 操作时间范围：[开始日期, 结束日期]，没选是空数组
const dateRange = ref([])

// 组装请求参数：日期没选就不传这两个字段
const buildParams = () => {
  const [startDate, endDate] = dateRange.value || []
  return {
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    username: query.username || undefined,
    module: query.module || undefined,
    startDate: startDate || undefined,
    endDate: endDate || undefined
  }
}

// 加载分页数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await getLogPage(buildParams())
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
  query.module = ''
  dateRange.value = []
  query.pageNum = 1
  loadData()
}

// 每页条数变化后回到第一页
const handleSizeChange = () => {
  query.pageNum = 1
  loadData()
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
