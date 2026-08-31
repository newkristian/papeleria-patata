-- Agrega prioridad explícita a las promociones de cliente, requerida por el motor de
-- promociones automáticas (T4) para desempatar candidatas de distintos orígenes con el
-- mismo beneficio monetario.

ALTER TABLE promociones_cliente
    ADD COLUMN prioridad INTEGER NOT NULL DEFAULT 0;

ALTER TABLE promociones_cliente
    ADD CONSTRAINT chk_promocion_cliente_prioridad CHECK (prioridad >= 0);
