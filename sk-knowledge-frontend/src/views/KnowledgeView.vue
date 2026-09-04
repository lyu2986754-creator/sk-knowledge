<template>
  <div>
    <n-button type="primary" @click="showCreateModal = true">添加知识库</n-button>
    <n-grid :x-gap="8" :y-gap="8" :cols="4" item-responsive responsive="screen">
      <n-gi v-for="item in tableData" :key="item.id">
        <n-card :title="item.knowledgeName" closable @close="handleDelete(item)" hoverable class="knowledge-card">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <p class="" style="margin: 0;">{{ item.description || '无描述' }}</p>
            <n-button type="info" @click="handleKnoweledgeDoc(item.id)">查看文件</n-button>
          </div>
        </n-card>
      </n-gi>
    </n-grid>



    <!-- 添加知识库弹窗 -->
    <n-modal v-model:show="showCreateModal" style="width: 600px;">
      <n-card style="width: 600px" title="模态框" :bordered="false" size="huge" role="dialog" aria-modal="true">
        <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto"
          require-mark-placement="right-hanging" size="medium">
          <n-form-item label="名称" path="knowledgeName">
            <n-input v-model:value="formData.knowledgeName" placeholder="请输入知识库名称" />
          </n-form-item>

          <n-form-item label="知识分隔符" path="knowledgeSeparator">
            <n-input v-model:value="formData.knowledgeSeparator" placeholder="请输入知识分隔符" />
          </n-form-item>

          <n-form-item label="检索条数" path="retrieveLimit">
            <n-input-number v-model:value="formData.retrieveLimit" :min="1" :max="100" placeholder="请输入检索条数"
              style="width: 100%" />
          </n-form-item>

          <n-form-item label="文本块大小" path="textBlockSize">
            <n-input-number v-model:value="formData.textBlockSize" :min="100" :max="10000" placeholder="请输入文本块大小"
              style="width: 100%" />
          </n-form-item>

          <n-form-item label="向量库" path="vectorModelId">
            <n-select v-model:value="formData.vectorModelId" :options="vectorModels" placeholder="请选择向量库"
              :loading="loading.vectorModels" filterable clearable
              @focus="!vectorModels.length && fetchVectorModels()" />
          </n-form-item>

          <n-form-item label="向量模型" path="embeddingModelId">
            <n-select v-model:value="formData.embeddingModelId" :options="embeddingModels" placeholder="请选择向量模型"
              :loading="loading.embeddingModels" filterable clearable
              @focus="!embeddingModels.length && fetchEmbeddingModels()" />
          </n-form-item>

          <n-form-item label="重叠字符" path="overlapChar">
            <n-input v-model:value="formData.overlapChar" placeholder="请输入重叠字符" />
          </n-form-item>

          <n-form-item label="提问分隔符" path="questionSeparator">
            <n-input v-model:value="formData.questionSeparator" placeholder="请输入提问分隔符" />
          </n-form-item>

          <n-form-item label="是否公开" path="share">
            <n-switch :value="formData.share === 1" @update:value="val => { formData.share = val ? 1 : 0 }" />
          </n-form-item>

          <n-form-item label="描述" path="description">
            <n-input v-model:value="formData.description" type="textarea" :autosize="{
              minRows: 3,
              maxRows: 5
            }" placeholder="请输入描述信息" />
          </n-form-item>
        </n-form>

        <template #action>
          <n-space>
            <n-button @click="handleCancel">取消</n-button>
            <n-button type="primary" :loading="submitting" @click="handleSubmit">
              确定
            </n-button>
          </n-space>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useMessage, useDialog, NModal, NSwitch, NGi, NGrid, NForm, NFormItem, NInput, NInputNumber, NSelect, NSpace, NButton, NCard } from 'naive-ui';
import { useRouter } from 'vue-router';
import { getVectorModels, getEmbeddingModels, createKnowledge, getKnowledgeBase, deleteKnowledgeBase } from '@/api/knowledgeBase';

const message = useMessage();
const dialog = useDialog()
const formRef = ref(null);
const showCreateModal = ref(false);
const submitting = ref(false);

// 表单数据
const formData = reactive({
  knowledgeName: '',
  knowledgeSeparator: '\n',
  retrieveLimit: 5,
  textBlockSize: 1000,
  vectorModelId: null,
  embeddingModelId: null,
  overlapChar: '\n',
  questionSeparator: '\n',
  share: 1,
  description: ''
});

const tableData = ref([]);

// 加载状态
const loading = reactive({
  vectorModels: false,
  embeddingModels: false,
  knowledgeBase: false
});

// 下拉选项
const vectorModels = ref([]);
const embeddingModels = ref([]);

// 表单验证规则
const rules = {
  knowledgeName: [
    { required: true, message: '请输入知识库名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
  ],
};

// 获取向量库列表
const fetchVectorModels = async () => {
  try {
    loading.vectorModels = true
    const data = await getVectorModels()

    vectorModels.value = data.map(item => ({
      label: item.vectorName + '-' + item.host + ':' + item.port,
      value: item.id
    }));
  } catch (error) {
    console.error('获取向量库列表失败:', error);
    message.error('获取向量库列表失败');
  } finally {
    loading.vectorModels = false;
  }
};

// 获取向量模型列表
const fetchEmbeddingModels = async () => {
  try {
    loading.embeddingModels = true;
    const data = await getEmbeddingModels({ type: 'embedding' });
    embeddingModels.value = data.map(item => ({
      label: item.name,
      value: item.id
    }));
  } catch (error) {
    console.error('获取向量模型列表失败:', error);
    message.error('获取向量模型列表失败');
  } finally {
    loading.embeddingModels = false;
  }
};

// 提交表单
const handleSubmit = () => {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      try {
        submitting.value = true;
        await createKnowledge({ ...formData });
        message.success('添加成功');
        showCreateModal.value = false;
        // 重置表单
        formRef.value?.restoreValidation();
        Object.assign(formData, {
          knowledgeName: '',
          knowledgeSeparator: '\n',
          retrieveLimit: 5,
          textBlockSize: 1000,
          vectorModelId: null,
          embeddingModelId: null,
          overlapChar: '\n',
          questionSeparator: '\n',
          share: 1,
          description: ''
        });
        // 刷新列表
        fetchData()
      } catch (error) {
        console.error('添加知识库失败:', error);
        message.error('添加知识库失败');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 取消
const handleCancel = () => {
  showCreateModal.value = false;
  formRef.value?.restoreValidation();
};

//删除知识库
const handleDelete = (item) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除知识库 "${item.knowledgeName}" 吗？该操作同时会删除该知识库下的文件。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await deleteKnowledgeBase(item.id)
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

const router = useRouter()

// 查看知识库下的文件
const handleKnoweledgeDoc = (knowledgeId) => {
  router.push({
    name: 'files',
    params: { knowledgeId }
  });
}


// 获取数据
const fetchData = async () => {
  try {
    loading.knowledgeBase = true

    const data = await getKnowledgeBase()
    tableData.value = data;
  } finally {
    loading.knowledgeBase = false;
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.knowledge-card {
	border-radius: 15px;
	overflow: hidden;
	transition: all 0.3s ease;
	box-shadow: 0 5px 14px rgba(0, 0, 0, 0.10);
	margin: 10px; 
}

.knowledge-card:hover {
	transform: translateY(-10px); 
	box-shadow: 0 10px 25px rgba(0, 0, 0, 0.20);
}
</style>
