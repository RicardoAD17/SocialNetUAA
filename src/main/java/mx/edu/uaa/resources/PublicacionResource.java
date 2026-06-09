package mx.edu.uaa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mx.edu.uaa.data.EventoRepository;
import mx.edu.uaa.data.PublicacionRepository;
import mx.edu.uaa.data.UsuarioRepository;
import mx.edu.uaa.model.Publicacion;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Path("/publicaciones")
public class PublicacionResource {

    // Repositorios
    private PublicacionRepository repo = new PublicacionRepository();
    private EventoRepository eventoRepo = new EventoRepository();
    private UsuarioRepository usuarioRepo = new UsuarioRepository();

    @POST
    @Path("/crear")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearPublicacion(
            @FormDataParam("titulo") String titulo,
            @FormDataParam("idAutor") Integer idAutor,
            @FormDataParam("description") String description,
            @FormDataParam("idEvento") Integer idEvento,
            @FormDataParam("intereses") List<Integer> intereses,
            @FormDataParam("imagen") InputStream fileInputStream,
            @FormDataParam("imagen") FormDataContentDisposition fileMetaData
    ) {
        try {
            // 1. Validar Autor
            if (idAutor == null || usuarioRepo.obtenerPorId(idAutor) == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Error: Autor no válido.\"}").build();
            }

            // 2. Manejo de Imagen (A BASE64)
            List<String> listaRutas = new ArrayList<>(); 
            if (fileMetaData != null && fileMetaData.getFileName() != null && !fileMetaData.getFileName().isEmpty()) {
                String nombreOriginal = fileMetaData.getFileName().toLowerCase();
                String base64String = convertirImagenABase64(fileInputStream, nombreOriginal);
                listaRutas.add(base64String); // Guardamos la cadena gigante de texto
            }

            // 3. Crear Objeto
            Publicacion p = new Publicacion();
            p.setTitulo(titulo);
            p.setIdAutor(idAutor);
            p.setDescription(description);
            p.setIdEvento(idEvento);
            p.setIntereses(intereses != null ? intereses : new ArrayList<>());
            p.setImagePaths(listaRutas);
            p.setIdComentarios(new ArrayList<>());

            // 4. Guardar en MongoDB
            Publicacion guardada = repo.guardar(p); 
            return Response.ok(guardada).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response actualizarPublicacion(
            @PathParam("id") int id,
            @FormDataParam("titulo") String titulo,
            @FormDataParam("description") String description,
            @FormDataParam("imagen") InputStream fileInputStream,
            @FormDataParam("imagen") FormDataContentDisposition fileMetaData
    ) {
        try {
            Publicacion p = repo.obtenerPorId(id);
            if (p == null) return Response.status(Response.Status.NOT_FOUND).build();

            if (titulo != null && !titulo.isEmpty()) p.setTitulo(titulo);
            if (description != null) p.setDescription(description);
            p.setUpdateAt(java.time.LocalDateTime.now());

            if (fileMetaData != null && fileMetaData.getFileName() != null && !fileMetaData.getFileName().isEmpty()) {
                String nombreOriginal = fileMetaData.getFileName().toLowerCase();
                String base64String = convertirImagenABase64(fileInputStream, nombreOriginal);
                
                List<String> nuevasRutas = new ArrayList<>();
                nuevasRutas.add(base64String);
                p.setImagePaths(nuevasRutas); // Reemplaza la imagen vieja con la nueva
            }

            repo.actualizar(p);
            return Response.ok(p).build();

        } catch (Exception e) {
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@PathParam("id") int id) {
        try {
            if (repo.eliminar(id)) {
                return Response.ok("{\"message\": \"Publicación eliminada\"}").build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPorId(@PathParam("id") int id) {
        try {
            Publicacion p = repo.obtenerPorId(id);
            if (p != null) return Response.ok(p).build();
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPublicaciones() {
        try {
            List<Publicacion> resultados = repo.obtenerTodas();
            return Response.ok(resultados).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error al buscar: " + e.getMessage()).build();
        }
    }

    // ==========================================
    // MÉTODO AUXILIAR: Convertir a Base64
    // ==========================================
    private String convertirImagenABase64(InputStream inputStream, String fileName) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] imageBytes = buffer.toByteArray();
        String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);

        String mimeType = "image/jpeg";
        if (fileName.toLowerCase().endsWith(".png")) mimeType = "image/png";
        else if (fileName.toLowerCase().endsWith(".webp")) mimeType = "image/webp";

        return "data:" + mimeType + ";base64," + base64Image;
    }
}
