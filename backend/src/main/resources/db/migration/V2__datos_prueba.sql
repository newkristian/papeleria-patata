-- ============================================================================
-- V2: Datos de prueba — 2 usuarios, catálogo, productos, ventas de ejemplo
-- ============================================================================
-- Este script es idempotente: solo inserta si la tabla está vacía.
-- Compatible con PostgreSQL y H2 en modo PostgreSQL.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Tienda principal
-- ----------------------------------------------------------------------------
INSERT INTO tiendas (id, nombre, direccion, telefono, email, rfc)
SELECT 1, 'Papelería Patata Sucursal Centro',
       'Av. Independencia 123, Centro', '555-0100',
       'contacto@papeleriapatata.com', 'PAP123456ABC'
WHERE NOT EXISTS (SELECT 1 FROM tiendas WHERE id = 1);

-- ----------------------------------------------------------------------------
-- 2. Usuarios de prueba
--    admin@pos.com / admin123  → ADMINISTRADOR
--    caja@pos.com  / caja123   → VENDEDOR
-- ----------------------------------------------------------------------------
INSERT INTO usuarios (id, username, password, nombre, apellidos, email, rol, tienda_id, activo)
SELECT 1, 'admin@pos.com',
       '$2b$10$pUfQRW6lLue8c.3eTG7C3OV9P97.rJqxQ1OYlM3hMl7eiT3H/9Bai',
       'Administrador', 'del Sistema', 'admin@pos.com',
       'ADMINISTRADOR', 1, TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE id = 1);

INSERT INTO usuarios (id, username, password, nombre, apellidos, email, rol, tienda_id, activo)
SELECT 2, 'caja@pos.com',
       '$2b$10$fzt7F/Z.7NLM4dXKZbH4s.cjerTggJ0KD5P4TUMvFF2uJz7SNcsQK',
       'María', 'López', 'caja@pos.com',
       'VENDEDOR', 1, TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE id = 2);

-- ----------------------------------------------------------------------------
-- 3. Categorías
-- ----------------------------------------------------------------------------
INSERT INTO categorias (id, nombre, descripcion)
SELECT 1, 'Papelería Básica', 'Cuadernos, hojas, lápices, plumas, gomas'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE id = 1);

INSERT INTO categorias (id, nombre, descripcion)
SELECT 2, 'Oficina', 'Engrapadoras, clips, folders, notas adhesivas'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE id = 2);

INSERT INTO categorias (id, nombre, descripcion)
SELECT 3, 'Arte y Diseño', 'Colores, plumones, acuarelas, pinceles'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE id = 3);

INSERT INTO categorias (id, nombre, descripcion)
SELECT 4, 'Tecnología', 'USB, cables, audífonos, baterías, calculadoras'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE id = 4);

-- ----------------------------------------------------------------------------
-- 4. Proveedores
-- ----------------------------------------------------------------------------
INSERT INTO proveedores (id, nombre, rfc, telefono, email, contacto, porcentaje_comision)
SELECT 1, 'Distribuidora Escolar MX', 'DES123456ABC', '555-0200',
       'ventas@distribuidoraescolar.mx', 'Juan Pérez', 30.0
WHERE NOT EXISTS (SELECT 1 FROM proveedores WHERE id = 1);

INSERT INTO proveedores (id, nombre, rfc, telefono, email, contacto, porcentaje_comision)
SELECT 2, 'Papelería del Norte SA', 'PNS789012DEF', '555-0300',
       'info@papelerianorte.com', 'Ana García', 25.0
WHERE NOT EXISTS (SELECT 1 FROM proveedores WHERE id = 2);

-- ----------------------------------------------------------------------------
-- 5. Clientes
--    ID 1 reservado para cliente anónimo (PÚBLICO GENERAL)
-- ----------------------------------------------------------------------------
INSERT INTO clientes (id, telefono, nombre, email, fecha_registro, total_compras, nivel)
SELECT 1, 'ANÓNIMO', 'PÚBLICO GENERAL', NULL, CURRENT_DATE, 0, 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE id = 1);

INSERT INTO clientes (id, telefono, nombre, email, fecha_registro, total_compras, nivel)
SELECT 2, '555-1111', 'Carlos Hernández', 'carlos@email.com', CURRENT_DATE, 0, 'Regular'
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE id = 2);

INSERT INTO clientes (id, telefono, nombre, email, fecha_registro, total_compras, nivel)
SELECT 3, '555-2222', 'Laura Martínez', 'laura@email.com', CURRENT_DATE - 30, 3500.0, 'Frecuente'
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE id = 3);

