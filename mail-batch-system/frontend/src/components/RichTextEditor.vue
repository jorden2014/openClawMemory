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
        <button type="button" @click="execFormat('bold')" title="加粗" class="toolbar-btn"><strong>B</strong></button>
        <button type="button" @click="execFormat('italic')" title="斜体" class="toolbar-btn"><em>I</em></button>
        <button type="button" @click="execFormat('underline')" title="下划线" class="toolbar-btn"><u>U</u></button>
        <button type="button" @click="execFormat('strike')" title="删除线" class="toolbar-btn"><s>S</s></button>
        <el-divider direction="vertical" />
        <button type="button" @click="execFormat('list', 'ordered')" title="有序列表" class="toolbar-btn-text">1. 列表</button>
        <button type="button" @click="execFormat('list', 'bullet')" title="无序列表" class="toolbar-btn-text">• 列表</button>
        <el-divider direction="vertical" />
        <button type="button" @click="execFormat('align', 'center')" title="居中" class="toolbar-btn-text">居中</button>
        <button type="button" @click="execFormat('align', 'right')" title="右对齐" class="toolbar-btn-text">右对齐</button>
        <el-divider direction="vertical" />
        <button type="button" @click="insertLink" title="插入链接" class="toolbar-btn-text">🔗链接</button>
        <button type="button" @click="handleImageInsert" title="插入图片" class="toolbar-btn-text">🖼️图片</button>
        <button type="button" @click="showImageManager" title="图片管理" class="toolbar-btn-text">📷管理</button>
        <button type="button" @click="openImageResizeDialog" title="调整图片尺寸" class="toolbar-btn-text">📐尺寸</button>
        <button type="button" @click="execFormat('clean')" title="清除格式" class="toolbar-btn-text">清除格式</button>
      </div>
      <div class="toolbar-right">
        <el-button size="small" type="primary" @click="insertSalutation" title="插入称呼变量">
          {{ label }}
        </el-button>
      </div>
    </div>

    <!-- Quill 编辑器容器 -->
    <div ref="editorRef" class="quill-editor"></div>

    <!-- 图片管理对话框 -->
    <el-dialog 
      v-model="imageManagerVisible" 
      title="图片管理" 
      width="900px"
      :close-on-click-modal="false"
    >
      <div class="image-manager-dialog">
        <!-- 上传区域 -->
        <div class="upload-section">
          <el-upload
            class="upload-demo"
            action="/api/upload/image"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeImageUpload"
            list-type="picture-card"
            :show-file-list="false"
            multiple
          >
            <el-icon><Plus /></el-icon>
            <div class="el-upload__text">点击上传</div>
          </el-upload>
          <div class="upload-tip">
            <el-icon><InfoFilled /></el-icon>
            支持 jpg、png、gif 格式，单个文件不超过 10MB
          </div>
        </div>

        <el-divider content-position="left">已上传的图片</el-divider>

        <!-- 图片列表 -->
        <div v-loading="imagesLoading" class="images-container">
          <div v-if="uploadedImages.length === 0 && !imagesLoading" class="empty-state">
            <el-empty description="暂无图片，点击上方上传" :image-size="100" />
          </div>

          <div v-else class="image-grid">
            <div 
              v-for="img in uploadedImages" 
              :key="img.url" 
              class="image-card"
              :class="{ 'selected': selectedImageUrl === img.url }"
              @click="selectImage(img)"
            >
              <div class="image-preview">
                <img :src="img.url" :alt="img.filename" loading="lazy" />
                <div class="image-overlay">
                  <el-icon :size="24"><Check /></el-icon>
                </div>
              </div>
              <div class="image-info">
                <div class="filename" :title="img.filename">
                  {{ img.filename }}
                </div>
                <div class="image-meta">
                  <span class="size">{{ formatFileSize(img.size) }}</span>
                </div>
              </div>
              <div class="image-actions">
                <el-button 
                  size="small" 
                  type="primary" 
                  @click.stop="insertImageToEditor(img.url)"
                >
                  <el-icon><Promotion /></el-icon>
                  插入
                </el-button>
                <el-button 
                  size="small" 
                  type="danger" 
                  @click.stop="deleteImage(img)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部操作 -->
        <div class="dialog-footer" v-if="selectedImageUrl">
          <el-divider />
          <div class="selected-info">
            <span>已选择 1 张图片</span>
            <el-button type="primary" @click="insertSelectedImage">
              插入到编辑器
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 图片尺寸调整对话框 -->
    <el-dialog 
      v-model="imageResizeVisible" 
      title="调整图片尺寸" 
      width="500px"
    >
      <div class="image-resize-dialog">
        <div class="resize-preview">
          <img ref="resizeImageRef" :src="selectedImageSrc" alt="预览" class="preview-img" />
        </div>
        <div class="resize-controls">
          <div class="control-item">
            <label>宽度：</label>
            <el-input-number 
              v-model="imageWidth" 
              :min="50" 
              :max="800" 
              @change="updateImageSize" 
            />
            <span class="unit">px</span>
          </div>
          <div class="control-item">
            <label>高度：</label>
            <el-input-number 
              v-model="imageHeight" 
              :min="50" 
              :max="800" 
              @change="updateImageSize" 
            />
            <span class="unit">px</span>
          </div>
          <div class="control-item">
            <el-checkbox v-model="keepAspectRatio" @change="toggleAspectRatio">保持宽高比</el-checkbox>
          </div>
        </div>
        <div class="dialog-footer">
          <el-button @click="imageResizeVisible = false">取消</el-button>
          <el-button type="primary" @click="applyImageResize">应用</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Plus, InfoFilled, Check, Promotion, Delete } from '@element-plus/icons-vue'
