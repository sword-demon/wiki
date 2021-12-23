<template>
  <a-layout>
    <a-layout-sider width="200" style="background: #fff">
      <a-menu
          mode="inline"
          :style="{ height: '100%', borderRight: 0 }"
          @click="handleClick"
      >
        <a-menu-item key="welcome">
          <MailOutlined/>
          <span>欢迎</span>
        </a-menu-item>
        <a-sub-menu v-for="item in level1" :key="item.id">
          <template v-slot:title>
              <span>
                <user-outlined/>
                {{ item.name }}
              </span>
          </template>
          <a-menu-item v-for="child in item.children" :key="child.id">
            <MailOutlined/>
            <span>{{ child.name }}</span>
          </a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-layout-sider>
    <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <div class="welcome" v-show="isShowWelcome">
        <h1>欢迎使用无解的知识库</h1>
      </div>
      <a-list v-show="!isShowWelcome" :grid="{ gutter: 20, column: 3 }" item-layout="vertical" size="large"
              :data-source="ebooks">
        <template #renderItem="{ item }">
          <a-list-item key="item.name">
            <template #actions>
              <span v-for="{ type, text } in actions" :key="type">
                <component v-bind:is="type" style="margin-right: 8px"/>
                {{ text }}
              </span>
            </template>
            <a-list-item-meta :description="item.description">
              <template #title>
                <a :href="item.href">{{ item.name }}</a>
              </template>
              <template #avatar>
                <a-avatar :src="item.cover"/>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </a-layout-content>
  </a-layout>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue';
import axios from 'axios';
import { message } from 'ant-design-vue'
import { Tool } from '@/util/tool'

export default defineComponent({
  name: 'home',
  // vue3新增的初始化方法，组件加载完之后就会初始化
  setup() {

    const ebooks = ref(); // 响应式的数据
    // const ebooks1 = reactive({books: []}); // 响应式的数据 方式2

    const level1 = ref()
    let categorys: any

    // 查询所有分类
    const handleQueryCategory = () => {
      axios.get("/category/all").then((response) => {
        const data = response.data
        if (data.success) {
          categorys = data.content
          console.log('原始数组: ', categorys)

          level1.value = []
          level1.value = Tool.array2Tree(categorys, 0)
          console.log('树形结构: ', level1.value)
        } else {
          message.error(data.message)
        }
      })
    }

    // 查询电子书
    const handleQueryEbook = () => {
      axios.get("/ebook/list", {
        params: {
          page: 1,
          size: 1000,
          categoryId2: categoryId2
        }
      })
          .then((response) => {
            const data = response.data;
            ebooks.value = data.content.list
            // ebooks1.books = data.content
            // console.log(response);
          });
    }

    const isShowWelcome = ref(true)
    let categoryId2 = 0

    // 菜单点击
    const handleClick = (value: any) => {
      // console.log(value)
      if (value.key === 'welcome') {
        isShowWelcome.value = true
      } else {
        categoryId2 = value.key
        isShowWelcome.value = false
        // 点击分类时调用
        handleQueryEbook()
      }
    }

// 生命周期函数里比较适合写初始化内容
    onMounted(() => {

      // 初始加载分类
      handleQueryCategory()

      // handleQueryEbook();
    })

    const actions: Record<string, string>[] = [
      {type: 'StarOutlined', text: '156'},
      {type: 'LikeOutlined', text: '156'},
      {type: 'MessageOutlined', text: '2'},
    ];

    return {
      ebooks,
      // books: toRef(ebooks1, "books"),
      actions,
      level1,
      isShowWelcome,
      handleClick
    }
  }
});
</script>

<style scoped>
.ant-avatar {
  width: 50px;
  height: 50px;
  line-height: 50px;
  border-radius: 8%;
  margin: 5px 0;
}
</style>
