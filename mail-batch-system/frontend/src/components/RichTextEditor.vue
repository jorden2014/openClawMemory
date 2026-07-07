<template>
  <div class="rich-editor-wrapper">
    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <div class="toolbar-left">
        <select v-model="selectedFont" @change="setFont" class="toolbar-select">
          <option value="sans-serif">默认字体</option>
          <option value="serif">Serif</option>
          <option value="monospace">Monospace</option>
          <option value="Arial, sans-serif">Arial</option>
          <option value="'Times New Roman', serif">Times New Roman</option>
          <option value="'Courier New', monospace">Courier New</option>
          <option value="'Microsoft YaHei', sans-serif">微软雅黑</option>
          <option value="SimSun, serif">宋体</option>
          <option value="SimHei, sans-serif">黑体</option>
          <option value="KaiTi, serif">楷体</option>
        </select>
        <select v-model="selectedSize" @change="setSize" class="toolbar-select">
          <option value="12px">小</option>
          <option value="14px">正常</option>
          <option value="18px">大</option>
          <option value="24px">超大</option>
        </select>
        <el-divider direction="vertical" />
        <button @click="execFormat('bold')" title="加粗" class="toolbar-btn"><strong>B</strong></button>
        <button @click="execFormat('italic')" title="斜体" class="toolbar-btn"><em>I</em></button>
        <button @click="execFormat('underline')" title="下划线" class="toolbar-btn"><u>U</u></button>
        <button @click="execFormat('strike')" title="删除线" class="toolbar-btn"><s>S</s></button>
        <el-divider direction="vertical" />
        <button @click="execFormat('list', 'ordered')" title="有序列表" class="toolbar-btn-text">1. 列表</button>
        <button @click="execFormat('list', 'bullet')" title="无序列表" class="toolbar-btn-text">• 列表</button>
        <el-divider direction="vertical" />
        <button @click="execFormat('align', 'center')" title="居中" class="toolbar-btn-text">居中</button>
        <button @click="execFormat('align', 'right')" title="右对齐" class="toolbar-btn-text">右对齐</button>
        <el-divider direction="vertical" />
        <button @click="insertLink" title="插入链接" class="toolbar-btn-text">🔗链接</button>
        <button @click="handleImageInsert" title="插入图片" class="toolbar-btn-text">🖼️图片</button>
        <button @click="execFormat('clean')" title="清除格式" class="toolbar-btn-text">清除格式</button>
      </div>
      <div class="toolbar-right">
        <el-button size="small" type="primary" @click="insertSalutation" title="插入称呼变量">
          {{ label }}
        </el-button>
      </div>
    </div>

    <!-- Quill 编辑器容器 -->
    <div ref="editorRef" class="quill-editor"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'

const props = defineProps<{ 
  modelValue: string
  label?: string 
}>()
const label = props.label || '插入称呼'

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'insert-salutation'): void
}>()

const editorRef = ref<HTMLDivElement>()
let quill: any = null
const selectedFont = ref('sans-serif')
const selectedSize = ref('14px')

// 动态加载 Quill（从 CDN）
async function loadQuill() {
  if ((window as any).Quill) {
    return (window as any).Quill
  }
  
  // 加载 CSS
  if (!document.querySelector('link[href*="quill"]')) {
    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = 'https://cdn.quilljs.com/1.3.7/quill.snow.css'
    document.head.appendChild(link)
  }
  
  // 加载 JS
  return new Promise((resolve) => {
    const script = document.createElement('script')
    script.src = 'https://cdn.quilljs.com/1.3.7/quill.min.js'
    script.onload = () => resolve((window as any).Quill)
    document.head.appendChild(script)
  })
}

// 初始化 Quill 编辑器
onMounted(async () => {
  if (!editorRef.value) return

  const Quill = await loadQuill()
  
  quill = new Quill(editorRef.value, {
    theme: 'snow',
    modules: {
      toolbar: false, // 使用自定义工具栏
    },
    placeholder: '请输入邮件内容...',
  })

  // 设置初始内容
  if (props.modelValue) {
    quill.root.innerHTML = props.modelValue
  }

  // 监听内容变化
  quill.on('text-change', () => {
    const content = quill.root.innerHTML || ''
    if (content !== props.modelValue) {
      emit('update:modelValue', content)
    }
  })
})

// 监听外部 modelValue 变化
watch(
  () => props.modelValue,
  (newVal) => {
    if (quill && newVal !== quill.root.innerHTML) {
      quill.root.innerHTML = newVal || ''
    }
  }
)

// 执行格式操作
function execFormat(format: string, value?: string) {
  if (!quill) return
  quill.focus()
  if (value !== undefined) {
    quill.format(format, value)
  } else {
    quill.format(format, !quill.getFormat()[format])
  }
}

// 设置字体
function setFont() {
  if (!quill) return
  quill.format('font', selectedFont.value)
}

// 设置字号
function setSize() {
  if (!quill) return
  quill.format('size', selectedSize.value)
}

// 插入链接
function insertLink() {
  if (!quill) return
  const url = prompt('请输入链接地址：', 'https://')
  if (url) {
    const range = quill.getSelection()
    if (range) {
      quill.insertText(range.index, url, 'link', url)
      quill.setSelection(range.index + url.length)
    }
  }
}

// 插入图片
function handleImageInsert() {
  if (!quill) return

  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) return

    const reader = new FileReader()
    reader.onload = () => {
      const base64 = reader.result as string
      const range = quill!.getSelection()
      if (range) {
        quill!.insertEmbed(range.index, 'image', base64)
        quill!.setSelection(range.index + 1)
      }
      syncContent()
    }
    reader.readAsDataURL(file)
  }
  input.click()
}

// 插入称呼变量
function insertSalutation() {
  if (!quill) return
  const range = quill.getSelection()
  if (range) {
    quill.insertText(range.index, '{{称呼}}')
    quill.setSelection(range.index + 6)
  } else {
    quill.root.innerHTML += '{{称呼}}'
  }
  emit('insert-salutation')
  syncContent()
}

// 同步内容到父组件
function syncContent() {
  if (quill) {
    const content = quill.root.innerHTML
    emit('update:modelValue', content)
  }
}
</script>

<style scoped>
.rich-editor-wrapper {
  border: 1px solid #ccc;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #ccc;
  background: #f8f9fa;
  flex-wrap: wrap;
  gap: 8px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.toolbar-select {
  height: 28px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 0 4px;
  font-size: 12px;
  cursor: pointer;
}

.toolbar-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0 2px;
}

.toolbar-btn:hover {
  background: #ecf5ff;
  border-color: #409eff;
}

.toolbar-btn-text {
  height: 28px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  padding: 0 8px;
  margin: 0 2px;
}

.toolbar-btn-text:hover {
  background: #ecf5ff;
  border-color: #409eff;
}

.quill-editor {
  padding: 12px;
  min-height: 200px;
  max-height: 500px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
}

.quill-editor :deep(.ql-editor) {
  padding: 0;
  min-height: 180px;
}

/* Quill 内置工具栏隐藏 */
.quill-editor :deep(.ql-toolbar) {
  display: none;
}
</style>
