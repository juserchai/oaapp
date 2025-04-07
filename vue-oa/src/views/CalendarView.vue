<template>
  <div class="app-container">
    <PageHeader title="日程">
      <template #actions>
        <div class="action-buttons">
          <i class="ri-search-line"></i>
          <i class="ri-more-2-fill"></i>
        </div>
      </template>
    </PageHeader>

    <!-- 视图切换 -->
    <div class="view-switcher">
      <div 
        v-for="view in views" 
        :key="view.value" 
        :class="['view-option', { active: currentView === view.value }]"
        @click="currentView = view.value"
      >
        {{ view.label }}
      </div>
    </div>

    <!-- 月份导航 -->
    <div class="month-header">
      <i class="ri-arrow-left-s-line" @click="changeMonth(-1)"></i>
      <div class="current-month">{{ currentMonthLabel }}</div>
      <i class="ri-arrow-right-s-line" @click="changeMonth(1)"></i>
    </div>

    <!-- 月视图 -->
    <div v-if="currentView === 'month'" class="month-view">
      <!-- 星期标题 -->
      <div class="weekdays">
        <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
      </div>

      <!-- 日历格子 -->
      <div class="calendar-grid">
        <div 
          v-for="(day, index) in daysInMonth" 
          :key="index" 
          :class="[
            'day-cell', 
            { 'current-day': day.isToday, 'other-month': !day.isCurrentMonth }
          ]"
        >
          <div class="day-number">{{ day.date }}</div>
          <div class="day-events">
            <div 
              v-for="(event, eventIndex) in day.events.slice(0, 2)" 
              :key="eventIndex" 
              class="event-item" 
              :class="'event-' + event.type"
            >
              {{ event.title }}
            </div>
            <div v-if="day.events.length > 2" class="more-events">
              +{{ day.events.length - 2 }} 更多
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 周视图 -->
    <div v-if="currentView === 'week'" class="week-view">
      <!-- 星期栏 -->
      <div class="week-header">
        <div class="time-column"></div>
        <div 
          v-for="(day, index) in currentWeek" 
          :key="index" 
          :class="['week-day', { 'current-day': day.isToday }]"
        >
          <div class="day-name">{{ day.dayShort }}</div>
          <div class="day-number">{{ day.date }}</div>
        </div>
      </div>

      <!-- 时间轴 -->
      <div class="week-content">
        <div class="time-column">
          <div v-for="hour in 12" :key="hour" class="time-slot">
            {{ hour + 8 }}:00
          </div>
        </div>
        <div class="week-grid">
          <div 
            v-for="(day, dayIndex) in currentWeek" 
            :key="dayIndex" 
            class="day-column"
          >
            <div 
              v-for="(event, eventIndex) in day.events" 
              :key="eventIndex" 
              class="week-event"
              :class="'event-' + event.type"
              :style="{
                top: `${(event.startHour - 8) * 60 + event.startMinute}px`,
                height: `${event.duration}px`
              }"
            >
              {{ event.title }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 日视图 -->
    <div v-if="currentView === 'day'" class="day-view">
      <div class="day-header">
        <div class="current-date">{{ currentDateLabel }}</div>
      </div>
      
      <div class="day-timeline">
        <div class="time-column">
          <div v-for="hour in 12" :key="hour" class="time-slot">
            {{ hour + 8 }}:00
          </div>
        </div>
        <div class="events-column">
          <div 
            v-for="(event, index) in todayEvents" 
            :key="index" 
            class="day-event"
            :class="'event-' + event.type"
            :style="{
              top: `${(event.startHour - 8) * 60 + event.startMinute}px`,
              height: `${event.duration}px`
            }"
          >
            <div class="event-time">{{ event.startHour }}:{{ event.startMinute.toString().padStart(2, '0') }}</div>
            <div class="event-title">{{ event.title }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加按钮 -->
    <div class="add-button">
      <i class="ri-add-line"></i>
    </div>
  </div>
  
  <TabBar />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import PageHeader from '@/components/PageHeader.vue';
import TabBar from '@/components/TabBar.vue';

// 当前视图
const views = [
  { label: '日', value: 'day' },
  { label: '周', value: 'week' },
  { label: '月', value: 'month' }
];
const currentView = ref('month');

// 日期相关
const today = new Date();
const currentMonth = ref(today.getMonth());
const currentYear = ref(today.getFullYear());
const currentDate = ref(today.getDate());

// 月份导航标签
const currentMonthLabel = computed(() => {
  const monthNames = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'];
  return `${currentYear.value}年 ${monthNames[currentMonth.value]}`;
});

// 当前日期标签
const currentDateLabel = computed(() => {
  const date = new Date(currentYear.value, currentMonth.value, currentDate.value);
  const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  return `${currentYear.value}年${currentMonth.value + 1}月${currentDate.value}日 ${dayNames[date.getDay()]}`;
});

// 星期标题
const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

// 改变月份
const changeMonth = (delta) => {
  let newMonth = currentMonth.value + delta;
  let newYear = currentYear.value;
  
  if (newMonth < 0) {
    newMonth = 11;
    newYear--;
  } else if (newMonth > 11) {
    newMonth = 0;
    newYear++;
  }
  
  currentMonth.value = newMonth;
  currentYear.value = newYear;
};

// 模拟数据 - 日程事件
const generateEvents = () => {
  const eventTypes = ['meeting', 'holiday', 'task', 'personal'];
  const eventTitles = [
    '团队会议', '项目评审', '客户沟通', '产品设计', 
    '版本发布', '代码评审', '休假', '培训课程'
  ];
  
  return Array(5).fill().map(() => {
    const startHour = 8 + Math.floor(Math.random() * 8);
    return {
      title: eventTitles[Math.floor(Math.random() * eventTitles.length)],
      type: eventTypes[Math.floor(Math.random() * eventTypes.length)],
      startHour,
      startMinute: Math.floor(Math.random() * 4) * 15,
      duration: 30 + Math.floor(Math.random() * 4) * 30
    };
  });
};

// 计算当月的日期格子数据
const daysInMonth = computed(() => {
  const days = [];
  const firstDay = new Date(currentYear.value, currentMonth.value, 1);
  const lastDay = new Date(currentYear.value, currentMonth.value + 1, 0);
  
  // 上个月的剩余日期
  const prevMonthDays = firstDay.getDay();
  const prevMonth = new Date(currentYear.value, currentMonth.value, 0);
  for (let i = prevMonthDays - 1; i >= 0; i--) {
    days.push({
      date: prevMonth.getDate() - i,
      isCurrentMonth: false,
      isToday: false,
      events: generateEvents().slice(0, Math.floor(Math.random() * 2))
    });
  }
  
  // 当月的日期
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const isToday = i === today.getDate() && 
                  currentMonth.value === today.getMonth() && 
                  currentYear.value === today.getFullYear();
    days.push({
      date: i,
      isCurrentMonth: true,
      isToday,
      events: generateEvents().slice(0, Math.floor(Math.random() * 4))
    });
  }
  
  // 下个月的开始日期
  const nextMonthDays = 42 - days.length;
  for (let i = 1; i <= nextMonthDays; i++) {
    days.push({
      date: i,
      isCurrentMonth: false,
      isToday: false,
      events: generateEvents().slice(0, Math.floor(Math.random() * 2))
    });
  }
  
  return days;
});

