<template>
  <header class="page-header">
    <a v-if="showBackButton" @click="goBack" class="back-button" aria-label="返回">
      <i class="ri-arrow-left-s-line"></i>
    </a>
    <h1 class="page-title">{{ title }}</h1>
    <slot name="actions"></slot>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router';

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  showBackButton: {
    type: Boolean,
    default: false
  }
});

const router = useRouter();

const goBack = () => {
  router.back();
};
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-button {
  color: var(--primary-color);
  font-size: 22px;
  display: flex;
  align-items: center;
  margin-right: 10px;
  cursor: pointer;
}

.page-title {
  flex: 1;
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}
</style> 