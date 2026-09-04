package com.kristianconk.api_papeleria.producto.foto;

import com.kristianconk.api_papeleria.enums.EstadoProcesamientoFoto;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.storage.ByteArrayMultipartFile;
import com.kristianconk.api_papeleria.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoFotoProcessorTest {

    @Mock
    private ProductoFotoRepository fotoRepository;

    @Mock
    private StorageService storageService;

    private ProductoFotoProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ProductoFotoProcessor(fotoRepository, storageService);
    }

    private Path crearArchivoTemporalImagen(int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Path tempFile = Files.createTempFile("test_foto_", ".jpg");
        ImageIO.write(img, "jpg", tempFile.toFile());
        return tempFile;
    }

    @Test
    @DisplayName("Debe procesar imagen correctamente generando foto principal y miniatura")
    void debeProcesarFotoExitosamente() throws IOException {
        Long fotoId = 100L;
        Path tempFile = crearArchivoTemporalImagen(800, 600);

        Producto producto = new Producto();
        producto.setId(10L);

        ProductoFoto foto = new ProductoFoto();
        foto.setId(fotoId);
        foto.setProducto(producto);
        foto.setEstadoProcesamiento(EstadoProcesamientoFoto.PENDIENTE);

        when(fotoRepository.findById(fotoId)).thenReturn(Optional.of(foto));
        when(storageService.store(any(ByteArrayMultipartFile.class), eq("productos/10/100")))
                .thenReturn("productos/10/100/foto.jpg")
                .thenReturn("productos/10/100/thumb_80.jpg");

        processor.procesarFotoAsincrona(fotoId, tempFile, "jpg");

        // Verificar que se guardó en estado COMPLETADO
        ArgumentCaptor<ProductoFoto> fotoCaptor = ArgumentCaptor.forClass(ProductoFoto.class);
        verify(fotoRepository, atLeast(2)).save(fotoCaptor.capture());

        ProductoFoto fotoFinal = fotoCaptor.getValue();
        assertEquals(EstadoProcesamientoFoto.COMPLETADO, fotoFinal.getEstadoProcesamiento());
        assertEquals("productos/10/100/foto.jpg", fotoFinal.getRutaArchivo());
        assertEquals("productos/10/100/thumb_80.jpg", fotoFinal.getRutaMiniatura());
        assertNull(fotoFinal.getMensajeError());

        // Verificar que el archivo temporal fue eliminado
        assertFalse(Files.exists(tempFile), "El archivo temporal debe haberse limpiado");
    }

    @Test
    @DisplayName("Debe marcar estado ERROR si el archivo temporal está corrupto")
    void debeMarcarErrorSiImagenCorrupta() throws IOException {
        Long fotoId = 200L;
        Path tempFile = Files.createTempFile("corrupto_", ".jpg");
        Files.write(tempFile, new byte[]{0x00, 0x01, 0x02}); // Archivo inválido

        Producto producto = new Producto();
        producto.setId(20L);

        ProductoFoto foto = new ProductoFoto();
        foto.setId(fotoId);
        foto.setProducto(producto);
        foto.setEstadoProcesamiento(EstadoProcesamientoFoto.PENDIENTE);

        when(fotoRepository.findById(fotoId)).thenReturn(Optional.of(foto));

        processor.procesarFotoAsincrona(fotoId, tempFile, "jpg");

        ArgumentCaptor<ProductoFoto> fotoCaptor = ArgumentCaptor.forClass(ProductoFoto.class);
        verify(fotoRepository, atLeast(2)).save(fotoCaptor.capture());

        ProductoFoto fotoFinal = fotoCaptor.getValue();
        assertEquals(EstadoProcesamientoFoto.ERROR, fotoFinal.getEstadoProcesamiento());
        assertNotNull(fotoFinal.getMensajeError());

        // Verificar que el archivo temporal fue eliminado
        assertFalse(Files.exists(tempFile), "El archivo temporal debe haberse limpiado");
    }
}
