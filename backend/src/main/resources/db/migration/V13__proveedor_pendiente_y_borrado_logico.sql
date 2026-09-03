-- Proveedor reservado para productos cuyo origen comercial aún no está definido.
-- La relación productos.proveedor_id permanece NOT NULL.

ALTER TABLE proveedores
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN es_sistema BOOLEAN NOT NULL DEFAULT FALSE;

DO $$
DECLARE
    proveedor_pendiente_id BIGINT;
BEGIN
    SELECT id
      INTO proveedor_pendiente_id
      FROM proveedores
     WHERE UPPER(TRIM(nombre)) = 'PENDIENTE'
     ORDER BY id
     LIMIT 1;

    IF proveedor_pendiente_id IS NULL THEN
        INSERT INTO proveedores (
            nombre,
            porcentaje_comision,
            activo,
            es_sistema
        ) VALUES (
            'PENDIENTE',
            0.00,
            TRUE,
            TRUE
        );
    ELSE
        UPDATE proveedores
           SET nombre = 'PENDIENTE',
               porcentaje_comision = 0.00,
               activo = TRUE,
               es_sistema = TRUE
         WHERE id = proveedor_pendiente_id;
    END IF;
END $$;

CREATE UNIQUE INDEX ux_proveedores_unico_sistema
    ON proveedores (es_sistema)
    WHERE es_sistema = TRUE;

CREATE INDEX idx_proveedores_activo_nombre
    ON proveedores (activo, nombre);

ALTER TABLE proveedores
    ADD CONSTRAINT chk_proveedor_sistema_protegido CHECK (
        es_sistema = FALSE
        OR (
            nombre = 'PENDIENTE'
            AND activo = TRUE
            AND porcentaje_comision = 0.00
        )
    );

CREATE OR REPLACE FUNCTION proteger_proveedor_sistema()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.es_sistema = TRUE THEN
        IF TG_OP = 'DELETE' OR NEW IS DISTINCT FROM OLD THEN
            RAISE EXCEPTION 'El proveedor PENDIENTE es una configuración protegida del sistema'
                USING ERRCODE = '23514';
        END IF;
    END IF;
    RETURN CASE WHEN TG_OP = 'DELETE' THEN OLD ELSE NEW END;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_proteger_proveedor_sistema
    BEFORE UPDATE OR DELETE
    ON proveedores
    FOR EACH ROW
    EXECUTE FUNCTION proteger_proveedor_sistema();

CREATE OR REPLACE FUNCTION impedir_pago_proveedor_sistema()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM proveedores
         WHERE id = NEW.proveedor_id
           AND es_sistema = TRUE
    ) THEN
        RAISE EXCEPTION 'El proveedor PENDIENTE no puede recibir pagos'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_impedir_pago_proveedor_sistema
    BEFORE INSERT OR UPDATE OF proveedor_id
    ON pagos_proveedor
    FOR EACH ROW
    EXECUTE FUNCTION impedir_pago_proveedor_sistema();
