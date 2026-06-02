package mx.edu.uaa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mx.edu.uaa.data.ComentarioRepository;
import mx.edu.uaa.data.PublicacionRepository;
import mx.edu.uaa.data.UsuarioRepository;
import mx.edu.uaa.model.Comentario;
import mx.edu.uaa.model.Publicacion;

// Importante: Para usar FormData
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.util.ArrayList;

@Path("/comentarios")
public class ComentarioResource {

    private ComentarioRepository comentarioRepo = new ComentarioRepository();
    private PublicacionRepository publicacionRepo = new PublicacionRepository();
    private UsuarioRepository usuarioRepo = new UsuarioRepository();

    // ==========================================
    // CREAR COMENTARIO (Con FormData)
    // ==========================================
    @POST
    @Path("/crear") // Agregamos /crear para ser consistentes con las otras clases
    @Consumes(MediaType.MULTIPART_FORM_DATA) // <--- CAMBIO PRINCIPAL
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearComentario(
            @FormDataParam("idUsuario") Integer idUsuario,
            @FormDataParam("idPublicacion") Integer idPublicacion,
            @FormDataParam("idComentarioPadre") Integer idComentarioPadre, // Puede ser nulo
            @FormDataParam("descripcion") String descripcion
    ) {
        try {
            // 1. Validaciones básicas
            if (idUsuario == null || idPublicacion == null || descripcion == null || descripcion.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Faltan datos obligatorios (usuario, publicacion o descripcion)\"}").build();
            }

            // 2. Validar Existencia de Usuario
            if (usuarioRepo.obtenerPorId(idUsuario) == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"El usuario no existe\"}").build();
            }

            // 3. Validar Existencia de Publicación
            Publicacion publicacion = publicacionRepo.obtenerPorId(idPublicacion);
            if (publicacion == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"La publicación no existe\"}").build();
            }

            // 4. Crear el Objeto Manualmente
            Comentario c = new Comentario();
            c.setIdUsuario(idUsuario);
            c.setIdPublicacion(idPublicacion);
            c.setDescripcion(descripcion);
            
            // Si mandan 0 o vacío, lo dejamos como null (comentario principal)
            if (idComentarioPadre != null && idComentarioPadre > 0) {
                c.setIdComentarioPadre(idComentarioPadre);
            } else {
                c.setIdComentarioPadre(null);
            }

            // 5. Guardar en el Repositorio (Aquí se genera el ID y la Fecha)
            Comentario guardado = comentarioRepo.guardar(c);

            // 6. Actualizar la referencia en la Publicación
            if (publicacion.getIdComentarios() == null) {
                publicacion.setIdComentarios(new ArrayList<>());
            }
            publicacion.getIdComentarios().add(guardado.getIdComentario());
            publicacionRepo.actualizar(publicacion); // Guardar cambio en publicacion.json

            return Response.ok(guardado).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }

    // ==========================================
    // OBTENER COMENTARIOS (GET)
    // ==========================================
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerComentarios(@QueryParam("idPublicacion") Integer idPublicacion) {
        try {
            if (idPublicacion == null) {
                return Response.ok(comentarioRepo.obtenerTodos()).build();
            }
            return Response.ok(comentarioRepo.obtenerPorPublicacion(idPublicacion)).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
	// ==========================================
    // ELIMINAR COMENTARIO (DELETE)
    // ==========================================
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarComentario(@PathParam("id") Integer id) {
        try {
            // 1. Buscar el comentario antes de borrarlo (para saber de qué publicación es)
            Comentario c = comentarioRepo.obtenerPorId(id);
            
            if (c == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"El comentario no existe\"}").build();
            }

            // 2. Borrar el comentario (y sus respuestas) del repositorio
            boolean borrado = comentarioRepo.eliminar(id);

            if (borrado) {
                // 3. ACTUALIZAR LA PUBLICACIÓN (Limpieza de referencia)
                // Obtenemos la publicación padre
                mx.edu.uaa.model.Publicacion publicacion = publicacionRepo.obtenerPorId(c.getIdPublicacion());
                
                if (publicacion != null && publicacion.getIdComentarios() != null) {
                    // Removemos el ID de la lista de la publicación
                    // Ojo: Casteamos a (Object) para que remueva el elemento, no el índice
                    publicacion.getIdComentarios().remove((Object) id);
                    
                    // Guardamos la publicación actualizada
                    publicacionRepo.actualizar(publicacion);
                }

                return Response.ok("{\"message\": \"Comentario eliminado correctamente\"}").build();
            }

            return Response.serverError().entity("{\"message\": \"No se pudo eliminar\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }
}