import { getImageList, deleteImage as deleteImageApi } from '../api/image'

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

// 图片尺寸调整相关
const imageResizeVisible = ref(false)
const selectedImageSrc = ref('')
const imageWidth = ref(200)
const imageHeight = ref(200)
const keepAspectRatio = ref(true)
const currentImageElement = ref<HTMLImageElement | null>(null)
const originalAspectRatio = ref(1)

// 打开图片尺寸调整对话框
function openImageResizeDialog() {
  if (!quill) return
  
  // 获取编辑器中的所有图片
  const container = quill.root
  const images = container.querySelectorAll('img')
  
  if (images.length === 0) {
    alert('编辑器中没有图片')
    return
  }
  
  // 简单策略：选择第一张图片，或者提示用户点击图片
  // 更好的方式：监听图片点击事件
  let selectedImg: HTMLImageElement | null = null
  
  // 尝试找到光标附近的图片
  const range = quill.getSelection()
  if (range) {
    // 从光标位置向前搜索图片
    let node = quill.root.childNodes[0]
    // 简化：直接选择第一张图片并提示
  }
  
  // 如果没有选中特定图片，选择第一张并提示
  if (!selectedImg && images.length > 0) {
    selectedImg = images[0] as HTMLImageElement
    if (images.length > 1) {
      alert(`检测到 ${images.length} 张图片，将调整第一张图片的尺寸。\n提示：后续会支持点击选择图片。`)
    }
  }
  
  if (!selectedImg) {
    alert('请先在编辑器中插入图片')
    return
  }
  
  currentImageElement.value = selectedImg
  selectedImageSrc.value = selectedImg.src
  imageWidth.value = selectedImg.width || 200
  imageHeight.value = selectedImg.height || 200
  originalAspectRatio.value = imageWidth.value / imageHeight.value
  imageResizeVisible.value = true
}

// 更新图片尺寸（预览）
function updateImageSize() {
  if (keepAspectRatio.value) {
    // 保持宽高比
    imageHeight.value = Math.round(imageWidth.value / originalAspectRatio.value)
  }
}

// 切换宽高比锁定
function toggleAspectRatio() {
  if (keepAspectRatio.value) {
    originalAspectRatio.value = imageWidth.value / imageHeight.value
  }
}

// 应用图片尺寸调整
function applyImageResize() {
  if (!currentImageElement.value) return
  
  currentImageElement.value.width = imageWidth.value
  currentImageElement.value.height = imageHeight.value
  
  // 触发更新
  syncContent()
  
  imageResizeVisible.value = false
  alert('图片尺寸已调整！')
}

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
    // 先修复旧图片 URL（将内网 IP 替换为域名）
    let fixedContent = props.modelValue.replace(
      /http:\/\/[\d.]+:8080\/uploads\//g,
      'https://wy2026.top/uploads/'
    )
    quill.root.innerHTML = fixedContent
  }

  // 监听内容变化
  quill.on('text-change', () => {
    // 使用 setTimeout 避免频繁触发
    setTimeout(() => {
      const content = quill.root.innerHTML || ''
      if (content !== props.modelValue) {
        emit('update:modelValue', content)
      }
    }, 100)
  })
})

