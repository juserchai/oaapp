<template>
  <div class="app-container">
    <PageHeader title="我的">
      <template #actions>
        <div class="action-buttons">
          <i class="ri-settings-3-line" @click="showSettings = true"></i>
        </div>
      </template>
    </PageHeader>

    <!-- 用户资料卡片 -->
    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar">
          <div class="avatar-placeholder">张</div>
        </div>
        <div class="profile-info">
          <h2 class="profile-name">张三</h2>
          <p class="profile-title">产品经理 · 研发部</p>
        </div>
      </div>
      <div class="profile-stats">
        <div class="stat-item">
          <div class="stat-value">12</div>
          <div class="stat-label">待办</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">5</div>
          <div class="stat-label">日程</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">8</div>
          <div class="stat-label">待审批</div>
        </div>
      </div>
    </div>

    <!-- 快捷功能 -->
    <div class="section-title">快捷功能</div>
    <div class="quick-actions">
      <div class="action-grid">
        <div class="action-item" v-for="(action, index) in quickActions" :key="index" @click="handleActionClick(action)">
          <div class="action-icon" :style="{ backgroundColor: action.bgColor }">
            <i :class="action.icon"></i>
          </div>
          <div class="action-name">{{ action.name }}</div>
        </div>
      </div>
    </div>

    <!-- 我的应用 -->
    <div class="section-title">我的应用</div>
    <div class="my-apps">
      <div class="app-grid">
        <div class="app-item" v-for="(app, index) in myApps" :key="index" @click="handleAppClick(app)">
          <div class="app-icon" :style="{ backgroundColor: app.bgColor }">
            <i :class="app.icon"></i>
          </div>
          <div class="app-name">{{ app.name }}</div>
        </div>
      </div>
    </div>

    <!-- 其他服务 -->
    <div class="section-title">其他服务</div>
    <div class="service-list">
      <div class="service-item" v-for="(service, index) in otherServices" :key="index" @click="handleServiceClick(service)">
        <div class="service-icon">
          <i :class="service.icon"></i>
        </div>
        <div class="service-content">
          <div class="service-name">{{ service.name }}</div>
          <div class="service-desc" v-if="service.description">{{ service.description }}</div>
        </div>
        <div class="service-action">
          <i class="ri-arrow-right-s-line"></i>
        </div>
      </div>
    </div>

    <!-- 退出按钮 -->
    <div class="logout-button" @click="showLogoutConfirm = true">
      退出登录
    </div>

    <!-- 设置弹窗 -->
    <div v-if="showSettings" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>设置</h3>
          <i class="ri-close-line" @click="showSettings = false"></i>
        </div>
        <div class="modal-body">
          <div class="settings-list">
            <div class="settings-item">
              <div class="settings-label">暗黑模式</div>
              <div class="settings-control">
                <label class="toggle">
                  <input type="checkbox" v-model="darkMode">
                  <span class="toggle-slider"></span>
                </label>
              </div>
            </div>
            <div class="settings-item">
              <div class="settings-label">消息通知</div>
              <div class="settings-control">
                <label class="toggle">
                  <input type="checkbox" v-model="notifications">
                  <span class="toggle-slider"></span>
                </label>
              </div>
            </div>
            <div class="settings-item">
              <div class="settings-label">字体大小</div>
              <div class="settings-control font-size-control">
                <button 
                  :class="['font-size-btn', fontSize === 'small' ? 'active' : '']" 
                  @click="fontSize = 'small'"
                >小</button>
                <button 
                  :class="['font-size-btn', fontSize === 'medium' ? 'active' : '']" 
                  @click="fontSize = 'medium'"
                >中</button>
                <button 
                  :class="['font-size-btn', fontSize === 'large' ? 'active' : '']" 
                  @click="fontSize = 'large'"
                >大</button>
              </div>
            </div>
            <div class="settings-item">
              <div class="settings-label">关于</div>
              <div class="settings-control">
                <i class="ri-arrow-right-s-line"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 退出确认弹窗 -->
    <div v-if="showLogoutConfirm" class="modal-overlay">
      <div class="confirm-dialog">
        <div class="confirm-title">确认退出登录?</div>
        <div class="confirm-buttons">
          <button class="btn-cancel" @click="showLogoutConfirm = false">取消</button>
          <button class="btn-confirm" @click="logout">确认</button>
        </div>
      </div>
    </div>
  </div>
  
  <TabBar />
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/PageHeader.vue';
import TabBar from '@/components/TabBar.vue';

const router = useRouter();

// 状态管理
const showSettings = ref(false);
const showLogoutConfirm = ref(false);
const darkMode = ref(false);
const notifications = ref(true);
const fontSize = ref('medium');

// 快捷功能
const quickActions = [
  { name: '请假申请', icon: 'ri-calendar-check-line', bgColor: '#007AFF' },
  { name: '报销申请', icon: 'ri-money-cny-circle-line', bgColor: '#FF9500' },
  { name: '考勤打卡', icon: 'ri-time-line', bgColor: '#34C759' },
  { name: '会议预约', icon: 'ri-vidicon-line', bgColor: '#5856D6' }
];

