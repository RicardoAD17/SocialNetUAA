package mx.edu.uaa.data;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GoogleCaptchaService {

    // TU CLAVE SECRETA DE GOOGLE (La que empieza con 6L...)
    private static final String SECRET_KEY = "6LcdjiQsAAAAAKJZV0mCykXYeWiKuXkJbrtzhfcM"; 
    
    // URL OFICIAL DE GOOGLE
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public static boolean esValido(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            URL url = new URL(VERIFY_URL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Parámetros obligatorios: secret y response
            String params = "secret=" + SECRET_KEY + "&response=" + token;
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            try (InputStream is = conn.getInputStream();
                 JsonReader jsonReader = Json.createReader(is)) {
                JsonObject jsonObject = jsonReader.readObject();
                // Devuelve true si Google dice que es válido
                return jsonObject.getBoolean("success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
