import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import { setupNaiveUI } from './plugins/naive-ui'
import { createDiscreteApi} from 'naive-ui'

// 创建全局的 message 和 dialog 实例
const { message, dialog, notification, loadingBar } = createDiscreteApi(
  ['message', 'dialog', 'notification', 'loadingBar']
)

const app = createApp(App)

// 全局挂载 Naive UI 组件
setupNaiveUI(app)
// 全局挂载 router
app.use(router)

// 全局挂载 message 和 dialog
app.config.globalProperties.$message = message
app.config.globalProperties.$dialog = dialog
app.config.globalProperties.$notification = notification
app.config.globalProperties.$loadingBar = loadingBar

app.mount('#app')