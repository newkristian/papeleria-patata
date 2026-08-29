-- Completa la migración de importes y porcentajes financieros a tipos decimales
-- exactos para clientes, promociones y proveedores.

ALTER TABLE clientes
    ALTER COLUMN total_compras TYPE NUMERIC(19, 2)
        USING ROUND(total_compras::NUMERIC, 2);

ALTER TABLE promociones_cliente
    ALTER COLUMN porcentaje_descuento TYPE NUMERIC(5, 2)
        USING ROUND(porcentaje_descuento::NUMERIC, 2),
    ALTER COLUMN monto_minimo_compra TYPE NUMERIC(19, 2)
        USING ROUND(monto_minimo_compra::NUMERIC, 2),
    ALTER COLUMN monto_descuento_fijo TYPE NUMERIC(19, 2)
        USING ROUND(monto_descuento_fijo::NUMERIC, 2);

ALTER TABLE proveedores
    ALTER COLUMN porcentaje_comision TYPE NUMERIC(5, 2)
        USING ROUND(porcentaje_comision::NUMERIC, 2);

ALTER TABLE pagos_proveedor
    ALTER COLUMN total_ventas TYPE NUMERIC(19, 2)
        USING ROUND(total_ventas::NUMERIC, 2),
    ALTER COLUMN comision_tienda TYPE NUMERIC(19, 2)
        USING ROUND(comision_tienda::NUMERIC, 2),
    ALTER COLUMN monto_pagar TYPE NUMERIC(19, 2)
        USING ROUND(monto_pagar::NUMERIC, 2);

ALTER TABLE clientes
    ADD CONSTRAINT chk_cliente_total_compras CHECK (total_compras >= 0);

ALTER TABLE promociones_cliente
    ADD CONSTRAINT chk_promocion_cliente_porcentaje
        CHECK (porcentaje_descuento IS NULL OR
               (porcentaje_descuento > 0 AND porcentaje_descuento <= 100)),
    ADD CONSTRAINT chk_promocion_cliente_monto_minimo
        CHECK (monto_minimo_compra IS NULL OR monto_minimo_compra >= 0),
    ADD CONSTRAINT chk_promocion_cliente_monto_fijo
        CHECK (monto_descuento_fijo IS NULL OR monto_descuento_fijo > 0);

ALTER TABLE proveedores
    ADD CONSTRAINT chk_proveedor_porcentaje_comision
        CHECK (porcentaje_comision >= 0 AND porcentaje_comision <= 100);

ALTER TABLE pagos_proveedor
    ADD CONSTRAINT chk_pago_proveedor_total_ventas CHECK (total_ventas >= 0),
    ADD CONSTRAINT chk_pago_proveedor_comision CHECK (comision_tienda >= 0),
    ADD CONSTRAINT chk_pago_proveedor_monto CHECK (monto_pagar >= 0);
