<template>
  <a-layout>
    <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <p>
        <a-form layout="inline" :model="param">
          <a-form-item>
            <a-input v-model:value="param.name" placeholder="名称"></a-input>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleQuery({page: 1, size: pagination.pageSize})">
              查询
            </a-button>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="add">
              新增
            </a-button>
          </a-form-item>
        </a-form>
      </p>
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
        <template v-slot:category="{text, record}">
          <span>{{ getCategoryName(record.category1Id) }} / {{ getCategoryName(record.category2Id) }}</span>
        </template>
        <template v-slot:action="{text, record}">
          <!-- 空格组件 -->
          <a-space size="small">
            <router-link :to="'/admin/doc?ebookId=' + record.id">
              <a-button type="primary">
                文档管理
              </a-button>
            </router-link>
            <a-button type="primary" @click="edit(record)">
              编辑
            </a-button>
            <a-popconfirm
                title="确认要删除吗?"
                ok-text="确认"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
            >
              <a-button type="danger">
                删除
              </a-button>
            </a-popconfirm>
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
    <a-form :modal="ebook" :label-col="{span: 6}">
      <a-form-item label="封面">
        <a-input v-model:value="ebook.cover"/>
      </a-form-item>
      <a-form-item label="名称">
        <a-input v-model:value="ebook.name"/>
      </a-form-item>
      <a-form-item label="分类">
        <a-cascader v-model:value="categoryIds" :field-names="{label: 'name', value: 'id', children: 'children'}"
                    :options="level1"/>
      </a-form-item>
      <a-form-item label="描述">
        <a-input v-model:value="ebook.description" type="text"/>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue'
import axios from 'axios'
import { message } from 'ant-design-vue'
import { Tool } from '@/util/tool'

export default defineComponent({
  name: 'AdminEbook',
  setup() {
    const param = ref()
    // 这里必须设置value 为一个空对象
    param.value = {}
    const ebooks = ref()
    const pagination = ref({
      current: 1,
      pageSize: 10,
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
        title: '分类',
        slots: {customRender: 'category'}
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
          size: params.size,
          name: param.value.name
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

    // 加载分类
    // 数组 [100, 101] 对应 前端开发/Vue
    const categoryIds = ref()

    // 普通变量
    let categorys: any

    // 表单
    const ebook = ref()
    const modalVisible = ref(false)
    const modalLoading = ref(false)
    const handleModalOk = () => {
      modalLoading.value = true
      // 两个分类
      ebook.value.category1Id = categoryIds.value[0]
      ebook.value.category2Id = categoryIds.value[1]

      axios.post("/ebook/save", ebook.value).then((response) => {
        modalLoading.value = false
        const data = response.data
        if (data.success) {
          modalVisible.value = false
          modalLoading.value = false
          // 重新加载列表
          handleQuery({
            page: pagination.value.current, // 重新查询当前页
            size: pagination.value.pageSize // 响应式变量 必须加上 .value  size 必须和后端的 PageReq 的size一致
          })
        } else {
          message.error(data.message)
        }
      })
    }

    // 编辑
    const edit = (record: any) => {
      modalVisible.value = true;
      ebook.value = Tool.copy(record);
      // 组合进行显示
      categoryIds.value = [ebook.value.category1Id, ebook.value.category2Id]
    }

    // 新增
    const add = () => {
      modalVisible.value = true;
      // 清空
      ebook.value = {}
    }

    const level1 = ref()

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

          // 加载完分类后加载电子书
          handleQuery({
            page: 1,
            size: pagination.value.pageSize // 响应式变量 必须加上 .value  size 必须和后端的 PageReq 的size一致
          })
        } else {
          message.error(data.message)
        }
      })
    }

    // 删除
    const handleDelete = (id: number) => {
      axios.delete("/ebook/delete/" + id).then((response) => {
        const data = response.data
        if (data.success) {
          // 重新加载列表
          handleQuery({
            page: pagination.value.current, // 重新查询当前页
            size: pagination.value.pageSize // 响应式变量 必须加上 .value  size 必须和后端的 PageReq 的size一致
          })
        } else {
          message.error(data.message)
        }
      })
    }

    onMounted(() => {
      handleQueryCategory()
    })

    const getCategoryName = (categoryId: number) => {
      let result = ""
      categorys.forEach((item: any) => {
        if (item.id === categoryId) {
          result = item.name
        }
      })
      return result
    }

    return {
      param,
      ebooks,
      pagination,
      columns,
      loading,
      handleTableChange,
      edit,
      add,
      handleDelete,
      ebook,
      modalVisible,
      modalLoading,
      handleModalOk,
      handleQuery,
      categoryIds,
      level1,
      handleQueryCategory,
      getCategoryName
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