import axios from 'axios'; // 需安装 axios：npm install axios

// 创建 axios 实例（统一配置请求基础路径、超时等）
const service = axios.create({
    // baseURL: import.meta.env.VITE_API_BASE_URL || '/', // 从环境变量读取基础路径
    baseURL: process.env.VUE_APP_BASE_URL || 'http://localhost:8080',
    timeout: 30000, // 超时时间 30 秒（AI 响应可能较慢）
    headers: {
        'Content-Type': 'application/json'
    }
});

/**
 * 通用 AI 聊天接口（支持多轮对话）
 * @param {Array} chatHistory - 对话历史：[{ role: 'user/assistant', content: '消息内容' }]
 * @param {string} newMessage - 用户新发送的消息
 * @returns {Promise} - 响应结果
 */
export const aiChat = (chatHistory, newMessage) => {
    return service({
        url: '/api/ai/chat', // 对应后端新增的 /chat 接口
        method: 'post',
        data: {
            chatHistory,
            newMessage
        }
    });
};

/**
 * 原有日志分析接口（保留，供其他页面使用）
 * @param {string} logContent - 日志内容
 * @returns {Promise} - 响应结果
 */
export const aiAnalyzeLog = (logContent) => {
    return service({
        url: '/api/ai/analyze-log',
        method: 'post',
        data: { logContent }
    });
};