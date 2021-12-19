<template>
  <a-layout>
    <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <a-table
          :columns="columns"
          :row-key="record => record.id"
          :data-source="ebooks"
          :pagination="pagination"
          :loading="Loading"
          @change="handleTableChange">
        <template #cover="{text: cover}">
          <img v-if="cover" :src="cover" alt="avatar">
        </template>
        <template v-slot:action="{text, record}">
          <!-- 空格组件 -->
          <a-space size="small">
            <a-button type="primary" @click="edit">
              编辑
            </a-button>
            <a-button type="danger">
              删除
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-layout-content>
  </a-layout>

  <a-modal
      title="电子书表单"
      v-model:visible="modalVisible"
      :confirm-loading="modalLoading"
      @ok="handleModalOk">
    <p>text</p>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue'
import axios from 'axios'
import { message } from 'ant-design-vue'

export default defineComponent({
  name: 'AdminEbook',
  setup() {
    const ebooks = ref()
    const pagination = ref({
      current: 1,
      pageSize: 4,
      total: 0
    })
    const loading = ref(false)

    const columns = [
      {
        title: '封面',
        dataIndex: 'cover',
        slots: {customRender: 'cover'}
      },
      {
        title: '名称',
        dataIndex: 'name'
      },
      {
        title: '分类一',
        key: 'category1Id',
        dataIndex: 'category1Id'
      },
      {
        title: '分类二',
        dataIndex: 'category2Id',
        slots: {customRender: 'name'}
      },
      {
        title: '文档数',
        dataIndex: 'docCount'
      },
      {
        title: '阅读数',
        dataIndex: 'viewCount'
      },
      {
        title: '点赞数',
        dataIndex: 'voteCount'
      },
      {
        title: 'Action',
        key: 'action',
        slots: {customRender: 'action'}
      }
    ]

    const handleQuery = (params: any) => {
      loading.value = true
      // 如果不清空现有数据，则编辑保存重新加载数据后，再点编辑
      ebooks.value = []
      axios.get("/ebook/list", {
        params: {
          page: params.page,
          size: params.size
        }
      }).then((response) => {
        loading.value = false
        const data = response.data
        if (data.success) {
          ebooks.value = data.content.list

          pagination.value.current = params.page
          pagination.value.total = data.content.total
        } else {
          message.error(data.message)
        }
      })
    }

    // 表格点击页码时触发
    const handleTableChange = (pagination: any) => {
      console.log('看看自带的分页参数都有啥: ' + pagination)
      handleQuery({
        page: pagination.current,
        size: pagination.pageSize
      })
    }

    // 表单
    const modalVisible = ref(false)
    const modalLoading = ref(false)
    const handleModalOk = () => {
      modalLoading.value = true
      setTimeout(() => {
        modalVisible.value = true
        modalLoading.value = true
      }, 2000)
    }

    // 编辑
    const edit = () => {
      modalVisible.value = true
    }

    onMounted(() => {
      handleQuery({
        page: 1,
        size: pagination.value.pageSize // 响应式变量 必须加上 .value  size 必须和后端的 PageReq 的size一致
      })
    })

    return {
      ebooks,
      pagination,
      columns,
      loading,
      handleTableChange,
      edit,
      modalVisible,
      modalLoading,
      handleModalOk
    }
  }
})
</script>

<style scoped>
img {
  width: 50px;
  height: 50px;
}
</style>