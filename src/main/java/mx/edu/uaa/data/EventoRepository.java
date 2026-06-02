package mx.edu.uaa.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mx.edu.uaa.model.Evento;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventoRepository {

    // Ruta específica para los eventos
    private static final String RUTA_ARCHIVO = "/home/vboxuser/marvinBeak/Eventos/eventos.json";

    // Configuración del Mapper (Vital para las fechas)
    private static final ObjectMapper jsonMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // --- GUARDAR (CREAR) ---
    public synchronized void guardarEvento(Evento nuevoEvento) throws IOException {
        File file = new File(RUTA_ARCHIVO);
        List<Evento> eventos;

        // 1. Leer eventos existentes o crear lista nueva
        if (file.exists() && file.length() > 0) {
            eventos = obtenerTodos();
        } else {
            eventos = new ArrayList<>();
            // Crear carpeta si no existe
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
        }

        // 2. Calcular ID Autoincrementable
        int nuevoId = 1;
        if (!eventos.isEmpty()) {
            nuevoId = eventos.stream()
                    .mapToInt(e -> e.getIdEvento() != null ? e.getIdEvento() : 0)
                    .max()
                    .orElse(0) + 1;
        }
        nuevoEvento.setIdEvento(nuevoId);

        // 3. Agregar y Guardar
        eventos.add(nuevoEvento);
        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, eventos);
    }

    // --- OBTENER TODOS ---
    public List<Evento> obtenerTodos() throws IOException {
        File file = new File(RUTA_ARCHIVO);
        
        // Validación de archivo vacío o inexistente
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        
        return jsonMapper.readValue(file, new TypeReference<List<Evento>>() {});
    }

    // --- OBTENER POR ID ---
    public Evento obtenerPorId(Integer id) throws IOException {
        List<Evento> eventos = obtenerTodos();
        
        return eventos.stream()
                .filter(e -> e.getIdEvento().equals(id))
                .findFirst()
                .orElse(null);
    }

    // --- ELIMINAR ---
    public synchronized boolean eliminarEvento(Integer id) throws IOException {
        File file = new File(RUTA_ARCHIVO);
        List<Evento> eventos = obtenerTodos();

        // removeIf devuelve true si borró algo
        boolean borrado = eventos.removeIf(e -> e.getIdEvento().equals(id));

        if (borrado) {
            // Sobrescribimos el archivo solo si hubo cambios
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, eventos);
        }

        return borrado;
    }
	// --- ACTUALIZAR ---
    public synchronized void actualizar(Evento eventoEditado) throws IOException {
        File file = new File(RUTA_ARCHIVO);
        List<Evento> lista = obtenerTodos();

        boolean encontrado = false;
        for (int i = 0; i < lista.size(); i++) {
            // Buscamos por ID
            if (lista.get(i).getIdEvento().equals(eventoEditado.getIdEvento())) {
                // Reemplazamos el objeto viejo con el nuevo
                lista.set(i, eventoEditado);
                encontrado = true;
                break;
            }
        }

        if (encontrado) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        }
    }
}
