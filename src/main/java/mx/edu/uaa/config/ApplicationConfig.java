package mx.edu.uaa.config;


// CAMBIOS AQUÍ:
import jakarta.ws.rs.ApplicationPath; 
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

//@ApplicationPath("/api")
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        packages("mx.edu.uaa.resources");
        register(MultiPartFeature.class);
        register(CorsFilter.class);
    }
}
