export interface Proveedor {
  id: number;
  nombre: string;
  rfc?: string | null;
  telefono?: string | null;
  email?: string | null;
  contacto?: string | null;
  porcentajeComision: number;
  activo: boolean;
  sistema: boolean;
}

export interface ProveedorRequest {
  nombre: string;
  rfc?: string | null;
  telefono?: string | null;
  email?: string | null;
  contacto?: string | null;
  porcentajeComision?: number;
}
