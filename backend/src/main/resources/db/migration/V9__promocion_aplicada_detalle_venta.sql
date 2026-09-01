-- Referencia a la promoción efectivamente aplicada en cada línea de venta (T5). Se
-- usan dos columnas FK excluyentes porque una línea puede ganar una promoción de
-- producto/categoría (tabla promociones) o una promoción de cliente (tabla
-- promociones_cliente), nunca ambas ni ninguna otra combinación distinta a la que ya
-- indica tipo_descuento.

ALTER TABLE detalles_venta
    ADD COLUMN promocion_producto_id BIGINT REFERENCES promociones(id),
    ADD COLUMN promocion_cliente_id BIGINT REFERENCES promociones_cliente(id);

ALTER TABLE detalles_venta
    ADD CONSTRAINT chk_detalle_promocion_tipo CHECK (
        (tipo_descuento = 'CANTIDAD' AND promocion_producto_id IS NOT NULL AND promocion_cliente_id IS NULL) OR
        (tipo_descuento = 'CLIENTE' AND promocion_cliente_id IS NOT NULL AND promocion_producto_id IS NULL) OR
        (tipo_descuento IN ('NINGUNO', 'MANUAL') AND promocion_producto_id IS NULL AND promocion_cliente_id IS NULL)
    );

CREATE INDEX idx_detalles_venta_promocion_producto_id ON detalles_venta(promocion_producto_id);
CREATE INDEX idx_detalles_venta_promocion_cliente_id ON detalles_venta(promocion_cliente_id);
