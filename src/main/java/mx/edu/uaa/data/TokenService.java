package mx.edu.uaa.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TokenService {

    private static final String RUTA_NFS = "/home/vboxuser/marvinBeak/Usuarios/";
    private static final String TOKEN_FILE = RUTA_NFS + "tokensPendientes.json";

    private static final ObjectMapper mapper = new ObjectMapper();

    public synchronized void guardarToken(String correo, String token) throws IOException {
        File file = new File(TOKEN_FILE);
        Map<String, String> tokens;

        if (file.exists()) {
            tokens = mapper.readValue(file, new TypeReference<Map<String, String>>() {});
        } else {
            tokens = new HashMap<>();
        }

        tokens.put(correo, token);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, tokens);
    }

    public synchronized boolean validarToken(String correo, String token) throws IOException {
        File file = new File(TOKEN_FILE);

        if (!file.exists()) return false;

        Map<String, String> tokens = mapper.readValue(file, new TypeReference<Map<String, String>>() {});

        if (!tokens.containsKey(correo)) return false;

        return tokens.get(correo).equals(token);
    }

    public synchronized void eliminarToken(String correo) throws IOException {
        File file = new File(TOKEN_FILE);
        if (!file.exists()) return;

        Map<String, String> tokens = mapper.readValue(file, new TypeReference<Map<String, String>>() {});
        tokens.remove(correo);

        mapper.writerWithDefaultPrettyPrinter().writeValue(file, tokens);
    }
}
