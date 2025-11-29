// 引入之前封装的 axios 工具（若没有，先看步骤 2.0）
import request from '@/utils/request';

/**
 * AI 分析日志
 * @param {string} logContent - 要分析的日志内容
 * @returns {Promise} - 响应结果
 */
export function aiAnalyzeLog(logContent) {
    return request({
        url: '/api/ai/analyze-log', // 后端 AI 接口地址
        method: 'post',
        data: { logContent } // 请求体（和后端接口参数对应）
    });
}

/**
 * AI 生成日志标题
 * @param {string} logContent - 日志内容
 * @returns {Promise} - 响应结果
 */
export function aiGenerateLogTitle(logContent) {
    return request({
        url: '/api/ai/generate-log-title',
        method: 'post',
        data: { logContent }
    });
}