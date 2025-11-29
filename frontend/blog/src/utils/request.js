import axios from 'axios';

// 创建 axios 实例
const request = axios.create({
    baseURL: 'http://localhost:8080', // 后端服务地址（本地开发）
    timeout: 10000, // AI 响应可能慢，超时设为 10 秒
    headers: {
        'Content-Type': 'application/json' // 默认 JSON 格式
    }
});

// 响应拦截器（统一处理结果）
request.interceptors.response.use(
    response => response.data, // 直接返回响应体
    error => {
        console.error('请求失败：', error);
        alert('接口请求失败，请重试！');
        return Promise.reject(error);
    }
);

export default request;