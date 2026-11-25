<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AvatarBadge from '../components/AvatarBadge.vue'
import { useSessionStore } from '../stores/session'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()

const loadingUsername = ref('')
const errorMessage = ref('')

// 本周演示账号（正式版由包 1 的注册/登录页替换）
const accounts = [
  { username: 'xiaoyu', nickname: '小屿', desc: '记录生活 · 有 1 位粉丝' },
  { username: 'alan', nickname: '阿岚', desc: '摄影与徒步 · 已关注小屿' },
  { username: 'xingye', nickname: '星野', desc: '夜跑爱好者' },
  { username: 'baizhi', nickname: '白芷', desc: '植物与旧书' }
]

async function login(account) {
  if (loadingUsername.value) return
  loadingUsername.value = account.username
  errorMessage.value = ''
  try {
    const data = await session.login(account.username)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : `/u/${data.userId}`
    router.replace(redirect)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loadingUsername.value = ''
  }
}
</script>

<template>
  <section class="page page--narrow">
    <div class="login-card">
      <h1 class="login-card__title">登录见屿</h1>
      <p class="login-card__hint">
        本周为演示登录（免密码），正式账号体系由包 1 提供。
      </p>

      <ul class="account-list">
        <li v-for="account in accounts" :key="account.username">
          <button
            class="account-list__item"
            type="button"
            :disabled="Boolean(loadingUsername)"
            @click="login(account)"
          >
            <AvatarBadge :nickname="account.nickname" :seed="account.username.length" :size="46" />
            <span class="account-list__text">
              <strong>{{ account.nickname }}</strong>
              <small>{{ account.desc }}</small>
            </span>
            <span class="account-list__action">
              {{ loadingUsername === account.username ? '登录中…' : '进入' }}
            </span>
          </button>
        </li>
      </ul>

      <p v-if="errorMessage" class="state state--error">{{ errorMessage }}</p>
    </div>
  </section>
</template>
