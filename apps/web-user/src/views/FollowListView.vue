<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AvatarBadge from '../components/AvatarBadge.vue'
import FollowButton from '../components/FollowButton.vue'
import { fetchFollowList } from '../api/social'
import { useSessionStore } from '../stores/session'

const props = defineProps({
  userId: { type: Number, required: true },
  tab: { type: String, default: 'followers' }
})

const router = useRouter()
const session = useSessionStore()
const PAGE_SIZE = 20

const users = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const errorMessage = ref('')
const toast = ref('')

const isFollowers = computed(() => props.tab === 'followers')
const title = computed(() => (isFollowers.value ? '粉丝' : '关注'))
const emptyText = computed(() =>
  isFollowers.value ? '还没有粉丝，去别的作品下互动一下吧。' : '还没有关注任何人。'
)
const hasMore = computed(() => users.value.length < total.value)

async function load(reset) {
  if (loading.value) return
  loading.value = true
  errorMessage.value = ''
  if (reset) {
    page.value = 1
    users.value = []
  }
  try {
    const result = await fetchFollowList(props.userId, props.tab, page.value, PAGE_SIZE)
    users.value = reset ? result.items : users.value.concat(result.items)
    total.value = result.total
  } catch (error) {
    errorMessage.value = error.code === 50002 ? '这个用户不存在或已停用' : error.message
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.userId, props.tab],
  () => load(true),
  { immediate: true }
)

async function loadMore() {
  page.value += 1
  await load(false)
}

function onFollowChange(user, state) {
  user.isFollowing = state.isFollowing
  user.isMutual = state.isMutual
  toast.value = state.isFollowing
    ? state.isMutual
      ? `已与 ${user.nickname} 互相关注`
      : `已关注 ${user.nickname}`
    : `已取消关注 ${user.nickname}`
  window.setTimeout(() => (toast.value = ''), 2200)

  // 在「关注」列表里取消关注后，这一行不再属于该列表
  if (!isFollowers.value && !state.isFollowing) {
    users.value = users.value.filter((item) => item.userId !== user.userId)
    total.value = Math.max(0, total.value - 1)
  }
}

function showError(message) {
  toast.value = message
  window.setTimeout(() => (toast.value = ''), 2200)
}
</script>

<template>
  <section class="page">
    <header class="subpage-head">
      <button class="link-back" type="button" @click="router.push({ name: 'profile', params: { userId } })">
        ← 返回主页
      </button>
      <h1>{{ title }}列表</h1>
      <p class="subpage-head__meta">共 {{ total }} 人</p>
    </header>

    <nav class="tabs">
      <button
        class="tabs__item"
        :class="{ 'tabs__item--active': isFollowers }"
        type="button"
        @click="router.push({ name: 'followers', params: { userId } })"
      >
        粉丝
      </button>
      <button
        class="tabs__item"
        :class="{ 'tabs__item--active': !isFollowers }"
        type="button"
        @click="router.push({ name: 'following', params: { userId } })"
      >
        关注
      </button>
    </nav>

    <p v-if="errorMessage" class="state state--error">{{ errorMessage }}</p>

    <div v-else class="panel">
      <p v-if="!loading && users.length === 0" class="state">{{ emptyText }}</p>

      <ul class="user-list">
        <li v-for="user in users" :key="user.userId" class="user-list__item">
          <button
            class="user-list__main"
            type="button"
            @click="router.push({ name: 'profile', params: { userId: user.userId } })"
          >
            <AvatarBadge :nickname="user.nickname" :avatar-url="user.avatarUrl || ''" :seed="user.userId" :size="52" />
            <span class="user-list__text">
              <strong>
                {{ user.nickname }}
                <span v-if="user.isMutual" class="badge badge--friend">好友</span>
              </strong>
              <small>@{{ user.username }}</small>
              <em>{{ user.bio || '这个人很神秘，还没有填写简介。' }}</em>
            </span>
          </button>
          <FollowButton
            :user-id="user.userId"
            :is-following="user.isFollowing"
            :is-mutual="user.isMutual"
            :is-self="user.userId === session.currentUserId"
            compact
            @change="(state) => onFollowChange(user, state)"
            @error="showError"
          />
        </li>
      </ul>

      <div v-if="hasMore" class="list-more">
        <button class="btn btn--ghost" type="button" :disabled="loading" @click="loadMore">
          {{ loading ? '加载中…' : '加载更多' }}
        </button>
      </div>

      <p v-else-if="users.length > 0" class="list-end">没有更多了</p>
    </div>

    <p v-if="toast" class="toast">{{ toast }}</p>
  </section>
</template>
