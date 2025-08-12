<script setup>
import { ref, onMounted } from 'vue';
import api from '../lib/http';

const tab = ref('keywords');

/* -------------------- 키워드 -------------------- */
const kws = ref([]);
const newKw = ref('');

async function loadKeywords() {
  const { data } = await api.get('/api/keywords');
  kws.value = data?.data || [];
}
async function addKeyword() {
  const word = newKw.value.trim();
  if (!word) return;

  try {
    await api.post('/api/keywords', { word });
    newKw.value = '';
    await loadKeywords();
  } catch (e) {
    const status = e?.response?.status;
    const msg = e?.response?.data?.error?.message;

    if (status === 401) return alert('로그인이 필요합니다. 다시 로그인해 주세요.');
    if (status === 409) return alert('이미 등록된 키워드입니다.');
    if (status === 400) return alert(msg || '입력값을 확인해 주세요.');
    alert(msg || '추가 실패');
  }
}
async function toggleKeyword(k) {
  await api.patch(`/api/keywords/${k.id}`, { enabled: !k.enabled });
  await loadKeywords();
}
async function deleteKeyword(k) {
  if (!confirm(`삭제할까요? ${k.word}`)) return;
  await api.delete(`/api/keywords/${k.id}`);
  await loadKeywords();
}

const srcs = ref([]);

// 생성 폼 상태 (site만 받음)
const form = ref({
  code: '',
  name: '',
  baseUrl: '',
  enabled: true,
  site: ''          // ✅ params.site
});

async function loadSources() {
  const { data } = await api.get('/api/sources');
  srcs.value = data?.data || [];
}

async function addSource() {
  try {
    const code = form.value.code.trim();
    const name = form.value.name.trim();
    const baseUrl = (form.value.baseUrl || '').trim();
    const site = (form.value.site || '').trim();

    if (!code) return alert('코드를 입력해 주세요.');
    if (!name) return alert('이름을 입력해 주세요.');
    if (!site) return alert('Site 도메인을 입력해 주세요. (예: news.naver.com)');

    const payload = {
      code: code.toUpperCase(),
      name,
      baseUrl: baseUrl || null,
      enabled: !!form.value.enabled,
      collector: 'AGGREGATOR_RSS_SITE',
      params: { site }
    };

    await api.post('/api/sources', payload);

    // 폼 리셋 & 목록 갱신
    form.value = { code: '', name: '', baseUrl: '', enabled: true, site: '' };
    await loadSources();
  } catch (e) {
    alert(e?.response?.data?.error?.message || '추가 실패');
  }
}

async function toggleSource(s) {
  await api.patch(`/api/sources/${s.id}/enabled`, { enabled: !s.enabled });
  await loadSources();
}

async function updateSource(s) {
  const name = prompt('이름 수정', s.name || '');
  if (name === null) return;

  const baseUrl = prompt('기본 URL 수정', s.baseUrl || '');
  if (baseUrl === null) return;

  let p = s.params;
  if (typeof p === 'string') {
    try { p = JSON.parse(p); } catch { p = {}; }
  }
  if (!p || typeof p !== 'object') p = {};
  const currentSite = p.site || '';

  const site = prompt('Site 도메인 수정', currentSite);
  if (site === null) return;
  if (!site.trim()) return alert('Site 도메인은 비워둘 수 없습니다.');

  // 사용 여부도 바꾸고 싶다면 간단 제어(옵션)
  const changeEnabled = confirm('사용 여부도 함께 저장할까요? [확인: 현재 상태 유지 / 취소: 상태 반전]');
  const enabled = changeEnabled ? s.enabled : !s.enabled;

  const payload = {
    name: name.trim(),
    baseUrl: (baseUrl || '').trim() || null,
    enabled,
    collector: 'AGGREGATOR_RSS_SITE',
    params: { site: site.trim() }
  };

  await api.patch(`/api/sources/${s.id}`, payload);
  await loadSources();
}

