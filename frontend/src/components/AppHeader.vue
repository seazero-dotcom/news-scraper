<template>
  <header class="nav">
    <router-link to="/" class="brand">NewsScraper</router-link>
    <nav class="spacer"></nav>

    <router-link to="/" class="link">대시보드</router-link>
    <router-link to="/settings" class="link">설정</router-link>

    <span v-if="auth.isAuthed" class="muted" style="margin:0 8px 0 12px">
      안녕하세요, {{ auth.user?.name || auth.user?.email }}
    </span>
    <button v-if="auth.isAuthed" class="btn" @click="doLogout">로그아웃</button>
    <router-link v-else class="btn" to="/login">로그인</router-link>
  </header>
</template>

<script setup>
import { useAuth } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuth()
const router = useRouter()

function doLogout() {
  auth.logout()
  router.push('/')
}
</script>
