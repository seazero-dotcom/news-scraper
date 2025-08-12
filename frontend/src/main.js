import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './assets/theme.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

// 로그인 복구
import { useAuth } from './stores/auth'
const auth = useAuth()
auth.load()

// 401 응답 시 강제 로그아웃 처리
window.addEventListener('auth:logout', () => {
    auth.logout()
    router.push('/login')
})

app.mount('#app')
