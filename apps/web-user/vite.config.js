import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发期把 /api 代理到本地后端，避免前端写死域名与跨域配置。
// 包 1 的 Monorepo 骨架落地后，这里的 proxy 目标改由 .env 提供即可。
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    },
    watch: {
      // 部分编辑器/工具用「临时目录 + 原子替换」保存文件，这类目录不需要参与热更新
      ignored: ['**/.*.tmpdir/**', '**/*.tmp']
    }
  }
})