async function deleteSource(s) {
  if (!confirm(`삭제할까요? ${s.name} (연관 기사 있으면 실패)`)) return;
  try {
    await api.delete(`/api/sources/${s.id}`);
    await loadSources();
    alert('삭제되었습니다.');
  } catch (e) {
    const status = e?.response?.status;
    const msg = e?.response?.data?.error?.message;

    if (status === 409) {
      const ok = confirm(`${msg || '연관 기사 때문에 삭제할 수 없습니다.'}\n기사까지 함께 삭제할까요? (되돌릴 수 없습니다)`);
      if (!ok) return;
      await api.delete(`/api/sources/${s.id}`, { params: { force: true } });
      await loadSources();
      alert('연관 기사와 함께 삭제되었습니다.');
      return;
    }
    alert(msg || '삭제 실패');
  }
}

onMounted(async () => {
  await Promise.all([loadKeywords(), loadSources()]);
});
</script>

<template>
  <section>
    <h2>설정</h2>
    <div class="mb12">
      <button class="btn mr8" :class="{active: tab==='keywords'}" @click="tab='keywords'">키워드</button>
      <button class="btn" :class="{active: tab==='sources'}" @click="tab='sources'">소스</button>
    </div>

    <!-- 키워드 탭 -->
    <div v-if="tab==='keywords'">
      <div class="mb12">
        <input class="input mr8" v-model="newKw" placeholder="키워드(예: 선거, 코로나)" />
        <button class="btn" @click="addKeyword">추가</button>
      </div>
      <table class="table">
        <thead><tr><th>단어</th><th>상태</th><th>액션</th></tr></thead>
        <tbody>
        <tr v-for="k in kws" :key="k.id">
          <td>{{ k.word }}</td>
          <td><span class="badge">{{ k.enabled ? 'ON' : 'OFF' }}</span></td>
          <td>
            <button class="btn mr8" @click="toggleKeyword(k)">{{ k.enabled ? 'OFF로' : 'ON으로' }}</button>
            <button class="btn" @click="deleteKeyword(k)">삭제</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-else>
      <!-- 생성 폼 -->
      <div class="mb12 src-form">
        <div class="row">
          <label>코드</label>
          <input class="input" v-model="form.code" placeholder="코드 (예: NAVER, DAUM)" />
        </div>
        <div class="row">
          <label>이름</label>
          <input class="input" v-model="form.name" placeholder="이름 (예: 네이버 뉴스)" />
        </div>
        <div class="row">
          <label>Base URL</label>
          <input class="input" v-model="form.baseUrl" placeholder="기본 URL (https://...)" />
        </div>
        <div class="row">
          <label>사용 여부</label>
          <input type="checkbox" v-model="form.enabled" />
        </div>
        <div class="row">
          <label>Site 도메인</label>
          <input class="input" v-model="form.site" placeholder="예: news.naver.com" />
        </div>
        <div class="row">
          <label></label>
          <button class="btn" @click="addSource">추가</button>
        </div>
      </div>

      <!-- 목록 -->
      <table class="table">
        <thead>
        <tr><th>코드</th><th>이름</th><th>URL</th><th>상태</th><th>액션</th></tr>
        </thead>
        <tbody>
        <tr v-for="s in srcs" :key="s.id">
          <td>{{ s.code }}</td>
          <td>{{ s.name }}</td>
          <td><a :href="s.baseUrl" target="_blank">{{ s.baseUrl }}</a></td>
          <td><span class="badge">{{ s.enabled ? 'ON' : 'OFF' }}</span></td>
          <td>
            <button class="btn mr8" @click="toggleSource(s)">{{ s.enabled ? 'OFF로' : 'ON으로' }}</button>
            <button class="btn mr8" @click="updateSource(s)">수정</button>
            <button class="btn" @click="deleteSource(s)">삭제</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.btn.active { background:#111; color:#fff; border-color:#111; }
.src-form { border:1px solid #eee; border-radius:12px; padding:12px; background:#fff; }
.row { display:grid; grid-template-columns:160px 1fr; gap:12px; align-items:center; margin:10px 0; }
.inline { display:flex; gap:16px; align-items:center; }
.input { padding:8px 10px; border:1px solid #e5e7eb; border-radius:8px; }
.badge { padding:2px 8px; border:1px solid #e5e7eb; border-radius:999px; background:#f8fafc; }
.table { width:100%; border-collapse: collapse; }
.table th, .table td { padding:8px; border-bottom:1px solid #eee; text-align:left; }
.mr8 { margin-right:8px; }
.mb12 { margin-bottom:12px; }
.btn { padding:8px 12px; border-radius:8px; border:1px solid #ddd; background:#fff; cursor:pointer; }
</style>
