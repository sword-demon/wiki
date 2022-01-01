<template>
  <a-layout>
    <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <!-- 间距 -->
      <a-row :gutter="24">
        <a-col :span="8">
          <p>
            <a-form layout="inline" :model="param">
              <a-form-item>
                <a-button type="primary" @click="handleQuery()">
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
              v-if="level1.length > 0"
              :columns="columns"
              :row-key="record => record.id"
              :data-source="level1"
              :pagination="false"
              :loading="Loading"
              size="small"
              :defaultExpandAllRows="true">
            <template #name="{text, record}">
              {{ record.sort }} {{ text }}
            </template>
            <template v-slot:action="{text, record}">
              <!-- 空格组件 -->
              <a-space size="small">
                <a-button type="primary" @click="edit(record)" size="small">
                  编辑
                </a-button>
                <a-popconfirm
                    title="确认要删除吗?"
                    ok-text="确认"
                    cancel-text="取消"
                    @confirm="handleDelete(record.id)"
                >
                  <a-button type="danger" size="small">
                    删除
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table>
        </a-col>
        <a-col :span="16">
          <p>
            <a-form layout="inline" :model="param">
              <a-form-item>
                <a-button type="primary" @click="handleSave">
                  保存
                </a-button>
              </a-form-item>
            </a-form>
          </p>
          <a-form :modal="doc" layout="vertical" :label-col="{span: 6}">
            <a-form-item>
              <a-input v-model:value="doc.name" placeholder="名称"/>
            </a-form-item>
            <a-form-item>
              <!-- replaceFields 替换 treeNode 中 title,value,key,children 字段为 treeData 中对应的字段 -->
              <a-tree-select
                  v-model:value="doc.parent"
                  style="width: 100%"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  :tree-data="treeSelectData"
                  placeholder="请选择父文档"
                  tree-default-expand-all
                  :replaceFields="{title: 'name', key: 'id', value: 'id'}"
              >
              </a-tree-select>
            </a-form-item>
            <a-form-item>
              <a-input v-model:value="doc.sort" placeholder="排序"/>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handlePreviewContent">
                <EyeOutlined /> 内容预览
              </a-button>
            </a-form-item>
            <a-form-item>
              <div id="content"></div>
            </a-form-item>
          </a-form>
        </a-col>
      </a-row>

      <!-- 抽屉 -->
      <a-drawer width="900" placement="right" :closeable="false" :visible="drawerVisible" @close="onDrawerClose">
        <div class="wangeditor" :innerHTML="previewHtml"></div>
      </a-drawer>
    </a-layout-content>
  </a-layout>

  <!--  <a-modal-->
  <!--      title="文档表单"-->
  <!--      v-model:visible="modalVisible"-->
  <!--      :confirm-loading="modalLoading"-->
  <!--      @ok="handleModalOk">-->
  <!--    -->
  <!--  </a-modal>-->
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, createVNode } from 'vue'
import axios from 'axios'
import { message, Modal } from 'ant-design-vue'
import { Tool } from '@/util/tool'
import { useRoute } from "vue-router";
import ExclamationCircleOutlined from "@ant-design/icons-vue/ExclamationCircleOutlined";
import E from 'wangeditor'

