import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../pages/Dashboard.vue'
import Settings from '../pages/Settings.vue'
import Login from '../pages/Login.vue'
import OAuth2Success from '../pages/OAuth2Success.vue'

const routes = [
    { path: '/', component: Dashboard },
    { path: '/settings', component: Settings, meta: { requiresAuth: true } },
    { path: '/login', component: Login },
    { path: '/oauth2/success', component: OAuth2Success },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.meta?.requiresAuth && !token) {
        next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
        next()
    }
})

export default router