// 当前周的数据
const currentWeek = computed(() => {
  const week = [];
  const startOfWeek = new Date(currentYear.value, currentMonth.value, currentDate.value);
  startOfWeek.setDate(startOfWeek.getDate() - startOfWeek.getDay());
  
  for (let i = 0; i < 7; i++) {
    const day = new Date(startOfWeek);
    day.setDate(day.getDate() + i);
    
    week.push({
      date: day.getDate(),
      month: day.getMonth(),
      year: day.getFullYear(),
      dayShort: weekdays[i].substring(0, 1),
      isToday: day.getDate() === today.getDate() && 
               day.getMonth() === today.getMonth() && 
               day.getFullYear() === today.getFullYear(),
      events: generateEvents()
    });
  }
  
  return week;
});

// 今天的事件
const todayEvents = computed(() => {
  return generateEvents();
});

// 挂载时设置当前日期
onMounted(() => {
  currentMonth.value = today.getMonth();
  currentYear.value = today.getFullYear();
  currentDate.value = today.getDate();
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

.view-switcher {
  display: flex;
  background-color: var(--surface-background);
  border-radius: 16px;
  padding: 4px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.view-option {
  flex: 1;
  text-align: center;
  padding: 8px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.view-option.active {
  background-color: var(--primary-color);
  color: white;
}

.month-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  font-weight: 600;
}

.month-header i {
  font-size: 20px;
  color: var(--primary-color);
  cursor: pointer;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background-color: var(--border-color);
  border-radius: 12px;
  overflow: hidden;
}

.day-cell {
  aspect-ratio: 1;
  background-color: var(--surface-background);
  padding: 4px;
  display: flex;
  flex-direction: column;
}

.day-number {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.day-events {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
  flex: 1;
}

.event-item {
  font-size: 10px;
  padding: 2px 4px;
  border-radius: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.more-events {
  font-size: 10px;
  color: var(--primary-color);
}

.current-day {
  background-color: rgba(0, 122, 255, 0.05);
}

.current-day .day-number {
  color: var(--primary-color);
}

.other-month {
  opacity: 0.5;
}

/* 周视图样式 */
.week-view {
  display: flex;
  flex-direction: column;
  height: 600px;
  overflow-y: auto;
  background-color: var(--surface-background);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.week-header {
  display: flex;
  position: sticky;
  top: 0;
  background-color: var(--surface-background);
  z-index: 1;
  border-bottom: 1px solid var(--border-color);
}

.time-column {
  width: 50px;
  flex-shrink: 0;
  border-right: 1px solid var(--border-color);
}

.week-day {
  flex: 1;
  text-align: center;
  padding: 8px 4px;
}

.day-name {
  font-size: 12px;
  color: var(--text-secondary);
}

.week-day.current-day {
  background-color: rgba(0, 122, 255, 0.05);
}

.week-content {
  display: flex;
  flex: 1;
  position: relative;
}

.time-slot {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--text-tertiary);
  border-bottom: 1px solid var(--border-color);
}

.week-grid {
  display: flex;
  flex: 1;
  position: relative;
}

.day-column {
  flex: 1;
  position: relative;
  border-right: 1px solid var(--border-color);
  height: 720px; /* 12小时 * 60px */
}

.week-event {
  position: absolute;
  left: 2px;
  right: 2px;
  padding: 4px;
  font-size: 11px;
  border-radius: 4px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 日视图样式 */
.day-view {
  display: flex;
  flex-direction: column;
  height: 600px;
  overflow-y: auto;
  background-color: var(--surface-background);
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.day-header {
  padding: 12px;
  text-align: center;
  font-weight: 600;
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  background-color: var(--surface-background);
  z-index: 1;
}

.day-timeline {
  display: flex;
  flex: 1;
}

.events-column {
  flex: 1;
  position: relative;
  height: 720px; /* 12小时 * 60px */
}

.day-event {
  position: absolute;
  left: 8px;
  right: 8px;
  padding: 6px;
  border-radius: 6px;
  overflow: hidden;
}

.event-time {
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 4px;
}

.event-title {
  font-size: 14px;
}

/* 事件类型样式 */
.event-meeting {
  background-color: rgba(0, 122, 255, 0.1);
  color: var(--primary-color);
}

.event-holiday {
  background-color: rgba(255, 149, 0, 0.1);
  color: var(--warning-color);
}

.event-task {
  background-color: rgba(52, 199, 89, 0.1);
  color: var(--success-color);
}

.event-personal {
  background-color: rgba(88, 86, 214, 0.1);
  color: rgb(88, 86, 214);
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
}

@media (max-width: 375px) {
  .weekday, .day-number {
    font-size: 12px;
  }
  
  .event-item {
    font-size: 9px;
    padding: 1px 2px;
  }
}
</style> 