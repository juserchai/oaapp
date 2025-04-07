<template>
  <div class="login-container">
    <div class="login-header">
      <div class="login-logo">
        <i class="ri-building-line"></i>
        <span>OA System</span>
      </div>
    </div>

    <div class="login-card">
      <h1 class="login-title">欢迎登录</h1>
      <p class="login-subtitle">请输入您的账号和密码</p>
      
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <div class="input-with-icon">
            <i class="ri-user-line"></i>
            <input 
              id="username" 
              v-model="loginForm.username" 
              type="text" 
              placeholder="请输入用户名" 
              autocomplete="username"
              :class="{ 'has-error': errors.username }"
            >
          </div>
          <div v-if="errors.username" class="error-message">{{ errors.username }}</div>
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <div class="input-with-icon">
            <i class="ri-lock-line"></i>
            <input 
              id="password" 
              v-model="loginForm.password" 
              :type="showPassword ? 'text' : 'password'" 
              placeholder="请输入密码" 
              autocomplete="current-password"
              :class="{ 'has-error': errors.password }"
            >
            <i 
              :class="showPassword ? 'ri-eye-line' : 'ri-eye-off-line'" 
              class="toggle-password" 
              @click="showPassword = !showPassword"
            ></i>
          </div>
          <div v-if="errors.password" class="error-message">{{ errors.password }}</div>
        </div>
        
        <div class="form-options">
          <label class="checkbox-container">
            <input type="checkbox" v-model="loginForm.rememberMe">
            <span class="checkmark"></span>
            <span>记住我</span>
          </label>
          <a href="#" class="forgot-password">忘记密码？</a>
        </div>
        
        <button 
          type="submit" 
          class="login-button" 
          :disabled="isLoading"
        >
          <i v-if="isLoading" class="ri-loader-2-line spinning"></i>
          <span v-else>登录</span>
        </button>
      </form>

      <div class="login-methods">
        <p class="methods-divider">
          <span>其他登录方式</span>
        </p>
        <div class="social-login">
          <button class="social-button">
            <i class="ri-wechat-line"></i>
          </button>
          <button class="social-button">
            <i class="ri-dingding-line"></i>
          </button>
          <button class="social-button">
            <i class="ri-smartphone-line"></i>
          </button>
        </div>
      </div>
    </div>

    <div class="login-footer">
      <p>© 2025 OA System - 版权所有</p>
      <p>Technical Support: IT Department</p>
    </div>

    <!-- 提示消息 -->
    <div v-if="showToast" class="toast" :class="toastType">
      <i :class="toastIcon"></i>
      <span>{{ toastMessage }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const showPassword = ref(false);
const isLoading = ref(false);
const showToast = ref(false);
const toastMessage = ref('');
const toastType = ref('success');

const loginForm = reactive({
  username: 'admin',
  password: '123456',
  rememberMe: false
});

const errors = reactive({
  username: '',
  password: ''
});

// 计算toast图标
const toastIcon = computed(() => {
  const iconMap = {
    success: 'ri-checkbox-circle-line',
    error: 'ri-error-warning-line',
    info: 'ri-information-line'
  };
  return iconMap[toastType.value] || iconMap.info;
});

// 显示toast提示
const showToastMessage = (message, type = 'success') => {
  toastMessage.value = message;
  toastType.value = type;
  showToast.value = true;
  
  setTimeout(() => {
    showToast.value = false;
  }, 3000);
};

// 验证表单
const validateForm = () => {
  let isValid = true;
  
  // 重置错误信息
  errors.username = '';
  errors.password = '';
  
  if (!loginForm.username.trim()) {
    errors.username = '请输入用户名';
    isValid = false;
  }
  
  if (!loginForm.password) {
    errors.password = '请输入密码';
    isValid = false;
  } else if (loginForm.password.length < 6) {
    errors.password = '密码长度不能少于6位';
    isValid = false;
  }
  
  return isValid;
};

// 登录处理
const handleLogin = async () => {
  if (!validateForm()) return;
  
  isLoading.value = true;
  
  try {
    // 模拟登录请求
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    // 如果用户名是admin并且密码是123456，登录成功
    if (loginForm.username === 'admin' && loginForm.password === '123456') {
      // 存储登录状态
      localStorage.setItem('isLoggedIn', 'true');
      localStorage.setItem('username', loginForm.username);
      
      showToastMessage('登录成功');
      
      // 检查是否有重定向
      const redirectPath = route.query.redirect || '/';
      
      // 直接跳转到首页，不使用setTimeout
      router.push(redirectPath);
    } else {
      showToastMessage('用户名或密码错误', 'error');
    }
  } catch (error) {
    showToastMessage('登录失败，请稍后再试', 'error');
    console.error('登录失败:', error);
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--background-color);
  position: relative;
}

.login-header {
  padding: 24px 20px;
}

.login-logo {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-color);
}

