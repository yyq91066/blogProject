import { createRouter, createWebHashHistory } from 'vue-router'
import App from '../App.vue'
import App2 from '../app2.vue'

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
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

export default router