// 我的应用
const myApps = [
  { name: '工资条', icon: 'ri-file-list-3-line', bgColor: '#FF2D55' },
  { name: '日程安排', icon: 'ri-calendar-line', bgColor: '#007AFF' },
  { name: '通讯录', icon: 'ri-contacts-book-line', bgColor: '#5856D6' },
  { name: '公告信息', icon: 'ri-message-2-line', bgColor: '#FF9500' },
  { name: '知识库', icon: 'ri-book-open-line', bgColor: '#34C759' },
  { name: '会议室', icon: 'ri-community-line', bgColor: '#AF52DE' }
];

// 其他服务
const otherServices = [
  { name: '个人信息', icon: 'ri-user-settings-line', description: '修改个人基本信息' },
  { name: '账号安全', icon: 'ri-shield-keyhole-line', description: '密码修改、账号绑定' },
  { name: '隐私设置', icon: 'ri-lock-line', description: '信息可见范围设置' },
  { name: '消息设置', icon: 'ri-notification-line', description: '推送通知、提醒设置' },
  { name: '帮助中心', icon: 'ri-question-line' }
];

// 处理快捷功能点击
const handleActionClick = (action) => {
  console.log('点击了快捷功能:', action.name);
  // 这里可以根据功能执行相应的路由跳转或操作
  if (action.name === '请假申请') {
    // 跳转到请假申请页面
  } else if (action.name === '报销申请') {
    // 跳转到报销申请页面
  }
};

// 处理应用点击
const handleAppClick = (app) => {
  console.log('点击了应用:', app.name);
  // 这里可以根据应用执行相应的路由跳转
};

// 处理服务点击
const handleServiceClick = (service) => {
  console.log('点击了服务:', service.name);
  // 这里可以根据服务执行相应的操作
};

// 退出登录
const logout = () => {
  console.log('退出登录');
  // 这里可以执行退出登录的逻辑，如清除token等
  showLogoutConfirm.value = false;
  // 跳转到登录页
  router.push('/login');
};
</script>

<style scoped>
.app-container {
  max-width: 540px;
  margin: 0 auto;
  padding: 20px 16px;
  padding-bottom: 80px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  font-size: 20px;
  color: var(--primary-color);
}

/* 用户资料卡片 */
.profile-card {
  background-color: var(--surface-background);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 36px;
  overflow: hidden;
  margin-right: 16px;
  background-color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder {
  color: white;
  font-size: 36px;
  font-weight: 500;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: none;
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: var(--text-primary);
}

.profile-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.stat-item {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 章节标题 */
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 24px 0 16px 0;
  color: var(--text-primary);
}

/* 快捷功能 */
.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  font-size: 22px;
  color: white;
}

.action-name {
  font-size: 13px;
  color: var(--text-primary);
  text-align: center;
}

/* 我的应用 */
.app-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.app-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.app-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  font-size: 22px;
  color: white;
}

.app-name {
  font-size: 13px;
  color: var(--text-primary);
  text-align: center;
}

/* 其他服务 */
.service-list {
  background-color: var(--surface-background);
  border-radius: 12px;
  overflow: hidden;
}

.service-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
}

.service-item:last-child {
  border-bottom: none;
}

.service-icon {
  font-size: 20px;
  color: var(--primary-color);
  margin-right: 16px;
  width: 24px;
  display: flex;
  justify-content: center;
}

.service-content {
  flex: 1;
}

.service-name {
  font-size: 15px;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.service-desc {
  font-size: 13px;
  color: var(--text-tertiary);
}

.service-action {
  color: var(--text-tertiary);
  font-size: 20px;
}

/* 退出按钮 */
.logout-button {
  margin: 40px auto;
  text-align: center;
  padding: 12px 0;
  background-color: var(--surface-background);
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  color: var(--error-color);
  cursor: pointer;
  max-width: 280px;
}

/* 模态弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 90%;
  max-width: 400px;
  background-color: var(--background-color);
  border-radius: 16px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.modal-header i {
  font-size: 24px;
  cursor: pointer;
  color: var(--text-secondary);
}

.modal-body {
  padding: 16px;
}

/* 设置列表 */
.settings-list {
  display: flex;
  flex-direction: column;
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
}

.settings-item:last-child {
  border-bottom: none;
}

.settings-label {
  font-size: 16px;
  color: var(--text-primary);
}

.settings-control {
  display: flex;
  align-items: center;
}

/* 开关 */
.toggle {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 26px;
}

.toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--border-color);
  border-radius: 26px;
  transition: .3s;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 22px;
  width: 22px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  border-radius: 50%;
  transition: .3s;
}

input:checked + .toggle-slider {
  background-color: var(--primary-color);
}

input:checked + .toggle-slider:before {
  transform: translateX(24px);
}

/* 字体大小控制 */
.font-size-control {
  display: flex;
  gap: 8px;
}

.font-size-btn {
  background-color: var(--surface-background);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 4px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.font-size-btn.active {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

/* 确认弹窗 */
.confirm-dialog {
  width: 85%;
  max-width: 320px;
  background-color: var(--background-color);
  border-radius: 16px;
  padding: 24px 20px;
  text-align: center;
}

.confirm-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 24px;
  color: var(--text-primary);
}

.confirm-buttons {
  display: flex;
  justify-content: space-around;
}

.btn-cancel,
.btn-confirm {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  min-width: 100px;
}

.btn-cancel {
  background-color: var(--surface-background);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.btn-confirm {
  background-color: var(--error-color);
  color: white;
}

@media (max-width: 400px) {
  .action-grid,
  .app-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .action-icon,
  .app-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
  
  .action-name,
  .app-name {
    font-size: 12px;
  }
}
</style> 