import { get, post } from '@/utils/request';

/**
 * 用户登录
 * @param {Object} data 登录参数
 * @param {string} data.username 用户名
 * @param {string} data.password 密码
 * @returns {Promise} 返回Promise对象
 */
export function login(data) {
  return post('/login', data, {headers: {
    'Content-Type': 'application/x-www-form-urlencoded'
  }});
}

/**
 * 获取用户信息
 * @returns {Promise} 返回Promise对象
 */
export function getInfo() {
  return get('/info');
}

/**
 * 用户登出
 * @returns {Promise} 返回Promise对象
 */
export function logout() {
  return post('/logout');
}

/**
 * 刷新token
 * @param {string} refreshToken 刷新token
 * @returns {Promise} 返回Promise对象
 */
export function refreshToken(refreshToken) {
  return post('/auth/refresh-token', { refreshToken });
}

/**
 * 修改密码
 * @param {Object} data 密码参数
 * @param {string} data.oldPassword 旧密码
 * @param {string} data.newPassword 新密码
 * @returns {Promise} 返回Promise对象
 */
export function updatePassword(data) {
  return post('/auth/update-password', data);
}
