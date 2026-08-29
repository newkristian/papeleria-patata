# Requerimientos del proyecto

Este directorio es la fuente funcional vigente del proyecto Papelería Patata.
Cada archivo describe una funcionalidad concreta y debe mantenerse sincronizado
con el comportamiento real del sistema.

## Estados permitidos

- **Borrador:** idea registrada cuyo alcance todavía no ha sido acordado.
- **En análisis:** requerimiento bajo revisión técnica o funcional.
- **Aprobado:** alcance aceptado, aún sin implementación iniciada.
- **En desarrollo:** implementación iniciada o existente, pero incompleta o con
  defectos conocidos que impiden considerarla terminada.
- **Implementado:** comportamiento verificado y sin pendientes esenciales dentro
  del alcance documentado.
- **Descartado:** requerimiento que no se implementará; el documento debe explicar
  la decisión.

## Reglas de mantenimiento

1. Un requerimiento aprobado debe documentarse antes de modificar código.
2. No se marcará como `Implementado` únicamente porque existan clases o endpoints.
3. Los pendientes y riesgos conocidos deben quedar explícitos.
4. Los documentos históricos de `docs/legacy/` no son fuentes vigentes.
5. Los cambios de estado deben registrar la fecha de revisión.

## Inventario por área

- `autenticacion/`: acceso, tokens y cambio obligatorio de contraseña.
- `usuarios/`, `tiendas/`, `categorias/`, `productos/`: administración de catálogos.
- `inventario/`: existencias y movimientos.
- `clientes/` y `proveedores/`: relaciones comerciales.
- `ventas/`, `caja/` y `reportes/`: operación del POS.
- `sitio-publico/` y `pos/`: experiencia frontend.
- `infraestructura/`: ejecución, despliegue y operación.
- `auditoria/` y `calidad/`: trazabilidad y estrategia de verificación.
