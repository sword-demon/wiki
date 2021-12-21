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
          :data-source="categorys"
          :pagination="pagination"
          :loading="Loading"
          @change="handleTableChange">
        <template #cover="{text: cover}">
          <img v-if="cover" :src="cover" alt="avatar">
        </template>
        <template v-slot:action="{text, record}">
          <!-- 空格组件 -->
          <a-space size="small">
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
      title="分类表单"
      v-model:visible="modalVisible"
      :confirm-loading="modalLoading"
      @ok="handleModalOk">
    <a-form :modal="category" :label-col="{span: 6}">
      <a-form-item label="名称">
        <a-input v-model:value="category.name"/>
      </a-form-item>
      <a-form-item label="排序">
        <a-input v-model:value="category.sort"/>
      </a-form-item>
      <a-form-item label="父分类">
        <a-input v-model:value="category.parent"/>
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
  name: 'AdminCategory',
  setup() {
    const param = ref()
    // 这里必须设置value 为一个空对象
    param.value = {}
    const categorys = ref()
    const pagination = ref({
      current: 1,
      pageSize: 10,
      total: 0
    })
    const loading = ref(false)

    const columns = [
      {
        title: '名称',
        dataIndex: 'name'
      },
      {
        title: '顺序',
        key: 'sort',
        dataIndex: 'sort'
      },
      {
        title: '父分类',
        dataIndex: 'parent',
        slots: {customRender: 'parent'}
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
      categorys.value = []
      axios.get("/category/list", {
        params: {
          page: params.page,
          size: params.size,
          name: param.value.name
        }
      }).then((response) => {
        loading.value = false
        const data = response.data
        if (data.success) {
          categorys.value = data.content.list

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
    const category = ref()
    const modalVisible = ref(false)
    const modalLoading = ref(false)
    const handleModalOk = () => {
      modalLoading.value = true
      axios.post("/category/save", category.value).then((response) => {
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
      category.value = Tool.copy(record);
    }

    // 新增
    const add = () => {
      modalVisible.value = true;
      // 清空
      category.value = {}
    }

    // 删除
    const handleDelete = (id: number) => {
      axios.delete("/category/delete/" + id).then((response) => {
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
      handleQuery({
        page: 1,
        size: pagination.value.pageSize // 响应式变量 必须加上 .value  size 必须和后端的 PageReq 的size一致
      })
    })

    return {
      param,
      categorys,
      pagination,
      columns,
      loading,
      handleTableChange,
      edit,
      add,
      handleDelete,
      category,
      modalVisible,
      modalLoading,
      handleModalOk,
      handleQuery
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