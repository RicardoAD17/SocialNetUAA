package mx.edu.uaa.data;

import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HCaptchaService {
    // Lee la variable desde el entorno del sistema operativo
    private static final String SECRET_KEY = System.getenv("HCAPTCHA_SECRET");

    public static boolean esValido(String token) {
        try {
            // Verifica que la clave no sea nula antes de usarla
            if (SECRET_KEY == null) {
                System.out.println("Error: Variable HCAPTCHA_SECRET no configurada.");
                return false;
            }

            String postData = "secret=" + URLEncoder.encode(SECRET_KEY, "UTF-8") +
                              "&response=" + URLEncoder.encode(token, "UTF-8");

            URL url = new URL("https://hcaptcha.com/siteverify");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());
            return json.getBoolean("success");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
