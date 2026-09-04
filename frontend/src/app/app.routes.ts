import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'pos',
    canActivate: [authGuard],
    loadComponent: () => import('./features/pos/pos-layout/pos-layout').then(m => m.PosLayoutComponent)
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () => import('./features/admin/pages/perfil-placeholder.component').then(m => m.PerfilPlaceholderComponent)
  },
  {
    path: 'admin',
    canActivate: [roleGuard(['ADMINISTRADOR', 'GERENTE', 'INVENTARISTA'])],
    loadComponent: () => import('./features/admin/admin-layout/admin-layout').then(m => m.AdminLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () => import('./features/admin/admin-dashboard/admin-dashboard').then(m => m.AdminDashboardComponent)
      },
      {
        path: 'productos',
        canActivate: [roleGuard(['ADMINISTRADOR', 'GERENTE', 'INVENTARISTA'])],
        loadComponent: () => import('./features/admin/pages/admin-placeholder.component').then(m => m.AdminPlaceholderComponent),
        data: { titulo: 'Gestión de Productos y Catálogo', descripcion: 'Catálogo de productos, código de barras y subida segura de fotos.' }
      },
      {
        path: 'categorias',
        canActivate: [roleGuard(['ADMINISTRADOR', 'GERENTE', 'INVENTARISTA'])],
        loadComponent: () => import('./features/admin/categorias/categorias-admin.component').then(m => m.CategoriasAdminComponent)
      },
      {
        path: 'proveedores',
        canActivate: [roleGuard(['ADMINISTRADOR', 'GERENTE'])],
        loadComponent: () => import('./features/admin/proveedores/proveedores-admin.component').then(m => m.ProveedoresAdminComponent)
      },
      {
        path: 'inventario',
        canActivate: [roleGuard(['ADMINISTRADOR', 'GERENTE', 'INVENTARISTA'])],
        loadComponent: () => import('./features/admin/pages/admin-placeholder.component').then(m => m.AdminPlaceholderComponent),
        data: { titulo: 'Control de Inventario', descripcion: 'Entradas, salidas y ajustes absolutos de inventario.' }
      },
      {
        path: 'usuarios',
        canActivate: [roleGuard(['ADMINISTRADOR'])],
        loadComponent: () => import('./features/admin/pages/admin-placeholder.component').then(m => m.AdminPlaceholderComponent),
        data: { titulo: 'Gestión de Usuarios y Accesos', descripcion: 'Administración de cuentas de empleados, roles y restablecimiento de claves.' }
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];

