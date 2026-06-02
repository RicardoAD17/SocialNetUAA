package mx.edu.uaa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mx.edu.uaa.data.InteresRepository;
import mx.edu.uaa.model.Interes;

@Path("/catalogos")
public class InteresResource {

    // Instanciamos el repositorio que maneja el JSON
    private InteresRepository interesRepo = new InteresRepository();

    // ==========================================
    // OBTENER TODOS LOS INTERESES (GET)
    // ==========================================
    @GET
    @Path("/intereses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIntereses() {
        try {
            // Retorna la lista completa de intereses para llenar el <select> en Angular
            return Response.ok(interesRepo.obtenerTodos()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error al obtener intereses").build();
        }
    }

    // ==========================================
    // CREAR NUEVO INTERÉS (POST)
    // ==========================================
    @POST
    @Path("/intereses")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearInteres(Interes i) {
        try {
            // Validamos que venga el nombre
            if (i.getNombre() == null || i.getNombre().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"El nombre del interés es obligatorio\"}").build();
            }

            // Guardamos en el JSON y retornamos el objeto creado con su nuevo ID
            return Response.ok(interesRepo.guardar(i)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error al guardar interés").build();
        }
    }
}
