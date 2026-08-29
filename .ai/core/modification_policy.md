# Política de Modificaciones

## Objetivo

Mantener el proyecto estable durante toda su evolución.

## Cambios Permitidos

El agente puede:

- implementar funcionalidades solicitadas
- corregir errores
- mejorar legibilidad
- eliminar código muerto relacionado
- mejorar documentación

## Cambios Restringidos

El agente no deberá:

- reorganizar paquetes completos
- cambiar arquitectura global
- sustituir librerías
- modificar configuraciones críticas
- actualizar versiones importantes

sin autorización explícita.

## Refactorización

Las refactorizaciones deberán ser:

- pequeñas
- justificadas
- relacionadas con el trabajo solicitado

Nunca deberán convertirse en un objetivo independiente.

## Dependencias

No incorporar nuevas dependencias cuando exista una solución razonable utilizando las tecnologías ya presentes en el proyecto.

## Cambios Masivos

Si una solicitud requiere modificar una gran cantidad de archivos, el agente deberá proponer un plan antes de comenzar la implementación.