<script setup>
import { computed } from 'vue'

const props = defineProps({
  nickname: { type: String, default: '' },
  avatarUrl: { type: String, default: '' },
  seed: { type: [Number, String], default: 0 },
  size: { type: Number, default: 72 }
})

const initial = computed(() => (props.nickname || '?').trim().charAt(0).toUpperCase())
const tone = computed(() => Math.abs(Number(props.seed) || 0) % 5)
const style = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`,
  fontSize: `${Math.round(props.size * 0.4)}px`
}))
</script>

<template>
  <img v-if="avatarUrl" class="avatar" :src="avatarUrl" :alt="nickname" :style="style" />
  <span v-else class="avatar avatar--fallback" :class="`avatar--tone-${tone}`" :style="style">
    {{ initial }}
  </span>
</template>
