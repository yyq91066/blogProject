// /* eslint-disable */
// import { createApp } from 'vue'
// import App from './App.vue'
//
// createApp(App).mount('#app')


//
// import { createApp } from 'vue'
// import App from './App.vue'
// import router from './router'
//
// createApp(App).use(router).mount('#app')


import { createApp } from 'vue'
import App from './views/AIChat.vue'
import router from './router'

const app = createApp(App)
app.use(router)
app.mount('#app')
