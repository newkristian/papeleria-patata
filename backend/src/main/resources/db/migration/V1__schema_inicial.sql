-- ============================================================================
-- V1: Esquema inicial — Sistema POS Papelería (PostgreSQL)
-- ============================================================================

-- Secuencia para folios de venta
CREATE SEQUENCE IF NOT EXISTS seq_folio_venta START WITH 1 INCREMENT BY 1;

-- ----------------------------------------------------------------------------
-- Tablas de catálogo / configuración
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS tiendas (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    direccion   VARCHAR(255),
    telefono    VARCHAR(50),
    email       VARCHAR(255),
    rfc         VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS categorias (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL UNIQUE,
    descripcion VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS proveedores (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(255) NOT NULL,
    rfc                 VARCHAR(20),
    telefono            VARCHAR(50),
    email               VARCHAR(255),
    contacto            VARCHAR(255),
    porcentaje_comision DOUBLE PRECISION NOT NULL DEFAULT 0
);

-- ----------------------------------------------------------------------------
-- Usuarios (implementa UserDetails de Spring Security)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS usuarios (
    id                  BIGSERIAL PRIMARY KEY,
    username            VARCHAR(255) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,
    nombre              VARCHAR(255) NOT NULL,
    apellidos           VARCHAR(255),
    email               VARCHAR(255) UNIQUE,
    rol                 VARCHAR(50) NOT NULL,
    tienda_id           BIGINT REFERENCES tiendas(id),
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- Clientes
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS clientes (
    id              BIGSERIAL PRIMARY KEY,
    telefono        VARCHAR(50) UNIQUE,
    nombre          VARCHAR(255),
    email           VARCHAR(255),
    fecha_registro  DATE,
    total_compras   DOUBLE PRECISION NOT NULL DEFAULT 0,
    nivel           VARCHAR(50) DEFAULT 'Regular'
);

-- ----------------------------------------------------------------------------
-- Productos
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS productos (
    id                  BIGSERIAL PRIMARY KEY,
    codigo_barras       VARCHAR(100) NOT NULL UNIQUE,
    nombre              VARCHAR(255) NOT NULL,
    descripcion         VARCHAR(1000),
    categoria_id        BIGINT NOT NULL REFERENCES categorias(id),
    proveedor_id        BIGINT NOT NULL REFERENCES proveedores(id),
    costo_compra        DOUBLE PRECISION NOT NULL,
    porcentaje_ganancia DOUBLE PRECISION NOT NULL,
    precio_venta        DOUBLE PRECISION NOT NULL,
    stock_minimo        INTEGER DEFAULT 5,
    stock_actual        INTEGER DEFAULT 0,
    unidad_medida       VARCHAR(50),
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- Fotos de productos
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS producto_fotos (
    id              BIGSERIAL PRIMARY KEY,
    producto_id     BIGINT NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
    nombre_archivo  VARCHAR(500) NOT NULL,
    ruta_archivo    VARCHAR(1000) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    tamanio         BIGINT NOT NULL,
    es_principal    BOOLEAN DEFAULT FALSE,
    orden           INTEGER DEFAULT 0,
    fecha_subida    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ancho           INTEGER,
    alto            INTEGER,
    descripcion     VARCHAR(500)
);

-- ----------------------------------------------------------------------------
-- Ventas y detalles
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ventas (
    id              BIGSERIAL PRIMARY KEY,
    folio           VARCHAR(50) NOT NULL UNIQUE,
    usuario_id      BIGINT NOT NULL REFERENCES usuarios(id),
    tienda_id       BIGINT NOT NULL REFERENCES tiendas(id),
    cliente_id      BIGINT REFERENCES clientes(id),
    venta_anonima   BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_venta     TIMESTAMP NOT NULL,
    subtotal        DOUBLE PRECISION NOT NULL DEFAULT 0,
    descuento       DOUBLE PRECISION DEFAULT 0,
    impuesto        DOUBLE PRECISION DEFAULT 0,
    total           DOUBLE PRECISION NOT NULL DEFAULT 0,
    metodo_pago     VARCHAR(50),
    estado          VARCHAR(50) DEFAULT 'COMPLETADA',
    fecha_creacion  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS detalles_venta (
    id              BIGSERIAL PRIMARY KEY,
    venta_id        BIGINT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id     BIGINT NOT NULL REFERENCES productos(id),
    cantidad        INTEGER NOT NULL,
    precio_unitario DOUBLE PRECISION NOT NULL,
    subtotal        DOUBLE PRECISION NOT NULL
);

-- ----------------------------------------------------------------------------
-- Promociones por cliente
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS promociones_cliente (
    id                      BIGSERIAL PRIMARY KEY,
    cliente_id              BIGINT NOT NULL REFERENCES clientes(id),
    descripcion             VARCHAR(500) NOT NULL,
    porcentaje_descuento    DOUBLE PRECISION,
    monto_minimo_compra     DOUBLE PRECISION,
    monto_descuento_fijo    DOUBLE PRECISION,
    fecha_inicio            DATE NOT NULL,
    fecha_fin               DATE NOT NULL,
    activa                  BOOLEAN DEFAULT TRUE
);

-- ----------------------------------------------------------------------------
-- Pagos a proveedores
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS pagos_proveedor (
    id                  BIGSERIAL PRIMARY KEY,
    proveedor_id        BIGINT NOT NULL REFERENCES proveedores(id),
    fecha_inicio        DATE NOT NULL,
    fecha_fin           DATE NOT NULL,
    total_ventas        DOUBLE PRECISION NOT NULL,
    comision_tienda     DOUBLE PRECISION NOT NULL,
    monto_pagar         DOUBLE PRECISION NOT NULL,
    fecha_pago          DATE NOT NULL,
    referencia_pago     VARCHAR(255),
    pagado              BOOLEAN DEFAULT FALSE
);

-- ----------------------------------------------------------------------------
-- Movimientos de inventario (bitácora)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS inventario_movimientos (
    id                BIGSERIAL PRIMARY KEY,
    producto_id       BIGINT NOT NULL REFERENCES productos(id),
    usuario_id        BIGINT NOT NULL REFERENCES usuarios(id),
    tipo              VARCHAR(50) NOT NULL,
    cantidad          INTEGER NOT NULL,
    motivo            VARCHAR(500),
    costo_unitario    DOUBLE PRECISION NOT NULL,
    fecha_movimiento  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- Índices para rendimiento
-- ----------------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_productos_categoria    ON productos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_productos_proveedor    ON productos(proveedor_id);
CREATE INDEX IF NOT EXISTS idx_productos_activo       ON productos(activo);
CREATE INDEX IF NOT EXISTS idx_productos_stock        ON productos(stock_actual, stock_minimo);
CREATE INDEX IF NOT EXISTS idx_ventas_fecha           ON ventas(fecha_venta);
CREATE INDEX IF NOT EXISTS idx_ventas_tienda_fecha    ON ventas(tienda_id, fecha_venta);
CREATE INDEX IF NOT EXISTS idx_ventas_cliente         ON ventas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_ventas_usuario         ON ventas(usuario_id);
CREATE INDEX IF NOT EXISTS idx_detalles_venta         ON detalles_venta(venta_id);
CREATE INDEX IF NOT EXISTS idx_detalles_producto      ON detalles_venta(producto_id);
CREATE INDEX IF NOT EXISTS idx_inventario_producto    ON inventario_movimientos(producto_id);
CREATE INDEX IF NOT EXISTS idx_inventario_fecha       ON inventario_movimientos(fecha_movimiento);
