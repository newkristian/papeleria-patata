# --- Etapa 1: Build ---
FROM node:22-alpine AS build
WORKDIR /app

# Copiamos manifest primero para aprovechar la caché de capas
COPY package.json package-lock.json ./
RUN npm ci

# Copiamos el resto del código y compilamos en modo producción
COPY . .
RUN npm run build -- --configuration production

# --- Etapa 2: Run ---
FROM nginx:1.27-alpine

# Config de nginx: proxy a /api y fallback de rutas para el router de Angular
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Salida del build de Angular (application builder -> dist/<project>/browser)
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
