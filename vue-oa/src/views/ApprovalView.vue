<template>
  <div class="app-container">
    <PageHeader title="审批">
      <template #actions>
        <div class="action-buttons">
          <i class="ri-search-line"></i>
          <i class="ri-more-2-fill"></i>
        </div>
      </template>
    </PageHeader>

    <!-- 审批标签页 -->
    <div class="approval-tabs">
      <div 
        v-for="tab in tabs" 
        :key="tab.value" 
        :class="['tab-item', { active: currentTab === tab.value }]"
        @click="currentTab = tab.value"
      >
        {{ tab.label }}
        <span v-if="tab.count" class="tab-badge">{{ tab.count }}</span>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <div 
        v-for="category in categories" 
        :key="category.value" 
        :class="['filter-item', { active: currentCategory === category.value }]"
        @click="currentCategory = category.value"
      >
        {{ category.label }}
      </div>
    </div>

    <!-- 待我审批 -->
    <div v-if="currentTab === 'pending'" class="approval-list">
      <div 
        v-for="(approval, index) in pendingApprovals" 
        :key="index" 
        class="approval-card"
        v-show="currentCategory === 'all' || approval.type === currentCategory"
      >
        <div class="approval-header">
          <div class="approval-type">{{ getApprovalTypeLabel(approval.type) }}</div>
          <div :class="['approval-status', getStatusClass(approval.status)]">{{ getStatusLabel(approval.status) }}</div>
        </div>
        <div class="approval-content">
          <div class="approval-title">{{ approval.title }}</div>
          <div class="approval-info">
            <div class="info-item">
              <span class="label">提交人：</span>
              <span class="value">{{ approval.submitter }}</span>
            </div>
            <div class="info-item">
              <span class="label">提交时间：</span>
              <span class="value">{{ approval.submitTime }}</span>
            </div>
            <div v-if="approval.type === 'leave'" class="info-item">
              <span class="label">请假时间：</span>
              <span class="value">{{ approval.startDate }} 至 {{ approval.endDate }}</span>
            </div>
            <div v-if="approval.type === 'expense'" class="info-item">
              <span class="label">报销金额：</span>
              <span class="value">¥{{ approval.amount.toFixed(2) }}</span>
            </div>
          </div>
        </div>
        <div class="approval-footer">
          <button class="btn-reject" @click="rejectApproval(approval.id)">拒绝</button>
          <button class="btn-approve" @click="approveApproval(approval.id)">同意</button>
        </div>
      </div>
      <div v-if="filteredApprovals.length === 0" class="empty-state">
        <i class="ri-inbox-line"></i>
        <p>没有待审批的内容</p>
      </div>
    </div>

    <!-- 我的审批 -->
    <div v-if="currentTab === 'my'" class="approval-list">
      <div 
        v-for="(approval, index) in myApprovals" 
        :key="index" 
        class="approval-card"
        v-show="currentCategory === 'all' || approval.type === currentCategory"
      >
        <div class="approval-header">
          <div class="approval-type">{{ getApprovalTypeLabel(approval.type) }}</div>
          <div :class="['approval-status', getStatusClass(approval.status)]">{{ getStatusLabel(approval.status) }}</div>
        </div>
        <div class="approval-content">
          <div class="approval-title">{{ approval.title }}</div>
          <div class="approval-info">
            <div class="info-item">
              <span class="label">提交时间：</span>
              <span class="value">{{ approval.submitTime }}</span>
            </div>
            <div v-if="approval.approver" class="info-item">
              <span class="label">审批人：</span>
              <span class="value">{{ approval.approver }}</span>
            </div>
            <div v-if="approval.type === 'leave'" class="info-item">
              <span class="label">请假时间：</span>
              <span class="value">{{ approval.startDate }} 至 {{ approval.endDate }}</span>
            </div>
            <div v-if="approval.type === 'expense'" class="info-item">
              <span class="label">报销金额：</span>
              <span class="value">¥{{ approval.amount.toFixed(2) }}</span>
            </div>
          </div>
        </div>
        <div class="approval-progress">
          <div class="progress-item">
            <div class="progress-dot complete">1</div>
            <div class="progress-line"></div>
            <div class="progress-text">已提交</div>
          </div>
          <div class="progress-item">
            <div :class="['progress-dot', approval.status !== 'pending' ? 'complete' : '']">2</div>
            <div class="progress-line"></div>
            <div class="progress-text">审批中</div>
          </div>
          <div class="progress-item">
            <div :class="['progress-dot', approval.status === 'approved' ? 'complete' : '']">3</div>
            <div class="progress-text">已完成</div>
          </div>
        </div>
      </div>
      <div v-if="filteredMyApprovals.length === 0" class="empty-state">
        <i class="ri-file-list-3-line"></i>
        <p>没有审批记录</p>
      </div>
    </div>

    <!-- 抄送我的 -->
    <div v-if="currentTab === 'cc'" class="approval-list">
      <div 
        v-for="(approval, index) in ccApprovals" 
        :key="index" 
        class="approval-card"
        v-show="currentCategory === 'all' || approval.type === currentCategory"
      >
        <div class="approval-header">
          <div class="approval-type">{{ getApprovalTypeLabel(approval.type) }}</div>
          <div :class="['approval-status', getStatusClass(approval.status)]">{{ getStatusLabel(approval.status) }}</div>
        </div>
        <div class="approval-content">
          <div class="approval-title">{{ approval.title }}</div>
          <div class="approval-info">
            <div class="info-item">
              <span class="label">提交人：</span>
              <span class="value">{{ approval.submitter }}</span>
            </div>
            <div class="info-item">
              <span class="label">提交时间：</span>
              <span class="value">{{ approval.submitTime }}</span>
            </div>
            <div v-if="approval.type === 'leave'" class="info-item">
              <span class="label">请假时间：</span>
              <span class="value">{{ approval.startDate }} 至 {{ approval.endDate }}</span>
            </div>
            <div v-if="approval.type === 'expense'" class="info-item">
              <span class="label">报销金额：</span>
              <span class="value">¥{{ approval.amount.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-if="filteredCcApprovals.length === 0" class="empty-state">
        <i class="ri-file-copy-2-line"></i>
        <p>没有抄送给你的审批</p>
      </div>
    </div>

    <!-- 添加按钮 -->
    <div class="add-button" @click="showAddApproval = true">
      <i class="ri-add-line"></i>
    </div>
  </div>
  
  <TabBar />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import PageHeader from '@/components/PageHeader.vue';
import TabBar from '@/components/TabBar.vue';

const route = useRoute();

// 标签页和当前选中的标签
const tabs = [
  { label: '待我审批', value: 'pending', count: 5 },
  { label: '我的审批', value: 'my', count: 0 },
  { label: '抄送我的', value: 'cc', count: 0 }
];
const currentTab = ref('pending');

// 分类和当前选中的分类
const categories = [
  { label: '全部', value: 'all' },
  { label: '请假', value: 'leave' },
  { label: '报销', value: 'expense' },
  { label: '出差', value: 'travel' },
  { label: '采购', value: 'purchase' }
];
const currentCategory = ref('all');

// 是否显示添加审批对话框
const showAddApproval = ref(false);

// 模拟数据 - 待我审批
const pendingApprovals = ref([
  {
    id: 'p001',
    type: 'leave',
    title: '请假申请 - 事假',
    status: 'pending',
    submitter: '李四',
    submitTime: '2025-04-06 15:30',
    startDate: '2025-04-10',
    endDate: '2025-04-12',
    reason: '家中有事，需要回家处理。'
  },
  {
    id: 'p002',
    type: 'expense',
    title: '报销申请 - 差旅费',
    status: 'pending',
    submitter: '王五',
    submitTime: '2025-04-06 14:20',
    amount: 1580.50,
    reason: '出差上海产生的交通费和住宿费。'
  },
  {
    id: 'p003',
    type: 'travel',
    title: '出差申请 - 客户拜访',
    status: 'pending',
    submitter: '赵六',
    submitTime: '2025-04-06 11:45',
    startDate: '2025-04-15',
    endDate: '2025-04-18',
    reason: '前往广州拜访客户，商讨合作事宜。'
  },
  {
    id: 'p004',
    type: 'purchase',
    title: '采购申请 - 办公用品',
    status: 'pending',
    submitter: '张三',
    submitTime: '2025-04-06 09:10',
    amount: 2800.00,
    reason: '采购办公纸、笔记本等办公用品。'
  },
  {
    id: 'p005',
    type: 'leave',
    title: '请假申请 - 年假',
    status: 'pending',
    submitter: '孙七',
    submitTime: '2025-04-05 16:40',
    startDate: '2025-04-20',
    endDate: '2025-04-24',
    reason: '计划休年假。'
  }
]);

// 模拟数据 - 我的审批
const myApprovals = ref([
  {
    id: 'm001',
    type: 'leave',
    title: '请假申请 - 年假',
    status: 'approved',
    submitTime: '2025-04-02 10:15',
    approver: '陈经理',
    startDate: '2025-04-08',
    endDate: '2025-04-09',
    reason: '计划休年假。'
  },
  {
    id: 'm002',
    type: 'expense',
    title: '报销申请 - 培训费',
    status: 'rejected',
    submitTime: '2025-03-28 14:30',
    approver: '陈经理',
    amount: 3000.00,
    reason: '参加前端开发技术培训的费用。'
  },
  {
    id: 'm003',
    type: 'purchase',
    title: '采购申请 - 电脑配件',
    status: 'pending',
    submitTime: '2025-04-05 11:20',
    amount: 1200.00,
    reason: '购买电脑内存和硬盘。'
  }
]);

// 模拟数据 - 抄送给我的
const ccApprovals = ref([
  {
    id: 'c001',
    type: 'travel',
    title: '出差申请 - 项目交付',
    status: 'approved',
    submitter: '王五',
    submitTime: '2025-04-03 16:20',
    startDate: '2025-04-12',
    endDate: '2025-04-15',
    reason: '前往深圳客户现场进行项目交付。'
  },
  {
    id: 'c002',
    type: 'purchase',
    title: '采购申请 - 服务器',
    status: 'pending',
    submitter: '技术部',
    submitTime: '2025-04-04 15:40',
    amount: 25000.00,
    reason: '购买新服务器设备。'
  }
]);

// 根据当前分类过滤审批列表
const filteredApprovals = computed(() => {
  if (currentCategory.value === 'all') {
    return pendingApprovals.value;
  }
  return pendingApprovals.value.filter(item => item.type === currentCategory.value);
});

const filteredMyApprovals = computed(() => {
  if (currentCategory.value === 'all') {
    return myApprovals.value;
  }
  return myApprovals.value.filter(item => item.type === currentCategory.value);
});

const filteredCcApprovals = computed(() => {
  if (currentCategory.value === 'all') {
    return ccApprovals.value;
  }
  return ccApprovals.value.filter(item => item.type === currentCategory.value);
});

// 获取审批类型标签
const getApprovalTypeLabel = (type) => {
  const typeMap = {
    'leave': '请假',
    'expense': '报销',
    'travel': '出差',
    'purchase': '采购'
  };
  return typeMap[type] || type;
};

// 获取状态标签
const getStatusLabel = (status) => {
  const statusMap = {
    'pending': '待审批',
    'approved': '已通过',
    'rejected': '已拒绝'
  };
  return statusMap[status] || status;
};

// 获取状态类名
const getStatusClass = (status) => {
  const classMap = {
    'pending': 'status-pending',
    'approved': 'status-approved',
    'rejected': 'status-rejected'
  };
  return classMap[status] || '';
};

// 审批操作
const approveApproval = (id) => {
  const index = pendingApprovals.value.findIndex(item => item.id === id);
  if (index !== -1) {
    pendingApprovals.value[index].status = 'approved';
    // 在实际应用中，这里需要调用API更新状态
    setTimeout(() => {
      pendingApprovals.value.splice(index, 1);
      tabs[0].count--;
    }, 1000);
  }
};

const rejectApproval = (id) => {
  const index = pendingApprovals.value.findIndex(item => item.id === id);
  if (index !== -1) {
    pendingApprovals.value[index].status = 'rejected';
    // 在实际应用中，这里需要调用API更新状态
    setTimeout(() => {
      pendingApprovals.value.splice(index, 1);
      tabs[0].count--;
    }, 1000);
  }
};

// 检查URL参数
onMounted(() => {
  // 如果URL中有type参数，则自动选中对应分类
  if (route.query.type) {
    currentCategory.value = route.query.type;
    currentTab.value = 'my'; // 假设从首页快捷操作进来，默认选择"我的审批"标签
  }
});
</script>

<style scoped>
.app-container {
  max-width: 540px;
  margin: 0 auto;
  padding: 20px 16px;
  padding-bottom: 80px;
  position: relative;
}

.action-buttons {
  display: flex;
  gap: 16px;
  font-size: 20px;
  color: var(--primary-color);
}

/* 标签页样式 */
.approval-tabs {
  display: flex;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 8px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
  position: relative;
  cursor: pointer;
}

.tab-item.active {
  color: var(--primary-color);
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 25%;
  right: 25%;
  height: 2px;
  background-color: var(--primary-color);
  border-radius: 1px;
}

.tab-badge {
  display: inline-block;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  background-color: var(--error-color);
  color: white;
  font-size: 11px;
  border-radius: 8px;
  text-align: center;
  padding: 0 4px;
  margin-left: 4px;
}

/* 分类筛选样式 */
.category-filter {
  display: flex;
  overflow-x: auto;
  padding-bottom: 8px;
  margin-bottom: 16px;
  gap: 8px;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}

.category-filter::-webkit-scrollbar {
  display: none; /* Chrome/Safari/Webkit */
}

.filter-item {
  padding: 6px 12px;
  background-color: var(--surface-background);
  border-radius: 16px;
  font-size: 13px;
  white-space: nowrap;
  cursor: pointer;
  border: 1px solid var(--border-color);
}

.filter-item.active {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

/* 审批卡片样式 */
.approval-card {
  background-color: var(--surface-background);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  margin-bottom: 16px;
  overflow: hidden;
}

.approval-header {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}

.approval-type {
  font-weight: 600;
  font-size: 15px;
}

.approval-status {
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 4px;
}

.status-pending {
  background-color: rgba(255, 149, 0, 0.1);
  color: var(--warning-color);
}

.status-approved {
  background-color: rgba(52, 199, 89, 0.1);
  color: var(--success-color);
}

.status-rejected {
  background-color: rgba(255, 45, 85, 0.1);
  color: var(--error-color);
}

.approval-content {
  padding: 16px;
}

.approval-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 12px;
}

.approval-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  font-size: 14px;
  line-height: 1.5;
}

.label {
  color: var(--text-secondary);
  flex-shrink: 0;
  width: 80px;
}

.value {
  color: var(--text-primary);
  flex: 1;
}

.approval-footer {
  display: flex;
  padding: 12px 16px;
  gap: 12px;
  border-top: 1px solid var(--border-color);
}

.btn-reject, .btn-approve {
  flex: 1;
  padding: 8px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  text-align: center;
  cursor: pointer;
  border: none;
  transition: background-color 0.2s;
}

.btn-reject {
  background-color: var(--surface-background);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
}

.btn-approve {
  background-color: var(--primary-color);
  color: white;
}

.btn-reject:hover {
  background-color: var(--hover-background);
}

.btn-approve:hover {
  background-color: var(--primary-dark);
}

/* 流程进度 */
.approval-progress {
  display: flex;
  padding: 16px;
  border-top: 1px solid var(--border-color);
}

.progress-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.progress-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: white;
  margin-bottom: 8px;
}

.progress-dot.complete {
  background-color: var(--primary-color);
}

.progress-line {
  position: absolute;
  width: 100%;
  height: 1px;
  background-color: var(--border-color);
  top: 12px;
  left: 50%;
  z-index: 0;
}

.progress-text {
  font-size: 12px;
  color: var(--text-secondary);
}

.progress-item:last-child .progress-line {
  display: none;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: var(--text-tertiary);
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 15px;
}

/* 添加按钮 */
.add-button {
  position: fixed;
  right: 20px;
  bottom: 76px;
  width: 50px;
  height: 50px;
  border-radius: 25px;
  background-color: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
  z-index: 10;
  cursor: pointer;
}

@media (max-width: 375px) {
  .tab-item {
    font-size: 14px;
    padding: 10px 4px;
  }
  
  .filter-item {
    font-size: 12px;
    padding: 5px 10px;
  }
  
  .approval-type, .approval-title {
    font-size: 15px;
  }
  
  .info-item {
    font-size: 13px;
  }
}
</style> 