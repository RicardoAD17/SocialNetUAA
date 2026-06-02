package mx.edu.uaa.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.edu.uaa.model.Centro;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CentroRepository {
    private static final String RUTA = "/home/vboxuser/marvinBeak/Catalogos/centros.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public List<Centro> obtenerTodos() throws IOException {
        File file = new File(RUTA);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        return mapper.readValue(file, new TypeReference<List<Centro>>() {});
    }

    public Centro guardar(Centro c) throws IOException {
        File file = new File(RUTA);
        List<Centro> lista = obtenerTodos();
        if (!file.exists() && file.getParentFile() != null) file.getParentFile().mkdirs();
	int nuevoId = lista.stream().mapToInt(Centro::getIdCentro).max().orElse(0) + 1;
    c.setIdCentro(nuevoId); 
    // -----------------------------------

    lista.add(c);
    mapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
    return c;
    }
    
    public Centro obtenerPorId(Integer id) throws IOException {
        return obtenerTodos().stream().filter(c -> c.getIdCentro().equals(id)).findFirst().orElse(null);
    }
// Ya tenías obtenerPorId, ahora agregamos eliminar
    public boolean eliminar(Integer id) throws IOException {
        File file = new File(RUTA);
        List<Centro> lista = obtenerTodos();
        
        boolean borrado = lista.removeIf(c -> c.getIdCentro().equals(id));
        
        if (borrado) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        }
        return borrado;
    }
}
