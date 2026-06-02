package mx.edu.uaa.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.edu.uaa.model.Interes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InteresRepository {
    private static final String RUTA = "/home/vboxuser/marvinBeak/Catalogos/intereses.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public List<Interes> obtenerTodos() throws IOException {
        File file = new File(RUTA);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        return mapper.readValue(file, new TypeReference<List<Interes>>() {});
    }

    public Interes guardar(Interes interes) throws IOException {
        File file = new File(RUTA);
        List<Interes> lista = obtenerTodos();
        
        if (!file.exists() && file.getParentFile() != null) file.getParentFile().mkdirs();

        int nuevoId = lista.stream().mapToInt(Interes::getIdInteres).max().orElse(0) + 1;
        interes.setIdInteres(nuevoId);
        lista.add(interes);

        mapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        return interes;
    }
    
    // Método para obtener los nombres dado una lista de IDs (Útil para mostrar en el feed)
    public List<Interes> obtenerPorListaIds(List<Integer> idsBuscados) throws IOException {
        if (idsBuscados == null || idsBuscados.isEmpty()) return new ArrayList<>();
        
        return obtenerTodos().stream()
                .filter(i -> idsBuscados.contains(i.getIdInteres()))
                .collect(Collectors.toList());
    }
}
