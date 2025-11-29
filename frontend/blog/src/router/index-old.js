import { createRouter, createWebHashHistory } from 'vue-router'
import App from '../App.vue'
import App2 from '../app2.vue'
import AILogAnalysis from '@/views/AILogAnalysis';
import AIChat from '@/views/AIChat.vue';


const routes = [
    {
        path: '/',
        name: 'Home',
        component: App
    },
    {
        path: '/app2',
        name: 'App2',
        component: App2
    },
    {
        path: '/ai-log-analysis',
        name: 'AILogAnalysis',
        component: AILogAnalysis,
        meta: { title: 'AI 日志分析' }
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

export default router