export default defineComponent({
  name: 'AdminDoc',
  setup() {

    const route = useRoute()
    // 得到路由中的参数
    console.log("路由: ", route)
    console.log("route.path: ", route.path)
    console.log("route.query: ", route.query)
    console.log("route.params: ", route.params)
    console.log("route.fullPath: ", route.fullPath)
    console.log("route.name: ", route.name)
    console.log("route.meta: ", route.meta)

    const param = ref()
    // 这里必须设置value 为一个空对象
    param.value = {}
    const docs = ref()
    const loading = ref(false)

    // 树选择组件会随着当前编辑的节点而变化，所以单独声明一个响应式的变量
    const treeSelectData = ref()
    treeSelectData.value = []

    const columns = [
      {
        title: '名称',
        dataIndex: 'name',
        slots: {customRender: 'name'}
      },
      {
        title: 'Action',
        key: 'action',
        slots: {customRender: 'action'}
      }
    ]

    // 一级文档
    const level1 = ref()
    // 初始化
    level1.value = []

    const handleQuery = () => {
      loading.value = true
      // 如果不清空现有数据，则编辑保存重新加载数据后，再点编辑
      // 清除表格数据
      level1.value = []
      axios.get("/doc/all/" + route.query.ebookId).then((response) => {
        loading.value = false
        const data = response.data
        if (data.success) {
          docs.value = data.content
          console.log('原始数组: ', docs.value)

          level1.value = []
          level1.value = Tool.array2Tree(docs.value, 0)
          console.log('树形机构: ', level1.value)

          // 父文档下拉框初始化，相当于点击新增
          // 代码简洁优化
          treeSelectData.value = Tool.copy(level1.value) || []
          // 添加一个无字
          // if (!treeSelectData.value) {
          //   treeSelectData.value = []
          // }
          treeSelectData.value.unshift({id: 0, name: '无'})
        } else {
          message.error(data.message)
        }
      })
    }

    // 内容查询
    const handleQueryContent = () => {
      axios.get("/doc/find-content/" + doc.value.id).then((response) => {
        const data = response.data
        if (data.success) {
          // 给富文本框赋值
          editor.txt.html(data.content)
        } else {
          message.error(data.message)
        }
      })
    }

    // 表单
    const doc = ref()
    // 初始赋值为空对象 -> 初始从路由里获取电子书id并赋值
    doc.value = {
      ebookId: route.query.ebookId
    }
    const modalVisible = ref(false)
    const modalLoading = ref(false)
    const editor = new E("#content")
    // 调整为0，防止下拉框被挡住
    editor.config.zIndex = 0

    const handleSave = () => {
      // 和后端的字段内容联系起来
      doc.value.content = editor.txt.html()
      axios.post("/doc/save", doc.value).then((response) => {
        const data = response.data
        if (data.success) {
          // 消息提示
          message.success("保存成功!")

          // 重新加载列表
          handleQuery()
        } else {
          message.error(data.message)
        }
      })
    }

    // 将某节点及其子子孙孙节点全部置为disabled
    const setDisable = (treeSelectData: any, id: any) => {
      console.log(treeSelectData, id)
      // 遍历数组，即遍历某一层节点
      for (let i = 0; i < treeSelectData.length; i++) {
        const node = treeSelectData[i]
        if (node.id === id) {
          // 如果当前节点就是目标节点
          console.log("disabled", node)
          // 将目标节点设置为disabled
          node.disabled = true

          // 遍历所有自及诶单，将所有子节点全部都加上disabled
          const children = node.children
          if (Tool.isNotEmpty(children)) {
            for (let j = 0; j < children.length; j++) {
              setDisable(children, children[j].id)
            }
          }
        } else {
          // 如果当前节点不是目标节点，则到其子节点再找找看
          const children = node.children
          if (Tool.isNotEmpty(children)) {
            setDisable(children, id)
          }
        }
      }
    }

    // 存储删除的id结果集
    const deleteIds: Array<string> = []
    // 获取所有要删除的文档名称
    const deleteNames: Array<string> = [];

    const getDeleteIds = (treeSelectData: any, id: any) => {
      // console.log(treeSelectData, id)
      // 遍历数组，即遍历某一层节点
      for (let i = 0; i < treeSelectData.length; i++) {
        const node = treeSelectData[i]
        if (node.id === id) {
          // 将目标放入id结果集
          deleteIds.push(id)
          // 将目标名称放入名称结果集
          deleteNames.push(node.name)

          // 遍历所有自及诶单，将所有子节点全部都加上disabled
          const children = node.children
          if (Tool.isNotEmpty(children)) {
            for (let j = 0; j < children.length; j++) {
              getDeleteIds(children, children[j].id)
            }
          }
        } else {
          // 如果当前节点不是目标节点，则到其子节点再找找看
          const children = node.children
          if (Tool.isNotEmpty(children)) {
            getDeleteIds(children, id)
          }
        }
      }
    }

    // 编辑
    const edit = (record: any) => {
      // 没有模态框了可以去除
      // modalVisible.value = true;

      // 编辑的时候先去除富文本框
      editor.txt.html("")
      doc.value = Tool.copy(record);
      // 等到doc.value 有值再去查
      handleQueryContent()

      // 不能选择当前节点及其所有子孙节点
      treeSelectData.value = Tool.copy(level1.value)
      setDisable(treeSelectData.value, record.id)

      // 为选择树添加一个 "无"字
      treeSelectData.value.unshift({id: 0, name: '无'})

      // setTimeout(() => {
      //   editor.create()
      // }, 100)
    }

    // 新增
    const add = () => {
      // modalVisible.value = true;
      // 先去除富文本框
      editor.txt.html("")
      // 清空
      doc.value = {
        ebookId: route.query.ebookId
      }

      treeSelectData.value = Tool.copy(level1.value) || []
      // 为选择树添加一个 "无"字
      treeSelectData.value.unshift({id: 0, name: '无'})

      // 模态框里需要写
      // setTimeout(() => {
      //   editor.create()
      // }, 100)
    }

    // 删除
    const handleDelete = (id: number) => {
      console.log(level1.value, id, deleteIds)
      // 清空数组，否则多次删除时，数组会一直增加
      deleteIds.length = 0
      deleteNames.length = 0
      // 注意，响应式变量，必须加上 .value
      getDeleteIds(level1.value, id)
      Modal.confirm({
        title: '重要提醒',
        icon: createVNode(ExclamationCircleOutlined),
        content: '将删除: 【' + deleteNames.join(",") + "】 删除后不可恢复,确定删除?",
        onOk() {
          axios.delete("/doc/delete/" + deleteIds.join(",")).then((response) => {
            const data = response.data
            if (data.success) {
              // 重新加载列表
              handleQuery()
            } else {
              message.error(data.message)
            }
          })
        }
      })
    }

    // 富文本预览
    const drawerVisible = ref(false)
    const previewHtml = ref()
    const handlePreviewContent = () => {
      const html = editor.txt.html()
      previewHtml.value = html
      drawerVisible.value = true
    }

    const onDrawerClose = () => {
      drawerVisible.value = false
    }

    onMounted(() => {
      handleQuery()
      editor.create()
    })

    return {
      param,
      level1,
      // docs,
      columns,
      loading,
      edit,
      add,
      handleDelete,
      doc,
      modalVisible,
      modalLoading,
      handleSave,
      handleQuery,
      treeSelectData,
      drawerVisible,
      previewHtml,
      handlePreviewContent,
      onDrawerClose
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