<script setup>
import { useRoute } from 'vue-router';
import 'remixicon/fonts/remixicon.css'
import '@/assets/css/apple-design.css'
import '@/assets/css/common.css'

const route = useRoute();
</script>

<template>
  <router-view />
</template>

<style>
/* 全局样式 */
body {
  margin: 0;
  min-height: 100vh;
  width: 100%;
  font-size: 16px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  overflow-x: hidden;
  background-color: var(--background-color);
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

* {
  box-sizing: border-box;
}
</style>
