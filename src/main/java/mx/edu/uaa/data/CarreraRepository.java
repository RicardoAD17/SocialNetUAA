package mx.edu.uaa.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.edu.uaa.model.Carrera;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CarreraRepository {
    private static final String RUTA = "/home/vboxuser/marvinBeak/Catalogos/carreras.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public List<Carrera> obtenerTodas() throws IOException {
        File file = new File(RUTA);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        return mapper.readValue(file, new TypeReference<List<Carrera>>() {});
    }
    
    // Obtener carreras filtradas por Centro (Para los selects en cascada)
    public List<Carrera> obtenerPorCentro(Integer idCentro) throws IOException {
        return obtenerTodas().stream()
                .filter(c -> c.getIdCentro().equals(idCentro))
                .collect(Collectors.toList());
    }

public synchronized Carrera guardar(Carrera c) throws IOException {
    File file = new File(RUTA);
    List<Carrera> lista = obtenerTodas(); // Asegúrate de tener este método implementado
    
    // Crear carpeta si no existe (buena práctica)
    if (!file.exists() && file.getParentFile() != null) {
        file.getParentFile().mkdirs();
    }

    // --- AUTOINCREMENTO CARRERA ---
    // Cambiamos Centro::getIdCentro por Carrera::getIdCarrera
    int nuevoId = lista.stream()
            .mapToInt(Carrera::getIdCarrera) 
            .max()
            .orElse(0) + 1;
            
    c.setIdCarrera(nuevoId);
    // ------------------------------

    lista.add(c);
    mapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
    return c;
}
    public Carrera obtenerPorId(Integer id) throws IOException {
        return obtenerTodas().stream()
                .filter(c -> c.getIdCarrera().equals(id))
                .findFirst()
                .orElse(null);
    }

    public boolean eliminar(Integer id) throws IOException {
        File file = new File(RUTA);
        List<Carrera> lista = obtenerTodas();
        
        boolean borrado = lista.removeIf(c -> c.getIdCarrera().equals(id));
        
        if (borrado) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        }
        return borrado;
    }
}
