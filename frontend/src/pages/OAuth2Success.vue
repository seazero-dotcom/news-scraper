<template>
  <div class="card" style="max-width:480px;margin:40px auto">
    <h2>로그인 처리 중…</h2>
    <p class="mt8">잠시만 기다려 주세요.</p>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuth()

onMounted(() => {
  const token = route.query.token
  const redirect = route.query.redirect || '/settings'
  if (token) {
    auth.setToken(token)
    router.replace(redirect)
  } else {
    router.replace('/login')
  }
})
</script>
