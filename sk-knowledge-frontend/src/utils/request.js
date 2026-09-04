import axios from 'axios';
import { createDiscreteApi } from 'naive-ui';

// 创建独立的 message 实例
const { message } = createDiscreteApi(['message']);

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 100000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    // 对请求错误做些什么
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;    
    
    // 这里根据后端返回的数据结构进行调整
    if (res.code !== 200) {
      const errorMsg = res.msg || '请求失败';

      // 401: 未授权，跳转到登录页
      if (res.code === 401) {
        localStorage.removeItem('token');
        window.location.href = '/login';
      } else {
        // 显示错误消息
        message.error(errorMsg);
      }

      const error = new Error(errorMsg);
      error.code = res.code;
      
      return Promise.reject(error);
    } else {
      if(res.data){       
        return res.data;
      }
      return res;
    }
  },
  error => {
    console.error('Response Error:', error);
    let errorMessage = '请求失败';
    
    if (error.response) {
      // 服务器返回了响应，但状态码不在 2xx 范围内
      const { status, data } = error.response;
      
      switch (status) {
        case 400:
          errorMessage = data.message || '请求参数错误';
          break;
        case 401:
          errorMessage = '未授权，请重新登录';
          localStorage.removeItem('token');
          window.location.href = '/login';
          break;
        case 403:
          errorMessage = '拒绝访问';
          break;
        case 404:
          errorMessage = '请求的资源不存在';
          break;
        case 500:
          errorMessage = '服务器内部错误';
          break;
        default:
          errorMessage = data?.message || `连接错误: ${status}`;
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      errorMessage = '服务器未响应，请检查网络连接';
    } else {
      // 请求设置出错
      errorMessage = error.message || '请求设置出错';
    }
    
    // 显示错误提示
    message.error(errorMessage);
    
    return Promise.reject(new Error(errorMessage));
  }
);

/**
 * GET 请求
 * @param {string} url 请求地址
 * @param {object} params 请求参数
 * @param {object} config 其他配置
 * @returns {Promise} Promise 对象
 */
const get = (url, params = {}, config = {}) => {
 
  return service({
    method: 'get',
    url,
    params,
    ...config
  });
};

/**
 * POST 请求
 * @param {string} url 请求地址
 * @param {object} data 请求体数据
 * @param {object} config 其他配置
 * @returns {Promise} Promise 对象
 */
const post = (url, data = {}, config = {}) => {
  return service({
    method: 'post',
    url,
    data,
    ...config
  });
};

/**
 * PUT 请求
 * @param {string} url 请求地址
 * @param {object} data 请求体数据
 * @param {object} config 其他配置
 * @returns {Promise} Promise 对象
 */
const put = (url, data = {}, config = {}) => {
  return service({
    method: 'put',
    url,
    data,
    ...config
  });
};

/**
 * DELETE 请求
 * @param {string} url 请求地址
 * @param {object} params 请求参数
 * @param {object} config 其他配置
 * @returns {Promise} Promise 对象
 */
const del = (url, params = {}, config = {}) => {
  return service({
    method: 'delete',
    url,
    params,
    ...config
  });
};

/**
 * 上传文件
 * @param {string} url 上传地址
 * @param {FormData} formData 表单数据
 * @param {object} config 其他配置
 * @returns {Promise} Promise 对象
 */
const upload = (url, formData, config = {}) => {
  return service({
    method: 'post',
    url,
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
      ...(config.headers || {})
    },
    ...config
  });
};

// 导出所有方法
export {
  service,
  get,
  post,
  put,
  del,
  upload
};

// 同时保持默认导出以保持向后兼容
export default {
  service,
  get,
  post,
  put,
  del,
  upload
};
