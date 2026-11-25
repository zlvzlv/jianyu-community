<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { followUser, unfollowUser } from '../api/social'
import { useSessionStore } from '../stores/session'

const props = defineProps({
  userId: { type: Number, required: true },
  isFollowing: { type: Boolean, default: false },
  isMutual: { type: Boolean, default: false },
  isSelf: { type: Boolean, default: false },
  compact: { type: Boolean, default: false }
})

const emit = defineEmits(['change', 'error'])

const session = useSessionStore()
const route = useRoute()
const router = useRouter()
const loading = ref(false)

const label = computed(() => {
  if (loading.value) return '处理中…'
  if (!props.isFollowing) return '关注'
  return props.isMutual ? '互相关注' : '已关注'
})

const hint = computed(() =>
  props.isSelf ? '' : props.isFollowing ? '点击取消关注' : '关注 TA'
)

function requireLogin() {
  router.push({ name: 'login', query: { redirect: route.fullPath } })
}

async function toggle() {
  if (props.isSelf || loading.value) return
  if (!session.isLoggedIn) {
    requireLogin()
    return
  }
  loading.value = true
  try {
    const state = props.isFollowing ? await unfollowUser(props.userId) : await followUser(props.userId)
    emit('change', state)
  } catch (error) {
    if (error.status === 401) {
      session.logout()
      requireLogin()
    } else {
      emit('error', error.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <button
    v-if="isSelf"
    class="btn btn--ghost"
    :class="{ 'btn--compact': compact }"
    type="button"
    disabled
  >
    本人主页
  </button>
  <button
    v-else
    class="btn"
    :class="[isFollowing ? 'btn--soft' : 'btn--primary', { 'btn--compact': compact }]"
    type="button"
    :title="hint"
    :disabled="loading"
    @click="toggle"
  >
    {{ label }}
  </button>
</template>
