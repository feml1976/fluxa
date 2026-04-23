# ── Etapa 1: Build con Node ─────────────────────────────────────────
FROM node:18-alpine AS build
WORKDIR /app

# Instalar dependencias primero (cache si package.json no cambia)
COPY package*.json ./
RUN npm ci --silent

# Copiar fuentes y construir (tsc + vite build)
COPY . .
RUN npm run build

# ── Etapa 2: Servir con nginx ────────────────────────────────────────
FROM nginx:alpine AS runtime

# Copiar el build generado por Vite
COPY --from=build /app/dist /usr/share/nginx/html

# Configuración de nginx: SPA routing + proxy a backend
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
