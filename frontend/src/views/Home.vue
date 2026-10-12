<template>
  <div class="home">
    <!-- 统计卡片：管理员看全部数据，部门负责人看本部门数据，普通员工不展示卡片 -->
    <el-row v-if="showCards" :gutter="20">
      <el-col :span="4" v-for="item in cardList" :key="item.label" class="card-col">
        <el-card shadow="hover" :class="{ 'card-clickable': item.path }" @click="handleCardClick(item)">
          <div class="card-body">
            <el-icon class="card-icon" :style="{ backgroundColor: item.color }">
              <component :is="item.icon" />
            </el-icon>
            <div class="card-text">
              <div class="card-value">{{ item.value }}</div>
              <div class="card-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 欢迎文字：三种角色文案不同 -->
    <el-card class="welcome" shadow="never">
      <div class="welcome-title">{{ welcomeTitle }}</div>
      <p class="welcome-text">{{ welcomeText }}</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOverview } from '@/api/stats'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

// 只有管理员和部门负责人展示统计卡片，普通员工只看欢迎语
const showCards = computed(() => userStore.isAdmin || userStore.isManager)

// 欢迎语按角色区分
const welcomeTitle = computed(() => {
  if (userStore.isAdmin) return '欢迎回来，系统管理员'
  if (userStore.isManager) return '欢迎回来，这里是您管理的部门概况'
  return '欢迎回来'
})

const welcomeText = computed(() => {
  if (userStore.isAdmin) {
    return '本系统用于管理企业部门与员工信息。左侧菜单可以进入「部门管理」维护部门数据，进入「员工管理」维护员工档案，支持按姓名、部门、在职状态等条件查询，以及新增、编辑、删除操作。'
  }
  if (userStore.isManager) {
    return '您当前是部门负责人，可以查看本部门的部门信息、账号信息和操作日志，并审批本部门员工的请假申请，以上范围由系统自动限定。'
  }
  return '欢迎使用员工管理系统。左侧菜单可以进入「我的请假」提交和查看自己的请假申请，在「个人中心」查看个人资料并修改密码。'
})

// 五个统计卡片的配置：图标、颜色、文案、数值字段、点击跳转路径
const cardList = ref([
  { label: '员工总数', value: 0, icon: 'User', color: '#409eff', field: 'employeeCount' },
  { label: '在职人数', value: 0, icon: 'CircleCheck', color: '#67c23a', field: 'onJobCount' },
  { label: '部门数量', value: 0, icon: 'OfficeBuilding', color: '#e6a23c', field: 'departmentCount' },
  { label: '本月入职', value: 0, icon: 'Calendar', color: '#f56c6c', field: 'monthHireCount' },
  {
    label: '待审批请假',
    value: 0,
    icon: 'Clock',
    color: '#909399',
    field: 'pendingLeaveCount',
    // 点击跳到请假审批页面
    path: '/leave-audit'
  }
])

// 只有配置了 path 的卡片可以点击跳转
const handleCardClick = (item) => {
  if (item.path) {
    router.push(item.path)
  }
}

// 进入首页时拉一次统计数据（普通员工没有卡片，不需要请求）
onMounted(async () => {
  if (!showCards.value) return
  const data = await getOverview()
  cardList.value.forEach((item) => {
    item.value = data?.[item.field] ?? 0
  })
})
</script>

<style scoped>
/* 5 张卡片平分一行，覆盖 el-col-4 的固定宽度 */
.card-col {
  flex: 1 1 0;
  max-width: none;
}

.card-clickable {
  cursor: pointer;
}

.card-body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  font-size: 28px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.card-label {
  font-size: 14px;
  color: #909399;
}

.welcome {
  margin-top: 20px;
}

/* 普通员工没有卡片时，欢迎卡片不需要上边距 */
.home > .welcome:first-child {
  margin-top: 0;
}

.welcome-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.welcome-text {
  margin: 0;
  line-height: 1.8;
  color: #606266;
}
</style>
