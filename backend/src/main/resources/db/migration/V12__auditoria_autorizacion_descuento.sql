-- T8: completa la fotografía de auditoría de una autorización de descuento manual con
-- tres datos que hoy se calculan pero nunca se conservan: el rol que tenía el
-- autorizador en el momento de emitirla (si su rol cambia después, la auditoría no
-- debe cambiar con él), el costo de compra considerado para el piso de GERENTE, y el
-- monto de la mejor promoción automática disponible en ese momento (para poder
-- justificar después por qué se prefirió el descuento manual).

ALTER TABLE autorizaciones_descuento
    ADD COLUMN rol_autorizador VARCHAR(30),
    ADD COLUMN costo_considerado NUMERIC(19, 2),
    ADD COLUMN monto_promocion_automatica_disponible NUMERIC(19, 2);

-- Backfill best-effort para filas existentes: no hay snapshot histórico previo a esta
-- migración, así que se usa el rol/costo actuales como mejor aproximación disponible.
UPDATE autorizaciones_descuento a
SET rol_autorizador = u.rol
FROM usuarios u
WHERE u.id = a.autorizador_usuario_id
  AND a.rol_autorizador IS NULL;

UPDATE autorizaciones_descuento a
SET costo_considerado = p.costo_compra
FROM productos p
WHERE p.id = a.producto_id
  AND a.costo_considerado IS NULL;

UPDATE autorizaciones_descuento
SET monto_promocion_automatica_disponible = 0
WHERE monto_promocion_automatica_disponible IS NULL;

ALTER TABLE autorizaciones_descuento
    ALTER COLUMN rol_autorizador SET NOT NULL,
    ALTER COLUMN costo_considerado SET NOT NULL,
    ALTER COLUMN monto_promocion_automatica_disponible SET NOT NULL;

ALTER TABLE autorizaciones_descuento
    ADD CONSTRAINT chk_autorizacion_descuento_rol CHECK (rol_autorizador IN ('GERENTE', 'ADMINISTRADOR')),
    ADD CONSTRAINT chk_autorizacion_descuento_costo CHECK (costo_considerado >= 0),
    ADD CONSTRAINT chk_autorizacion_descuento_monto_promo CHECK (monto_promocion_automatica_disponible >= 0);
