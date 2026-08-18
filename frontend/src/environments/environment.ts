// Entorno de desarrollo: usado por `ng serve` / `npm start`.
// El backend corre aparte (contenedor Docker o local) en el puerto 8080 y
// tiene CORS habilitado para http://localhost:4200 (ver SecurityConfig.java
// del backend), así que aquí se llama directo, sin proxy ni nginx.
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
};
