<script setup>
import { useRouter } from 'vue-router'
import AvatarBadge from './components/AvatarBadge.vue'
import { useSessionStore } from './stores/session'

const session = useSessionStore()
const router = useRouter()

// 演示导航：本周没有首页，用这几个入口快速切换演示账号的主页
const demoProfiles = [
  { userId: 1, nickname: '小屿' },
  { userId: 2, nickname: '阿岚' },
  { userId: 3, nickname: '星野' },
  { userId: 4, nickname: '白芷' }
]

function logout() {
  session.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="topbar__inner">
        <router-link class="brand" to="/u/1">
          <span class="brand__mark">见</span>
          <span class="brand__text">
            <strong>见屿</strong>
            <small>社交与好友圈 · 个人主页</small>
          </span>
        </router-link>

        <div class="topbar__right">
          <template v-if="session.isLoggedIn">
            <router-link class="current-user" :to="{ name: 'profile', params: { userId: session.currentUserId } }">
              <AvatarBadge :nickname="session.user.nickname" :avatar-url="session.user.avatarUrl || ''" :seed="session.user.userId" :size="32" />
              <span>{{ session.user.nickname }}</span>
            </router-link>
            <button class="btn btn--ghost btn--compact" type="button" @click="logout">退出</button>
          </template>
          <router-link v-else class="btn btn--primary btn--compact" :to="{ name: 'login' }">登录</router-link>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>

    <footer class="footer">
      <span class="footer__label">演示主页：</span>
      <router-link
        v-for="item in demoProfiles"
        :key="item.userId"
        class="footer__link"
        :to="{ name: 'profile', params: { userId: item.userId } }"
      >
        {{ item.nickname }}
      </router-link>
      <span class="footer__note">数据库表结构由 Flyway 管理</span>
    </footer>
  </div>
</template>
