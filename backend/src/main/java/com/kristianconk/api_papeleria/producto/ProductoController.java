package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.inventario.AjusteInventarioDTO;
import com.kristianconk.api_papeleria.producto.foto.ProductoFotoDTO;
import com.kristianconk.api_papeleria.producto.foto.ProductoFotoService;
import com.kristianconk.api_papeleria.producto.foto.SubirFotoRequest;
import com.kristianconk.api_papeleria.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "API para gestión de productos")
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoFotoService fotoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA')")
    @Operation(summary = "Crear nuevo producto")
    public ResponseEntity<ProductoDetalleDTO> crearProducto(
            @Valid @RequestBody ProductoCrearRequestDTO request,
            @AuthenticationPrincipal Usuario usuario) {
        ProductoDetalleDTO producto = productoService.crearProducto(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA')")
    @Operation(summary = "Actualizar producto existente")
    public ResponseEntity<ProductoDetalleDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoActualizarRequestDTO request,
            @AuthenticationPrincipal Usuario usuario) {
        ProductoDetalleDTO producto = productoService.actualizarProducto(id, request, usuario);
        return ResponseEntity.ok(producto);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    @Operation(summary = "Desactivar un producto sin eliminar su historial")
    public ResponseEntity<ProductoDetalleDTO> desactivarProducto(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(productoService.desactivarProducto(id, usuario));
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    @Operation(summary = "Reactivar un producto")
    public ResponseEntity<ProductoDetalleDTO> reactivarProducto(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(productoService.reactivarProducto(id, usuario));
    }

    // GET /api/productos/buscar?soloStockBajo=true&page=0&size=20
    // GET /api/productos/buscar?termino=cuaderno&categoriaId=5&page=0&size=10&sort=nombre,asc
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA', 'VENDEDOR')")
    @Operation(summary = "Búsqueda avanzada de productos con paginación")
    public ResponseEntity<Page<ProductoListadoDTO>> buscarProductos(
            @Parameter(description = "Término de búsqueda (nombre, descripción o código)")
            @RequestParam(required = false) String termino,

            @Parameter(description = "ID de categoría")
            @RequestParam(required = false) Long categoriaId,

            @Parameter(description = "ID de proveedor")
            @RequestParam(required = false) Long proveedorId,

            @Parameter(description = "Filtrar por activos/inactivos")
            @RequestParam(required = false) Boolean activo,

            @Parameter(description = "Precio mínimo")
            @RequestParam(required = false) BigDecimal precioMin,

            @Parameter(description = "Precio máximo")
            @RequestParam(required = false) BigDecimal precioMax,

            @Parameter(description = "Solo productos con stock bajo")
            @RequestParam(required = false) Boolean soloStockBajo,

            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal Usuario usuario) {

        ProductoBusquedaDTO busqueda = new ProductoBusquedaDTO(
                termino, categoriaId, proveedorId, activo, precioMin, precioMax, soloStockBajo);

        Page<ProductoListadoDTO> productos = productoService.buscarProductos(busqueda, pageable, usuario);
        return ResponseEntity.ok(productos);
    }

    // GET /api/productos/codigo/7501234567890
    @GetMapping("/codigo/{codigoBarras}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA', 'VENDEDOR')")
    @Operation(summary = "Buscar producto por código de barras (resultado único)")
    public ResponseEntity<ProductoListadoDTO> buscarPorCodigoBarras(
            @PathVariable String codigoBarras) {
        ProductoListadoDTO producto = productoService.buscarActivoPorCodigoBarras(codigoBarras);
        return ResponseEntity.ok(producto);
    }

    @GetMapping("/categoria/{categoriaId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA', 'VENDEDOR')")
    @Operation(summary = "Listar productos por categoría (paginado)")
    public ResponseEntity<Page<ProductoListadoDTO>> buscarPorCategoria(
            @PathVariable Long categoriaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductoListadoDTO> productos = productoService.buscarPorCategoria(categoriaId, pageable);
        return ResponseEntity.ok(productos);
    }

    // GET /api/productos/proveedor/3?termino=marca&page=0&size=15
    @GetMapping("/proveedor/{proveedorId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA', 'VENDEDOR')")
    @Operation(summary = "Listar productos por proveedor (paginado)")
    public ResponseEntity<Page<ProductoListadoDTO>> buscarPorProveedor(
            @PathVariable Long proveedorId,
            @RequestParam(required = false) String termino,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductoListadoDTO> productos = productoService.buscarPorProveedor(proveedorId, termino, pageable);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA')")
    @Operation(summary = "Obtener detalle de producto por ID")
    public ResponseEntity<ProductoDetalleDTO> obtenerProducto(@PathVariable Long id) {
        ProductoDetalleDTO producto = productoService.buscarPorId(id);
        return ResponseEntity.ok(producto);
    }

    @PostMapping("/ajustar-inventario")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA')")
    @Operation(summary = "Ajustar inventario de producto")
    public ResponseEntity<ProductoDetalleDTO> ajustarInventario(
            @Valid @RequestBody AjusteInventarioDTO ajuste,
            @AuthenticationPrincipal Usuario usuario) {
        ProductoDetalleDTO producto = productoService.ajustarInventario(ajuste, usuario);
        return ResponseEntity.ok(producto);
    }

    @GetMapping("/stock-bajo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA')")
    @Operation(summary = "Listar productos con stock bajo (paginado)")
    public ResponseEntity<Page<ProductoListadoDTO>> productosStockBajo(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Usuario usuario) {
        ProductoBusquedaDTO busqueda = new ProductoBusquedaDTO(null, null, null, true, null, null, true);
        Page<ProductoListadoDTO> productos = productoService.buscarProductos(
                busqueda, pageable, usuario);
        return ResponseEntity.ok(productos);
    }

    @PostMapping(value = "/{productoId}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir foto para un producto")
    public ResponseEntity<ProductoFotoDTO> subirFoto(
            @PathVariable Long productoId,
            @ModelAttribute SubirFotoRequest request,
            @AuthenticationPrincipal Usuario usuario) {

        ProductoFotoDTO foto = fotoService.subirFoto(productoId, request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(foto);
    }

    @GetMapping("/{productoId}/fotos")
    @Operation(summary = "Listar todas las fotos de un producto")
    public ResponseEntity<List<ProductoFotoDTO>> listarFotos(@PathVariable Long productoId) {
        return ResponseEntity.ok(fotoService.listarFotos(productoId));
    }

    @GetMapping("/{productoId}/fotos/{fotoId}")
    @Operation(summary = "Descargar foto (con opción de thumbnail)")
    public ResponseEntity<Resource> descargarFoto(
            @PathVariable Long productoId,
            @PathVariable Long fotoId,
            @RequestParam(required = false) Integer size) {

        Resource resource = fotoService.descargarFoto(productoId, fotoId, size);

        String contentType = "image/jpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{productoId}/fotos/{fotoId}")
    @Operation(summary = "Eliminar foto de producto")
    public ResponseEntity<Void> eliminarFoto(
            @PathVariable Long productoId,
            @PathVariable Long fotoId,
            @AuthenticationPrincipal Usuario usuario) {

        fotoService.eliminarFoto(productoId, fotoId, usuario);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productoId}/fotos/{fotoId}/principal")
    @Operation(summary = "Establecer foto como principal")
    public ResponseEntity<ProductoFotoDTO> establecerPrincipal(
            @PathVariable Long productoId,
            @PathVariable Long fotoId,
            @AuthenticationPrincipal Usuario usuario) {

        ProductoFotoDTO foto = fotoService.establecerFotoPrincipal(productoId, fotoId, usuario);
        return ResponseEntity.ok(foto);
    }
}
