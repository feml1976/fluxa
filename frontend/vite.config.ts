import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // Fija el Origin que ve el backend para que coincida con el CORS permitido,
        // independientemente del puerto que use Vite en cada arranque
        headers: { origin: 'http://localhost:5173' },
      },
    },
  },
})
