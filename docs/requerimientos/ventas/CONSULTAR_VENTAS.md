# Consultar ventas

**Estado:** En desarrollo  
**Última revisión:** 28 de agosto de 2026

## Objetivo

Consultar ventas generales, por ID, del día y por cliente.

## Implementación verificada

- Existen endpoints de listado, detalle, ventas del día por tienda del usuario e
  historial por cliente.

## Pendientes conocidos

- Paginar los listados potencialmente grandes.
- Restringir consultas generales y por cliente conforme a tienda y rol para evitar
  acceso horizontal a información de otras tiendas.
- Añadir filtros, orden estable y pruebas de autorización.
