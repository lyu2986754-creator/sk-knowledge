import {
  create,
  NButton,
  NInput,
  NForm,
  NFormItem,
  zhCN
} from 'naive-ui'


export function setupNaiveUI(app) {
  const naive = create({
    components: [
      NButton,
      NInput,
      NForm,
      NFormItem
      // 注册全局组件
    ]
  })
  app.use(naive)
}