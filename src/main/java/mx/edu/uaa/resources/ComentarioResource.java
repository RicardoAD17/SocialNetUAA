package mx.edu.uaa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mx.edu.uaa.data.ComentarioRepository;
import mx.edu.uaa.data.PublicacionRepository;
import mx.edu.uaa.data.UsuarioRepository;
import mx.edu.uaa.model.Comentario;
import mx.edu.uaa.model.Publicacion;
import org.glassfish.jersey.media.multipart.FormDataParam;

// Importaciones nativas de MongoDB
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.List;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

@Path("/comentarios")
public class ComentarioResource {

    // Repositorios de MySQL
    private ComentarioRepository sqlRepo = new ComentarioRepository();
    private PublicacionRepository publicacionRepo = new PublicacionRepository();
    private UsuarioRepository usuarioRepo = new UsuarioRepository();

    // ==========================================
    // CONEXIÓN NATIVA A MONGODB (Para Metabase)
    // ==========================================
    private static final String URI_MONGODB = "mongodb://172.25.219.232:27017";
    private static final MongoClient mongoClient = MongoClients.create(URI_MONGODB);
    private static final MongoDatabase database = mongoClient.getDatabase("socialnet_db");
    private static final MongoCollection<Document> mongoCollection = database.getCollection("comentarios");

    // ==========================================
    // CREAR COMENTARIO (Guarda en MySQL y MongoDB)
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

            // 1. GUARDAR EN MYSQL (Fuente principal de la aplicación)
            Comentario sqlComentario = new Comentario();
            sqlComentario.setIdUsuario(idUsuario);
            sqlComentario.setIdPublicacion(idPublicacion);
            sqlComentario.setDescripcion(descripcion);
            if (idComentarioPadre != null && idComentarioPadre > 0) {
                sqlComentario.setIdComentarioPadre(idComentarioPadre);
            }
            // MySQL nos devuelve el objeto ya con el ID Autoincrementable y la Fecha generada
            Comentario guardadoSQL = sqlRepo.guardar(sqlComentario);

            // 2. GUARDAR EN MONGODB (Nutrir el Dashboard de Metabase)
            Document mongoDoc = new Document("idComentario", guardadoSQL.getIdComentario())
                    .append("idUsuario", guardadoSQL.getIdUsuario())
                    .append("idPublicacion", guardadoSQL.getIdPublicacion())
                    .append("idComentarioPadre", guardadoSQL.getIdComentarioPadre())
                    .append("descripcion", guardadoSQL.getDescripcion())
                    // Convertir LocalDateTime a Date nativo de Mongo
                    .append("fechaComentario", Date.from(guardadoSQL.getFechaComentario().atZone(ZoneId.systemDefault()).toInstant()));
            
            mongoCollection.insertOne(mongoDoc);

            // 3. ACTUALIZAR LA REFERENCIA EN LA PUBLICACIÓN (MongoDB)
            if (publicacion.getIdComentarios() == null) {
                publicacion.setIdComentarios(new ArrayList<>());
            }
            publicacion.getIdComentarios().add(guardadoSQL.getIdComentario());
            publicacionRepo.actualizar(publicacion); 

            // Devolvemos el objeto MySQL a Angular
            return Response.ok(guardadoSQL).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }

    // ==========================================
    // OBTENER COMENTARIOS (Lee de MySQL)
    // ==========================================
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerComentarios(@QueryParam("idPublicacion") Integer idPublicacion) {
        try {
            // Leemos de MySQL porque es más rápido y seguro para el frontend
            if (idPublicacion == null) {
                return Response.ok(sqlRepo.obtenerTodos()).build();
            }
            return Response.ok(sqlRepo.obtenerPorPublicacion(idPublicacion)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error al obtener comentarios: " + e.getMessage()).build();
        }
    }

    // ==========================================
    // ELIMINAR COMENTARIO (Borra en ambas DBs)
    // ==========================================
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarComentario(@PathParam("id") Integer id) {
        try {
            // 1. Buscar en MySQL
            Comentario c = sqlRepo.obtenerPorId(id);
            if (c == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"El comentario no existe\"}").build();
            }

            // 2. Borrar de MySQL
            sqlRepo.eliminar(id);

            // 3. Borrar de MongoDB (Metabase)
            mongoCollection.deleteOne(Filters.eq("idComentario", id));

            // 4. ACTUALIZAR LA PUBLICACIÓN (Limpiar referencia)
            Publicacion publicacion = publicacionRepo.obtenerPorId(c.getIdPublicacion());
            if (publicacion != null && publicacion.getIdComentarios() != null) {
                publicacion.getIdComentarios().remove((Object) id);
                publicacionRepo.actualizar(publicacion);
            }

            return Response.ok("{\"message\": \"Comentario eliminado de MySQL y MongoDB correctamente\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }
    // ==========================================
    // SINCRONIZAR COMENTARIOS VIEJOS (MySQL -> MongoDB)
    // ==========================================
    @POST
    @Path("/sincronizar-antiguos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sincronizarAntiguos() {
        try {
            // 1. Obtener todos los comentarios antiguos de MySQL
            List<Comentario> viejos = sqlRepo.obtenerTodos();
            int procesados = 0;

            for (Comentario c : viejos) {
                // Verificar si el comentario ya existe en Mongo para no duplicarlo
                Document existe = mongoCollection.find(Filters.eq("idComentario", c.getIdComentario())).first();
                
                if (existe == null) {
                    // 2. Insertarlo en MongoDB para que Metabase lo pueda leer
                    Document mongoDoc = new Document("idComentario", c.getIdComentario())
                            .append("idUsuario", c.getIdUsuario())
                            .append("idPublicacion", c.getIdPublicacion())
                            .append("idComentarioPadre", c.getIdComentarioPadre())
                            .append("descripcion", c.getDescripcion())
                            .append("fechaComentario", Date.from(c.getFechaComentario().atZone(ZoneId.systemDefault()).toInstant()));
                    
                    mongoCollection.insertOne(mongoDoc);
                    procesados++;
                    
                    // 3. Vincularlo a la Publicación en MongoDB para que Angular lo detecte
                    Publicacion pub = publicacionRepo.obtenerPorId(c.getIdPublicacion());
                    if (pub != null) {
                        if (pub.getIdComentarios() == null) {
                            pub.setIdComentarios(new ArrayList<>());
                        }
                        // Solo agregamos el ID si no está ya en la lista
                        if (!pub.getIdComentarios().contains(c.getIdComentario())) {
                            pub.getIdComentarios().add(c.getIdComentario());
                            publicacionRepo.actualizar(pub);
                        }
                    }
                }
            }
            return Response.ok("{\"message\": \"Sincronización completa. Comentarios copiados a MongoDB y vinculados: " + procesados + "\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error en la sincronización: " + e.getMessage()).build();
        }
    }
}