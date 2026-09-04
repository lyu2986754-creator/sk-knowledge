<template>
  <n-config-provider :locale="currentLocale">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <n-loading-bar-provider>
            <router-view />
          </n-loading-bar-provider>
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { NMessageProvider, NDialogProvider, NNotificationProvider, NLoadingBarProvider, NConfigProvider,zhCN } from 'naive-ui';
import { ref, computed } from 'vue'

// 存储当前语言标识
const currentLang = ref('zh')

// 根据当前语言切换对应的本地化配置
const currentLocale = computed(() => {
  switch(currentLang.value) {
    case 'en': return enUS
    default: return zhCN
  }
})
</script>

<style>
#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  min-height: 100vh;
  background-color: #f5f7fa;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
