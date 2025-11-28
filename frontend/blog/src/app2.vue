<template>
  <div class="image-manage-container">
    <h2>📷 图片管理系统（博客/商品/头像通用）</h2>

    <!-- 1. 图片上传区域 -->
    <div class="upload-section">
      <h3>上传图片</h3>
      <div class="upload-form">
        <!-- 业务类型选择 -->
        <select v-model="selectedBusinessType" class="form-select">
          <option value="blog_cover">博客封面</option>
          <option value="user_avatar">用户头像</option>
          <option value="goods_img">商品图片</option>
          <option value="article_img">文章配图</option>
        </select>

        <!-- 关联业务ID输入 -->
        <input
            type="text"
            v-model="businessId"
            placeholder="输入关联业务ID（如博客ID/用户ID）"
            class="form-input"
        />

        <!-- 文件选择按钮 -->
        <label class="file-label">
          选择图片
          <input
              type="file"
              accept="image/png,image/jpg,image/jpeg,image/webp"
              @change="handleFileSelect"
              hidden
          />
        </label>

        <!-- 上传按钮 -->
        <button @click="handleUpload" class="btn upload-btn" :disabled="!selectedFile">
          开始上传
        </button>
      </div>

      <!-- 上传成功预览 -->
      <div class="upload-preview" v-if="uploadedImage.imageUrl">
        <h4>上传成功预览</h4>
        <div class="preview-card">
          <img :src="uploadedImage.imageUrl" alt="预览图" class="preview-img" />
          <div class="preview-info">
            <p>原始文件名：{{ uploadedImage.originalName }}</p>
            <p>图片ID：{{ uploadedImage.imageId }}</p>
            <p>大小：{{ formatFileSize(uploadedImage.imageSize) }}</p>
            <p>后缀：{{ uploadedImage.imageSuffix }}</p>
            <div class="preview-actions">
              <button @click="copyImageUrl" class="btn copy-btn">复制URL</button>
              <button @click="handleDeleteImage(uploadedImage.imageId)" class="btn delete-btn">
                删除图片
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. 图片列表查询区域 -->
    <div class="list-section">
      <h3>查询图片列表</h3>
      <div class="query-form">
        <select v-model="queryBusinessType" class="form-select">
          <option value="blog_cover">博客封面</option>
          <option value="user_avatar">用户头像</option>
          <option value="goods_img">商品图片</option>
          <option value="article_img">文章配图</option>
        </select>
        <input
            type="text"
            v-model="queryBusinessId"
            placeholder="输入要查询的业务ID"
            class="form-input"
        />
        <button @click="getImageList" class="btn query-btn">查询</button>
      </div>

      <!-- 列表展示 -->
      <div class="image-list" v-if="imageList.length > 0">
        <div class="image-item" v-for="img in imageList" :key="img.imageId">
          <img :src="img.imageUrl" alt="图片" class="item-img" />
          <div class="item-info">
            <p>名称：{{ img.originalName }}</p>
            <p>ID：{{ img.imageId }}</p>
            <p>大小：{{ formatFileSize(img.imageSize) }}</p>
            <p>上传时间：{{ formatDate(img.uploadTime) }}</p>
          </div>
          <div class="item-actions">
            <button @click="copyUrl(img.imageUrl)" class="btn copy-btn small-btn">复制URL</button>
            <button @click="handleDeleteImage(img.imageId)" class="btn delete-btn small-btn">删除</button>
          </div>
        </div>
      </div>
      <div class="empty-tip" v-else-if="hasQueried">
        暂无图片数据，请更换查询条件试试～
      </div>
    </div>

    <!-- 3. 单独获取图片URL区域 -->
    <div class="url-section">
      <h3>单独获取图片URL</h3>
      <div class="url-form">
        <input
            type="text"
            v-model="targetImageId"
            placeholder="输入图片ID（上传后返回的imageId）"
            class="form-input"
        />
        <button @click="getImageUrlById" class="btn query-btn">获取URL</button>
        <div class="url-result" v-if="imageUrlResult">
          <input type="text" :value="imageUrlResult" readonly class="url-input" />
          <button @click="copyUrl(imageUrlResult)" class="btn copy-btn small-btn">复制</button>
        </div>
      </div>
    </div>

    <!-- 提示弹窗 -->
    <div class="toast" v-if="toastMessage">{{ toastMessage }}</div>
  </div>
