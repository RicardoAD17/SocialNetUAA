package mx.edu.uaa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mx.edu.uaa.data.ComentarioMongoRepository;
import mx.edu.uaa.data.PublicacionRepository;
import mx.edu.uaa.data.UsuarioRepository;
import mx.edu.uaa.model.ComentarioMongo;
import mx.edu.uaa.model.Publicacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component // IMPORTANTE: Permite que Spring inyecte el repositorio de Mongo
@Path("/comentarios")
public class ComentarioResource {

    // IMPORTANTE: Se inyecta la conexión a Mongo. ¡No usar 'new'!
    @Autowired
    private ComentarioMongoRepository comentarioMongoRepo;

    private PublicacionRepository publicacionRepo = new PublicacionRepository();
    private UsuarioRepository usuarioRepo = new UsuarioRepository();

    // ==========================================
    // CREAR COMENTARIO (Guardar en MongoDB)
    // ==========================================
    @POST
    @Path("/crear")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearComentario(
            @FormDataParam("idUsuario") Integer idUsuario,
            @FormDataParam("idPublicacion") Integer idPublicacion,
            @FormDataParam("idComentarioPadre") Integer idComentarioPadre,
            @FormDataParam("descripcion") String descripcion
    ) {
        try {
            if (idUsuario == null || idPublicacion == null || descripcion == null || descripcion.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Faltan datos obligatorios\"}").build();
            }

            if (usuarioRepo.obtenerPorId(idUsuario) == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"El usuario no existe\"}").build();
            }

            Publicacion publicacion = publicacionRepo.obtenerPorId(idPublicacion);
            if (publicacion == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"La publicación no existe\"}").build();
            }

            // 1. Crear el Documento de Mongo
            ComentarioMongo c = new ComentarioMongo();
            
            // Generamos un ID numérico pseudo-aleatorio para mantener compatibilidad con la lista de MySQL
            int idGenerado = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            c.setIdComentario(idGenerado);
            
            c.setIdUsuario(idUsuario);
            c.setIdPublicacion(idPublicacion);
            c.setDescripcion(descripcion);
            c.setFechaComentario(LocalDateTime.now()); // Guardamos la fecha exacta del sistema
            
            if (idComentarioPadre != null && idComentarioPadre > 0) {
                c.setIdComentarioPadre(idComentarioPadre);
            }

            // 2. Guardar en MongoDB usando el método heredado 'save'
            ComentarioMongo guardado = comentarioMongoRepo.save(c);

            // 3. Actualizar la referencia en la Publicación (MySQL)
            if (publicacion.getIdComentarios() == null) {
                publicacion.setIdComentarios(new ArrayList<>());
            }
            publicacion.getIdComentarios().add(guardado.getIdComentario());
            publicacionRepo.actualizar(publicacion); 

            return Response.ok(guardado).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }

    // ==========================================
    // OBTENER COMENTARIOS (Leer de MongoDB)
    // ==========================================
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerComentarios(@QueryParam("idPublicacion") Integer idPublicacion) {
        try {
            if (idPublicacion == null) {
                // findAll() es nativo de MongoRepository
                return Response.ok(comentarioMongoRepo.findAll()).build();
            }
            // Utilizamos el método personalizado que creamos
            return Response.ok(comentarioMongoRepo.findByIdPublicacion(idPublicacion)).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    // ==========================================
    // ELIMINAR COMENTARIO (Borrar de MongoDB)
    // ==========================================
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarComentario(@PathParam("id") Integer id) {
        try {
            // 1. Buscar el comentario en Mongo
            ComentarioMongo c = comentarioMongoRepo.findByIdComentario(id).orElse(null);
            
            if (c == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"El comentario no existe\"}").build();
            }

            // 2. Borrar de Mongo
            comentarioMongoRepo.deleteByIdComentario(id);

            // 3. ACTUALIZAR LA PUBLICACIÓN (Limpiar referencia)
            Publicacion publicacion = publicacionRepo.obtenerPorId(c.getIdPublicacion());
            
            if (publicacion != null && publicacion.getIdComentarios() != null) {
                publicacion.getIdComentarios().remove((Object) id);
                publicacionRepo.actualizar(publicacion);
            }

            return Response.ok("{\"message\": \"Comentario fragmentado eliminado correctamente\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }
}