// 监听外部 modelValue 变化
watch(
  () => props.modelValue,
  (newVal) => {
    if (quill && newVal !== quill.root.innerHTML) {
      // 修复旧图片 URL
      let fixedContent = (newVal || '').replace(
        /http:\/\/[\d.]+:8080\/uploads\//g,
        'https://wy2026.top/uploads/'
      )
      quill.root.innerHTML = fixedContent
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
  if (!url) return
  
  const range = quill.getSelection()
  if (!range) {
    alert('请先点击编辑器，定位光标位置')
    return
  }
  
  // 如果有选中文本，直接转换为链接
  if (range.length > 0) {
    quill.formatText(range.index, range.length, 'link', url)
  } else {
    // 如果没有选中文本，插入链接文本
    const linkText = url.replace(/^https?:\/\//, '') // 去掉协议前缀作为显示文本
    quill.insertText(range.index, linkText)
    quill.formatText(range.index, linkText.length, 'link', url)
    // 移动光标到链接后面
    quill.setSelection(range.index + linkText.length)
  }
  
  // 触发更新
  syncContent()
  alert('链接已插入！')
}

// 插入图片
function handleImageInsert() {
  if (!quill) {
    alert('编辑器未初始化')
    return
  }

  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) {
      console.log('未选择文件')
      return
    }

    console.log('开始上传图片:', file.name, '大小:', (file.size / 1024 / 1024).toFixed(2) + 'MB')

    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      alert('请选择图片文件')
      return
    }

    // 验证文件大小（限制 10MB）
    if (file.size > 10 * 1024 * 1024) {
      alert('图片太大，请选择小于 10MB 的图片')
      return
    }

    // 图片压缩（如果大于 1MB）
    let uploadFile: File | Blob = file
    if (file.size > 1 * 1024 * 1024) {
      try {
        console.log('开始压缩图片...')
        const compressedBlob = await compressImage(file)
        // 将 Blob 转换为 File 对象
        uploadFile = new File([compressedBlob], file.name, { type: 'image/jpeg' })
        console.log('压缩完成:', (uploadFile.size / 1024 / 1024).toFixed(2) + 'MB')
      } catch (e) {
        console.error('图片压缩失败，使用原图:', e)
        uploadFile = file
      }
    }

    // 创建 FormData
    const formData = new FormData()
    formData.append('file', uploadFile)

    try {
      // 调用上传接口
      console.log('正在上传图片...')
      
      // 使用 AbortController 设置超时
      const controller = new AbortController()
      const timeoutId = setTimeout(() => controller.abort(), 30000) // 30秒超时
      
      const response = await fetch('/api/upload/image', {
        method: 'POST',
        body: formData,
        signal: controller.signal
      })
      
      clearTimeout(timeoutId)

      if (response.status === 413) {
        alert('图片太大，请选择小于 10MB 的图片')
        return
      }

      const result = await response.json()
      console.log('上传结果:', result)

      if (result.success) {
        // 上传成功，插入图片
        const currentQuill = quill
        if (!currentQuill) {
          alert('编辑器丢失')
          return
        }
        
        // 获取当前光标位置
        const range = currentQuill.getSelection() || { index: currentQuill.getLength() }
        console.log('插入图片到位置:', range.index, 'URL:', result.url)
        
        // 插入图片
        currentQuill.insertEmbed(range.index, 'image', result.url)
        
        // 移动光标到图片后面
        currentQuill.setSelection(range.index + 1)
        
        // 触发父组件更新
        syncContent()
        
        console.log('图片插入成功:', result.url)
        alert('图片上传并插入成功！')
      } else {
        // 显示后端返回的错误信息
        alert('上传失败: ' + (result.message || '未知错误'))
      }
    } catch (error: any) {
      console.error('图片上传失败:', error)
      if (error.name === 'AbortError') {
        alert('上传超时，请检查网络连接后重试')
      } else {
        alert('上传失败，请检查网络连接后重试')
      }
    }
  }
  
  // 触发文件选择
  input.click()
}

// 图片压缩函数
function compressImage(file: File, maxWidth = 1920, quality = 0.8): Promise<Blob> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (e) => {
      const img = new Image()
      img.src = e.target?.result as string
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width
        let height = img.height
        
        // 如果图片宽度超过最大宽度，等比例缩放
        if (width > maxWidth) {
          height = (height * maxWidth) / width
          width = maxWidth
        }
        
        canvas.width = width
        canvas.height = height
        const ctx = canvas.getContext('2d')!
        ctx.drawImage(img, 0, 0, width, height)
        
        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob)
            } else {
              reject(new Error('Canvas toBlob failed'))
            }
          },
          'image/jpeg',
          quality
        )
      }
      img.onerror = reject
    }
    reader.onerror = reject
  })
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
    // 避免重复触发
    if (content !== props.modelValue) {
      emit('update:modelValue', content)
    }
  }
}

// ========== 图片管理功能 ==========

const imageManagerVisible = ref(false)
const uploadedImages = ref<Array<{url: string, filename: string, size: number, uploadTime?: string}>>([])
const imagesLoading = ref(false)
const selectedImageUrl = ref<string>('')

// 显示图片管理器
function showImageManager() {
  console.log('打开图片管理器')
  imageManagerVisible.value = true
  console.log('imageManagerVisible:', imageManagerVisible.value)
  loadUploadedImages()
}

