<template>
  <div style="border: 1px solid #ccc; border-radius: 4px">
    <!-- 工具栏 -->
    <div style="border-bottom: 1px solid #ccc; padding: 4px 8px; display: flex; align-items: center; gap: 4px">
      <el-button-group>
        <el-button size="small" @click="execCommand('bold')" title="加粗"><strong>B</strong></el-button>
        <el-button size="small" @click="execCommand('italic')" title="斜体"><em>I</em></el-button>
        <el-button size="small" @click="execCommand('underline')" title="下划线"><u>U</u></el-button>
      </el-button-group>
      <el-divider direction="vertical" />
      <el-button-group>
        <el-button size="small" @click="execCommand('insertUnorderedList')" title="无序列表">• 列表</el-button>
        <el-button size="small" @click="execCommand('insertOrderedList')" title="有序列表">1. 列表</el-button>
      </el-button-group>
      <el-divider direction="vertical" />
      <el-button size="small" type="primary" @click="insertSalutation" title="插入称呼变量">{{ 称呼 }}</el-button>
    </div>

    <!-- 编辑区域 -->
    <div
      ref="editorRef"
      contenteditable="true"
      style="padding: 12px; min-height: 200px; outline: none; font-size: 14px; line-height: 1.6"
      @input="handleInput"
      v-html="modelValue"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'insert-salutation'): void
}>()

const editorRef = ref<HTMLDivElement>()

function execCommand(command: string) {
  document.execCommand(command, false)
  editorRef.value?.focus()
}

function insertSalutation() {
  document.execCommand('insertText', false, '{{称呼}}')
  emit('insert-salutation')
  syncContent()
}

function handleInput() {
  syncContent()
}

function syncContent() {
  if (editorRef.value) {
    emit('update:modelValue', editorRef.value.innerHTML)
  }
}

onMounted(() => {
  // 确保初始内容已渲染
  if (editorRef.value && props.modelValue) {
    editorRef.value.innerHTML = props.modelValue
  }
})
</script>
