<template>
  <div class="files-container">
    <n-card :bordered="false" class="full-upload-card">
      <div class="upload-header">
        <n-select v-model:value="selectedKnowledgeBase" :options="knowledgeBases" :loading="knowledgeBaseloading"
          placeholder="请先选择知识库" clearable style="margin-bottom: 16px; width: 300px;" />
      </div>
      <n-upload multiple directory-dnd :show-file-list="false" :custom-request="handleUpload" class="full-upload"
        :disabled="!selectedKnowledgeBase || showProgress">
        <n-upload-dragger class="full-upload-dragger">
          <div class="upload-content">
            <n-icon size="64" :depth="3">
              <CloudUploadOutlined />
            </n-icon>
            <n-text style="font-size: 18px; margin-top: 16px; display: block">
              {{ showProgress ? '文件上传中，请稍候...' : '点击或者拖动文件到该区域来上传' }}
            </n-text>
            <n-p depth="3" style="margin: 12px 0 0 0; font-size: 14px">
              支持单个或批量上传
            </n-p>
          </div>
        </n-upload-dragger>
      </n-upload>

      <!-- 上传进度条 -->
      <div v-if="showProgress" class="upload-progress-container">
        <div class="progress-info">
          <span>{{ currentUploadFile }}</span>
          <span>{{ uploadProgress }}%</span>
        </div>
        <n-progress :percentage="uploadProgress" :show-indicator="false" />
      </div>

      <n-tabs type="line" animated v-model:value="activeTab" @update:value="handleTabChange">
        <n-tab-pane name="all" tab="全部文件">
          <file-list 
            :files="currentFiles" 
            :pagination="knowledgeDocPagination"
            :loading="loading"
            @refresh="fetchData"
            @update:page="handlePageChange"
            @update:page-size="handlePageSizeChange"
          />
        </n-tab-pane>
        <n-tab-pane name="trash" tab="回收站">
          <file-list 
            :files="trashFiles" 
            :show-actions="false" 
            :pagination="trashPagination"
            :loading="loading"
            @refresh="fetchTrashData"
            @update:page="handleTrashPageChange"
            @update:page-size="handleTrashPageSizeChange"
          />
        </n-tab-pane>
      </n-tabs>
    </n-card>


  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { CloudUploadOutlined } from '@vicons/antd';
import { NCard, NTabs, NTabPane, NUpload, NUploadDragger, NText, NIcon, NP, NSelect, useMessage, NProgress } from 'naive-ui';
import { getKnowledgeBase } from '@/api/knowledgeBase';
import { getKnowledgeDocPage,getTrashPage } from '@/api/knowledgeDoc';
import FileList from '@/components/FileList.vue';

const loading = ref(false);
const activeTab = ref('all');
const currentFiles = ref([]);
const trashFiles = ref([]);
const previewVisible = ref(false);
const currentFile = ref(null);
const uploadProgress = ref(0); // 上传进度
const showProgress = ref(false); // 是否显示进度条
const currentUploadFile = ref(''); // 当前上传的文件名

