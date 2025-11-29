import { createApp } from 'vue'
import App from './views/AIChat.vue'
import router from './router'

createApp(App)
    .use(router)
    .mount('#app')
