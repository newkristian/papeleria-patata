package com.kristianconk.api_papeleria.producto.foto;

import com.kristianconk.api_papeleria.error.ArchivoDemasiadoGrandeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorSeguridadImagenTest {

    private ValidadorSeguridadImagen validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorSeguridadImagen();
    }

    private byte[] crearImagenValida(String format, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    @Test
    @DisplayName("Debe validar exitosamente imagen JPEG válida")
    void debeValidarImagenJpegValida() throws IOException {
        byte[] jpegBytes = crearImagenValida("jpg", 200, 200);
        MockMultipartFile file = new MockMultipartFile("archivo", "prueba.jpg", "image/jpeg", jpegBytes);

        ValidadorSeguridadImagen.InfoImagenValidada info = validador.validar(file);

        assertNotNull(info);
        assertEquals("JPEG", info.formato());
        assertEquals(200, info.ancho());
        assertEquals(200, info.alto());
        assertEquals("jpg", info.extension());
        assertEquals("prueba.jpg", info.nombreSanitizado());
    }

    @Test
    @DisplayName("Debe validar exitosamente imagen PNG válida")
    void debeValidarImagenPngValida() throws IOException {
        byte[] pngBytes = crearImagenValida("png", 100, 150);
        MockMultipartFile file = new MockMultipartFile("archivo", "test.png", "image/png", pngBytes);

        ValidadorSeguridadImagen.InfoImagenValidada info = validador.validar(file);

        assertNotNull(info);
        assertEquals("PNG", info.formato());
        assertEquals(100, info.ancho());
        assertEquals(150, info.alto());
        assertEquals("png", info.extension());
    }

    @Test
    @DisplayName("Debe rechazar archivo nulo o vacío")
    void debeRechazarArchivoVacio() {
        MockMultipartFile emptyFile = new MockMultipartFile("archivo", "vacio.jpg", "image/jpeg", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> validador.validar(emptyFile));
        assertThrows(IllegalArgumentException.class, () -> validador.validar(null));
    }

    @Test
    @DisplayName("Debe rechazar archivo mayor a 4MB lanzando ArchivoDemasiadoGrandeException")
    void debeRechazarArchivoMayorA4MB() {
        byte[] oversized = new byte[(int) (ValidadorSeguridadImagen.MAX_FILE_SIZE + 1024)];
        MockMultipartFile file = new MockMultipartFile("archivo", "grande.jpg", "image/jpeg", oversized);

        assertThrows(ArchivoDemasiadoGrandeException.class, () -> validador.validar(file));
    }

    @Test
    @DisplayName("Debe rechazar archivo falso con extensión .jpg pero contenido SVG")
    void debeRechazarSvgDisfrazado() {
        String svgContent = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert('xss')</script></svg>";
        MockMultipartFile file = new MockMultipartFile("archivo", "peligro.jpg", "image/jpeg", svgContent.getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validador.validar(file));
        assertTrue(ex.getMessage().contains("Formato o firma de archivo no permitido") || ex.getMessage().contains("SVG"));
    }

    @Test
    @DisplayName("Debe sanitizar nombres de archivo con path traversal")
    void debeSanitizarPathTraversal() {
        String limpio = ValidadorSeguridadImagen.sanitizarNombreArchivo("../../etc/passwd.jpg");
        assertFalse(limpio.contains(".."));
        assertFalse(limpio.contains("/"));
        assertEquals("passwd.jpg", limpio);

        String vacio = ValidadorSeguridadImagen.sanitizarNombreArchivo("../../../");
        assertEquals("imagen.jpg", vacio);
    }
}
