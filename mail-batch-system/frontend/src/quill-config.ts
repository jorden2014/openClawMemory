// Quill 编辑器配置 - 自定义字体列表
import Quill from 'quill'

// 扩展字体列表
const Font = Quill.import('formats/font') as any
Font.whitelist = [
  'sans-serif',
  'serif',
  'monospace',
  'arial',
  'times-new-roman',
  'courier-new',
  'microsoft-yahei',
  'simsun',
  'simhei',
  'kaiti',
]
Quill.register(Font, true)

// 扩展字号列表
const Size = Quill.import('formats/size') as any
Size.whitelist = ['small', 'normal', 'large', 'huge']
Quill.register(Size, true)

export default Quill
