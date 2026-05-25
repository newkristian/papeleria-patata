package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.categoria.Categoria;
import com.kristianconk.api_papeleria.categoria.CategoriaDTO;
import com.kristianconk.api_papeleria.categoria.CategoriaRepository;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.enums.TipoMovimiento;
import com.kristianconk.api_papeleria.error.AccesoDenegadoException;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.inventario.AjusteInventarioDTO;
import com.kristianconk.api_papeleria.inventario.InventarioMovimiento;
import com.kristianconk.api_papeleria.inventario.InventarioMovimientoRepository;
import com.kristianconk.api_papeleria.producto.foto.ProductoFoto;
import com.kristianconk.api_papeleria.producto.foto.ProductoFotoDTO;
import com.kristianconk.api_papeleria.proveedor.Proveedor;
import com.kristianconk.api_papeleria.proveedor.ProveedorDTO;
import com.kristianconk.api_papeleria.proveedor.ProveedorRepository;
import com.kristianconk.api_papeleria.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final InventarioMovimientoRepository inventarioMovimientoRepository;

    // Constantes para cálculo de porcentajes
    private static final double PORCENTAJE_BAJO = 50.0;
    private static final double PORCENTAJE_MEDIO = 40.0;
    private static final double PORCENTAJE_ALTO = 30.0;
    private static final double COSTO_BAJO = 50.0;
    private static final double COSTO_MEDIO = 200.0;

    @Transactional
    public ProductoDetalleDTO crearProducto(ProductoRequestDTO request, Usuario usuario) {
        // Verificar permisos (solo inventarista, gerente o admin)
        if (usuario.getRol() != RolUsuario.INVENTARISTA &&
                usuario.getRol() != RolUsuario.GERENTE &&
                usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new AccesoDenegadoException("No tiene permisos para crear productos");
        }

        // Validar que no exista el código de barras
        if (productoRepository.findByCodigoBarras(request.codigoBarras()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con ese código de barras");
        }

        // Obtener categoría y proveedor
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Proveedor proveedor = proveedorRepository.findById(request.proveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

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
    public ProductoDetalleDTO actualizarProducto(Long id, ProductoRequestDTO request, Usuario usuario) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Verificar permisos
        verificarPermisosModificacion(usuario, producto);

        // Actualizar campos
        if (!producto.getCodigoBarras().equals(request.codigoBarras())) {
            if (productoRepository.findByCodigoBarras(request.codigoBarras()).isPresent()) {
                throw new IllegalArgumentException("Ya existe un producto con ese código de barras");
            }
            producto.setCodigoBarras(request.codigoBarras());
        }

        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());

        if (request.categoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            producto.setCategoria(categoria);
        }

        if (request.proveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(request.proveedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
            producto.setProveedor(proveedor);
        }

        // Actualizar costo y recalcular precio si es necesario
        if (request.costoCompra() != null && !request.costoCompra().equals(producto.getCostoCompra())) {
            producto.setCostoCompra(request.costoCompra());

            if (request.porcentajeGananciaManual() != null) {
                if (usuario.getRol() == RolUsuario.INVENTARISTA) {
                    throw new AccesoDenegadoException("Inventarista no puede fijar porcentaje manualmente");
                }
                producto.setPorcentajeGanancia(request.porcentajeGananciaManual());
            } else {
                producto.setPorcentajeGanancia(calcularPorcentajeGanancia(request.costoCompra()));
            }
        }

        producto.setStockMinimo(request.stockMinimo());
        producto.setUnidadMedida(request.unidadMedida());
        producto.setActivo(request.activo());

        Producto productoActualizado = productoRepository.save(producto);
        return mapToDetalleDTO(productoActualizado);
    }

    @Transactional
    public ProductoDetalleDTO ajustarInventario(AjusteInventarioDTO ajuste, Usuario usuario) {
        // Solo inventarista, gerente o admin
        if (usuario.getRol() != RolUsuario.INVENTARISTA &&
                usuario.getRol() != RolUsuario.GERENTE &&
                usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new AccesoDenegadoException("No tiene permisos para ajustar inventario");
        }

        Producto producto = productoRepository.findById(ajuste.productoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

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

        // Actualizar stock
        producto.setStockActual(producto.getStockActual() + ajuste.cantidad());

        inventarioMovimientoRepository.save(movimiento);
        Producto productoActualizado = productoRepository.save(producto);

        return mapToDetalleDTO(productoActualizado);
    }

    public Page<ProductoListadoDTO> buscarProductos(ProductoBusquedaDTO busqueda, Pageable pageable) {
        Page<Producto> productos;

        // Si solo pide stock bajo
        if (Boolean.TRUE.equals(busqueda.soloStockBajo())) {
            productos = productoRepository.findProductosStockBajo(pageable);
        }
        // Si hay rango de precios
        else if (busqueda.precioMin() != null && busqueda.precioMax() != null) {
            productos = productoRepository.findByRangoPrecio(
                    busqueda.precioMin(), busqueda.precioMax(), pageable);
        }
        // Búsqueda avanzada
        else {
            productos = productoRepository.buscarProductos(
                    busqueda.termino(),
                    busqueda.categoriaId(),
                    busqueda.proveedorId(),
                    busqueda.activo(),
                    pageable);
        }
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
    public ProductoDetalleDTO buscarPorCodigoBarras(String codigoBarras) {
        Producto producto = productoRepository.findByCodigoBarrasWithFotos(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return mapToDetalleDTO(producto);
    }

    public Page<ProductoListadoDTO> buscarPorCategoria(Long categoriaId, Pageable pageable) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        return productoRepository.findByCategoria(categoria, pageable)
                .map(this::mapToListadoDTO);
    }

    public Page<ProductoListadoDTO> buscarPorProveedor(Long proveedorId, String termino, Pageable pageable) {
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        if (termino != null && !termino.isBlank()) {
            return productoRepository.buscarProductosPorProveedor(proveedorId, termino, pageable)
                    .map(this::mapToListadoDTO);
        }

        return productoRepository.findByProveedor(proveedor, pageable)
                .map(this::mapToListadoDTO);
    }

    private double calcularPorcentajeGanancia(Double costoCompra) {
        if (costoCompra < COSTO_BAJO) {
            return PORCENTAJE_BAJO;
        } else if (costoCompra < COSTO_MEDIO) {
            return PORCENTAJE_MEDIO;
        } else {
            return PORCENTAJE_ALTO;
        }
    }

    private void verificarPermisosModificacion(Usuario usuario, Producto producto) {
        // Inventarista solo puede modificar productos de su tienda? (si aplica)
        if (usuario.getRol() == RolUsuario.INVENTARISTA) {
            // Aquí podrías verificar si el producto pertenece a la tienda del usuario
            // Por ahora solo verificamos que no sea admin/gerente
        } else if (usuario.getRol() != RolUsuario.GERENTE &&
                usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new AccesoDenegadoException("No tiene permisos para modificar productos");
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
                new ProveedorDTO(p.getProveedor().getId(), p.getProveedor().getNombre(), p.getProveedor().getRfc(), p.getProveedor().getTelefono()),
                p.getCostoCompra(),
                p.getPorcentajeGanancia(),
                p.getPrecioVenta(),
                p.getStockMinimo(),
                p.getStockActual(),
                p.getUnidadMedida(),
                p.isActivo(),
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