-- ----------------------------------------------------------------------------
-- 6. Productos (precios calculados según regla de negocio:
--    costo < 50 → 50% margen; costo < 200 → 40%; resto → 30%)
-- ----------------------------------------------------------------------------

-- Categoría 1: Papelería Básica
INSERT INTO productos (id, codigo_barras, nombre, descripcion, categoria_id, proveedor_id,
                        costo_compra, porcentaje_ganancia, precio_venta,
                        stock_minimo, stock_actual, unidad_medida, activo)
SELECT 1, '7501000110011', 'Cuaderno Profesional 100 hojas',
       'Cuaderno tamaño carta, pasta dura, rayado', 1, 1,
       25.0, 50.0, 37.50, 10, 50, 'pieza', TRUE
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE id = 1);

INSERT INTO productos (id, codigo_barras, nombre, descripcion, categoria_id, proveedor_id,
                        costo_compra, porcentaje_ganancia, precio_venta,
                        stock_minimo, stock_actual, unidad_medida, activo)
SELECT 2, '7501000110028', 'Lápiz HB #2 (caja 12 pzas)',
       'Lápices de madera, grafito HB, borrador incluido', 1, 1,
       30.0, 50.0, 45.00, 5, 30, 'caja', TRUE
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE id = 2);

INSERT INTO productos (id, codigo_barras, nombre, descripcion, categoria_id, proveedor_id,
                        costo_compra, porcentaje_ganancia, precio_venta,
                        stock_minimo, stock_actual, unidad_medida, activo)
SELECT 3, '7501000110035', 'Pluma Bic Cristal Negra (caja 12 pzas)',
       'Pluma punto mediano, tinta negra', 1, 2,
       35.0, 50.0, 52.50, 5, 40, 'caja', TRUE
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE id = 3);

-- Categoría 2: Oficina
INSERT INTO productos (id, codigo_barras, nombre, descripcion, categoria_id, proveedor_id,
                        costo_compra, porcentaje_ganancia, precio_venta,
                        stock_minimo, stock_actual, unidad_medida, activo)
SELECT 4, '7502000220012', 'Engrapadora Estándar',
       'Engrapadora metálica para 20 hojas, usa grapas estándar', 2, 1,
       55.0, 40.0, 77.00, 3, 12, 'pieza', TRUE
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE id = 4);

INSERT INTO productos (id, codigo_barras, nombre, descripcion, categoria_id, proveedor_id,
                        costo_compra, porcentaje_ganancia, precio_venta,
                        stock_minimo, stock_actual, unidad_medida, activo)
SELECT 5, '7502000220029', 'Paquete de 500 Hojas Blancas',
       'Hojas tamaño carta, 75 g/m², blancas', 2, 1,
       45.0, 50.0, 67.50, 10, 100, 'paquete', TRUE
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE id = 5);

-- Categoría 3: Arte y Diseño
INSERT INTO productos (id, codigo_barras, nombre, descripcion, categoria_id, proveedor_id,
                        costo_compra, porcentaje_ganancia, precio_venta,
                        stock_minimo, stock_actual, unidad_medida, activo)
SELECT 6, '7503000330013', 'Estuche de Colores de Madera (24 pzas)',
       'Colores de madera, punta suave, variedad de tonos', 3, 2,
       80.0, 40.0, 112.00, 5, 20, 'pieza', TRUE
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE id = 6);

INSERT INTO productos (id, codigo_barras, nombre, descripcion, categoria_id, proveedor_id,
                        costo_compra, porcentaje_ganancia, precio_venta,
                        stock_minimo, stock_actual, unidad_medida, activo)
SELECT 7, '7503000330020', 'Set de Plumones Punta Pincel (12 colores)',
       'Plumones punta pincel, base agua, no tóxicos', 3, 2,
       120.0, 40.0, 168.00, 3, 15, 'set', TRUE
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE id = 7);

-- Categoría 4: Tecnología
INSERT INTO productos (id, codigo_barras, nombre, descripcion, categoria_id, proveedor_id,
                        costo_compra, porcentaje_ganancia, precio_venta,
                        stock_minimo, stock_actual, unidad_medida, activo)
SELECT 8, '7504000440014', 'USB 32GB Kingston',
       'Memoria USB 3.0, 32GB, color negro', 4, 2,
       95.0, 40.0, 133.00, 3, 25, 'pieza', TRUE
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE id = 8);

INSERT INTO productos (id, codigo_barras, nombre, descripcion, categoria_id, proveedor_id,
                        costo_compra, porcentaje_ganancia, precio_venta,
                        stock_minimo, stock_actual, unidad_medida, activo)
