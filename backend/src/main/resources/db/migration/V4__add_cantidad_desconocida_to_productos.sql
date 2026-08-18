-- ============================================================================
-- V4: Agregar columna cantidad_desconocida a productos
-- ============================================================================

ALTER TABLE productos ADD COLUMN cantidad_desconocida BOOLEAN NOT NULL DEFAULT FALSE;
