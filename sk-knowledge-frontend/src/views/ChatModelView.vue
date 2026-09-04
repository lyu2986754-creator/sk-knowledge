<template>
  <div>
    <!-- 表格工具栏 -->
    <n-space style="margin-bottom: 16px;">
      <n-button type="primary" @click="handleAdd">
        <template #icon>
          <n-icon>
            <PlusOutlined />
          </n-icon>
        </template>
        添加模型
      </n-button>
      <n-button @click="handleRefresh">
        <template #icon>
          <n-icon>
            <ReloadOutlined />
          </n-icon>
        </template>
        刷新
      </n-button>
    </n-space>

    <!-- 数据表格 -->
    <n-data-table remote :columns="columns" :data="tableData" :pagination="chatModelPagination" :loading="loading"
      :row-key="row => row.id" bordered :scroll-x="1200" />

    <!-- 添加/编辑对话框 -->
    <n-modal v-model:show="showModal" :title="formData.id ? '编辑模型' : '添加模型'" preset="dialog" style="width: 600px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto"
        require-mark-placement="right-hanging">
        <n-form-item label="模型名称" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入模型名称" />
        </n-form-item>
        <n-form-item label="分类" path="category">
          <n-select v-model:value="formData.category" :options="categoryOptions" placeholder="请选择分类" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="formData.type" :options="modelTypeOptions" placeholder="请选择模型类型" />
        </n-form-item>
        <n-form-item label="API 主机" path="apiHost">
          <n-input v-model:value="formData.apiHost" placeholder="请输入API主机地址" />
        </n-form-item>
        <n-form-item label="API 密钥" path="apiKey">
          <n-input v-model:value="formData.apiKey" placeholder="请输入API密钥" />
        </n-form-item>
        <n-form-item label="价格" path="price">
          <n-input-number v-model:value="formData.price" :min="0" :step="0.01" style="width: 100%" />
        </n-form-item>
        <n-form-item label="描述" path="describe">
          <n-input v-model:value="formData.describe" type="textarea" placeholder="请输入模型描述" :autosize="{
            minRows: 3,
            maxRows: 5
          }" />
        </n-form-item>
        <n-form-item label="备注" path="remark">
          <n-input v-model:value="formData.remark" type="textarea" placeholder="请输入备注信息" :autosize="{
            minRows: 2,
            maxRows: 4
          }" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space>
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ formData.id ? '更新' : '添加' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import {

  NSpace,
  NButton,
  NIcon,

  NDataTable,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NSelect,
  useMessage,
  useDialog
} from 'naive-ui'
import {
  getChatModelPage,
  deleteChatModel,
  createChatModel,
  updateChatModel,
  getChatModelDetail
} from '@/api/chatModel'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined
} from '@vicons/antd'

import handlePagination from '@/utils/handlePagination'

import { formatDate } from '@/utils/dateUtil'

const message = useMessage()
const dialog = useDialog()
const formRef = ref(null)

// 表格数据
const tableData = ref([])
const loading = ref(false)
const showModal = ref(false)
const submitting = ref(false)

// 模型类型选项
const modelTypeOptions = [
  { label: 'Chat', value: 'chat' },
  { label: 'embedding', value: 'embedding' }
]

// 分类选项
const categoryOptions = [
  { label: 'GPT', value: 'gpt' },
  { label: 'Claude', value: 'claude' },
  { label: 'DeepSeek', value: 'deepseek' },
  { label: 'Qwen', value: 'qwen' }
]

// 表单数据
const formData = ref({
  id: null,
  category: 'gpt', // 默认选择GPT
  name: '',
  describe: '',
  price: 0,
  type: 'chat',
  apiHost: '',
  apiKey: '',
  remark: ''
})

// 表单验证规则
const rules = {
  category: { required: true, message: '请输入分类', trigger: 'blur' },
  name: { required: true, message: '请输入模型名称', trigger: 'blur' },
  type: { required: true, message: '请输入模型类型', trigger: 'blur' },
  apiHost: { required: true, message: '请输入API主机地址', trigger: 'blur' },
  apiKey: { required: true, message: '请输入API密钥', trigger: 'blur' }
}

// 初始化分页
const { pagination: chatModelPagination, updateTotal } = handlePagination(() => {
  fetchData()
})

// 获取数据
const fetchData = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: chatModelPagination.value.page,
      pageSize: chatModelPagination.value.pageSize
    }
    const data = await getChatModelPage(params)
    tableData.value = data.list;
    updateTotal(data.itemCount);
  } finally {
    loading.value = false;
  }
}

// 表格列定义
const columns = [
  { title: '模型名称', key: 'name', width: 180 },
  { title: '请求地址', key: 'apiHost', width: 300 },
  {
    title: '密钥', key: 'apiKey', width: 120,
    render: (row) => {
      if (!row.apiKey) return '';
      const key = String(row.apiKey);
      if (key.length <= 6) return '*'.repeat(6);
      return `${key.substring(0, 3)}******${key.substring(key.length - 3)}`;
    }
  },
  { title: '描述', key: 'describe', width: 200 },
  { title: '价格', key: 'price', width: 100, render: (row) => `¥${(row.price || 0).toFixed(2)}` },
  { title: '分类', key: 'category', width: 120 },
  { title: '类型', key: 'type', width: 100 },
  { title: '备注', key: 'remark', width: 200 },
  {
    title: '操作', key: 'action', align: 'center', width: 180,
    render: (row) => h('div', { class: 'action-buttons' }, [
      h(NButton, {
        size: 'small',
        type: 'info',
        onClick: () => handleEdit(row)
      }, {
        icon: () => h(NIcon, null, { default: () => h(EditOutlined) })
      }),
      h(NButton, {
        size: 'small',
        type: 'error',
        onClick: () => handleDelete(row),
        style: 'margin-left: 8px;'
      }, {
        icon: () => h(NIcon, null, { default: () => h(DeleteOutlined) })
      })
    ])
  }
]

// 添加模型
const handleAdd = () => {
  formData.value = {
    id: null,
    category: '',
    name: '',
    describe: '',
    price: 0,
    type: '',
    apiHost: '',
    apiKey: '',
    remark: ''
  }
  showModal.value = true
}

// 编辑模型
const handleEdit = async (row) => {
  try {
    loading.value = true
    const res = await getChatModelDetail(row.id)
    if (res) {
      formData.value = { ...res }
      let key = String(formData.value.apiKey);
      if (key.length <= 6) {
        key = '*'.repeat(6)
      } else {
        key = `${key.substring(0, 3)}******${key.substring(key.length - 3)}`
      }
      formData.value.apiKey = key
      showModal.value = true
    }
  } catch (error) {
    console.error('获取模型详情失败:', error)
    message.error('获取模型详情失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = () => {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      try {
        submitting.value = true
        const result = formData.value.id
          ? updateChatModel(formData.value)
          : createChatModel(formData.value)

        const res = await result

        if (res) {
          message.success(formData.value.id ? '更新成功' : '添加成功')
          showModal.value = false
          fetchData()
        }
      } catch (error) {
        console.error('操作失败:', error)
        message.error(error.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 刷新数据
const handleRefresh = () => {
  chatModelPagination.value.page = 1
  fetchData()
}

// 删除模型
const handleDelete = (row) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除模型 "${row.name}" 吗？此操作不可恢复。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await deleteChatModel(row.id)
        if (res.code === 200) {
          message.success('删除成功')
          fetchData()
        } else {
          message.error(res.msg || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error)
        message.error('删除失败，请稍后重试')
      }
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.chat-sider {
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
}

.sider-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .chat-sider {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 1000;
  }
}
</style>