<template>
  <div class="app-container">
    <PageHeader title="待办">
      <template #actions>
        <div class="action-buttons">
          <i class="ri-search-line"></i>
          <i class="ri-more-2-fill"></i>
        </div>
      </template>
    </PageHeader>

    <!-- 待办统计 -->
    <div class="todo-stats">
      <div class="stat-card">
        <div class="stat-count">{{ activeTodos.length }}</div>
        <div class="stat-label">待处理</div>
      </div>
      <div class="stat-card">
        <div class="stat-count">{{ completedTodos.length }}</div>
        <div class="stat-label">已完成</div>
      </div>
      <div class="stat-card">
        <div class="stat-count">{{ totalTodos }}</div>
        <div class="stat-label">全部</div>
      </div>
    </div>

    <!-- 过滤栏 -->
    <div class="filter-bar">
      <div 
        v-for="filter in filters" 
        :key="filter.value" 
        :class="['filter-item', { active: currentFilter === filter.value }]"
        @click="currentFilter = filter.value"
      >
        {{ filter.label }}
      </div>
    </div>

    <!-- 待办列表 -->
    <div class="todo-list">
      <div v-if="filteredTodos.length === 0" class="empty-state">
        <i class="ri-check-double-line"></i>
        <p>{{ emptyStateMessage }}</p>
      </div>

      <div 
        v-for="(todo, index) in filteredTodos" 
        :key="todo.id" 
        class="todo-item"
      >
        <div class="todo-checkbox" @click="toggleTodoStatus(todo.id)">
          <i :class="todo.completed ? 'ri-checkbox-circle-fill' : 'ri-checkbox-blank-circle-line'"></i>
        </div>
        <div class="todo-content" @click="openTodoDetail(todo)">
          <div :class="['todo-title', { 'completed': todo.completed }]">{{ todo.title }}</div>
          <div class="todo-meta">
            <div class="todo-date">
              <i class="ri-time-line"></i>
              {{ todo.dueDate ? formatDate(todo.dueDate) : '无截止时间' }}
            </div>
            <div v-if="todo.priority" :class="['todo-priority', `priority-${todo.priority}`]">
              {{ getPriorityLabel(todo.priority) }}
            </div>
          </div>
        </div>
        <div class="todo-actions">
          <i class="ri-more-2-fill" @click.stop="showTodoActions(todo)"></i>
        </div>
      </div>
    </div>

    <!-- 添加按钮 -->
    <div class="add-button" @click="showTodoForm = true">
      <i class="ri-add-line"></i>
    </div>

    <!-- 待办详情 -->
    <div v-if="selectedTodo" class="todo-detail-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>待办详情</h3>
          <i class="ri-close-line" @click="selectedTodo = null"></i>
        </div>
        <div class="modal-body">
          <div :class="['todo-title-detail', { 'completed': selectedTodo.completed }]">
            {{ selectedTodo.title }}
          </div>
          
          <div class="detail-section">
            <div class="detail-label">状态</div>
            <div class="detail-value">
              <span :class="['status-badge', selectedTodo.completed ? 'completed' : 'active']">
                {{ selectedTodo.completed ? '已完成' : '待处理' }}
              </span>
            </div>
          </div>
          
          <div class="detail-section">
            <div class="detail-label">优先级</div>
            <div class="detail-value">
              <span :class="['priority-badge', `priority-${selectedTodo.priority}`]">
                {{ getPriorityLabel(selectedTodo.priority) }}
              </span>
            </div>
          </div>
          
          <div class="detail-section">
            <div class="detail-label">截止时间</div>
            <div class="detail-value">{{ selectedTodo.dueDate ? formatDate(selectedTodo.dueDate) : '无截止时间' }}</div>
          </div>
          
          <div class="detail-section">
            <div class="detail-label">描述</div>
            <div class="detail-value">{{ selectedTodo.description || '无描述' }}</div>
          </div>

          <div class="detail-section" v-if="selectedTodo.attachments && selectedTodo.attachments.length">
            <div class="detail-label">附件</div>
            <div class="detail-value attachments">
              <div class="attachment-item" v-for="(attachment, index) in selectedTodo.attachments" :key="index">
                <i class="ri-file-line"></i>
                <span>{{ attachment.name }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="selectedTodo = null">关闭</button>
          <button class="btn-primary" @click="toggleTodoStatus(selectedTodo.id)">
            {{ selectedTodo.completed ? '标记为未完成' : '标记为完成' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 添加待办表单 -->
    <div v-if="showTodoForm" class="todo-form-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ editingTodoId ? '编辑待办' : '添加待办' }}</h3>
          <i class="ri-close-line" @click="closeTodoForm"></i>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label for="todo-title">标题</label>
            <input type="text" id="todo-title" v-model="todoForm.title" placeholder="输入待办标题">
          </div>
          
          <div class="form-group">
            <label for="todo-description">描述</label>
            <textarea id="todo-description" v-model="todoForm.description" placeholder="输入待办详情描述"></textarea>
          </div>
          
          <div class="form-group">
            <label for="todo-due-date">截止时间</label>
            <input type="date" id="todo-due-date" v-model="todoForm.dueDate">
          </div>
          
          <div class="form-group">
            <label for="todo-priority">优先级</label>
            <select id="todo-priority" v-model="todoForm.priority">
              <option value="low">低</option>
              <option value="medium">中</option>
              <option value="high">高</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeTodoForm">取消</button>
          <button class="btn-primary" @click="saveTodo">保存</button>
        </div>
      </div>
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

// 过滤器
const filters = [
  { label: '全部', value: 'all' },
  { label: '今日', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '已完成', value: 'completed' }
];
const currentFilter = ref('all');

// 待办列表
const todos = ref([
  {
    id: 't001',
    title: '完成季度报告',
    description: '需要汇总所有部门数据，并制作演示文稿',
    dueDate: '2025-04-10',
    priority: 'high',
    completed: false,
    createTime: '2025-04-01'
  },
  {
    id: 't002',
    title: '安排团队会议',
    description: '讨论下一阶段项目计划和任务分配',
    dueDate: '2025-04-07',
    priority: 'medium',
    completed: false,
    createTime: '2025-04-02'
  },
  {
    id: 't003',
    title: '准备客户演示材料',
    description: '准备下周客户会议的演示文稿和演示脚本',
    dueDate: '2025-04-12',
    priority: 'high',
    completed: false,
    createTime: '2025-04-03'
  },
  {
    id: 't004',
    title: '回复重要邮件',
    description: '回复来自合作伙伴的合作提案邮件',
    dueDate: '2025-04-05',
    priority: 'medium',
    completed: true,
    createTime: '2025-04-03'
  },
  {
    id: 't005',
    title: '审核项目预算',
    description: '审核并调整第二季度项目预算',
    dueDate: '2025-04-08',
    priority: 'low',
    completed: false,
    createTime: '2025-04-04'
  },
  {
    id: 't006',
    title: '更新项目文档',
    description: '更新项目文档以反映最新的进展和变更',
    dueDate: '2025-04-15',
    priority: 'low',
    completed: true,
    createTime: '2025-04-04'
  }
]);

// 待办详情
const selectedTodo = ref(null);
const showTodoForm = ref(false);
const editingTodoId = ref(null);
const todoForm = ref({
  title: '',
  description: '',
  dueDate: '',
  priority: 'medium'
});

// 计算属性
const activeTodos = computed(() => todos.value.filter(todo => !todo.completed));
const completedTodos = computed(() => todos.value.filter(todo => todo.completed));
const totalTodos = computed(() => todos.value.length);

// 根据过滤条件筛选待办事项
const filteredTodos = computed(() => {
  switch (currentFilter.value) {
    case 'today':
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);
      
      return todos.value.filter(todo => {
        if (!todo.dueDate) return false;
        const dueDate = new Date(todo.dueDate);
        return dueDate >= today && dueDate < tomorrow;
      });
    
    case 'week':
      const now = new Date();
      const startOfWeek = new Date(now);
      startOfWeek.setDate(now.getDate() - now.getDay());
      startOfWeek.setHours(0, 0, 0, 0);
      
      const endOfWeek = new Date(startOfWeek);
      endOfWeek.setDate(startOfWeek.getDate() + 7);
      
      return todos.value.filter(todo => {
        if (!todo.dueDate) return false;
        const dueDate = new Date(todo.dueDate);
        return dueDate >= startOfWeek && dueDate < endOfWeek;
      });
    
    case 'completed':
      return completedTodos.value;
    
    case 'all':
    default:
      return todos.value;
  }
});

// 空状态消息
const emptyStateMessage = computed(() => {
  switch (currentFilter.value) {
    case 'today':
      return '今天没有待办事项';
    case 'week':
      return '本周没有待办事项';
    case 'completed':
      return '没有已完成的待办';
    default:
      return '没有待办事项';
  }
});

// 获取优先级标签
const getPriorityLabel = (priority) => {
  const priorityMap = {
    low: '低',
    medium: '中',
    high: '高'
  };
  return priorityMap[priority] || priority;
};

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 切换待办状态
const toggleTodoStatus = (id) => {
  const todoIndex = todos.value.findIndex(todo => todo.id === id);
  if (todoIndex !== -1) {
    todos.value[todoIndex].completed = !todos.value[todoIndex].completed;
    
    // 如果在详情页中，也要更新详情
    if (selectedTodo.value && selectedTodo.value.id === id) {
      selectedTodo.value.completed = todos.value[todoIndex].completed;
    }
  }
};

// 打开待办详情
const openTodoDetail = (todo) => {
  selectedTodo.value = { ...todo };
};

// 显示待办操作菜单
const showTodoActions = (todo) => {
  // 在实际应用中，这里可以显示一个操作菜单
  editTodo(todo);
};

// 编辑待办
const editTodo = (todo) => {
  editingTodoId.value = todo.id;
  todoForm.value = {
    title: todo.title,
    description: todo.description || '',
    dueDate: todo.dueDate || '',
    priority: todo.priority || 'medium'
  };
  showTodoForm.value = true;
};

// 关闭待办表单
const closeTodoForm = () => {
  showTodoForm.value = false;
  editingTodoId.value = null;
  todoForm.value = {
    title: '',
    description: '',
    dueDate: '',
    priority: 'medium'
  };
};

// 保存待办
const saveTodo = () => {
  if (!todoForm.value.title.trim()) {
    alert('请输入待办标题');
    return;
  }
  
  if (editingTodoId.value) {
    // 编辑现有待办
    const todoIndex = todos.value.findIndex(todo => todo.id === editingTodoId.value);
    if (todoIndex !== -1) {
      todos.value[todoIndex] = {
        ...todos.value[todoIndex],
        title: todoForm.value.title,
        description: todoForm.value.description,
        dueDate: todoForm.value.dueDate,
        priority: todoForm.value.priority
      };
    }
  } else {
    // 添加新待办
    const newTodo = {
      id: `t${Date.now()}`,
      title: todoForm.value.title,
      description: todoForm.value.description,
      dueDate: todoForm.value.dueDate,
      priority: todoForm.value.priority,
      completed: false,
      createTime: formatDate(new Date())
    };
    todos.value.unshift(newTodo);
  }
  
  closeTodoForm();
};

// 初始化
onMounted(() => {
  // 根据URL参数执行对应操作
  if (route.query.action === 'add') {
    showTodoForm.value = true;
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

/* 待办统计 */
.todo-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.stat-card {
  flex: 1;
  background-color: var(--surface-background);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  margin: 0 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.stat-count {
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 过滤栏 */
.filter-bar {
  display: flex;
  overflow-x: auto;
  padding-bottom: 8px;
  margin-bottom: 16px;
  gap: 8px;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}

.filter-bar::-webkit-scrollbar {
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

/* 待办列表 */
.todo-list {
  padding-bottom: 20px;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  background-color: var(--surface-background);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.todo-checkbox {
  padding-right: 12px;
  font-size: 24px;
  color: var(--primary-color);
  padding-top: 2px;
}

.todo-checkbox i {
  cursor: pointer;
}

.todo-content {
  flex: 1;
  cursor: pointer;
}

.todo-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.todo-title.completed {
  color: var(--text-tertiary);
  text-decoration: line-through;
}

.todo-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-secondary);
}

.todo-date {
  display: flex;
  align-items: center;
  gap: 4px;
}

.todo-priority {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.priority-high {
  background-color: rgba(255, 45, 85, 0.1);
  color: var(--error-color);
}

.priority-medium {
  background-color: rgba(255, 149, 0, 0.1);
  color: var(--warning-color);
}

.priority-low {
  background-color: rgba(52, 199, 89, 0.1);
  color: var(--success-color);
}

.todo-actions {
  padding-left: 12px;
  color: var(--text-secondary);
  cursor: pointer;
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

/* 待办详情和表单模态框 */
.todo-detail-modal,
.todo-form-modal {
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
  max-width: 480px;
  background-color: var(--background-color);
  border-radius: 16px;
  overflow: hidden;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
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
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  border-top: 1px solid var(--border-color);
  gap: 12px;
}

/* 待办详情样式 */
.todo-title-detail {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.todo-title-detail.completed {
  color: var(--text-tertiary);
  text-decoration: line-through;
}

.detail-section {
  margin-bottom: 16px;
}

.detail-label {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 4px;
}

.detail-value {
  color: var(--text-primary);
  font-size: 15px;
}

.status-badge,
.priority-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 13px;
}

.status-badge.active {
  background-color: rgba(0, 122, 255, 0.1);
  color: var(--primary-color);
}

.status-badge.completed {
  background-color: rgba(52, 199, 89, 0.1);
  color: var(--success-color);
}

.attachments {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background-color: var(--hover-background);
  border-radius: 8px;
}

/* 表单样式 */
.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 15px;
  background-color: var(--surface-background);
  color: var(--text-primary);
}

.form-group textarea {
  min-height: 100px;
  resize: vertical;
}

.form-group select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%23999' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
}

/* 按钮样式 */
.btn-cancel,
.btn-primary {
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}

.btn-cancel {
  background-color: var(--surface-background);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

@media (max-width: 375px) {
  .todo-title {
    font-size: 15px;
  }
  
  .stat-count {
    font-size: 20px;
  }
  
  .stat-label {
    font-size: 12px;
  }
  
  .filter-item {
    font-size: 12px;
    padding: 5px 10px;
  }
  
  .todo-meta,
  .todo-date {
    font-size: 12px;
  }
}
</style> 