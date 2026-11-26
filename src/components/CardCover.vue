<template>
  <img v-if="currentSrc" :src="currentSrc" alt="" loading="lazy" decoding="async" @error="onImageError" />
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({ src: String, fallback: String })
const currentSrc = ref('')
watch(() => [props.src, props.fallback], () => {
  currentSrc.value = props.src || props.fallback
}, { immediate: true })

function onImageError() {
  currentSrc.value = currentSrc.value === props.fallback ? '' : props.fallback
}
</script>
