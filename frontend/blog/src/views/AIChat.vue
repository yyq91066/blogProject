<template>
  <div class="ai-chat-container">
    <!-- 顶部导航栏 -->
    <div class="chat-header">
      <h2>AI 聊天助手</h2>
      <button @click="handleClearChat" class="clear-btn" :disabled="isLoading">
        清空聊天
      </button>
    </div>

    <!-- 聊天消息区域 -->
    <div class="chat-message-container" ref="messageContainer">
      <!-- 欢迎提示（无聊天记录时显示） -->
      <div class="welcome-tip" v-if="chatHistory.length === 0 && !isLoading">
        <p>👋 你好！我是你的 AI 聊天助手</p>
        <p>可以问我任何问题（技术、生活、学习等），我会为你详细解答～</p>
      </div>

      <!-- 聊天消息列表 -->
      <div
          class="chat-message"
          v-for="(msg, index) in chatHistory"
          :key="index"
          :class="msg.role === 'user' ? 'user-message' : 'ai-message'"
      >
        <!-- 头像 -->
        <div class="avatar">
          <span v-if="msg.role === 'user'">我</span>
          <span v-else>AI</span>
        </div>
        <!-- 消息内容 -->
        <div class="message-content">{{ msg.content }}</div>
      </div>

      <!-- 加载中提示 -->
      <div class="loading" v-if="isLoading">
        <div class="spinner"></div>
        <p>AI 正在思考...</p>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-container">
      <textarea
          v-model.trim="newMessage"
          placeholder="输入你想聊的内容..."
          @keyup.enter.prevent="handleSendMessage"
          :disabled="isLoading"
          class="message-input"
          rows="3"
      ></textarea>
      <button
          @click="handleSendMessage"
          class="send-btn"
          :disabled="isLoading || !newMessage.trim()"
      >
        发送
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick } from 'vue';
import { aiChat } from '@/api/ai'; // 导入聊天接口（后续会写）

// 聊天历史：[{ role: 'user/assistant', content: '消息内容' }]
const chatHistory = ref([]);
// 用户输入的新消息
const newMessage = ref('');
// 加载状态
const isLoading = ref(false);
// 消息容器（用于滚动到底部）
const messageContainer = ref(null);

// 监听聊天历史变化，自动滚动到底部
watch(chatHistory, () => {
  scrollToBottom();
}, { deep: true });

// 页面挂载后，初始化滚动位置
onMounted(() => {
  scrollToBottom();
  // 从本地存储读取历史聊天记录（可选，优化体验）
  const savedHistory = localStorage.getItem('aiChatHistory');
  if (savedHistory) {
    chatHistory.value = JSON.parse(savedHistory);
  }
});

// 自动滚动到最新消息
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
    }
  });
};

