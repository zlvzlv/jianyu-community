<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AvatarBadge from '../components/AvatarBadge.vue'
import FollowButton from '../components/FollowButton.vue'
import { fetchProfile } from '../api/social'
import { useSessionStore } from '../stores/session'

const props = defineProps({
  userId: { type: Number, required: true }
})

const router = useRouter()
const session = useSessionStore()

const profile = ref(null)
const loading = ref(true)
const errorMessage = ref('')
const toast = ref('')

async function load() {
  loading.value = true
  errorMessage.value = ''
  try {
    profile.value = await fetchProfile(props.userId)
  } catch (error) {
    profile.value = null
    errorMessage.value = error.code === 50002 ? '这个用户不存在或已停用' : error.message
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(() => props.userId, load)

function onFollowChange(state) {
  if (!profile.value) return
  profile.value = {
    ...profile.value,
    isFollowing: state.isFollowing,
    isMutual: state.isMutual,
    followerCount: state.followerCount
  }
  toast.value = state.isFollowing
    ? state.isMutual
      ? '你们已成为好友（互相关注）'
      : '关注成功'
    : '已取消关注'
  window.setTimeout(() => (toast.value = ''), 2400)
}

function showError(message) {
  toast.value = message
  window.setTimeout(() => (toast.value = ''), 2400)
}

const statItems = [
  { key: 'followingCount', label: '关注', route: 'following' },
  { key: 'followerCount', label: '粉丝', route: 'followers' }
]

// 自己的主页说「我的作品」，别人的主页说「TA 的作品」
const postSectionTitle = computed(() => (profile.value?.isSelf ? '我的作品' : 'TA 的作品'))
</script>

<template>
  <section class="page">
    <p v-if="loading" class="state state--loading">正在加载主页…</p>

    <p v-else-if="errorMessage" class="state state--error">
      {{ errorMessage }}
      <button class="btn btn--ghost btn--compact" type="button" @click="router.push('/u/1')">回到演示主页</button>
    </p>

    <template v-else-if="profile">
      <header class="profile-card">
        <div class="profile-card__head">
          <AvatarBadge
            :nickname="profile.nickname"
            :avatar-url="profile.avatarUrl || ''"
            :seed="profile.userId"
            :size="84"
          />
          <div class="profile-card__identity">
            <h1 class="profile-card__nickname">
              {{ profile.nickname }}
              <span v-if="profile.isMutual" class="badge badge--friend">好友</span>
            </h1>
            <p class="profile-card__username">@{{ profile.username }}</p>
            <p class="profile-card__bio">{{ profile.bio || '这个人很神秘，还没有填写简介。' }}</p>
          </div>
          <div class="profile-card__action">
            <FollowButton
              :user-id="profile.userId"
              :is-following="profile.isFollowing"
              :is-mutual="profile.isMutual"
              :is-self="profile.isSelf"
              @change="onFollowChange"
              @error="showError"
            />
          </div>
        </div>

        <div class="profile-stats">
          <button
            v-for="item in statItems"
            :key="item.key"
            class="profile-stats__item"
            type="button"
            @click="router.push({ name: item.route, params: { userId: profile.userId } })"
          >
            <strong>{{ profile[item.key] }}</strong>
            <span>{{ item.label }}</span>
          </button>
          <div class="profile-stats__item profile-stats__item--static">
            <strong>0</strong>
            <span>作品</span>
          </div>
        </div>
      </header>

      <section class="panel">
        <div class="panel__head">
          <h2>{{ postSectionTitle }}</h2>
          <span class="panel__hint">作品列表等包 3 的瀑布流组件接入后替换这里</span>
        </div>
        <div class="post-grid">
          <article v-for="n in 6" :key="n" class="post-placeholder">
            <span>作品占位 {{ n }}</span>
          </article>
        </div>
      </section>

      <p v-if="toast" class="toast">{{ toast }}</p>
    </template>
  </section>
</template>