.login-logo i {
  margin-right: 8px;
  font-size: 28px;
}

.login-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  max-width: 420px;
  margin: 0 auto;
  width: 100%;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.login-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 32px;
}

.login-form {
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.input-with-icon {
  position: relative;
  display: flex;
  align-items: center;
}

.input-with-icon i {
  position: absolute;
  left: 12px;
  color: var(--text-tertiary);
  font-size: 18px;
}

.input-with-icon input {
  flex: 1;
  padding: 12px 12px 12px 40px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 16px;
  background-color: var(--surface-background);
  color: var(--text-primary);
  width: 100%;
  transition: border-color 0.2s;
}

.input-with-icon input:focus {
  border-color: var(--primary-color);
  outline: none;
}

.input-with-icon input.has-error {
  border-color: var(--error-color);
}

.error-message {
  color: var(--error-color);
  font-size: 13px;
  margin-top: 6px;
}

.toggle-password {
  position: absolute;
  right: 12px;
  color: var(--text-tertiary);
  cursor: pointer;
  font-size: 18px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.checkbox-container {
  display: flex;
  align-items: center;
  position: relative;
  padding-left: 28px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
}

.checkbox-container input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.checkmark {
  position: absolute;
  top: 0;
  left: 0;
  height: 18px;
  width: 18px;
  background-color: var(--surface-background);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.checkbox-container:hover input ~ .checkmark {
  border-color: var(--primary-color);
}

.checkbox-container input:checked ~ .checkmark {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.checkmark:after {
  content: "";
  position: absolute;
  display: none;
}

.checkbox-container input:checked ~ .checkmark:after {
  display: block;
}

.checkbox-container .checkmark:after {
  left: 6px;
  top: 2px;
  width: 4px;
  height: 9px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.forgot-password {
  font-size: 14px;
  color: var(--primary-color);
  text-decoration: none;
}

.login-button {
  width: 100%;
  padding: 14px;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-button:hover {
  background-color: var(--primary-dark);
}

.login-button:disabled {
  background-color: var(--primary-light);
  cursor: not-allowed;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.login-methods {
  margin-top: auto;
}

.methods-divider {
  display: flex;
  align-items: center;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 14px;
  margin: 24px 0;
}

.methods-divider::before,
.methods-divider::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid var(--border-color);
}

.methods-divider::before {
  margin-right: 16px;
}

.methods-divider::after {
  margin-left: 16px;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.social-button {
  width: 44px;
  height: 44px;
  border-radius: 22px;
  background-color: var(--surface-background);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: var(--text-primary);
  cursor: pointer;
  transition: background-color 0.2s;
}

.social-button:hover {
  background-color: var(--hover-background);
}

.login-footer {
  padding: 16px;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 12px;
}

.login-footer p {
  margin: 4px 0;
}

/* Toast 提示样式 */
.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: fadeIn 0.3s, fadeOut 0.3s 2.7s;
}

.toast.success {
  background-color: var(--success-background);
  color: var(--success-color);
}

.toast.error {
  background-color: var(--error-background);
  color: var(--error-color);
}

.toast.info {
  background-color: var(--primary-background);
  color: var(--primary-color);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translate(-50%, -20px); }
  to { opacity: 1; transform: translate(-50%, 0); }
}

@keyframes fadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}

@media (max-width: 480px) {
  .login-card {
    padding: 16px;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .login-subtitle {
    font-size: 14px;
    margin-bottom: 24px;
  }
  
  .input-with-icon input {
    padding: 10px 10px 10px 36px;
    font-size: 15px;
  }
  
  .form-options {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style> 