<template>
  <div id="app">
    <!-- 简洁的头部 -->
    <header class="header">
      <div class="container">
        <h1 class="header-title">游泳圈的空间</h1>
        <button class="btn" @click="showCreateForm = !showCreateForm">
          {{ showCreateForm ? '取消' : '写文章' }}
        </button>
      </div>
    </header>

    <main class="container main">
      <!-- 发布文章表单 -->
      <div v-if="showCreateForm" class="form-section">
        <h2>发布新文章</h2>
        <form @submit.prevent="createPost">
          <div class="form-group">
            <label for="title">标题</label>
            <input type="text" id="title" v-model="newPost.title" placeholder="请输入文章标题" required>
          </div>
          <div class="form-group">
            <label for="author">作者</label>
            <input type="text" id="author" v-model="newPost.author" placeholder="请输入您的名字" required>
          </div>
          <div class="form-group">
            <label for="content">内容</label>
            <textarea id="content" rows="5" v-model="newPost.content" placeholder="请输入文章内容" required></textarea>
          </div>
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="showCreateForm = false">取消</button>
            <button type="submit" class="btn btn-primary">发布文章</button>
          </div>
        </form>
      </div>

      <div class="content-wrapper">
        <!-- 文章列表 -->
        <section class="posts-section">
          <div v-if="loading" class="loading">
            加载中...
          </div>
          
          <div v-else>
            <div v-if="posts.length === 0" class="empty-state">
              暂无文章，快来发表第一篇文章吧！
            </div>
            
            <article v-for="post in posts" :key="post.id" class="post-card">
              <h2 class="post-title">{{ post.title }}</h2>
              <div class="post-meta">
                <span>{{ post.author }}</span>
                <span>{{ formatDate(post.createdTime) }}</span>
              </div>
              <p class="post-excerpt">{{ truncateContent(post.content, 200) }}</p>
              <!-- 修改为链接，打开新页面 -->
              <a :href="'#/post/' + post.id" class="btn-read-more" @click="viewFullPost(post)">
                阅读全文
              </a>
            </article>
          </div>
        </section>

        <!-- 侧边栏 -->
        <aside class="sidebar">
          <div class="widget">
            <h3>个人简介</h3>
            <p>QQ：885231379</p>
            <p>邮箱：885231379@qq.com</p>
            <a href="https://www.ycresume.com/cv/251106E7RW5UWG8PT" target="_blank">
              这是我的简历！</a>
          </div>
          
          <div class="widget">
            <h3>热门标签</h3>
            <div class="tags">
              <span v-for="(tag, index) in tags" :key="index" class="tag">
                {{ tag }}
              </span>
            </div>
          </div>
        </aside>
      </div>

    </main>

    <footer class="footer">
      <div class="container">
        <p>&copy; 2025 游泳圈的空间. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'App',
  data() {
    return {
      showCreateForm: false,
      loading: true,
      posts: [],
      newPost: {
        title: '',
        content: '',
        author: ''
      },
      tags: ["Vue", "springboot", "JavaScript", "CSS", "mybaties", "java", "springcloud"]
    }
  },
  mounted() {
    this.loadPosts()
  },
  methods: {
    async loadPosts() {
      try {
        const response = await axios.get('/api/posts')
        this.posts = response.data
        this.loading = false
      } catch (error) {
        console.error('加载文章失败:', error)
        this.loading = false
      }
    },
    async createPost() {
      try {
        // 验证输入
        if (!this.newPost.title.trim()) {
          alert('请输入文章标题')
          return
        }
        if (!this.newPost.author.trim()) {
          alert('请输入作者姓名')
          return
        }
        if (!this.newPost.content.trim()) {
          alert('请输入文章内容')
          return
        }
        
        await axios.post('/api/posts', this.newPost)
        // 重置表单
        this.newPost = { title: '', content: '', author: '' }
        this.showCreateForm = false
        // 重新加载文章列表
        this.loadPosts()
        
        // 显示成功消息
        alert('文章发布成功！')
      } catch (error) {
        console.error('发布文章失败:', error)
        alert('发布文章失败，请重试')
      }
    },
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    truncateContent(content, maxLength) {
      if (!content) return ''
      if (content.length <= maxLength) return content
      return content.substring(0, maxLength) + '...'
    },
    viewFullPost(post) {
      // 阻止默认行为，使用 sessionStorage 存储文章数据
      event.preventDefault();
      
      // 将文章数据存储到 sessionStorage
      sessionStorage.setItem('currentPost', JSON.stringify(post));
      
      // 打开新窗口显示文章详情
      const newWindow = window.open('', '_blank');
      newWindow.document.write(`
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>${post.title}</title>
          <style>
            body {
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
              line-height: 1.6;
              color: #333;
              max-width: 800px;
              margin: 0 auto;
              padding: 20px;
            }
            .post-title {
              font-size: 2rem;
              margin-bottom: 0.5rem;
              color: #2c3e50;
            }
            .post-meta {
              display: flex;
              justify-content: space-between;
              color: #6c757d;
              font-size: 0.9rem;
              margin-bottom: 2rem;
              border-bottom: 1px solid #eee;
              padding-bottom: 1rem;
            }
            .post-content {
              white-space: pre-wrap;
              font-size: 1.1rem;
              line-height: 1.8;
            }
            .back-link {
              display: inline-block;
              margin-top: 2rem;
              color: #007bff;
              text-decoration: none;
            }
            .back-link:hover {
              text-decoration: underline;
            }
          </style>
        </head>
        <body>
          <h1 class="post-title">${post.title}</h1>
          <div class="post-meta">
            <span>作者: ${post.author}</span>
            <span>发布时间: ${this.formatDate(post.createdTime)}</span>
          </div>
          <div class="post-content">${post.content.replace(/\n/g, '<br>')}</div>
          <a href="javascript:window.close()" class="back-link">关闭页面</a>
        </body>
        </html>
      `);
      newWindow.document.close();
    },
    delPost(){
      alert("你凭啥删啊？")
    }
  }
}
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  line-height: 1.6;
  color: #333;
  background-color: #f8f9fa;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Header styles */