SELECT 9, '7504000440021', 'Calculadora Científica Básica',
       'Calculadora 240 funciones, 2 líneas, batería + solar', 4, 1,
       220.0, 30.0, 286.00, 2, 8, 'pieza', TRUE
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE id = 9);

INSERT INTO productos (id, codigo_barras, nombre, descripcion, categoria_id, proveedor_id,
                        costo_compra, porcentaje_ganancia, precio_venta,
                        stock_minimo, stock_actual, unidad_medida, activo)
SELECT 10, '7504000440038', 'Cable HDMI 2m',
       'Cable HDMI 2.0, 2 metros, compatible 4K', 4, 2,
       65.0, 40.0, 91.00, 5, 35, 'pieza', TRUE
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE id = 10);

-- ----------------------------------------------------------------------------
-- 7. Ventas de prueba (para validar flujo completo)
-- ----------------------------------------------------------------------------

-- Venta 1: Cliente anónimo, 3 productos, pago en efectivo
INSERT INTO ventas (id, folio, usuario_id, tienda_id, cliente_id, venta_anonima,
                     fecha_venta, subtotal, descuento, impuesto, total, metodo_pago, estado)
SELECT 1, 'POS-260522-000001', 2, 1, 1, TRUE,
       '2026-05-22 10:30:00', 157.00, 0, 0, 157.00, 'EFECTIVO', 'COMPLETADA'
WHERE NOT EXISTS (SELECT 1 FROM ventas WHERE id = 1);

INSERT INTO detalles_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT 1, 1, 2, 37.50, 75.00
WHERE NOT EXISTS (SELECT 1 FROM detalles_venta WHERE venta_id = 1 AND producto_id = 1);

INSERT INTO detalles_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT 1, 3, 1, 52.50, 52.50
WHERE NOT EXISTS (SELECT 1 FROM detalles_venta WHERE venta_id = 1 AND producto_id = 3);

INSERT INTO detalles_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT 1, 5, 1, 67.50, 67.50
WHERE NOT EXISTS (SELECT 1 FROM detalles_venta WHERE venta_id = 1 AND producto_id = 5);

-- Venta 2: Cliente registrado (Carlos), 2 productos, pago con tarjeta
INSERT INTO ventas (id, folio, usuario_id, tienda_id, cliente_id, venta_anonima,
                     fecha_venta, subtotal, descuento, impuesto, total, metodo_pago, estado)
SELECT 2, 'POS-260522-000002', 2, 1, 2, FALSE,
       '2026-05-22 11:15:00', 245.00, 0, 0, 245.00, 'TARJETA_DEBITO', 'COMPLETADA'
WHERE NOT EXISTS (SELECT 1 FROM ventas WHERE id = 2);

INSERT INTO detalles_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT 2, 6, 1, 112.00, 112.00
WHERE NOT EXISTS (SELECT 1 FROM detalles_venta WHERE venta_id = 2 AND producto_id = 6);

INSERT INTO detalles_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT 2, 8, 1, 133.00, 133.00
WHERE NOT EXISTS (SELECT 1 FROM detalles_venta WHERE venta_id = 2 AND producto_id = 8);

-- Venta 3: Cliente frecuente (Laura), varios productos, pago con transferencia
INSERT INTO ventas (id, folio, usuario_id, tienda_id, cliente_id, venta_anonima,
                     fecha_venta, subtotal, descuento, impuesto, total, metodo_pago, estado)
SELECT 3, 'POS-260522-000003', 2, 1, 3, FALSE,
       '2026-05-22 15:45:00', 506.50, 25.0, 0, 481.50, 'TRANSFERENCIA', 'COMPLETADA'
WHERE NOT EXISTS (SELECT 1 FROM ventas WHERE id = 3);

INSERT INTO detalles_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT 3, 4, 1, 77.00, 77.00
WHERE NOT EXISTS (SELECT 1 FROM detalles_venta WHERE venta_id = 3 AND producto_id = 4);

INSERT INTO detalles_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT 3, 7, 1, 168.00, 168.00
WHERE NOT EXISTS (SELECT 1 FROM detalles_venta WHERE venta_id = 3 AND producto_id = 7);

INSERT INTO detalles_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT 3, 9, 1, 286.00, 286.00
WHERE NOT EXISTS (SELECT 1 FROM detalles_venta WHERE venta_id = 3 AND producto_id = 9);

-- Ajustar secuencia de folios después de inserts manuales
-- (ALTER SEQUENCE es compatible con PostgreSQL y H2 en modo PostgreSQL)
ALTER SEQUENCE seq_folio_venta RESTART WITH 4;
