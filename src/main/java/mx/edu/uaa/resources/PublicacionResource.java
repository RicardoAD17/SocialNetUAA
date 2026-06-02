package mx.edu.uaa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mx.edu.uaa.data.EventoRepository;
import mx.edu.uaa.data.PublicacionRepository;
import mx.edu.uaa.data.UsuarioRepository;
import mx.edu.uaa.model.Evento;
import mx.edu.uaa.model.Publicacion;
import mx.edu.uaa.model.Usuario;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
@Path("/publicaciones")
public class PublicacionResource {

    // Repositorios
    private PublicacionRepository repo = new PublicacionRepository();
    private EventoRepository eventoRepo = new EventoRepository();
    private UsuarioRepository usuarioRepo = new UsuarioRepository();

    // Ruta de imágenes (Ajusta si es necesario)
    private static final String RUTA_IMAGENES = "/home/vboxuser/marvinBeak/Publicaciones/imagenes/";


@POST
    @Path("/crear")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearPublicacion(
            @FormDataParam("titulo") String titulo,
            @FormDataParam("idAutor") Integer idAutor, // Recibimos Integer
            @FormDataParam("description") String description,
            @FormDataParam("idEvento") Integer idEvento,
            @FormDataParam("intereses") List<Integer> intereses,
            @FormDataParam("imagen") InputStream fileInputStream,
            @FormDataParam("imagen") FormDataContentDisposition fileMetaData
    ) {
        try {
            // 1. Validar Evento
            if (idEvento != null && idEvento > 0) {
                if (eventoRepo.obtenerPorId(idEvento) == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\": \"Error: El evento no existe.\"}").build();
                }
            }

            // 2. Validar Autor
            if (idAutor == null) {
                 return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"El idAutor es obligatorio.\"}").build();
            }
            if (usuarioRepo.obtenerPorId(idAutor) == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Error: El usuario autor con ID " + idAutor + " no existe.\"}").build();
            }

            // 3. Manejo de Imagen
            List<String> listaRutas = new ArrayList<>(); 

            if (fileMetaData != null && fileMetaData.getFileName() != null && !fileMetaData.getFileName().isEmpty()) {
                String nombreOriginal = fileMetaData.getFileName().toLowerCase();

                if (!nombreOriginal.endsWith(".png") && !nombreOriginal.endsWith(".jpg") &&
                    !nombreOriginal.endsWith(".jpeg") && !nombreOriginal.endsWith(".webp")) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\": \"Error: Solo se permiten imágenes (PNG, JPG, JPEG, WEBP)\"}").build();
                }

                String nombreArchivo = System.currentTimeMillis() + "_" + fileMetaData.getFileName();
                String rutaCompleta = RUTA_IMAGENES + nombreArchivo;

                guardarArchivoEnDisco(fileInputStream, rutaCompleta);
                listaRutas.add(nombreArchivo);
            }

            // 4. Crear Objeto
            Publicacion p = new Publicacion();
            p.setTitulo(titulo);
            p.setIdAutor(idAutor);
            p.setDescription(description);
            p.setIdEvento(idEvento);
            p.setIntereses(intereses != null ? intereses : new ArrayList<>());
            p.setImagePaths(listaRutas);
            p.setIdComentarios(new ArrayList<>());

            // 5. Guardar y Asignar a variable para retornar
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
            // NOTA: Ya no recibimos 'autor' ni 'idAutor' aquí para proteger la autoría
            @FormDataParam("description") String description,
            @FormDataParam("idEvento") Integer idEvento,
            @FormDataParam("intereses") List<Integer> intereses,
            @FormDataParam("imagenesAEliminar") List<String> imagenesAEliminar,
            @FormDataParam("imagen") InputStream fileInputStream,
            @FormDataParam("imagen") FormDataContentDisposition fileMetaData
    ) {
        try {
            // 1. Obtener original
            Publicacion p = repo.obtenerPorId(id);
            if (p == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // 2. Actualizar campos
            if (titulo != null && !titulo.isEmpty()) p.setTitulo(titulo);
            if (description != null) p.setDescription(description);
            if (idEvento != null) p.setIdEvento(idEvento <= 0 ? null : idEvento);
            if (intereses != null) p.setIntereses(intereses);

            p.setUpdateAt(java.time.LocalDateTime.now());

            // 3. Gestión de Imágenes
            if (p.getImagePaths() == null) p.setImagePaths(new ArrayList<>());
            List<String> rutasActuales = p.getImagePaths();

            // Borrar seleccionadas
            if (imagenesAEliminar != null && !imagenesAEliminar.isEmpty()) {
                for (int i = rutasActuales.size() - 1; i >= 0; i--) {
                    String rutaCompleta = RUTA_IMAGENES + rutasActuales.get(i); // Reconstruir ruta completa
                    File archivo = new File(rutaCompleta);
                    
                    // Si el nombre coincide con lo que pidieron borrar
                    if (imagenesAEliminar.contains(new File(rutasActuales.get(i)).getName())) {
                        if (archivo.exists()) archivo.delete();
                        rutasActuales.remove(i);
                    }
                }
            }

            // Agregar nueva
            if (fileMetaData != null && fileMetaData.getFileName() != null && !fileMetaData.getFileName().isEmpty()) {
                String nombreOriginal = fileMetaData.getFileName().toLowerCase();
                
                if (!nombreOriginal.endsWith(".png") && !nombreOriginal.endsWith(".jpg") &&
                    !nombreOriginal.endsWith(".jpeg") && !nombreOriginal.endsWith(".webp")) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\": \"Error: Archivo no es una imagen válida\"}").build();
                }

                String nombreArchivo = System.currentTimeMillis() + "_UPD_" + fileMetaData.getFileName();
                String rutaCompleta = RUTA_IMAGENES + nombreArchivo;

                guardarArchivoEnDisco(fileInputStream, rutaCompleta);
                
                // Guardar solo el nombre en la lista
                rutasActuales.add(nombreArchivo);
            }
            
            p.setImagePaths(rutasActuales);

            // 4. Guardar cambios
            repo.actualizar(p);

            return Response.ok(p).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }
    // ==========================================
    // 3. ELIMINAR (DELETE)
    // ==========================================
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@PathParam("id") int id) {
        try {
            Publicacion p = repo.obtenerPorId(id);
            if (p != null) {
                // Borrar archivos físicos
                if (p.getImagePaths() != null) {
                    for (String ruta : p.getImagePaths()) {
                        File archivo = new File(ruta);
                        if (archivo.exists()) archivo.delete();
                    }
                }
                // Borrar del JSON
                boolean borrado = repo.eliminar(id);
                if (borrado) {
                    return Response.ok("{\"message\": \"Publicación eliminada\"}").build();
                }
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }

    // ==========================================
    // 4. OBTENER POR ID (GET)
    // ==========================================
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPorId(@PathParam("id") int id) {
        try {
            Publicacion p = repo.obtenerPorId(id);
            if (p != null) {
                return Response.ok(p).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Publicación no encontrada\"}").build();
            }
        } catch (Exception e) {
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }

    // ==========================================
    // MÉTODO AUXILIAR
    // ==========================================
    private void guardarArchivoEnDisco(InputStream uploadedInputStream, String uploadedFileLocation) throws IOException {
        File folder = new File(RUTA_IMAGENES);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try (OutputStream out = new FileOutputStream(new File(uploadedFileLocation))) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        }
    }
	// ==========================================
    // 5. OBTENER/SERVIR IMAGEN POR NOMBRE (GET)
    // ==========================================
    @GET
    @Path("/imagenes/{nombreArchivo}")
    @Produces("image/jpeg")
    public Response obtenerImagen(@PathParam("nombreArchivo") String nombreArchivo) {
        
        // La ruta donde guardaste las fotos (DEBE coincidir con RUTA_IMAGENES)
        final String RUTA_BASE_IMAGENES = "/home/vboxuser/marvinBeak/Publicaciones/imagenes/"; 
        
        File archivo = new File(RUTA_BASE_IMAGENES + nombreArchivo);

        if (!archivo.exists()) {
            // Devolver un 404 si el archivo no existe en el disco
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Devolver el archivo como respuesta binaria
        return Response.ok(archivo)
                .header("Content-Disposition", "inline; filename=\"" + nombreArchivo + "\"")
                .build();
    }
	// ==========================================
    // OBTENER PUBLICACIONES (CON BUSCADOR)
    // ==========================================
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPublicaciones(
            @QueryParam("idEvento") Integer idEvento,
            @QueryParam("idInteres") List<Integer> interesesFiltro,
            @QueryParam("busqueda") String busqueda
    ) {
        try {
            // 1. Obtener todas
            List<Publicacion> resultados = repo.obtenerTodas();

            // 2. Filtro por Evento
            if (idEvento != null && idEvento > 0) {
                resultados.removeIf(p -> p.getIdEvento() == null || !p.getIdEvento().equals(idEvento));
            }

            // 3. Filtro por Intereses (Lista)
            if (interesesFiltro != null && !interesesFiltro.isEmpty()) {
                resultados.removeIf(p -> {
                    if (p.getIntereses() == null || p.getIntereses().isEmpty()) return true;
                    // Si son disjuntos (no comparten ninguno), se elimina
                    return java.util.Collections.disjoint(p.getIntereses(), interesesFiltro);
                });
            }

            // 4. FILTRO POR BÚSQUEDA DE TEXTO
            if (busqueda != null && !busqueda.isEmpty()) {
                String term = busqueda.toLowerCase().trim();

                resultados.removeIf(p -> {
                    // Buscamos coincidencia en Título O Descripción
                    boolean coincideTitulo = p.getTitulo() != null && p.getTitulo().toLowerCase().contains(term);
                    boolean coincideDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(term);

                    // Si NO coincide con ninguno, lo removemos (devuelve true para borrar)
                    return !(coincideTitulo || coincideDesc);
                });
            }
            
            // Opcional: Hidratar datos del autor (nombre y foto) si lo necesitas aquí
            // ...

            return Response.ok(resultados).build();

        } catch (Exception e) {
            // CORRECCIÓN: Aquí estaba el error de los "..."
            e.printStackTrace();
            return Response.serverError().entity("Error al buscar: " + e.getMessage()).build();
        }
    }
}
