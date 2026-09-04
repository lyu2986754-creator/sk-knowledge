import { ref } from 'vue';

export default function handlePagination(fetchDataCallback, options = {}) {
  // 默认配置
  const defaultOptions = {
    page: 1,
    pageSize: 10,
    pageSizes: [5, 10, 15, 20],
    showSizePicker: true,
    showQuickJumper: true,
    itemCount: 0,
  }

  // 合并默认配置和传入的选项
  const mergedOptions = { ...defaultOptions, ...options }

  // 分页状态
  const pagination = ref({
    page: mergedOptions.page,
    pageSize: mergedOptions.pageSize,
    itemCount: mergedOptions.itemCount,
    showSizePicker: mergedOptions.showSizePicker,
    pageSizes: mergedOptions.pageSizes,
    showQuickJumper: mergedOptions.showQuickJumper,
    // 自定义前缀显示总条数
    prefix: ({ itemCount }) => `共 ${itemCount} 条数据`,
    // 页码改变时触发
    onChange: (page) => {
      pagination.value.page = page;
      fetchDataCallback();
    },
    // 每页条数改变时触发
    onUpdatePageSize: (pageSize) => {
      pagination.value.pageSize = pageSize;
      pagination.value.page = 1; // 重置到第一页
      fetchDataCallback();
    }
  })

  // 重置分页到第一页
  const resetPagination = () => {
    pagination.value.page = 1;
  }

  // 更新总数
  const updateTotal = (total) => {
    pagination.value.itemCount = total;
  }

  // 获取分页参数
  const getPaginationParams = () => ({
    pageNum: pagination.value.page,
    pageSize: pagination.value.pageSize
  })

  return {
    pagination,
    resetPagination,
    updateTotal,
    getPaginationParams
  }
}
