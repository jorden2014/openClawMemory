<template>
  <div>
    <!-- 顶部操作栏 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-row :gutter="16" align="middle">
        <el-col :span="8">
          <el-input v-model="keyword" placeholder="搜索模板名称" clearable @clear="loadData" @keyup.enter="loadData">
            <template #append><el-button @click="loadData" icon="Search" /></template>
          </el-input>
        </el-col>
        <el-col :span="16" style="text-align: right">
          <el-button type="primary" @click="showDialog()">新建模板</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 模板列表 -->
    <el-row :gutter="16">
      <el-col :span="8" v-for="tpl in templates" :key="tpl.id" style="margin-bottom: 16px">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold">{{ tpl.name }}</span>
              <div>
                <el-button type="primary" text size="small" @click="handlePreview(tpl)">预览</el-button>
                <el-button type="primary" text size="small" @click="showDialog(tpl)">编辑</el-button>
                <el-popconfirm title="确认删除该模板？" @confirm="handleDelete(tpl.id)">
                  <template #reference>
                    <el-button type="danger" text size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </template>
          <div style="color: #999; font-size: 13px; margin-bottom: 8px">主题：{{ tpl.subject }}</div>
          <div style="max-height: 100px; overflow: hidden; font-size: 13px; color: #666" v-html="tpl.body" />
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!templates.length && !loading" description="暂无模板" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑模板' : '新建模板'" width="800px" destroy-on-close top="5vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="form.name" placeholder="如：2026年春节祝福" />
        </el-form-item>
        <el-form-item label="邮件主题" prop="subject">
          <el-input v-model="form.subject" placeholder="支持 {{称呼}} 变量">
            <template #append>
              <el-button @click="insertVariable('subject')">插入称呼</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="邮件正文" prop="body">
          <RichTextEditor v-model="form.body" @insert-salutation="insertVariable('body')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="模板预览" width="700px" destroy-on-close>
      <el-form label-width="80px" style="margin-bottom: 16px">
        <el-form-item label="示例称呼">
          <el-input v-model="previewSalutation" placeholder="输入称呼查看效果" style="width: 200px" />
        </el-form-item>
      </el-form>
      <div style="margin-bottom: 8px; color: #999; font-size: 13px">邮件主题：{{ previewSubject }}</div>
      <el-divider />
      <div v-html="previewBody" style="padding: 16px; border: 1px solid #eee; border-radius: 4px; min-height: 200px" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getTemplates, createTemplate, updateTemplate, deleteTemplate } from '../api/template'
import type { MailTemplate, TemplateRequest } from '../utils/types'
import RichTextEditor from '../components/RichTextEditor.vue'

const loading = ref(false)
const submitting = ref(false)
const templates = ref<MailTemplate[]>([])
const keyword = ref('')

const dialogVisible = ref(false)
const previewVisible = ref(false)
const editingId = ref('')
const formRef = ref<FormInstance>()
const previewTemplate = ref<MailTemplate | null>(null)
const previewSalutation = ref('张总')

const form = reactive<TemplateRequest>({ name: '', subject: '', body: '' })
const rules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  subject: [{ required: true, message: '请输入邮件主题', trigger: 'blur' }],
  body: [{ required: true, message: '请输入邮件正文', trigger: 'blur' }],
}

const previewSubject = computed(() =>
  (previewTemplate.value?.subject || '').replace(/\{\{称呼\}\}/g, previewSalutation.value)
)
const previewBody = computed(() =>
  (previewTemplate.value?.body || '').replace(/\{\{称呼\}\}/g, previewSalutation.value)
)

async function loadData() {
  loading.value = true
  try {
    const res = await getTemplates({ page: 0, size: 100, keyword: keyword.value || undefined })
    templates.value = res.data.content
  } finally {
    loading.value = false
  }
}

function showDialog(row?: MailTemplate) {
  if (row) {
    editingId.value = row.id
    Object.assign(form, { name: row.name, subject: row.subject, body: row.body })
  } else {
    editingId.value = ''
    Object.assign(form, { name: '', subject: '', body: '' })
  }
  dialogVisible.value = true
}

function insertVariable(field: 'subject' | 'body') {
  if (field === 'subject') {
    form.subject += '{{称呼}}'
  }
  // body 由 RichTextEditor 处理
}

function handlePreview(tpl: MailTemplate) {
  previewTemplate.value = tpl
  previewVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateTemplate(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createTemplate(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: string) {
  await deleteTemplate(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
