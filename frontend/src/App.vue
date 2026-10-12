<template>
  <!-- 所有页面都通过路由渲染，布局页 layout/index.vue 也在这里 -->
  <router-view />
</template>

<style>
/* 全局基础样式：去掉浏览器默认边距，统一字体 */
* {
  box-sizing: border-box;
}

html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
  font-size: 14px;
  color: #303133;
  background-color: #f0f2f5;
}
</style>
