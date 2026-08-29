-- Migra el núcleo financiero de productos, inventario y ventas a tipos decimales
-- exactos. Los valores históricos se redondean a dos decimales con la semántica
-- de PostgreSQL para NUMERIC.

ALTER TABLE productos
    ALTER COLUMN costo_compra TYPE NUMERIC(19, 2)
        USING ROUND(costo_compra::NUMERIC, 2),
    ALTER COLUMN porcentaje_ganancia TYPE NUMERIC(5, 2)
        USING ROUND(porcentaje_ganancia::NUMERIC, 2),
    ALTER COLUMN precio_venta TYPE NUMERIC(19, 2)
        USING ROUND(precio_venta::NUMERIC, 2);

ALTER TABLE inventario_movimientos
    ALTER COLUMN costo_unitario TYPE NUMERIC(19, 2)
        USING ROUND(costo_unitario::NUMERIC, 2);

ALTER TABLE ventas
    ALTER COLUMN subtotal TYPE NUMERIC(19, 2)
        USING ROUND(subtotal::NUMERIC, 2),
    ALTER COLUMN descuento TYPE NUMERIC(19, 2)
        USING ROUND(COALESCE(descuento, 0)::NUMERIC, 2),
    ALTER COLUMN impuesto TYPE NUMERIC(19, 2)
        USING ROUND(COALESCE(impuesto, 0)::NUMERIC, 2),
    ALTER COLUMN total TYPE NUMERIC(19, 2)
        USING ROUND(total::NUMERIC, 2);

ALTER TABLE detalles_venta
    RENAME COLUMN precio_unitario TO precio_lista_unitario;

ALTER TABLE detalles_venta
    ALTER COLUMN precio_lista_unitario TYPE NUMERIC(19, 2)
        USING ROUND(precio_lista_unitario::NUMERIC, 2),
    ALTER COLUMN subtotal TYPE NUMERIC(19, 2)
        USING ROUND(subtotal::NUMERIC, 2),
    ADD COLUMN tipo_descuento VARCHAR(30) NOT NULL DEFAULT 'NINGUNO',
    ADD COLUMN porcentaje_descuento NUMERIC(5, 2) NOT NULL DEFAULT 0,
    ADD COLUMN monto_descuento NUMERIC(19, 2) NOT NULL DEFAULT 0,
    ADD COLUMN precio_unitario_final NUMERIC(19, 2),
    ADD COLUMN autorizado_por_usuario_id BIGINT REFERENCES usuarios(id),
    ADD COLUMN motivo_descuento VARCHAR(500);

UPDATE detalles_venta
SET precio_unitario_final = precio_lista_unitario
WHERE precio_unitario_final IS NULL;

ALTER TABLE detalles_venta
    ALTER COLUMN precio_unitario_final SET NOT NULL,
    ADD CONSTRAINT chk_detalle_precio_lista_positivo
        CHECK (precio_lista_unitario > 0),
    ADD CONSTRAINT chk_detalle_porcentaje_descuento
        CHECK (porcentaje_descuento >= 0 AND porcentaje_descuento <= 100),
    ADD CONSTRAINT chk_detalle_monto_descuento
        CHECK (monto_descuento >= 0),
    ADD CONSTRAINT chk_detalle_precio_final
        CHECK (precio_unitario_final >= 0),
    ADD CONSTRAINT chk_detalle_subtotal
        CHECK (subtotal >= 0);

ALTER TABLE productos
    ADD CONSTRAINT chk_producto_costo_compra_positivo CHECK (costo_compra > 0),
    ADD CONSTRAINT chk_producto_porcentaje_ganancia CHECK (porcentaje_ganancia >= 0),
    ADD CONSTRAINT chk_producto_precio_venta_positivo CHECK (precio_venta > 0);

ALTER TABLE inventario_movimientos
    ADD CONSTRAINT chk_inventario_costo_unitario_positivo CHECK (costo_unitario > 0);

ALTER TABLE ventas
    ADD CONSTRAINT chk_venta_subtotal CHECK (subtotal >= 0),
    ADD CONSTRAINT chk_venta_descuento CHECK (descuento >= 0),
    ADD CONSTRAINT chk_venta_impuesto CHECK (impuesto >= 0),
    ADD CONSTRAINT chk_venta_total CHECK (total >= 0);
