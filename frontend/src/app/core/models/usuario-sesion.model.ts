export type RolUsuario = 'ADMINISTRADOR' | 'GERENTE' | 'INVENTARISTA' | 'VENDEDOR';

export interface UsuarioSesion {
  id: number;
  username: string;
  nombre: string;
  apellidos?: string | null;
  email: string;
  rol: RolUsuario;
  tiendaId?: number | null;
  tiendaNombre?: string | null;
}
