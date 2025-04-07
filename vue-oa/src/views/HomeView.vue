<template>
  <div class="app-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-title">您好，{{ userInfo.name }}</div>
      <div class="welcome-message">欢迎使用 OA 办公系统</div>
    </div>
    
    <!-- 功能模块 -->
    <div class="module-section">
      <h2 class="module-title">应用功能</h2>
      <div class="module-grid">
        <router-link :to="module.path" class="module-card" v-for="module in modules" :key="module.name">
          <div class="module-icon" :style="module.iconStyle">
            <i :class="module.icon"></i>
          </div>
          <div class="module-info">
            <div class="module-name">{{ module.name }}</div>
            <div class="module-desc">{{ module.description }}</div>
          </div>
        </router-link>
      </div>
    </div>
    
    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h2 class="module-title">快捷操作</h2>
      <router-link :to="action.path" class="action-button" v-for="action in quickActions" :key="action.name">
        <div class="action-icon" :style="action.iconStyle">
          <i :class="action.icon"></i>
        </div>
        <div class="action-text">
          <div class="action-title">{{ action.name }}</div>
          <div class="action-desc">{{ action.description }}</div>
        </div>
        <div class="action-arrow">
          <i class="ri-arrow-right-s-line"></i>
        </div>
      </router-link>
    </div>
  </div>
  
  <TabBar />
</template>

<script setup>
import { reactive } from 'vue';
import TabBar from '@/components/TabBar.vue';

// 用户信息
const userInfo = reactive({
  name: '张明',
  position: '产品经理',
  department: '研发中心'
});

// 模块数据
const modules = [
  {
    name: '日程管理',
    description: '查看和管理个人及团队日程',
    icon: 'ri-calendar-line',
    iconStyle: {},
    path: '/calendar'
  },
  {
    name: '审批流程',
    description: '请假、报销等各类审批',
    icon: 'ri-file-list-3-line',
    iconStyle: {},
    path: '/approval'
  },
  {
    name: '待办事项',
    description: '查看和管理个人待办任务',
    icon: 'ri-task-line',
    iconStyle: { 'background-color': 'rgba(52, 199, 89, 0.1)' },
    path: '/todo'
  },
  {
    name: '即时通讯',
    description: '与同事沟通协作',
    icon: 'ri-chat-4-line',
    iconStyle: { 'background-color': 'rgba(255, 149, 0, 0.1)' },
    path: '/chat'
  }
];

// 快捷操作
const quickActions = [
  {
    name: '查看通讯录',
    description: '查找公司员工信息',
    icon: 'ri-user-add-line',
    iconStyle: {},
    path: '/contacts'
  },
  {
    name: '请假申请',
    description: '快速发起请假流程',
    icon: 'ri-calendar-check-line',
    iconStyle: { 'background-color': 'rgba(52, 199, 89, 0.1)', 'color': 'var(--success-color)' },
    path: '/approval?type=leave'
  },
  {
    name: '报销申请',
    description: '快速发起报销流程',
    icon: 'ri-money-cny-circle-line',
    iconStyle: { 'background-color': 'rgba(255, 149, 0, 0.1)', 'color': 'var(--warning-color)' },
    path: '/approval?type=expense'
  }
];
</script>

<style scoped>
.app-container {
  max-width: 540px;
  margin: 0 auto;
  padding: 20px 16px;
  padding-bottom: 80px;
}

.welcome-banner {
  background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
  border-radius: 16px;
  padding: 24px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 6px 15px rgba(0, 122, 255, 0.15);
}

.welcome-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 8px;
}

.welcome-message {
  font-size: 14px;
  opacity: 0.8;
}

.module-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.module-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: var(--surface-background);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  transition: all 0.2s;
  text-decoration: none;
  color: inherit;
}

.module-card:hover, 
.module-card:active {
  background-color: var(--hover-background);
  transform: translateY(-2px);
}

.module-icon {
  width: 40px;
  height: 40px;
  background-color: rgba(0, 122, 255, 0.1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 20px;
  color: var(--primary-color);
}

.module-info {
  flex: 1;
  min-width: 0;
}

.module-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.module-desc {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.quick-actions {
  margin-bottom: 20px;
}

.action-button {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: var(--surface-background);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  margin-bottom: 12px;
  transition: all 0.2s;
  text-decoration: none;
  color: inherit;
}

.action-button:hover,
.action-button:active {
  background-color: var(--hover-background);
  transform: translateY(-1px);
}

.action-icon {
  width: 40px;
  height: 40px;
  background-color: rgba(0, 122, 255, 0.1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 20px;
  color: var(--primary-color);
  flex-shrink: 0;
}

.action-text {
  flex: 1;
  min-width: 0;
}

.action-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-desc {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-arrow {
  color: var(--text-tertiary);
  font-size: 20px;
  flex-shrink: 0;
}

@media (max-width: 375px) {
  .welcome-title {
    font-size: 20px;
  }
  
  .module-grid {
    gap: 8px;
  }
  
  .module-card, .action-button {
    padding: 14px;
  }
  
  .module-icon, .action-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }
}
</style> 