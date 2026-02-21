package com.kristianconk.api_papeleria.storage;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
//@Profile("local")
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    // Inyectamos el valor del yaml con @Value
    public FileSystemStorageService(@Value("${app.storage-service.upload-path}") String storageLocation) {
        // Validamos que no venga vacío
        if(storageLocation.trim().isEmpty()){
            throw new RuntimeException("La ruta de carga de archivos no puede estar vacía.");
        }
        this.rootLocation = Paths.get(storageLocation);
    }

    @PostConstruct // Se ejecuta justo después del constructor
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de almacenamiento", e);
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectorio) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Error: El archivo está vacío.");
            }

            // Limpiamos el nombre del archivo por seguridad
            String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            // Protección contra Path Traversal (ej. "../../../etc/passwd")
            if (filename.contains("..")) {
                throw new RuntimeException("Error: El archivo contiene una ruta inválida " + filename);
            }

            // Crear el subdirectorio si no existe (ej. uploads-dir/solicitudes/uuid-1234)
            Path destinationFolder = this.rootLocation.resolve(subDirectorio);
            if (!Files.exists(destinationFolder)) {
                Files.createDirectories(destinationFolder);
            }

            // Usamos un nombre único o mantenemos el original según tu lógica.
            // Aquí combinamos UUID (que vendría en el subDirectorio) con el nombre original.
            Path destinationFile = destinationFolder.resolve(filename);

            // Importante: Verificación extra de seguridad
            // Confirmamos que el archivo resultante sigue estando dentro de rootLocation
            if (!destinationFile.getParent().startsWith(this.rootLocation)) {
                throw new RuntimeException("Error: No se puede almacenar el archivo fuera del directorio actual.");
            }

            // Copiamos el archivo (Sobrescribe si existe con el mismo nombre)
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Retornamos la ruta relativa para guardarla en la BD
            // Ejemplo: "solicitudes/uuid-123/foto.png"
            return subDirectorio + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Fallo al guardar el archivo.", e);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("No se pudo leer el archivo: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al leer el archivo: " + filename, e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);

            // Opcional: Intentar borrar la carpeta padre si quedó vacía
            // (Lógica de limpieza para que no se queden carpetas huérfanas)
            Path parent = file.getParent();
            if (Files.exists(parent) && isDirEmpty(parent)) {
                Files.deleteIfExists(parent);
            }

        } catch (IOException e) {
            throw new RuntimeException("No se pudo borrar el archivo: " + filename, e);
        }
    }

    // Método auxiliar para verificar si una carpeta está vacía
    private boolean isDirEmpty(final Path directory) throws IOException {
        try (var dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }
}
