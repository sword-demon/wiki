<template>
  <a-layout-footer class="wiki-footer">
    Ant Design ©2021 wiki电子书
  </a-layout-footer>
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: 'the-footer'
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