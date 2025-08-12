// 아주 가벼운 토스트 전역 상태
import { reactive } from 'vue'

export const toasts = reactive([]) // [{id, message, type}]

export function toast(message, type = 'info', timeout = 2000) {
    const id = Date.now() + Math.random()
    toasts.push({ id, message, type })
    setTimeout(() => {
        const i = toasts.findIndex(t => t.id === id)
        if (i >= 0) toasts.splice(i, 1)
    }, timeout)
}
