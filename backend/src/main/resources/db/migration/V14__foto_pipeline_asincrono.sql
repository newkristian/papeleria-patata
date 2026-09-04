-- ============================================================================
-- V14: Pipeline asíncrono y seguro de fotografías de productos
-- ============================================================================

ALTER TABLE producto_fotos
    ALTER COLUMN ruta_archivo DROP NOT NULL;

ALTER TABLE producto_fotos
    ADD COLUMN IF NOT EXISTS estado_procesamiento VARCHAR(20) NOT NULL DEFAULT 'COMPLETADO',
    ADD COLUMN IF NOT EXISTS mensaje_error TEXT,
    ADD COLUMN IF NOT EXISTS ruta_miniatura VARCHAR(1000);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_foto_estado_procesamiento'
    ) THEN
        ALTER TABLE producto_fotos
            ADD CONSTRAINT chk_foto_estado_procesamiento
            CHECK (estado_procesamiento IN ('PENDIENTE', 'PROCESANDO', 'COMPLETADO', 'ERROR'));
    END IF;
END $$;
