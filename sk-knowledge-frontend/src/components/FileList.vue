<template>
  <div class="file-list">
    <n-data-table :columns="columns" :data="files" :pagination="pagination" :bordered="false" :loading="loading"
      :remote="true" @update:page="handlePageChange" @update:page-size="handlePageSizeChange">
      <template #empty>
        <n-empty description="暂无文件" />
      </template>
    </n-data-table>
  </div>
</template>

<script setup>
import { NButton, useDialog, NDataTable, NEmpty, NIcon } from 'naive-ui';
import { h } from 'vue';
import { FilePdfOutlined, FileWordOutlined, FilePptOutlined, FileMarkdownOutlined, FileExcelOutlined, FileTextOutlined, FileImageOutlined, FileZipOutlined, FileUnknownOutlined } from '@vicons/antd';
import { deleteDoc,recoverDoc } from '@/api/knowledgeDoc';
import { saveAs } from 'file-saver';
import { useMessage } from 'naive-ui';

const props = defineProps({
  files: {
    type: Array,
    default: () => []
  },
  showActions: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  pagination: {
    type: Object,
    default: () => ({
      page: 1,
      pageSize: 5,
      itemCount: 0,
      showSizePicker: true,
      pageSizes: [5, 10, 15, 20],
      showQuickJumper: true
    })
  }
});

const emit = defineEmits(['refresh', 'update:page', 'update:page-size']);

const columns = [
  {
    title: '文件名',
    key: 'docName',
    width: 200
  },
  {
    title: '知识库',
    key: 'knowledgeName',
    width: 150
  },
  {
    title: '类型',
    key: 'docType',
    width: 100,
    render: (row) => {
      const fileType = (row.docType || '').toLowerCase();
      const fileTypeMap = {
        'pdf': { icon: FilePdfOutlined, color: '#ff4d4f', name: 'PDF' },
        'doc': { icon: FileWordOutlined, color: '#1890ff', name: 'Word' },
        'docx': { icon: FileWordOutlined, color: '#1890ff', name: 'Word' },
        'ppt': { icon: FilePptOutlined, color: '#ff7a45', name: 'PPT' },
        'pptx': { icon: FilePptOutlined, color: '#ff7a45', name: 'PPT' },
        'xls': { icon: FileExcelOutlined, color: '#52c41a', name: 'Excel' },
        'xlsx': { icon: FileExcelOutlined, color: '#52c41a', name: 'Excel' },
        'md': { icon: FileMarkdownOutlined, color: '#722ed1', name: 'Markdown' },
        'txt': { icon: FileTextOutlined, color: '#8c8c8c', name: 'Text' },
        'jpg': { icon: FileImageOutlined, color: '#13c2c2', name: 'Image' },
        'jpeg': { icon: FileImageOutlined, color: '#13c2c2', name: 'Image' },
        'png': { icon: FileImageOutlined, color: '#13c2c2', name: 'Image' },
        'gif': { icon: FileImageOutlined, color: '#13c2c2', name: 'Image' },
        'zip': { icon: FileZipOutlined, color: '#fa8c16', name: 'ZIP' },
        'rar': { icon: FileZipOutlined, color: '#fa8c16', name: 'RAR' },
        '7z': { icon: FileZipOutlined, color: '#fa8c16', name: '7Z' }
      };

      const typeInfo = fileTypeMap[fileType] || { icon: FileUnknownOutlined, color: '#8c8c8c', name: fileType || '未知' };

      return h(
        'div',
        {
          style: 'display: flex; align-items: center; gap: 8px;',
          title: typeInfo.name
        },
        [
          h(
            NIcon,
            {
              component: typeInfo.icon,
              color: typeInfo.color,
              size: 18
            }
          ),
          typeInfo.name
        ]
      );
    }
  },
  {
    title: '大小',
    key: 'docSize',
    width: 120,
    render: (row) => {
      if (row.docSize === undefined || row.docSize === null) return '-';

      const bytes = Number(row.docSize);
      if (isNaN(bytes)) return '-';

      const units = ['B', 'KB', 'MB', 'GB'];
      let size = bytes;
      let unitIndex = 0;

      while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024;
        unitIndex++;
      }

      // 保留1位小数，如果小数部分为0则不显示
      const formattedSize = size % 1 === 0 ? size.toString() : size.toFixed(1);
      return `${formattedSize} ${units[unitIndex]}`;
    }
  }
];

// 如果是回收站，不显示操作列
if (props.showActions) {
  columns.push(  {
    title: '创建时间',
    key: 'createTime',
    width: 180
  })
  columns.push({
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row) => {
      return h('div', { class: 'actions' }, [
        h(
          NButton,
          {
            text: true,
            type: 'primary',
            size: 'small',
            onClick: () => handleDownload(row)
          },
          { default: () => '下载' }
        ),
        h(
          NButton,
          {
            text: true,
            type: 'error',
            size: 'small',
            style: 'margin-left: 8px',
            onClick: () => handleDelete(row)
          },
          { default: () => '删除' }
        )
      ])
    }
  })
}else{
  columns.push({
    title: '剩余时间',
    key: 'trashTime',
    width: 180,
    render: (row) => {
      if (!row.trashTime) return '-';
      const trashDate = new Date(row.trashTime);
      const now = new Date();
      const diffTime = Math.abs(now - trashDate);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      const remainingDays = Math.max(0, 30 - diffDays);
      return `${remainingDays} 天`;
    }
  })

  columns.push({
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row) => {
      return h('div', { class: 'actions' }, [
        h(
          NButton,
          {
            text: true,
            type: 'primary',
            size: 'small',
            onClick: () => handleRecover(row)
          },
          { default: () => '还原' }
        )
      ])
    }
  })
}


//还原
const handleRecover = async (file) => {
  try {
    dialog.warning({
      title: '确认还原文件',
      content: `确定要将文件 "${file.docName}" 还原吗？`,
      positiveText: '确认',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          await recoverDoc(file.id)
          message.success('还原成功')
          emit('refresh')
        } catch (error) {
          console.error('失败:', error)
          message.error(`失败: ${error.message || '未知错误'}`)
        }
      }
    });
  } catch (error) {
    console.error('失败:', error);
    message.error(`失败: ${error.message || '未知错误'}`);
  }
};

// 使用传入的分页配置

const handleDownload = async (file) => {

  const token = localStorage.getItem('token');
  if (!token) {
    throw new Error('未登录，请先登录');
  }

  const baseUrl = import.meta.env.VITE_API_BASE_URL

  const response = await fetch(baseUrl + '/knowledge/download?fileName=' + file.url, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  const blob = await response.blob();

  saveAs(blob, file.docName)

};

const message = useMessage();
const dialog = useDialog();

const handleDelete = async (file) => {
  try {
    dialog.warning({
      title: '确认将文件放入回收站',
      content: `确定要将文件 "${file.docName}" 放入回收站吗？30天之内可以恢复。`,
      positiveText: '确认',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          await deleteDoc(file.id);
          message.success('放入回收站成功');
          emit('refresh');
        } catch (error) {
          console.error('失败:', error);
          message.error(`失败: ${error.message || '未知错误'}`);
        }
      }
    });
  } catch (error) {
    console.error('失败:', error);
    message.error(`失败: ${error.message || '未知错误'}`);
  }
};

const handlePageChange = (page) => {
  emit('update:page', page);
};

const handlePageSizeChange = (pageSize) => {
  emit('update:page-size', pageSize);
};
</script>

<style scoped>
.file-list {
  margin-top: 16px;
}


.actions {
  display: flex;
  gap: 8px;
}
</style>
