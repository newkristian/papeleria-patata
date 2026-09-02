-- Corrige un defecto preexistente de V2__datos_prueba.sql: sus INSERT fijan el `id`
-- explícitamente en varias tablas cuya columna es BIGSERIAL (tiendas, usuarios,
-- categorias, proveedores, clientes, productos, ventas). Postgres no avanza la
-- secuencia asociada cuando se inserta un id explícito, así que la primera fila que el
-- backend deja autogenerar en cualquiera de esas tablas colisiona con un id ya usado
-- por el seed (`duplicate key value violates unique constraint`).
--
-- No se modifica V2 (migración ya aplicada); esta migración solo resincroniza cada
-- secuencia a MAX(id) de su tabla. Es segura de aplicar tanto en una base recién
-- sembrada como en una que ya tenga filas creadas después del seed: en ese caso
-- MAX(id) ya refleja lo real y el ajuste es un no-op o un avance correcto.
--
-- detalles_venta no está afectada: su INSERT en V2 nunca fija el id explícitamente.

SELECT setval(pg_get_serial_sequence('tiendas', 'id'), COALESCE((SELECT MAX(id) FROM tiendas), 1));
SELECT setval(pg_get_serial_sequence('usuarios', 'id'), COALESCE((SELECT MAX(id) FROM usuarios), 1));
SELECT setval(pg_get_serial_sequence('categorias', 'id'), COALESCE((SELECT MAX(id) FROM categorias), 1));
SELECT setval(pg_get_serial_sequence('proveedores', 'id'), COALESCE((SELECT MAX(id) FROM proveedores), 1));
SELECT setval(pg_get_serial_sequence('clientes', 'id'), COALESCE((SELECT MAX(id) FROM clientes), 1));
SELECT setval(pg_get_serial_sequence('productos', 'id'), COALESCE((SELECT MAX(id) FROM productos), 1));
SELECT setval(pg_get_serial_sequence('ventas', 'id'), COALESCE((SELECT MAX(id) FROM ventas), 1));
