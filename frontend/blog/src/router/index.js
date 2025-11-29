import { createRouter, createWebHistory } from 'vue-router'
import AIChat from '@/views/AIChat.vue'

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL || '/'),
    routes: [
        {
            path: '/',
            redirect: '/ai-chat'
        },
        {
            path: '/ai-chat',
            name: 'AIChat',
            component: AIChat
        }
    ]
})

export default router
