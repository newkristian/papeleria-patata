// Entorno de producción: usado por el build (`ng build --configuration production`)
// que sirve el contenedor de nginx del frontend. apiUrl vacío => rutas
// relativas ("/api/v1/..."), que nginx reenvía al backend (ver
// frontend/nginx.conf, location /api/).
export const environment = {
  production: true,
  apiUrl: '',
};
