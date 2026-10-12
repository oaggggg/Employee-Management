import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// Element Plus 中文语言包（分页、日期选择器等组件的文案）
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 全量引入 Element Plus，并指定中文
app.use(ElementPlus, { locale: zhCn })
app.use(createPinia())
app.use(router)

// 全局注册所有图标组件，模板里可直接写 <House /> 这种标签
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component)
}

app.mount('#app')
