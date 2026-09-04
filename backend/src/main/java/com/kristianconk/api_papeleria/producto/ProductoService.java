package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.categoria.Categoria;
import com.kristianconk.api_papeleria.categoria.CategoriaDTO;
import com.kristianconk.api_papeleria.categoria.CategoriaRepository;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.enums.TipoMovimiento;
import com.kristianconk.api_papeleria.error.AccesoDenegadoException;
import com.kristianconk.api_papeleria.error.ConflictException;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.inventario.AjusteInventarioDTO;
import com.kristianconk.api_papeleria.inventario.InventarioMovimiento;
import com.kristianconk.api_papeleria.inventario.InventarioMovimientoRepository;
import com.kristianconk.api_papeleria.producto.foto.ProductoFoto;
import com.kristianconk.api_papeleria.producto.foto.ProductoFotoDTO;
import com.kristianconk.api_papeleria.proveedor.Proveedor;
import com.kristianconk.api_papeleria.proveedor.ProveedorDTO;
import com.kristianconk.api_papeleria.proveedor.ProveedorPendienteService;
import com.kristianconk.api_papeleria.proveedor.ProveedorRepository;
import com.kristianconk.api_papeleria.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProveedorPendienteService proveedorPendienteService;
    private final InventarioMovimientoRepository inventarioMovimientoRepository;

    // Constantes para cálculo de porcentajes
    private static final BigDecimal PORCENTAJE_BAJO = new BigDecimal("50.00");
    private static final BigDecimal PORCENTAJE_MEDIO = new BigDecimal("40.00");
    private static final BigDecimal PORCENTAJE_ALTO = new BigDecimal("30.00");
    private static final BigDecimal COSTO_BAJO = new BigDecimal("50.00");
    private static final BigDecimal COSTO_MEDIO = new BigDecimal("200.00");

    @Transactional
    public ProductoDetalleDTO crearProducto(ProductoCrearRequestDTO request, Usuario usuario) {
        verificarPermisosEdicion(usuario);

        if (productoRepository.existsByCodigoBarrasIgnoreCase(request.codigoBarras())) {
            throw new ConflictException("Ya existe un producto con el código de barras: " + request.codigoBarras());
        }

        final Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        final Proveedor proveedor = resolverProveedor(request.proveedorId());

        // Crear producto
        Producto producto = new Producto();
        producto.setCodigoBarras(request.codigoBarras());
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);
        producto.setCostoCompra(request.costoCompra());
        producto.setStockMinimo(request.stockMinimo());
        producto.setUnidadMedida(request.unidadMedida());
        producto.setCantidadDesconocida(request.cantidadDesconocida());
        producto.setActivo(true);

        // Calcular porcentaje de ganancia
        if (request.porcentajeGananciaManual() != null) {
            // Si viene porcentaje manual, usarlo (solo gerente/admin pueden forzar)
            if (usuario.getRol() == RolUsuario.INVENTARISTA) {
                throw new AccesoDenegadoException("Inventarista no puede fijar porcentaje manualmente");
            }
            producto.setPorcentajeGanancia(request.porcentajeGananciaManual());
        } else {
            producto.setPorcentajeGanancia(calcularPorcentajeGanancia(request.costoCompra()));
        }

        // Calcular precio de venta (automático por @PreUpdate)
        producto.setStockActual(0); // Inicialmente sin stock

        Producto productoGuardado = productoRepository.save(producto);

        return mapToDetalleDTO(productoGuardado);
    }

    @Transactional
    public ProductoDetalleDTO actualizarProducto(
            Long id,
            ProductoActualizarRequestDTO request,
            Usuario usuario) {
        final Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        verificarPermisosEdicion(usuario);

        if (!producto.getCodigoBarras().equals(request.codigoBarras())) {
            if (productoRepository.existsByCodigoBarrasIgnoreCaseAndIdNot(request.codigoBarras(), id)) {
                throw new ConflictException(
                        "Ya existe un producto con el código de barras: " + request.codigoBarras());
            }
            producto.setCodigoBarras(request.codigoBarras());
        }

        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());

        final Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        producto.setCategoria(categoria);
        producto.setProveedor(resolverProveedor(request.proveedorId()));

        producto.setCostoCompra(request.costoCompra());
        if (request.porcentajeGananciaManual() != null) {
            if (usuario.getRol() == RolUsuario.INVENTARISTA) {
                throw new AccesoDenegadoException("Inventarista no puede fijar porcentaje manualmente");
            }
            producto.setPorcentajeGanancia(request.porcentajeGananciaManual());
        } else {
            producto.setPorcentajeGanancia(calcularPorcentajeGanancia(request.costoCompra()));
        }

        producto.setStockMinimo(request.stockMinimo());
        producto.setUnidadMedida(request.unidadMedida());

        final Producto productoActualizado = productoRepository.save(producto);
        return mapToDetalleDTO(productoActualizado);
    }

    @Transactional
    public ProductoDetalleDTO desactivarProducto(final Long id, final Usuario usuario) {
        verificarPermisosCambioEstado(usuario);
        return cambiarEstado(id, false);
    }

    @Transactional
    public ProductoDetalleDTO reactivarProducto(final Long id, final Usuario usuario) {
        verificarPermisosCambioEstado(usuario);
        return cambiarEstado(id, true);
    }

    @Transactional
    public ProductoDetalleDTO ajustarInventario(AjusteInventarioDTO ajuste, Usuario usuario) {
        // Solo inventarista, gerente o admin
        if (usuario.getRol() != RolUsuario.INVENTARISTA &&
                usuario.getRol() != RolUsuario.GERENTE &&
                usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new AccesoDenegadoException("No tiene permisos para ajustar inventario");
        }

        Producto producto = productoRepository.findByIdForUpdate(ajuste.productoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        if (!producto.isActivo()) {
            throw new ConflictException("No se puede ajustar inventario de un producto inactivo");
        }

        // Actualizar stock y validar semántica de inventario desconocido
        if (Boolean.TRUE.equals(ajuste.esFijarStockAbsoluto())) {
            if (ajuste.cantidad() < 0) {
                throw new IllegalArgumentException("El stock absoluto no puede ser negativo");
            }
            producto.setStockActual(ajuste.cantidad());
            producto.setCantidadDesconocida(false);
        } else {
            if (producto.isCantidadDesconocida()) {
                throw new IllegalArgumentException("No se pueden realizar ajustes relativos en un producto con cantidad desconocida. Realice primero un ajuste absoluto de inventario.");
            }
            final int stockResultante = producto.getStockActual() + ajuste.cantidad();
            if (stockResultante < 0) {
                throw new IllegalArgumentException("El ajuste relativo resulta en un stock negativo no permitido");
            }
            producto.setStockActual(stockResultante);
        }

        // Registrar movimiento de inventario
        InventarioMovimiento movimiento = new InventarioMovimiento();
        movimiento.setProducto(producto);
        movimiento.setUsuario(usuario);
        movimiento.setTipo(TipoMovimiento.AJUSTE);
        movimiento.setCantidad(ajuste.cantidad());
        movimiento.setMotivo(ajuste.motivo());
        movimiento.setCostoUnitario(producto.getCostoCompra());

        // Si hay nuevo costo, actualizar producto
        if (ajuste.nuevoCostoCompra() != null) {
            producto.setCostoCompra(ajuste.nuevoCostoCompra());
            producto.setPorcentajeGanancia(calcularPorcentajeGanancia(ajuste.nuevoCostoCompra()));
        }

        inventarioMovimientoRepository.save(movimiento);
        Producto productoActualizado = productoRepository.save(producto);

        return mapToDetalleDTO(productoActualizado);
    }

    @Transactional(readOnly = true)
    public Page<ProductoListadoDTO> buscarProductos(
            ProductoBusquedaDTO busqueda,
            Pageable pageable,
            Usuario usuario) {
        final Boolean activo = usuario.getRol() == RolUsuario.VENDEDOR || busqueda.activo() == null
                ? Boolean.TRUE
                : busqueda.activo();
        final Page<Producto> productos = productoRepository.buscarProductos(
                busqueda.termino(),
                busqueda.categoriaId(),
                busqueda.proveedorId(),
                activo,
                busqueda.precioMin(),
                busqueda.precioMax(),
                Boolean.TRUE.equals(busqueda.soloStockBajo()),
                pageable);
        // Forzar inicialización de fotos para evitar LazyInitializationException
        // y optimizar la consulta
        productos.getContent().forEach(p -> {
            if (p.getFotos() != null) {
                p.getFotos().size(); // Inicializar la colección lazy
            }
        });

        return productos.map(this::mapToListadoDTO);
    }

    // Método optimizado para buscar por ID con todas las fotos
    @Transactional(readOnly = true)
    public ProductoDetalleDTO buscarPorId(Long id) {
        Producto producto = productoRepository.findByIdWithFotos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return mapToDetalleDTO(producto);
    }

    // Método optimizado para buscar por código de barras con fotos
    @Transactional(readOnly = true)
    public ProductoListadoDTO buscarActivoPorCodigoBarras(String codigoBarras) {
        Producto producto = productoRepository.findByCodigoBarrasActivoWithFotos(codigoBarras.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return mapToListadoDTO(producto);
    }

    @Transactional(readOnly = true)
    public Page<ProductoListadoDTO> buscarPorCategoria(Long categoriaId, Pageable pageable) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        return productoRepository.findByCategoriaAndActivoTrue(categoria, pageable)
                .map(this::mapToListadoDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoListadoDTO> buscarPorProveedor(Long proveedorId, String termino, Pageable pageable) {
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        if (termino != null && !termino.isBlank()) {
            return productoRepository.buscarProductos(
                            termino.trim(), null, proveedorId, true, null, null, false, pageable)
                    .map(this::mapToListadoDTO);
        }

        return productoRepository.findByProveedorAndActivoTrue(proveedor, pageable)
                .map(this::mapToListadoDTO);
    }

    private BigDecimal calcularPorcentajeGanancia(final BigDecimal costoCompra) {
        if (costoCompra.compareTo(COSTO_BAJO) < 0) {
            return PORCENTAJE_BAJO;
        } else if (costoCompra.compareTo(COSTO_MEDIO) < 0) {
            return PORCENTAJE_MEDIO;
        } else {
            return PORCENTAJE_ALTO;
        }
    }

    private Proveedor resolverProveedor(final Long proveedorId) {
        if (proveedorId == null) {
            return proveedorPendienteService.obtener();
        }
        final Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
        if (proveedor.isSistema()) {
            throw new ConflictException(
                    "Para usar el proveedor PENDIENTE omita proveedorId; no dependa de su ID interno");
        }
        if (!proveedor.isActivo()) {
            throw new ConflictException("El proveedor seleccionado está inactivo");
        }
        return proveedor;
    }

    private ProductoDetalleDTO cambiarEstado(final Long id, final boolean activo) {
        final Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        producto.setActivo(activo);
        return mapToDetalleDTO(productoRepository.save(producto));
    }

    private void verificarPermisosEdicion(final Usuario usuario) {
        if (usuario.getRol() != RolUsuario.INVENTARISTA
                && usuario.getRol() != RolUsuario.GERENTE
                && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new AccesoDenegadoException("No tiene permisos para crear o modificar productos");
        }
    }

    private void verificarPermisosCambioEstado(final Usuario usuario) {
        if (usuario.getRol() != RolUsuario.GERENTE && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new AccesoDenegadoException("No tiene permisos para cambiar el estado de productos");
        }
    }

    private ProductoListadoDTO mapToListadoDTO(Producto p) {
        String urlThumbnail = null;
        boolean tieneFotos = false;

        // Buscar la foto principal o la primera foto
        if (p.getFotos() != null && !p.getFotos().isEmpty()) {
            tieneFotos = true;
            ProductoFoto fotoPrincipal = p.getFotos().stream()
                    .filter(ProductoFoto::getEsPrincipal)
                    .findFirst()
                    .orElse(p.getFotos().getFirst());

            // Generar URL del thumbnail (tamaño fijo para listados)
            urlThumbnail = String.format("/api/v1/productos/%d/fotos/%d?size=100",
                    p.getId(), fotoPrincipal.getId());
        }

        return new ProductoListadoDTO(
                p.getId(),
                p.getCodigoBarras(),
                p.getNombre(),
                p.getCategoria().getNombre(),
                p.getProveedor().getNombre(),
                p.getPrecioVenta(),
                p.getStockActual(),
                p.getStockMinimo(),
                p.isActivo(),
                p.isCantidadDesconocida(),
                urlThumbnail,
                tieneFotos
        );
    }

    private ProductoDetalleDTO mapToDetalleDTO(Producto p) {
        // Mapear fotos
        List<ProductoFotoDTO> fotosDTO = p.getFotos() != null ?
                p.getFotos().stream()
                        .map(f -> mapToFotoDTO(f, p.getId()))
                        .collect(Collectors.toList()) :
                List.of();

        // Encontrar foto principal
        ProductoFotoDTO fotoPrincipal = fotosDTO.stream()
                .filter(ProductoFotoDTO::esPrincipal)
                .findFirst()
                .orElse(null);

        return new ProductoDetalleDTO(
                p.getId(),
                p.getCodigoBarras(),
                p.getNombre(),
                p.getDescripcion(),
                new CategoriaDTO(p.getCategoria().getId(), p.getCategoria().getNombre(), p.getCategoria().getDescripcion()),
                new ProveedorDTO(
                        p.getProveedor().getId(),
                        p.getProveedor().getNombre(),
                        p.getProveedor().getRfc(),
                        p.getProveedor().getContacto()),
                p.getCostoCompra(),
                p.getPorcentajeGanancia(),
                p.getPrecioVenta(),
                p.getStockMinimo(),
                p.getStockActual(),
                p.getUnidadMedida(),
                p.isActivo(),
                p.isCantidadDesconocida(),
                p.getFechaCreacion(),
                p.getFechaActualizacion(),
                fotosDTO,
                fotoPrincipal
        );
    }

    private ProductoFotoDTO mapToFotoDTO(ProductoFoto f, Long productoId) {
        String baseUrl = String.format("/api/v1/productos/%d/fotos/%d", productoId, f.getId());
        return new ProductoFotoDTO(
                f.getId(),
                f.getNombreArchivo(),
                f.getContentType(),
                f.getTamanio(),
                f.getEsPrincipal(),
                f.getOrden(),
                f.getFechaSubida(),
                baseUrl,                    // URL original
                baseUrl + "?size=200"        // URL thumbnail (tamaño configurable)
        );
    }
}
