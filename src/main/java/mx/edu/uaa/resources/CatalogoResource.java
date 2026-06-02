package mx.edu.uaa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mx.edu.uaa.data.CarreraRepository;
import mx.edu.uaa.data.CentroRepository;
import mx.edu.uaa.data.DepartamentoRepository;
import mx.edu.uaa.model.Carrera;
import mx.edu.uaa.model.Centro;
import mx.edu.uaa.model.Departamento;
// IMPORTANTE: Importar FormDataParam
import org.glassfish.jersey.media.multipart.FormDataParam;
@Path("/catalogos")
public class CatalogoResource {

    private CentroRepository centroRepo = new CentroRepository();
    private CarreraRepository carreraRepo = new CarreraRepository();
    private DepartamentoRepository deptoRepo = new DepartamentoRepository();

    // ==========================================
    //                 CENTROS
    // ==========================================
    
    // Obtener todos
    @GET
    @Path("/centros")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCentros() {
        try {
            return Response.ok(centroRepo.obtenerTodos()).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }

    // Obtener por ID
    @GET
    @Path("/centros/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCentroPorId(@PathParam("id") Integer id) {
        try {
            Centro c = centroRepo.obtenerPorId(id);
            if (c != null) return Response.ok(c).build();
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }

    // Crear
    @POST
    @Path("/centros")
    @Consumes(MediaType.MULTIPART_FORM_DATA) // <--- Acepta Postman form-data
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearCentro(@FormDataParam("nombre") String nombre) {
        try {
            if (nombre == null || nombre.isEmpty()) {
                return Response.status(400).entity("{\"message\":\"El nombre es obligatorio\"}").build();
            }

            Centro c = new Centro();
            c.setNombre(nombre);
            
            return Response.ok(centroRepo.guardar(c)).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }
    // Eliminar
    @DELETE
    @Path("/centros/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarCentro(@PathParam("id") Integer id) {
        try {
            if (centroRepo.eliminar(id)) {
                return Response.ok("{\"message\": \"Centro eliminado\"}").build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }


    // ==========================================
    //                 CARRERAS
    // ==========================================

    // Obtener todas (con filtro opcional)
    @GET
    @Path("/carreras")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCarreras(@QueryParam("idCentro") Integer idCentro) {
        try {
            if (idCentro != null) {
                return Response.ok(carreraRepo.obtenerPorCentro(idCentro)).build();
            }
            return Response.ok(carreraRepo.obtenerTodas()).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }

    // Obtener por ID
    @GET
    @Path("/carreras/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCarreraPorId(@PathParam("id") Integer id) {
        try {
            Carrera c = carreraRepo.obtenerPorId(id);
            if (c != null) return Response.ok(c).build();
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }

    // Crear
@POST
    @Path("/carreras")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearCarrera(
            @FormDataParam("nombre") String nombre,
            @FormDataParam("idCentro") Integer idCentro
    ) {
        try {
            if (idCentro == null) {
                return Response.status(400).entity("{\"message\":\"idCentro es obligatorio\"}").build();
            }
            if (centroRepo.obtenerPorId(idCentro) == null) {
                return Response.status(400).entity("{\"message\":\"Centro no existe\"}").build();
            }

            Carrera c = new Carrera();
            c.setNombre(nombre);
            c.setIdCentro(idCentro);

            return Response.ok(carreraRepo.guardar(c)).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }
    // Eliminar
    @DELETE
    @Path("/carreras/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarCarrera(@PathParam("id") Integer id) {
        try {
            if (carreraRepo.eliminar(id)) {
                return Response.ok("{\"message\": \"Carrera eliminada\"}").build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }


    // ==========================================
    //              DEPARTAMENTOS
    // ==========================================

    // Obtener todos (con filtro opcional)
    @GET
    @Path("/departamentos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDepartamentos(@QueryParam("idCentro") Integer idCentro) {
        try {
            if (idCentro != null) {
                return Response.ok(deptoRepo.obtenerPorCentro(idCentro)).build();
            }
            return Response.ok(deptoRepo.obtenerTodos()).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }

    // Obtener por ID
    @GET
    @Path("/departamentos/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDepartamentoPorId(@PathParam("id") Integer id) {
        try {
            Departamento d = deptoRepo.obtenerPorId(id);
            if (d != null) return Response.ok(d).build();
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }

    // Crear
@POST
    @Path("/departamentos")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearDepartamento(
            @FormDataParam("nombre") String nombre,
            @FormDataParam("idCentro") Integer idCentro
    ) {
        try {
            if (idCentro == null) {
                return Response.status(400).entity("{\"message\":\"idCentro es obligatorio\"}").build();
            }
            if (centroRepo.obtenerPorId(idCentro) == null) {
                return Response.status(400).entity("{\"message\":\"Centro no existe\"}").build();
            }

            Departamento d = new Departamento();
            d.setNombre(nombre);
            d.setIdCentro(idCentro);

            return Response.ok(deptoRepo.guardar(d)).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }
    // Eliminar
    @DELETE
    @Path("/departamentos/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarDepartamento(@PathParam("id") Integer id) {
        try {
            if (deptoRepo.eliminar(id)) {
                return Response.ok("{\"message\": \"Departamento eliminado\"}").build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }
}
