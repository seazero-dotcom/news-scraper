import { defineStore } from 'pinia'

// base64url → JSON 파싱 (JWT payload용)
function parseJwt(token) {
    try {
        const payload = token.split('.')[1]
            .replace(/-/g, '+').replace(/_/g, '/')
        const json = atob(payload)
        return JSON.parse(decodeURIComponent(escape(json)))
    } catch (e) {
        return null
    }
}

export const useAuth = defineStore('auth', {
    state: () => ({
        token: null,
        user: null, // { name, email, exp(ms) }
    }),
    getters: {
        isAuthed: (s) => !!s.token,
    },
    actions: {
        setToken(token) {
            this.token = token
            localStorage.setItem('token', token)
            const claims = parseJwt(token)
            this.user = claims ? {
                name: claims.name || '',
                email: claims.email || '',
                exp: claims.exp ? claims.exp * 1000 : null,
            } : null
        },
        load() {
            const t = localStorage.getItem('token')
            if (t) this.setToken(t)
        },
        logout() {
            this.token = null
            this.user = null
            localStorage.removeItem('token')
        },
    },
})