// 分页配置
const createPagination = () => ({
  page: 1,
  pageSize: 5,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [5, 10, 15, 20],
  showQuickJumper: true,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`
});

const knowledgeDocPagination = ref(createPagination());
const trashPagination = ref(createPagination());

// 获取全部文件
const fetchData = async () => {
  try {
    loading.value = true;
    const knowledgeId = route.params.knowledgeId;
    const params = {
      pageNum: knowledgeDocPagination.value.page,
      pageSize: knowledgeDocPagination.value.pageSize,
      knowledgeId: knowledgeId 
    };
    const data = await getKnowledgeDocPage(params);
    currentFiles.value = data.list || [];
    knowledgeDocPagination.value.itemCount = data.itemCount || 0;
  } catch (error) {
    console.error('获取文件列表失败:', error);
    message.error('获取文件列表失败');
  } finally {
    loading.value = false;
  }
};


// 获取回收站文件
const fetchTrashData = async () => {
  try {
    loading.value = true;
    const params = {
      pageNum: trashPagination.value.page,
      pageSize: trashPagination.value.pageSize
    };
    const data = await getTrashPage(params);
    trashFiles.value = data.list || [];
    
    trashPagination.value.itemCount = data.itemCount || 0;
  } catch (error) {
    console.error('获取回收站文件失败:', error);
    message.error('获取回收站文件失败');
  } finally {
    loading.value = false;
  }
};

// 标签页切换处理
const handleTabChange = (tabName) => {
  activeTab.value = tabName;
  switch (tabName) {
    case 'all':
      fetchData();
      break;
    case 'trash':
      fetchTrashData();
      break;
  }
};

// 分页变化处理
const handlePageChange = (page) => {
  knowledgeDocPagination.value.page = page;
  fetchData();
};

const handlePageSizeChange = (pageSize) => {
  knowledgeDocPagination.value.pageSize = pageSize;
  knowledgeDocPagination.value.page = 1;
  fetchData();
};


const handleTrashPageChange = (page) => {
  trashPagination.value.page = page;
  fetchTrashData();
};

const handleTrashPageSizeChange = (pageSize) => {
  trashPagination.value.pageSize = pageSize;
  trashPagination.value.page = 1;
  fetchTrashData();
};


// 预览文件
const previewFile = (file) => {
  currentFile.value = file;
  previewVisible.value = true;
};

// 知识库相关状态
const knowledgeBases = ref([]);
const selectedKnowledgeBase = ref(null);
const knowledgeBaseloading = ref(false);
const message = useMessage();

const route = useRoute();
// 获取知识库列表
const fetchKnowledgeBases = async () => {
  try {
    knowledgeBaseloading.value = true
    const response = await getKnowledgeBase();
    if (response && Array.isArray(response)) {
      knowledgeBases.value = response.map(kb => ({
        label: kb.knowledgeName,
        value: kb.id
      }));

      // 如果路由中有knowledgeId，则设置选中的知识库
      const knowledgeId = route.params.knowledgeId;
      if (knowledgeId) {
        // 确保knowledgeId是字符串类型，因为value可能被序列化为字符串
        selectedKnowledgeBase.value = knowledgeBases.value.find(
          kb => String(kb.value) === String(knowledgeId)
        )?.value || null;
      }
    }
  } catch (error) {
    console.error('获取知识库列表失败:', error)
    message.error('获取知识库列表失败')
  } finally {
    knowledgeBaseloading.value = false
  }
};

// 处理文件上传
// 在文件顶部导入axios
import axios from 'axios';

const handleUpload = async ({ file, onFinish, onError }) => {
  if (!selectedKnowledgeBase.value) {
    message.warning('请先选择知识库');
    onError();
    return;
  }

  try {
    const formData = new FormData();
    formData.append('file', file.file);
    formData.append('knowledgeId', selectedKnowledgeBase.value);

    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('未登录，请先登录');
    }

    const baseUrl = import.meta.env.VITE_API_BASE_URL
    
    // 显示进度条
    uploadProgress.value = 0;
    showProgress.value = true;
    currentUploadFile.value = file.name;

    // 使用axios上传文件
    const response = await axios.post(baseUrl + '/knowledge/doc', formData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total) {
          const progress = Math.round((progressEvent.loaded / progressEvent.total) * 100);
          uploadProgress.value = progress;
        }
      }
    });

    // 检查响应状态
    if (response.data.code != 200) {
      message.error('上传失败');
      return;
    }
    
    // 刷新文件列表
    message.success('文件上传成功');
    fetchData();
    onFinish();
  } catch (error) {

    
    // 处理错误
    console.error('上传失败:', error);
    const errorMsg = error.response?.data?.message || error.message || '文件上传失败';
    message.error(errorMsg);
    onError();
  }finally{
        // 确保在出错时也隐藏进度条
    showProgress.value = false;
    currentUploadFile.value = '';
  }
};


onMounted(() => {
  fetchKnowledgeBases();
  // 默认加载全部文件
  fetchData();
});

defineExpose({
  previewFile
});
</script>

<style scoped>
.files-container {
  padding: 24px;
  height: 100%;
}

.full-upload-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.full-upload {
  margin-bottom: 16px;
}

.full-upload-dragger {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.full-upload-dragger:hover {
  border-color: #1890ff;
  background-color: #f0f9ff;
}

.upload-content {
  text-align: center;
  padding: 40px 20px;
}

.upload-progress-container {
  margin-top: 16px;
  padding: 12px;
  background-color: #f5f5f5;
  border-radius: 6px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}
</style>
