package mx.edu.uaa.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mx.edu.uaa.model.Comentario;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ComentarioRepository {

    private static final String RUTA = "/home/vboxuser/marvinBeak/Publicaciones/comentarios.json";
    
    private static final ObjectMapper jsonMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // --- GUARDAR (Con Autoincremento) ---
    public synchronized Comentario guardar(Comentario c) throws IOException {
        File file = new File(RUTA);
        List<Comentario> lista;

        if (file.exists() && file.length() > 0) {
            lista = jsonMapper.readValue(file, new TypeReference<List<Comentario>>() {});
        } else {
            lista = new ArrayList<>();
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
        }

        // Lógica de ID Autoincrementable
        int nuevoId = lista.stream()
                .mapToInt(Comentario::getIdComentario)
                .max().orElse(0) + 1;
        
        c.setIdComentario(nuevoId);
        
        lista.add(c);
        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        return c;
    }

    // --- OBTENER TODOS ---
    public List<Comentario> obtenerTodos() throws IOException {
        File file = new File(RUTA);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        return jsonMapper.readValue(file, new TypeReference<List<Comentario>>() {});
    }

    // --- OBTENER POR PUBLICACIÓN (Filtrado) ---
    public List<Comentario> obtenerPorPublicacion(Integer idPublicacion) throws IOException {
        return obtenerTodos().stream()
                .filter(c -> c.getIdPublicacion().equals(idPublicacion))
                .collect(Collectors.toList());
    }
// --- ELIMINAR (Desvinculando hijos en lugar de borrarlos) ---
    public synchronized boolean eliminar(Integer id) throws IOException {
        File file = new File(RUTA);
        List<Comentario> lista = obtenerTodos();

        boolean huboCambiosEnHijos = false;

        // PASO 1: Buscar hijos y quitarles la referencia (Set null)
        for (Comentario c : lista) {
            // Verificamos si este comentario es respuesta del que vamos a borrar
            if (c.getIdComentarioPadre() != null && c.getIdComentarioPadre().equals(id)) {
                
                c.setIdComentarioPadre(null); // Ahora es un comentario independiente
                
                huboCambiosEnHijos = true;
            }
        }

        // PASO 2: Borrar SOLO el comentario objetivo
        boolean borrado = lista.removeIf(c -> c.getIdComentario().equals(id));

        // PASO 3: Guardar si hubo cualquier cambio (ya sea borrado o actualización de hijos)
        if (borrado || huboCambiosEnHijos) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        }

        return borrado;
    }
    
    // Método auxiliar para buscar por ID (necesario para el Resource)
    public Comentario obtenerPorId(Integer id) throws IOException {
        return obtenerTodos().stream()
                .filter(c -> c.getIdComentario().equals(id))
                .findFirst()
                .orElse(null);
    }
}
