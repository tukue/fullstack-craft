import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Generic Vite config: dev proxy avoids CORS, prod build is static.
// Backend base URL is injected via VITE_API_URL.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_URL ?? 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
});
