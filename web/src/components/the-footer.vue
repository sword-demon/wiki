<template>
  <a-layout-footer class="wiki-footer">
    Ant Design ©2021 wiki电子书 <span v-show="user.id">欢迎: {{user.name}}</span>
  </a-layout-footer>
</template>

<script lang="ts">
import { computed, defineComponent } from 'vue'
import store from "@/store";

export default defineComponent({
  name: 'the-footer',
  setup() {
    const user = computed(() => store.state.user)

    return {
      user
    }
  }
})
</script>

<style>
.wiki-footer {
  text-align: center;
  height: 100px;
  width: 100%;
  position: absolute;
  bottom: 0;
  left: 0;
  line-height: 100px;
}
</style>