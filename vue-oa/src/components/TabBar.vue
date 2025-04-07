<template>
  <div class="tab-bar" v-if="!isLoginPage">
    <router-link to="/" class="tab-item" active-class="active">
      <div class="tab-icon"><i class="ri-home-5-line"></i></div>
      <div class="tab-label">首页</div>
    </router-link>
    <router-link to="/todo" class="tab-item" active-class="active">
      <div class="tab-icon"><i class="ri-todo-line"></i></div>
      <div class="tab-label">待办</div>
    </router-link>
    <router-link to="/calendar" class="tab-item" active-class="active">
      <div class="tab-icon"><i class="ri-calendar-line"></i></div>
      <div class="tab-label">日程</div>
    </router-link>
    <router-link to="/approval" class="tab-item" active-class="active">
      <div class="tab-icon"><i class="ri-file-list-3-line"></i></div>
      <div class="tab-label">审批</div>
    </router-link>
    <router-link to="/profile" class="tab-item" active-class="active">
      <div class="tab-icon"><i class="ri-user-line"></i></div>
      <div class="tab-label">我的</div>
    </router-link>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

// 判断当前是否为登录页
const isLoginPage = computed(() => {
  return route.path === '/login';
});
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  height: 56px;
  background-color: var(--surface-background);
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6px 4px;
  color: var(--text-tertiary);
  text-decoration: none;
}

.tab-item.active {
  color: var(--primary-color);
}

.tab-icon {
  font-size: 22px;
  margin-bottom: 3px;
}

.tab-label {
  font-size: 12px;
}
</style> 