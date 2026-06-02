package mx.edu.uaa.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.edu.uaa.model.Usuario;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    private static final String RUTA_ARCHIVO = "/home/vboxuser/marvinBeak/Usuarios/usuarios.json";
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    // --- OBTENER TODOS ---
    public List<Usuario> obtenerTodos() throws IOException {
        File file = new File(RUTA_ARCHIVO);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        return jsonMapper.readValue(file, new TypeReference<List<Usuario>>() {});
    }

    // --- GUARDAR (Crear) ---
    public synchronized void guardar(Usuario nuevoUsuario) throws IOException {
        File file = new File(RUTA_ARCHIVO);
        List<Usuario> usuarios = obtenerTodos();
        
        if (file.getParentFile() != null) file.getParentFile().mkdirs();

        int nuevoId = 1;
        if (!usuarios.isEmpty()) {
            nuevoId = usuarios.stream()
                    .mapToInt(u -> u.getIdUsuario() != null ? u.getIdUsuario() : 0)
                    .max()
                    .orElse(0) + 1;
        }
        nuevoUsuario.setIdUsuario(nuevoId);

        usuarios.add(nuevoUsuario);
        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, usuarios);
    }
    
    // --- ACTUALIZAR ---
    public synchronized void actualizar(Usuario usuarioEditado) throws IOException {
        File file = new File(RUTA_ARCHIVO);
        List<Usuario> lista = obtenerTodos();
        boolean encontrado = false;

        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getIdUsuario().equals(usuarioEditado.getIdUsuario())) {
                lista.set(i, usuarioEditado);
                encontrado = true;
                break;
            }
        }
        if (encontrado) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        }
    }

    // --- ELIMINAR ---
    public synchronized boolean eliminar(Integer id) throws IOException {
        File file = new File(RUTA_ARCHIVO);
        List<Usuario> lista = obtenerTodos();
        boolean borrado = lista.removeIf(u -> u.getIdUsuario().equals(id));

        if (borrado) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, lista);
        }
        return borrado;
    }

    // --- BUSCAR POR ID ---
    public Usuario obtenerPorId(Integer id) throws IOException {
        if (id == null) return null;
        return obtenerTodos().stream()
                .filter(u -> u.getIdUsuario().equals(id))
                .findFirst().orElse(null);
    }
    
    // --- BUSCAR POR CORREO ---
    public Usuario obtenerPorCorreo(String correo) throws IOException {
        if (correo == null) return null;
        return obtenerTodos().stream()
                .filter(u -> u.getCorreo() != null && u.getCorreo().equalsIgnoreCase(correo))
                .findFirst().orElse(null);
    }

    // --- BUSCAR POR NOMBRE (ESTE ES EL QUE TE FALTABA) ---
    public Usuario obtenerPorNombre(String nombre) throws IOException {
        if (nombre == null) return null;
        return obtenerTodos().stream()
                // Busca ignorando mayúsculas/minúsculas
                .filter(u -> u.getNombre() != null && u.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);
    }
}
