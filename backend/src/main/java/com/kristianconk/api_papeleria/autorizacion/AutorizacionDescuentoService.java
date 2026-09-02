package com.kristianconk.api_papeleria.autorizacion;

import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.error.AccesoDenegadoException;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import com.kristianconk.api_papeleria.promocion.MotorPromocionesService;
import com.kristianconk.api_papeleria.promocion.ResultadoPromocion;
import com.kristianconk.api_papeleria.usuario.Usuario;
import com.kristianconk.api_papeleria.usuario.UsuarioRepository;
import com.kristianconk.api_papeleria.utils.TokenOpacoGenerador;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Emite y consume autorizaciones opacas de un solo uso para descuentos manuales
 * excepcionales (T6). La emisión ({@link #solicitar}) la invoca el vendedor
 * autenticado, pero la identidad que realmente autoriza el descuento es la del
 * gerente/administrador reautenticado dentro del propio request. El consumo
 * ({@link #consumir}) lo invoca {@code VentaService} dentro de la transacción de
 * creación de la venta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutorizacionDescuentoService {

    private static final int ESCALA_MONETARIA = 2;
    private static final long VIGENCIA_MINUTOS = 2;

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final AutorizacionDescuentoRepository autorizacionDescuentoRepository;
    private final AuthenticationManager authenticationManager;
    private final MotorPromocionesService motorPromocionesService;
    private final TokenOpacoGenerador tokenOpacoGenerador;
    private final LimitadorIntentosAutorizacion limitadorIntentosAutorizacion;

    @Transactional
    public AutorizacionDescuentoResponseDTO solicitar(
            final SolicitudAutorizacionDescuentoDTO request, final Usuario vendedor) {
        if (vendedor.getRol() == RolUsuario.INVENTARISTA) {
            throw new AccesoDenegadoException("El rol INVENTARISTA no puede solicitar descuentos manuales");
        }
        if (vendedor.getTienda() == null) {
            throw new IllegalArgumentException("El vendedor no tiene una tienda asignada");
        }

        limitadorIntentosAutorizacion.verificarDisponible(vendedor.getId());

        final Usuario autorizador = reautenticar(request.username(), request.password(), vendedor.getId());

        if (autorizador.getRol() != RolUsuario.GERENTE && autorizador.getRol() != RolUsuario.ADMINISTRADOR) {
            limitadorIntentosAutorizacion.registrarFallo(vendedor.getId());
            log.warn("[POS/AutorizacionDescuentoService] - SOLICITAR: usuario {} con rol {} intentó autorizar un "
                    + "descuento manual sin permisos, vendedorId: {}", autorizador.getId(), autorizador.getRol(),
                    vendedor.getId());
            throw new AccesoDenegadoException(
                    "El usuario no tiene un rol autorizado para aprobar descuentos manuales");
        }

        if (autorizador.getRol() == RolUsuario.GERENTE
                && (autorizador.getTienda() == null
                        || !autorizador.getTienda().getId().equals(vendedor.getTienda().getId()))) {
            limitadorIntentosAutorizacion.registrarFallo(vendedor.getId());
            log.warn("[POS/AutorizacionDescuentoService] - SOLICITAR: gerente ID: {} no pertenece a la tienda del "
                    + "vendedor ID: {}", autorizador.getId(), vendedor.getId());
            throw new AccesoDenegadoException("El gerente no pertenece a la tienda de esta venta");
        }

        final Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con ID: " + request.productoId()));
        if (!producto.isActivo()) {
            throw new IllegalArgumentException("El producto " + producto.getNombre() + " no está activo");
        }

        final BigDecimal precioFinal = calcularPrecioFinal(producto.getPrecioVenta(), request.porcentaje());
        validarPisoDeCosto(autorizador, producto, precioFinal);

        // Éxito: se limpia el contador de intentos fallidos del vendedor.
        limitadorIntentosAutorizacion.registrarExito(vendedor.getId());

        final String tokenPlano = tokenOpacoGenerador.generar();
        final LocalDateTime ahora = LocalDateTime.now();

        // Comparación informativa/de auditoría contra la mejor promoción automática de
        // producto/categoría disponible en este momento. No considera promociones de
        // cliente porque aún no hay una venta confirmada con cliente asociado; es una
        // limitación conocida y documentada, no un cálculo aplicado.
        final ResultadoPromocion automatica = motorPromocionesService.evaluar(
                producto, request.cantidad(), null, ahora);

        final AutorizacionDescuento autorizacion = new AutorizacionDescuento();
        autorizacion.setTokenHash(tokenOpacoGenerador.hash(tokenPlano));
        autorizacion.setAutorizador(autorizador);
        autorizacion.setRolAutorizador(autorizador.getRol());
        autorizacion.setVendedor(vendedor);
        autorizacion.setTienda(vendedor.getTienda());
        autorizacion.setProducto(producto);
        autorizacion.setCantidad(request.cantidad());
        autorizacion.setPorcentaje(request.porcentaje());
        autorizacion.setMotivo(request.motivo());
        autorizacion.setCarritoId(request.carritoId());
        autorizacion.setCostoConsiderado(producto.getCostoCompra());
        autorizacion.setMontoPromocionAutomaticaDisponible(automatica.montoDescuento());
        autorizacion.setFechaEmision(ahora);
        autorizacion.setFechaExpiracion(ahora.plusMinutes(VIGENCIA_MINUTOS));
        autorizacionDescuentoRepository.save(autorizacion);

        log.info("[POS/AutorizacionDescuentoService] - SOLICITAR: autorización ID: {} emitida por usuario ID: {} "
                        + "(rol: {}) para vendedorId: {}, productoId: {}, cantidad: {}, porcentaje: {}",
                autorizacion.getId(), autorizador.getId(), autorizador.getRol(), vendedor.getId(),
                producto.getId(), request.cantidad(), request.porcentaje());

        return construirRespuesta(tokenPlano, autorizacion, producto, precioFinal, automatica);
    }

    /**
     * Revalida el contexto completo de una autorización y la consume de forma
     * atómica. Debe invocarse dentro de la misma transacción que persiste la venta:
     * si la venta falla después, el rollback también revierte el consumo.
     */
    @Transactional
    public AutorizacionDescuento consumir(final String referenciaOpaca, final Producto producto,
                                           final int cantidadTotal, final Usuario vendedor, final String carritoId) {
        final String tokenHash = tokenOpacoGenerador.hash(referenciaOpaca);
        final AutorizacionDescuento autorizacion = autorizacionDescuentoRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La autorización de descuento manual es inválida, expiró o ya fue utilizada"));

        final LocalDateTime ahora = LocalDateTime.now();
        if (autorizacion.isConsumida() || !autorizacion.getFechaExpiracion().isAfter(ahora)) {
            throw new IllegalArgumentException(
                    "La autorización de descuento manual es inválida, expiró o ya fue utilizada");
        }

        if (!autorizacion.getProducto().getId().equals(producto.getId())
                || !autorizacion.getCantidad().equals(cantidadTotal)
                || !autorizacion.getVendedor().getId().equals(vendedor.getId())
                || vendedor.getTienda() == null
                || !autorizacion.getTienda().getId().equals(vendedor.getTienda().getId())
                || !autorizacion.getCarritoId().equals(carritoId)) {
            log.warn("[POS/AutorizacionDescuentoService] - CONSUMIR: contexto no coincide para autorización ID: {}",
                    autorizacion.getId());
            throw new IllegalArgumentException(
                    "El producto, la cantidad, el vendedor, la tienda o el carrito no coinciden con la "
                            + "autorización solicitada");
        }

        // Defensa adicional: si el costo o el precio cambiaron entre la emisión y el
        // consumo (ventana de 2 minutos), se vuelve a exigir el piso de costo para
        // autorizaciones de GERENTE.
        final BigDecimal precioFinal = calcularPrecioFinal(producto.getPrecioVenta(), autorizacion.getPorcentaje());
        validarPisoDeCosto(autorizacion.getAutorizador(), producto, precioFinal);

        final int filasActualizadas = autorizacionDescuentoRepository.consumirSiVigente(autorizacion.getId(), ahora);
        if (filasActualizadas == 0) {
            throw new IllegalArgumentException(
                    "La autorización de descuento manual es inválida, expiró o ya fue utilizada");
        }

        log.info("[POS/AutorizacionDescuentoService] - CONSUMIR: autorización ID: {} consumida para productoId: "
                + "{}, cantidad: {}", autorizacion.getId(), producto.getId(), cantidadTotal);

        autorizacion.setConsumida(true);
        autorizacion.setFechaConsumo(ahora);
        return autorizacion;
    }

    private Usuario reautenticar(final String username, final String password, final Long vendedorId) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (final AuthenticationException e) {
            limitadorIntentosAutorizacion.registrarFallo(vendedorId);
            log.warn("[POS/AutorizacionDescuentoService] - REAUTENTICAR: intento fallido para vendedorId: {}",
                    vendedorId);
            throw e;
        }
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario se autenticó pero no se encontró su registro"));
    }

    private BigDecimal calcularPrecioFinal(final BigDecimal precioVenta, final BigDecimal porcentaje) {
        final BigDecimal factor = BigDecimal.ONE.subtract(porcentaje.movePointLeft(2));
        return precioVenta.multiply(factor).setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    private void validarPisoDeCosto(final Usuario autorizador, final Producto producto,
                                     final BigDecimal precioFinal) {
        if (autorizador.getRol() == RolUsuario.GERENTE && precioFinal.compareTo(producto.getCostoCompra()) < 0) {
            throw new IllegalArgumentException(
                    "Un gerente no puede autorizar un precio final por debajo del costo de compra vigente");
        }
    }

    private AutorizacionDescuentoResponseDTO construirRespuesta(
            final String tokenPlano, final AutorizacionDescuento autorizacion, final Producto producto,
            final BigDecimal precioFinal, final ResultadoPromocion automatica) {
        final BigDecimal subtotalLista = producto.getPrecioVenta()
                .multiply(BigDecimal.valueOf(autorizacion.getCantidad()))
                .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        final BigDecimal montoManual = subtotalLista
                .subtract(precioFinal.multiply(BigDecimal.valueOf(autorizacion.getCantidad())))
                .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);

        return new AutorizacionDescuentoResponseDTO(
                tokenPlano,
                autorizacion.getFechaExpiracion(),
                autorizacion.getPorcentaje(),
                montoManual,
                precioFinal,
                automatica.montoDescuento().compareTo(BigDecimal.ZERO) > 0,
                automatica.montoDescuento(),
                montoManual.subtract(automatica.montoDescuento()));
    }
}
