// src/lib/http.js
import axios from 'axios'
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const api = axios.create({ baseURL: API_BASE })

api.interceptors.request.use(cfg => {
    const t = localStorage.getItem('token')
    if (t) cfg.headers.Authorization = `Bearer ${t}`
    return cfg
})

api.interceptors.response.use(
    r => r,
    e => {
        if (e?.response?.status === 401) {
            localStorage.removeItem('token')
            window.dispatchEvent(new CustomEvent('auth:logout'))
        }
        return Promise.reject(e)
    }
)
export default api
