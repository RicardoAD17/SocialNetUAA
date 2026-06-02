package mx.edu.uaa.data;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TurnstileService {

    private static final String SECRET_KEY = "0x4AAAAAACFSxJ2MwAkyY9eRDM2nVpaWncM0x4AAAAAACFSxJ2MwAkyY9eRDM2nVpaWncM"; // <--- CAMBIAR
    // URL DIFERENTE A GOOGLE
    private static final String VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    public static boolean esValido(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            URL url = new URL(VERIFY_URL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Cloudflare pide los mismos parametros: secret y response
            String params = "secret=" + SECRET_KEY + "&response=" + token;

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            try (InputStream is = conn.getInputStream();
                 JsonReader jsonReader = Json.createReader(is)) {

                JsonObject jsonObject = jsonReader.readObject();
                return jsonObject.getBoolean("success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