// 发送消息
// 发送消息
const handleSendMessage = async () => {
  const message = newMessage.value.trim();
  if (!message || isLoading.value) return;

  // 1. 添加用户消息到历史记录
  const userMsg = { role: 'user', content: message };
  chatHistory.value.push(userMsg);
  newMessage.value = ''; // 清空输入框
  isLoading.value = true;

  try {
    // 修复点1：过滤无效历史消息，确保格式正确（和 Postman 一致）
    const validHistory = chatHistory.value.slice(0, -1)
        .filter(msg => msg?.role && msg?.content) // 只保留有 role 和 content 的消息
        .map(msg => ({
          role: msg.role === 'user' ? 'user' : 'assistant', // 强制 role 只能是 user/assistant
          content: msg.content.trim() // 去除内容前后空格
        }));

    // 2. 调用 AI 聊天接口（传递格式化后的历史对话）
    // const res = await aiChat(validHistory, message);
    const { data } = await aiChat(validHistory, message);
    console.log("接口返回完整数据：", data);

    // 修复点2：打印响应，方便排查（可选，但推荐）
   // console.log('接口返回完整数据：', res);

    // 修复点3：判断响应是否有效，添加兜底字段
    // const isSuccess = res?.success ?? false;
    // const aiResponse = res?.aiResponse ?? '';
    // const errorMsg = res?.message ?? '接口响应异常，请重试';
    const isSuccess = data?.success ?? false;
    const aiResponse = data?.aiResponse ?? '';
    const errorMsg = data?.message ?? '接口响应异常，请重试';


    if (isSuccess && aiResponse) {
      // 3. 添加 AI 回复到历史记录
      chatHistory.value.push({
        role: 'assistant',
        content: aiResponse
      });
      // 保存到本地存储（页面刷新不丢失）
      localStorage.setItem('aiChatHistory', JSON.stringify(chatHistory.value));
    } else {
      // 失败回滚：移除刚添加的用户消息
      chatHistory.value.pop();
      alert(`发送失败：${errorMsg}`); // 用兜底的 errorMsg
    }
  } catch (error) {
    console.error('聊天接口异常：', error);
    // 失败回滚：移除刚添加的用户消息
    chatHistory.value.pop();
    // 修复点4：网络错误也添加兜底提示
    alert(`发送失败：${error.message || '网络错误，请检查后端是否启动'}`);
  } finally {
    isLoading.value = false;
  }
};

// 清空聊天记录
const handleClearChat = () => {
  if (confirm('确定要清空所有聊天记录吗？')) {
    chatHistory.value = [];
    localStorage.removeItem('aiChatHistory'); // 清除本地存储
  }
};
</script>

<style scoped>
.ai-chat-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 20px;
  font-family: 'Microsoft YaHei', 'Inter', sans-serif;
}

/* 顶部导航 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.chat-header h2 {
  font-size: 20px;
  color: #2c3e50;
  margin: 0;
}

.clear-btn {
  padding: 6px 12px;
  border: 1px solid #e74c3c;
  border-radius: 6px;
  background: transparent;
  color: #e74c3c;
  cursor: pointer;
  transition: all 0.3s;
}

.clear-btn:hover:enabled {
  background: #e74c3c;
  color: white;
}

.clear-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* 消息容器 */
.chat-message-container {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 12px;
  background-color: #f9f9f9;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 欢迎提示 */
.welcome-tip {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.welcome-tip p {
  margin: 8px 0;
  font-size: 15px;
}

/* 聊天消息 */
.chat-message {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 80%;
}

/* 用户消息（靠右） */
.user-message {
  margin-left: auto;
  flex-direction: row-reverse;
}

/* 头像 */
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #42b983;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

.ai-message .avatar {
  background-color: #2c3e50;
}

/* 消息内容 */
.message-content {
  padding: 12px 16px;
  border-radius: 18px;
  background-color: white;
  color: #333;
  line-height: 1.6;
  font-size: 15px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.user-message .message-content {
  background-color: #42b983;
  color: white;
  border-top-right-radius: 4px;
}

.ai-message .message-content {
  border-top-left-radius: 4px;
}

/* 加载中 */
.loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
  margin: 10px auto;
}

/* 加载动画 */
.spinner {
  width: 20px;
  height: 20px;
  border: 3px solid #ddd;
  border-top: 3px solid #42b983;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 输入区域 */
.chat-input-container {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 12px;
  resize: none;
  font-size: 15px;
  transition: border-color 0.3s;
}

.message-input:focus {
  outline: none;
  border-color: #42b983;
}

.message-input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.send-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  background-color: #42b983;
  color: white;
  font-size: 15px;
  cursor: pointer;
  transition: background-color 0.3s;
  white-space: nowrap;
}

.send-btn:hover:enabled {
  background-color: #359469;
}

.send-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 响应式适配（手机端） */
@media (max-width: 600px) {
  .ai-chat-container {
    padding: 10px;
    height: calc(100vh - 20px);
  }

  .chat-message {
    max-width: 90%;
  }

  .send-btn {
    padding: 10px 16px;
    font-size: 14px;
  }
}
</style>