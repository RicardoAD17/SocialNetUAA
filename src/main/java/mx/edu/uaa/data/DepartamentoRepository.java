package mx.edu.uaa.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.edu.uaa.model.Departamento;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DepartamentoRepository {
    private static final String RUTA = "/home/vboxuser/marvinBeak/Catalogos/departamentos.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public List<Departamento> obtenerTodos() throws IOException {
        File file = new File(RUTA);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        return mapper.readValue(file, new TypeReference<List<Departamento>>() {});
    }

    public List<Departamento> obtenerPorCentro(Integer idCentro) throws IOException {
        return obtenerTodos().stream()
                .filter(d -> d.getIdCentro().equals(idCentro))
                .collect(Collectors.toList());
    }


public synchronized Departamento guardar(Departamento d) throws IOException {
    File file = new File(RUTA);
    List<Departamento> lista = obtenerTodos(); // Asegúrate de tener este método implementado

    // Crear carpeta si no existe
    if (!file.exists() && file.getParentFile() != null) {
        file.getParentFile().mkdirs();
    }

    // --- AUTOINCREMENTO DEPARTAMENTO ---
    // Cambiamos por Departamento::getIdDepartamento
    int nuevoId = lista.stream()
            .mapToInt(Departamento::getIdDepartamento)
            .max()
            .orElse(0) + 1;
            
    d.setIdDepartamento(nuevoId);
    // -----------------------------------

    lista.add(d);
    mapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
    return d;
}	
	public Departamento obtenerPorId(Integer id) throws IOException {
        return obtenerTodos().stream()
                .filter(d -> d.getIdDepartamento().equals(id))
                .findFirst()
                .orElse(null);
    }

    public boolean eliminar(Integer id) throws IOException {
        File file = new File(RUTA);
        List<Departamento> lista = obtenerTodos();
        
        boolean borrado = lista.removeIf(d -> d.getIdDepartamento().equals(id));
        
        if (borrado) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        }
        return borrado;
    }
}