</template>

<script>
import axios from 'axios';

// 配置axios基础路径（根据你的服务器实际地址修改）
// axios.defaults.baseURL = 'http://101.126.151.51:8080/blog'; // 带WAR包名blog
axios.defaults.baseURL = 'http://localhost:8080'; // 带WAR包名blog

export default {
  name: 'ImageManage',
  data() {
    return {
      // 上传相关
      selectedBusinessType: 'blog_cover', // 默认业务类型
      businessId: '', // 关联业务ID
      selectedFile: null, // 选中的文件
      uploadedImage: {}, // 上传成功后的图片信息

      // 列表查询相关
      queryBusinessType: 'blog_cover', // 查询业务类型
      queryBusinessId: '', // 查询业务ID
      imageList: [], // 图片列表数据
      hasQueried: false, // 是否执行过查询

      // 单独获取URL相关
      targetImageId: '', // 目标图片ID
      imageUrlResult: '', // 获取到的图片URL

      // 提示信息
      toastMessage: '' // 弹窗提示内容
    };
  },
  methods: {
    // 1. 选择文件
    handleFileSelect(e) {
      const file = e.target.files[0];
      if (file) {
        this.selectedFile = file;
        // 清空之前的预览
        this.uploadedImage = {};
      }
    },

    // 2. 上传图片
    async handleUpload() {
      if (!this.selectedFile) {
        this.showToast('请先选择图片！');
        return;
      }
      if (!this.businessId.trim()) {
        this.showToast('请输入关联业务ID！');
        return;
      }

      const formData = new FormData();
      formData.append('file', this.selectedFile);
      formData.append('businessType', this.selectedBusinessType);
      formData.append('businessId', this.businessId.trim());

      try {
        this.showToast('上传中...');
        const res = await axios.post('/api/image/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });

        if (res.data.success) {
          this.uploadedImage = res.data;
          this.showToast('上传成功！');
          // 重置表单
          this.selectedFile = null;
          // 刷新列表（如果查询条件和上传条件一致）
          if (this.queryBusinessType === this.selectedBusinessType && this.queryBusinessId === this.businessId.trim()) {
            this.getImageList();
          }
        } else {
          this.showToast('上传失败：' + res.data.message);
        }
      } catch (err) {
        this.showToast('上传异常：' + (err.response?.data?.message || err.message));
        console.error('上传失败', err);
      }
    },

    // 3. 查询图片列表
    async getImageList() {
      if (!this.queryBusinessId.trim()) {
        this.showToast('请输入要查询的业务ID！');
        return;
      }

      try {
        this.showToast('查询中...');
        const res = await axios.get('/api/image/list', {
          params: {
            businessType: this.queryBusinessType,
            businessId: this.queryBusinessId.trim()
          }
        });

        this.hasQueried = true;
        if (res.data.success) {
          this.imageList = res.data.imageList;
          this.showToast(`查询成功，共${res.data.total}张图片`);
        } else {
          this.imageList = [];
          this.showToast('查询失败：' + res.data.message);
        }
      } catch (err) {
        this.imageList = [];
        this.hasQueried = true;
        this.showToast('查询异常：' + (err.response?.data?.message || err.message));
        console.error('查询失败', err);
      }
    },

    // 4. 删除图片
    async handleDeleteImage(imageId) {
      if (!confirm('确定要删除这张图片吗？删除后不可恢复！')) {
        return;
      }

      try {
        this.showToast('删除中...');
        const res = await axios.delete(`/api/image/delete/${imageId}`);

        if (res.data.success) {
          this.showToast('删除成功！');
          // 刷新预览（如果删除的是当前预览图）
          if (this.uploadedImage.imageId === imageId) {
            this.uploadedImage = {};
          }
          // 刷新列表
          this.getImageList();
        } else {
          this.showToast('删除失败：' + res.data.message);
        }
      } catch (err) {
        this.showToast('删除异常：' + (err.response?.data?.message || err.message));
        console.error('删除失败', err);
      }
    },

    // 5. 单独获取图片URL
    async getImageUrlById() {
      if (!this.targetImageId.trim()) {
        this.showToast('请输入图片ID！');
        return;
      }

      try {
        this.showToast('获取中...');
        const res = await axios.get(`/api/image/url/${this.targetImageId.trim()}`);

        if (res.data.success) {
          this.imageUrlResult = res.data.imageUrl;
          this.showToast('获取成功！');
        } else {
          this.imageUrlResult = '';
          this.showToast('获取失败：' + res.data.message);
        }
      } catch (err) {
        this.imageUrlResult = '';
        this.showToast('获取异常：' + (err.response?.data?.message || err.message));
        console.error('获取URL失败', err);
      }
    },

    // 6. 复制URL到剪贴板
    copyImageUrl() {
      if (!this.uploadedImage.imageUrl) {
        this.showToast('暂无URL可复制！');
        return;
      }
      this.copyUrl(this.uploadedImage.imageUrl);
    },

    copyUrl(url) {
      navigator.clipboard.writeText(url).then(() => {
        this.showToast('URL已复制到剪贴板！');
      }).catch(() => {
        this.showToast('复制失败，请手动复制！');
      });
    },

    // 工具函数：格式化文件大小（字节转KB/MB）
    formatFileSize(size) {
      if (!size) return '0B';
      if (size < 1024) return size + 'B';
      if (size < 1024 * 1024) return (size / 1024).toFixed(2) + 'KB';
      return (size / (1024 * 1024)).toFixed(2) + 'MB';
    },

    // 工具函数：格式化日期
    formatDate(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },

    // 工具函数：显示提示弹窗
    showToast(message) {
      this.toastMessage = message;
      setTimeout(() => {
        this.toastMessage = '';
      }, 3000);
    }
  }
};
</script>

<style scoped>
.image-manage-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Microsoft YaHei', sans-serif;
}

