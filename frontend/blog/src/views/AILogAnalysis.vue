<template>
  <div class="ai-log-analysis">
    <h2>AI 日志分析工具</h2>
    <!-- 日志输入区域 -->
    <div class="input-area">
      <label>请输入日志内容：</label>
      <textarea
          v-model="logContent"
          placeholder="粘贴需要分析的 Spring Boot/MySQL 日志..."
          rows="10"
      ></textarea>
      <!-- 功能按钮 -->
      <div class="btn-group">
        <button @click="handleAnalyze" :disabled="isLoading">
          <span v-if="isLoading">分析中...</span>
          <span v-else>AI 分析日志</span>
        </button>
        <button @click="handleGenerateTitle" :disabled="isLoading || !logContent">
          生成日志标题
        </button>
        <button @click="logContent = ''">清空输入</button>
      </div>
    </div>
    <!-- AI 结果展示区域 -->
    <div class="result-area" v-if="resultVisible">
      <h3>AI 处理结果：</h3>
      <div class="result-card" v-if="success">
        <div v-if="isAnalyzeResult">
          <p>{{ aiResult }}</p>
        </div>
        <div v-else>
          <h4>生成的标题：{{ aiTitle }}</h4>
        </div>
      </div>
      <div class="error-card" v-else>
        <p>❌ {{ errorMsg }}</p>
      </div>
    </div>
  </div>
</template>
<script>
// 引入 AI 接口函数
import { aiAnalyzeLog, aiGenerateLogTitle } from '@/api/ai';

export default {
  name: 'AILogAnalysis',
  data() {
    return {
      logContent: '', // 输入的日志内容
      isLoading: false, // 加载状态
      resultVisible: false, // 结果是否显示
      success: false, // 接口是否成功
      aiResult: '', // AI 分析结果
      aiTitle: '', // AI 生成的标题
      errorMsg: '', // 错误信息
      isAnalyzeResult: true // 是否是分析结果（区分标题/分析）
    };
  },
  methods: {
    // 处理 AI 分析日志
    async handleAnalyze() {
      if (!this.logContent.trim()) {
        alert('请输入日志内容！');
        return;
      }

      this.isLoading = true;
      this.resultVisible = false;
      this.isAnalyzeResult = true;

      try {
        // 调用 AI 分析接口
        const res = await aiAnalyzeLog(this.logContent);
        this.success = res.success;
        if (res.success) {
          this.aiResult = res.result; // 展示 AI 分析结果
        } else {
          this.errorMsg = res.message || '分析失败';
        }
        this.resultVisible = true;
      } catch (error) {
        this.success = false;
        this.errorMsg = '网络错误或 AI 服务异常';
        this.resultVisible = true;
      } finally {
        this.isLoading = false;
      }
    },

    // 处理 AI 生成标题
    async handleGenerateTitle() {
      this.isLoading = true;
      this.resultVisible = false;
      this.isAnalyzeResult = false;

      try {
        const res = await aiGenerateLogTitle(this.logContent);
        this.success = res.success;
        if (res.success) {
          this.aiTitle = res.title; // 展示生成的标题
        } else {
          this.errorMsg = res.message || '生成标题失败';
        }
        this.resultVisible = true;
      } catch (error) {
        this.success = false;
        this.errorMsg = '网络错误或 AI 服务异常';
        this.resultVisible = true;
      } finally {
        this.isLoading = false;
      }
    }
  }
};
</script>
<style scoped>
.ai-log-analysis {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.input-area {
  margin-bottom: 30px;
}

textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin: 10px 0;
  font-size: 14px;
  resize: vertical;
}

.btn-group {
  display: flex;
  gap: 10px;
}

button {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  background-color: #42b983;
  color: white;
  cursor: pointer;
  font-size: 14px;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.result-area {
  padding: 20px;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.result-card {
  color: #333;
  line-height: 1.8;
}

.error-card {
  color: #e53e3e;
}
</style>
