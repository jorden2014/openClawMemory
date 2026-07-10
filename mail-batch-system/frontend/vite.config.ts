import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  base: '/mail/',  // 使用绝对路径，配合 Nginx 解决混合内容问题
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  server: {
    port: 3000,
    host: '0.0.0.0',  // 允许外部访问
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  // 优化构建配置 - 减少内存占用
  build: {
    // 关闭 source map 生成（节省内存和时间）
    sourcemap: false,
    // 减少 chunk 数量
    rollupOptions: {
      output: {
        manualChunks: {
          // 将大型库单独打包
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus'],
        },
      },
    },
    // 增加 chunk 大小警告限制
    chunkSizeWarningLimit: 1000,
  },
  // 优化依赖预构建（注释掉 quill，因为用 CDN 了）
  optimizeDeps: {
    include: ['vue', 'element-plus'],
  },
})
