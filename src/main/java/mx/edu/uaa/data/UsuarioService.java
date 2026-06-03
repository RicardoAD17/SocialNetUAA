package mx.edu.uaa.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.edu.uaa.model.Usuario;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UsuarioService {

    private static final String USUARIOS_FILE = "usuarios.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Usuario> cargarUsuarios() {
        try {
            File file = new File(USUARIOS_FILE);
            if (!file.exists()) return new ArrayList<>();

            return mapper.readValue(file, new TypeReference<List<Usuario>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void guardarUsuarios(List<Usuario> usuarios) throws Exception {
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(USUARIOS_FILE), usuarios);
    }

    public static boolean correoValidado(String correo) {
        return cargarUsuarios()
                .stream()
                .anyMatch(u -> u.getCorreo().equals(correo) && u.isCorreoValidado());
    }

    public static void marcarCorreoComoValidado(String correo) throws Exception {
        List<Usuario> lista = cargarUsuarios();

        for (Usuario u : lista) {
            if (u.getCorreo().equals(correo)) {
                u.setCorreoValidado(true);
            }
        }

        guardarUsuarios(lista);
    }

    public static void registrarUsuario(Usuario u) throws Exception {
        List<Usuario> lista = cargarUsuarios();
        lista.add(u);
        guardarUsuarios(lista);
    }
}
