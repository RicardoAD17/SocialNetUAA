package mx.edu.uaa.data;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private final String username = "ricardoalmada036@gmail.com";
    
    // Pega la contraseña nueva SIN espacios
    private final String password = "cdadoyyeoarqbilz";
    public void enviarCorreoValidacion(String destinatario, String token) throws Exception {

        Properties props = new Properties();
        props.put("mail.smtp.host","smtp.gmail.com");
        props.put("mail.smtp.port","587");
        props.put("mail.smtp.auth","true");
        props.put("mail.smtp.starttls.enable","true");
	props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        Session session = Session.getInstance(props, new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(username, password);
            }
        });
	Message msg = new MimeMessage(session); 
    // ----------------------------------------------

    msg.setFrom(new InternetAddress(username));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
    msg.setSubject("Código de Verificación - SocialNet UAA");

    String cuerpo = "Hola, tu código de verificación es: " + token + 
                    "\n\nIngrésalo en la aplicación para continuar el registro.";

    msg.setText(cuerpo);
    Transport.send(msg);
    }
}