h2 {
  color: #2c3e50;
  border-bottom: 2px solid #3498db;
  padding-bottom: 10px;
  margin-bottom: 30px;
}

h3 {
  color: #34495e;
  margin: 20px 0;
  font-size: 18px;
}

/* 上传区域样式 */
.upload-section, .list-section, .url-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.upload-form, .query-form, .url-form {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.form-select, .form-input {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  flex: 1;
  min-width: 150px;
}

.file-label {
  padding: 10px 15px;
  background: #3498db;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.file-label:hover {
  background: #2980b9;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.upload-btn {
  background: #2ecc71;
  color: white;
}

.upload-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.upload-btn:hover:not(:disabled) {
  background: #27ae60;
}

/* 预览区域样式 */
.upload-preview {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.preview-card {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.preview-img {
  width: 200px;
  height: auto;
  border-radius: 4px;
  border: 1px solid #eee;
}

.preview-info {
  flex: 1;
  min-width: 250px;
}

.preview-info p {
  margin: 8px 0;
  color: #555;
  font-size: 14px;
}

.preview-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

/* 列表区域样式 */
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 15px;
}

.image-item {
  background: white;
  border-radius: 8px;
  padding: 15px;
  width: calc(33.333% - 20px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: transform 0.3s;
}

.image-item:hover {
  transform: translateY(-5px);
}

.item-img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 10px;
}

.item-info p {
  margin: 5px 0;
  color: #555;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.small-btn {
  padding: 6px 12px;
  font-size: 12px;
}

/* 按钮样式细分 */
.query-btn {
  background: #9b59b6;
  color: white;
}

.query-btn:hover {
  background: #8e44ad;
}

.copy-btn {
  background: #f39c12;
  color: white;
}

.copy-btn:hover {
  background: #e67e22;
}

.delete-btn {
  background: #e74c3c;
  color: white;
}

.delete-btn:hover {
  background: #c0392b;
}

/* 提示弹窗样式 */
.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 20px;
  background: rgba(0,0,0,0.8);
  color: white;
  border-radius: 4px;
  font-size: 14px;
  z-index: 9999;
  animation: fadeInOut 3s ease-in-out;
}

.empty-tip {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 14px;
}

.url-form {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.url-input {
  flex: 1;
  min-width: 200px;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
}

/* 动画效果 */
@keyframes fadeInOut {
  0% { opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { opacity: 0; }
}

/* 响应式适配 */
@media (max-width: 992px) {
  .image-item {
    width: calc(50% - 20px);
  }
}

@media (max-width: 576px) {
  .image-item {
    width: 100%;
  }

  .upload-form, .query-form, .url-form {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>