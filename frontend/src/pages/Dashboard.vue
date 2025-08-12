<template>
  <div class="toolbar">
    <input v-model="q" placeholder="키워드/제목 검색" @keyup.enter="search">
    <select v-model.number="size" @change="() => loadArticles({ reset: true })">
      <option :value="20">20개</option>
      <option :value="50">50개</option>
      <option :value="100">100개</option>
    </select>
    <button class="btn" @click="search">검색</button>
    <button class="btn primary" :disabled="triggering" @click="triggerCrawl">
      {{ triggering ? '수집 중...' : '즉시 수집' }}
    </button>
  </div>

  <div v-if="items.length===0 && !loading" class="muted" style="padding:12px">표시할 기사가 없어요.</div>

  <div class="grid">
    <article v-for="it in items" :key="it.id" class="card">
      <div class="meta-row">
        <span class="badge source">{{ it.sourceName || it.sourceCode }}</span>
        <span v-if="it.keywordWord" class="badge keyword">#{{ it.keywordWord }}</span>
      </div>

      <a class="title" :href="it.url" target="_blank" rel="noreferrer">{{ it.title }}</a>

      <img v-if="it.imageUrl" class="thumb" :src="it.imageUrl" alt="" loading="lazy"
           @error="e => e.target.style.display='none'">

      <div class="times">
        <span v-if="it.publishedAt">발행: {{ new Date(it.publishedAt).toLocaleString('ko-KR',{hour12:false}) }}</span>
        <span>수집: {{ new Date(it.fetchedAt).toLocaleString('ko-KR',{hour12:false}) }}</span>
      </div>
    </article>
  </div>

  <div class="list-footer">
    <div v-if="loading" class="muted">불러오는 중…</div>
    <button v-else :disabled="reachedEnd" class="btn" @click="loadMore">
      {{ reachedEnd ? '마지막 페이지입니다' : '더 보기' }}
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../lib/http'
import { toast } from '../lib/toast'

const items = ref([])
const q = ref('')
const page = ref(0)
const size = ref(50)          // 기본 한 번에 50개
const loading = ref(false)
const reachedEnd = ref(false)
const triggering = ref(false)

function normalizeList(resData) {
  const body = resData?.data ?? resData
  return body?.content ?? body ?? []
}
function metaOf(resData) {
  return resData?.meta ?? null
}

async function loadArticles({ reset = false } = {}) {
  if (loading.value) return
  loading.value = true
  try {
    if (reset) { page.value = 0; items.value = []; reachedEnd.value = false }
    const { data } = await api.get('/api/articles', {
      params: { q: q.value, page: page.value, size: size.value, sort: 'recent' }
    })
    const list = normalizeList(data)
    const meta = metaOf(data)

    if (reset) items.value = list
    else items.value = items.value.concat(list)

    // 다음 페이지 계산
    if (meta && typeof meta.hasNext === 'boolean') {
      reachedEnd.value = !meta.hasNext
      page.value = meta.hasNext && typeof meta.nextPage === 'number'
          ? meta.nextPage
          : page.value + (list.length === size.value ? 1 : 0)
    } else {
      // 메타가 없으면 길이로 판단
      if (list.length < size.value) reachedEnd.value = true
      else page.value += 1
    }
  } catch (e) {
    toast(e?.response?.data?.error?.message || '목록 로딩 실패', 'error')
  } finally {
    loading.value = false
  }
}

function search() {
  loadArticles({ reset: true })
}

async function loadMore() {
  if (!reachedEnd.value) {
    await loadArticles()
  }
}

async function triggerCrawl() {
  if (triggering.value) return
  triggering.value = true
  try {
    await api.post('/api/crawl/trigger')
    toast('수집을 시작했어요!', 'success')
    // 수집 완료 반영 대기 후 처음부터 다시 로드
    await new Promise(r => setTimeout(r, 800))
    await loadArticles({ reset: true })
  } catch (e) {
    toast(e?.response?.data?.error?.message || '수집 트리거 실패', 'error')
  } finally {
    triggering.value = false
  }
}

onMounted(() => loadArticles({ reset: true }))
</script>

<style scoped>
.toolbar { display:flex; gap:8px; align-items:center; margin:12px 0; }
select, input { padding:8px 10px; border:1px solid #e5e7eb; border-radius:8px; }
.btn { padding:8px 12px; border-radius:8px; border:1px solid #ddd; background:#fff; cursor:pointer; }
.btn.primary { background:#2563eb; color:#fff; border-color:#2563eb; }
.muted { color:#6b7280; }
.grid { display:grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap:14px; align-items:start; }
.card { background:#fff; border:1px solid #e5e7eb; border-radius:14px; padding:14px; box-shadow:0 6px 16px rgba(0,0,0,0.05); }
.meta-row { display:flex; gap:6px; margin-bottom:8px; align-items:center; }
.badge { font-size:12px; padding:2px 8px; border-radius:999px; border:1px solid #e5e7eb; background:#f8fafc; color:#374151; }
.badge.source { background:#eef2ff; border-color:#e0e7ff; color:#4338ca; }
.badge.keyword{ background:#ecfeff; border-color:#cffafe; color:#0e7490; }
.title { display:block; font-weight:600; font-size:16px; color:#111827; text-decoration:none; margin:2px 0 8px; }
.title:hover { text-decoration: underline; }
.thumb { width:100%; height:168px; object-fit:cover; border-radius:10px; margin-bottom:8px; background:#f3f4f6; }
.times {
  display: flex;
  flex-direction: column; /* ✅ 세로 정렬 */
  gap: 2px;               /* 줄 간격 */
  color: #6b7280;
  font-size: 12px;
}
.list-footer { display:flex; justify-content:center; padding:16px 0; }
</style>