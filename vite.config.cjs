const { defineConfig, loadEnv } = require('vite')
const vue = require('@vitejs/plugin-vue')
const path = require('path')

module.exports = defineConfig(({ mode }) => ({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',
    port: 3000,
    proxy: {
      '/api': {
        target: process.env.FITNESS_API_TARGET || loadEnv(mode, __dirname, 'FITNESS_').FITNESS_API_TARGET || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}))