// 加载已上传的图片
async function loadUploadedImages() {
  imagesLoading.value = true
  try {
    // 调用后端 API 获取图片列表
    const response = await getImageList(1, 100) // 先获取前 100 张
    
    if (response.success && response.data) {
      // 转换数据格式
      uploadedImages.value = response.data.items.map((img: any) => ({
        url: img.url,
        filename: img.filename,
        size: img.size,
        uploadTime: img.uploadTime
      }))
      
      console.log('图片列表加载成功:', uploadedImages.value.length)
    } else {
      uploadedImages.value = []
      console.error('获取图片列表失败:', response)
    }
  } catch (error) {
    console.error('加载图片列表失败:', error)
    uploadedImages.value = []
    // 显示错误提示
    alert('加载图片列表失败，请稍后重试')
  } finally {
    imagesLoading.value = false
  }
}

// 选择图片
function selectImage(img: {url: string, filename: string, size: number}) {
  selectedImageUrl.value = img.url
}

// 插入选中的图片
function insertSelectedImage() {
  if (!selectedImageUrl.value || !quill) return
  
  const range = quill.getSelection(true)
  if (range) {
    quill.insertEmbed(range.index, 'image', selectedImageUrl.value)
    quill.setSelection(range.index + 1)
    syncContent()
  }
  
  imageManagerVisible.value = false
  selectedImageUrl.value = ''
}

// 上传前验证
function beforeImageUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    alert('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    alert('图片大小不能超过 10MB!')
    return false
  }
  return true
}

// 上传成功
function handleUploadSuccess(response: any) {
  if (response.success) {
    // 插入到编辑器
    const range = quill!.getSelection(true)
    if (range) {
      quill!.insertEmbed(range.index, 'image', response.url)
      quill!.setSelection(range.index + 1)
      syncContent()
    }
    
    // 重新加载图片列表
    loadUploadedImages()
    
    console.log('上传成功:', response.url)
  } else {
    alert('上传失败: ' + response.message)
  }
}

// 上传失败
function handleUploadError() {
  alert('上传失败，请重试')
}

// 插入图片到编辑器
function insertImageToEditor(url: string) {
  if (!quill) return
  
  const range = quill.getSelection(true)
  if (range) {
    quill.insertEmbed(range.index, 'image', url)
    quill.setSelection(range.index + 1)
    syncContent()
  }
  
  imageManagerVisible.value = false
}

// 删除图片
async function deleteImage(img: {url: string, filename: string, size: number}) {
  if (!confirm('确定要删除这张图片吗？')) return
  
  try {
    // 调用删除 API
    const result = await deleteImageApi(img.url)
    
    if (result.success) {
      // 从编辑器中移除图片
      if (quill) {
        const content = quill.root.innerHTML
        const newContent = content.replace(
          new RegExp(`<img[^>]+src="${img.url}"[^>]*>`, 'g'),
          ''
        )
        quill.root.innerHTML = newContent
        syncContent()
      }
      
      // 重新加载图片列表
      await loadUploadedImages()
      
      alert('删除成功')
      console.log('删除成功:', img.url)
    } else {
      alert('删除失败: ' + result.message)
    }
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败，请重试')
  }
}

// 格式化文件大小
function formatFileSize(bytes: number) {
  if (bytes === 0) return '未知'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
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

/* 图片管理对话框样式 */
.image-manager-dialog {
  padding: 10px 0;
}

.upload-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.upload-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.images-container {
  min-height: 200px;
}

.empty-state {
  padding: 40px 0;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 15px;
  margin-top: 20px;
}

.image-card {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  background: white;
}

.image-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.image-card.selected {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.image-preview {
  width: 100%;
  height: 140px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  position: relative;
}

.image-preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.3s;
}

.image-card:hover .image-preview img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(64, 158, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
}

.image-card.selected .image-overlay {
  opacity: 1;
}

.image-info {
  padding: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.image-info .filename {
  font-size: 12px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 5px;
}

.image-meta .size {
  font-size: 11px;
  color: #909399;
}

.image-actions {
  padding: 8px;
  display: flex;
  justify-content: space-around;
  gap: 5px;
}

.dialog-footer {
  margin-top: 20px;
}

.selected-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

/* 图片尺寸调整对话框样式 */
.image-resize-dialog {
  padding: 20px;
}

.resize-preview {
  text-align: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.preview-img {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
}

.resize-controls {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.control-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.control-item label {
  width: 60px;
  text-align: right;
  font-size: 14px;
  color: #606266;
}

.unit {
  margin-left: 5px;
  color: #909399;
  font-size: 12px;
}

.dialog-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

</style>
