<template>
  <div>
    <!-- 顶部操作栏 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-row :gutter="16" align="middle">
        <el-col :span="6">
          <el-input v-model="keyword" placeholder="搜索姓名/邮箱/称呼" clearable @clear="loadData" @keyup.enter="loadData">
            <template #append><el-button @click="loadData" icon="Search" /></template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="selectedTag" placeholder="按标签筛选" clearable @change="loadData">
            <el-option v-for="tag in tags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-col>
        <el-col :span="14" style="text-align: right">
          <el-button type="primary" @click="showDialog()">新增客户</el-button>
          <el-button @click="showImport = true">批量导入</el-button>
          <el-button @click="handleDownloadTemplate">下载模板</el-button>
          <el-button @click="handleExport">导出CSV</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 客户表格 -->
    <el-card shadow="never">
      <el-table :data="customers" stripe v-loading="loading">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="salutation" label="称呼" width="120" />
        <el-table-column prop="email" label="邮箱" width="250" />
        <el-table-column prop="tags" label="标签" width="200">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag" size="small" style="margin-right: 4px">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="showDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除该客户？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" text size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadData"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑客户' : '新增客户'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="客户姓名" />
        </el-form-item>
        <el-form-item label="称呼" prop="salutation">
          <el-input v-model="form.salutation" placeholder="如：张总、李老师" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="客户邮箱地址" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tags" multiple filterable allow-create default-first-option placeholder="输入标签后回车">
            <el-option v-for="tag in tags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog v-model="showImport" title="批量导入客户" width="500px" destroy-on-close>
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        accept=".csv,.xlsx,.xls"
        :limit="1"
        :on-exceed="() => ElMessage.warning('只能上传一个文件')"
        :on-change="handleFileChange"
      >
        <el-icon style="font-size: 48px; color: #c0c4cc"><UploadFilled /></el-icon>
        <div>将 CSV/Excel 文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div style="color: #999; font-size: 12px">支持 CSV、Excel 格式，表头需包含：姓名、称呼、邮箱</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="showImport = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="handleImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import {
  getCustomers,
  createCustomer,
  updateCustomer,
  deleteCustomer,
  importCustomers,
  exportCustomers,
  getAllTags,
} from '../api/customer'
import type { Customer, CustomerRequest } from '../utils/types'

const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const customers = ref<Customer[]>([])
const tags = ref<string[]>([])
const keyword = ref('')
const selectedTag = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const showImport = ref(false)
const editingId = ref('')
const formRef = ref<FormInstance>()
const uploadFile = ref<File | null>(null)

const form = reactive<CustomerRequest>({ name: '', salutation: '', email: '', tags: [], remark: '' })
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  salutation: [{ required: true, message: '请输入称呼', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email' as const, message: '邮箱格式不正确', trigger: 'blur' },
  ],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCustomers({ page: page.value - 1, size: size.value, keyword: keyword.value || undefined, tag: selectedTag.value || undefined })
    customers.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  try {
    const res = await getAllTags()
    tags.value = res.data
  } catch {}
}

// 下载模板
async function handleDownloadTemplate() {
  try {
    const res = await fetch('/mail/api/customers/import-template')
    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '客户导入模板.csv'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch {
    ElMessage.error('下载模板失败')
  }
}

function showDialog(row?: Customer) {
  if (row) {
    editingId.value = row.id
    Object.assign(form, { name: row.name, salutation: row.salutation, email: row.email, tags: [...row.tags], remark: row.remark })
  } else {
    editingId.value = ''
    Object.assign(form, { name: '', salutation: '', email: '', tags: [], remark: '' })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateCustomer(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createCustomer(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: string) {
  await deleteCustomer(id)
  ElMessage.success('删除成功')
  loadData()
}

function handleFileChange(file: any) {
  uploadFile.value = file.raw
}

async function handleImport() {
  if (!uploadFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  importing.value = true
  try {
    const res = await importCustomers(uploadFile.value)
    ElMessage.success(`导入完成：成功 ${res.data.success} 条，失败 ${res.data.failed} 条`)
    showImport.value = false
    loadData()
    loadTags()
  } finally {
    importing.value = false
  }
}

async function handleExport() {
  try {
    const res = await exportCustomers({ tag: selectedTag.value || undefined })
    const blob = new Blob([res as any], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '通讯录导出.csv'
    a.click()
    URL.revokeObjectURL(url)
  } catch {}
}

onMounted(() => {
  loadData()
  loadTags()
})
</script>
