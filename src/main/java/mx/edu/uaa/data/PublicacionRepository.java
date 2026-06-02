package mx.edu.uaa.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mx.edu.uaa.model.Publicacion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PublicacionRepository {

    private static final String RUTA_ARCHIVO = "/home/vboxuser/marvinBeak/Publicaciones/publicaciones.json";

    // Mapper configurado para manejar LocalDateTime
    private static final ObjectMapper jsonMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // --- GUARDAR (CREAR) ---
    public synchronized Publicacion guardar(Publicacion nuevaPublicacion) throws IOException {
        File file = new File(RUTA_ARCHIVO);
        List<Publicacion> publicaciones;

        // Leer existentes o crear lista nueva
        if (file.exists() && file.length() > 0) {
            publicaciones = obtenerTodas();
        } else {
            publicaciones = new ArrayList<>();
            // Crear carpeta si no existe
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
        }

        // Lógica ID Autoincrementable
        int nuevoId = 1;
        if (!publicaciones.isEmpty()) {
            nuevoId = publicaciones.stream()
                    .mapToInt(Publicacion::getIdPublicacion)
                    .max()
                    .orElse(0) + 1;
        }
        nuevaPublicacion.setIdPublicacion(nuevoId);

        // Guardar
        publicaciones.add(nuevaPublicacion);
        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, publicaciones);
        
        return nuevaPublicacion;
    }

    // --- OBTENER TODAS ---
    public List<Publicacion> obtenerTodas() throws IOException {
        File file = new File(RUTA_ARCHIVO);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        return jsonMapper.readValue(file, new TypeReference<List<Publicacion>>() {});
    }

    // --- OBTENER POR ID ---
    public Publicacion obtenerPorId(int id) throws IOException {
        List<Publicacion> lista = obtenerTodas();
        return lista.stream()
                .filter(p -> p.getIdPublicacion() == id)
                .findFirst()
                .orElse(null);
    }

    // --- OBTENER POR EVENTO (Filtrar) ---
    public List<Publicacion> obtenerPorEvento(Integer idEvento) throws IOException {
        return obtenerTodas().stream()
                .filter(p -> p.getIdEvento() != null && p.getIdEvento().equals(idEvento))
                .collect(Collectors.toList());
    }

    // --- OBTENER POR INTERÉS (Filtrar) ---
    public List<Publicacion> obtenerPorInteres(Integer idInteres) throws IOException {
        return obtenerTodas().stream()
                .filter(p -> p.getIntereses() != null && p.getIntereses().contains(idInteres))
                .collect(Collectors.toList());
    }

    // --- ACTUALIZAR ---
    public synchronized void actualizar(Publicacion publicacionEditada) throws IOException {
        File file = new File(RUTA_ARCHIVO);
        List<Publicacion> lista = obtenerTodas();

        boolean encontrado = false;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getIdPublicacion() == publicacionEditada.getIdPublicacion()) {
                lista.set(i, publicacionEditada); // Reemplazamos
                encontrado = true;
                break;
            }
        }

        if (encontrado) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        }
    }

    // --- ELIMINAR ---
    public synchronized boolean eliminar(int id) throws IOException {
        File file = new File(RUTA_ARCHIVO);
        List<Publicacion> lista = obtenerTodas();

        boolean borrado = lista.removeIf(p -> p.getIdPublicacion() == id);

        if (borrado) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        }
        return borrado;
    }
}
