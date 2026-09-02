// Refleja ClienteResponseDTO del backend (backend/src/main/java/.../cliente/).

export interface ClienteResumen {
  id: number;
  nombre: string;
  telefono: string;
  totalCompras: number;
  nivel: string;
}