.header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 1rem 0;
}

.header .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.btn {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background-color: #fff;
  cursor: pointer;
  border-radius: 4px;
  font-size: 0.9rem;
}

.btn:hover {
  background-color: #f8f9fa;
}

.btn-primary {
  background-color: #007bff;
  color: white;
  border-color: #007bff;
}

.btn-primary:hover {
  background-color: #0069d9;
  border-color: #0062cc;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
  border-color: #6c757d;
}

.btn-secondary:hover {
  background-color: #5a6268;
  border-color: #545b62;
}

/* Main content */
.main {
  flex: 1;
  padding: 2rem 0;
}

/* Form styles */
.form-section {
  background-color: #fff;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.form-section h2 {
  margin-bottom: 1rem;
  font-size: 1.3rem;
  color: #2c3e50;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: inherit;
  font-size: 1rem;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0,123,255,0.25);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1rem;
}

/* Content layout */
.content-wrapper {
  display: flex;
  gap: 2rem;
}

.posts-section {
  flex: 2;
}

.sidebar {
  flex: 1;
}

/* Post styles */
.post-card {
  background-color: #fff;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.post-title {
  font-size: 1.4rem;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  color: #6c757d;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.post-excerpt {
  margin-bottom: 1rem;
  color: #495057;
}

.btn-read-more {
  display: inline-block;
  background-color: transparent;
  border: 1px solid #007bff;
  color: #007bff;
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  text-decoration: none;
}.btn-del {
  display: inline-block;
  background-color: transparent;
  border: 1px solid #007bff;
  color: #007bff;
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  text-decoration: none;
}

.btn-read-more:hover {
  background-color: #007bff;
  color: white;
}

/* Widget styles */
.widget {
  background-color: #fff;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.widget h3 {
  margin-bottom: 1rem;
  font-size: 1.1rem;
  color: #2c3e50;
}

.widget p {
  color: #495057;
  line-height: 1.6;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag {
  background-color: #e9ecef;
  padding: 0.3rem 0.6rem;
  border-radius: 1rem;
  font-size: 0.85rem;
}

/* Empty state and loading */
.empty-state,
.loading {
  text-align: center;
  padding: 3rem;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  color: #6c757d;
}

/* Footer */
.footer {
  background-color: #fff;
  padding: 1.5rem 0;
  border-top: 1px solid #eee;
  text-align: center;
  color: #6c757d;
  font-size: 0.9rem;
}

/* Responsive */
@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .header .container {
    flex-direction: column;
    gap: 1rem;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
  }
}
</style>