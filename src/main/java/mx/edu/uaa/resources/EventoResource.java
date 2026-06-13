package mx.edu.uaa.resources;

import mx.edu.uaa.data.EventoRepository;
import mx.edu.uaa.model.Evento;

// Imports de Jersey Multipart
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.ContentDisposition; // <-- FALTABA ESTE

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Path("/eventos")
public class EventoResource {

    private EventoRepository repositorio = new EventoRepository();
    private static final String CARPETA_EVENTOS = "/home/vboxuser/marvinBeak/Eventos/fotos/";

    @POST
    @Path("/crear")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearEvento(
            @FormDataParam("titulo") String titulo,
            @FormDataParam("descripcion") String descripcion,
            @FormDataParam("lugar") String lugar,
            @FormDataParam("fechaInicio") String fechaInicioStr,
            @FormDataParam("fechaFin") String fechaFinStr,
            @FormDataParam("horaInicio") String horaInicioStr,
            @FormDataParam("horaFin") String horaFinStr,
            @FormDataParam("idCreador") Integer idCreador,
            @FormDataParam("verificado") Boolean verificado,
            @FormDataParam("imagenes") List<FormDataBodyPart> bodyParts
    ) {
        try {
            if (titulo == null || titulo.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"El título es obligatorio\"}").build();
            }

            // Conversiones
            LocalDate fechaInicio = LocalDate.parse(fechaInicioStr);
            LocalDate fechaFin = LocalDate.parse(fechaFinStr);
            LocalTime horaInicio = LocalTime.parse(horaInicioStr);
            LocalTime horaFin = LocalTime.parse(horaFinStr);

            Evento nuevoEvento = new Evento();
            nuevoEvento.setTitulo(titulo);
            nuevoEvento.setDescripcion(descripcion);
            nuevoEvento.setLugar(lugar);
            nuevoEvento.setFechaInicio(fechaInicio);
            nuevoEvento.setFechaFin(fechaFin);
            nuevoEvento.setHoraInicio(horaInicio);
            nuevoEvento.setHoraFin(horaFin);
            nuevoEvento.setIdCreador(idCreador);
            nuevoEvento.setVerificado(verificado != null ? verificado : false);

            // Guardar Imágenes
            List<String> rutasGuardadas = new ArrayList<>();
            File directorio = new File(CARPETA_EVENTOS);
            if (!directorio.exists()) directorio.mkdirs();

            if (bodyParts != null) {
                for (FormDataBodyPart part : bodyParts) {
                    ContentDisposition meta = part.getContentDisposition();
                    InputStream fileStream = part.getValueAs(InputStream.class);

                    if (meta.getFileName() != null && !meta.getFileName().isEmpty()) {
                        String nombreArchivo = titulo.replaceAll("\\s+", "_") + "_" 
                                             + System.currentTimeMillis() + "_" 
                                             + meta.getFileName();
                        
                        File destino = new File(CARPETA_EVENTOS + nombreArchivo);
                        Files.copy(fileStream, destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        rutasGuardadas.add(nombreArchivo);
                    }
                }
            }
            nuevoEvento.setImagenes(rutasGuardadas);

            repositorio.guardarEvento(nuevoEvento);

            return Response.ok("{\"message\": \"Evento creado\", \"id\": " + nuevoEvento.getIdEvento() + "}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"message\": \"Error: " + e.getMessage() + "\"}").build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarEvento(@PathParam("id") Integer id) {
        try {
            boolean exito = repositorio.eliminarEvento(id);
            if (exito) {
                return Response.ok("{\"message\": \"Evento eliminado correctamente\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"No se encontró el evento\"}").build();
            }
        } catch (Exception e) {
            return Response.serverError().entity("{\"message\": \"Error al eliminar\"}").build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerTodos() {
        try {
            List<Evento> lista = repositorio.obtenerTodos();
            return Response.ok(lista).build();
        } catch (Exception e) {
            return Response.serverError().entity("Error al leer eventos").build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerEventoPorId(@PathParam("id") Integer id) {
        try {
            Evento evento = repositorio.obtenerPorId(id);
            if (evento != null) {
                return Response.ok(evento).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Evento no encontrado\"}").build();
            }
        } catch (Exception e) {
            return Response.serverError().entity("{\"message\": \"Error al buscar\"}").build();
        }
    }
	// ==========================================
    // 5. ACTUALIZAR EVENTO (PUT)
    // ==========================================
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response actualizarEvento(
            @PathParam("id") Integer id,
            @FormDataParam("titulo") String titulo,
            @FormDataParam("descripcion") String descripcion,
            @FormDataParam("lugar") String lugar,
            
            // Fechas y Horas como String (Angular las manda así)
            @FormDataParam("fechaInicio") String fechaInicioStr,
            @FormDataParam("fechaFin") String fechaFinStr,
            @FormDataParam("horaInicio") String horaInicioStr,
            @FormDataParam("horaFin") String horaFinStr,
            
            @FormDataParam("verificado") Boolean verificado,
            
            // Para borrar imágenes específicas
            @FormDataParam("imagenesAEliminar") List<String> imagenesAEliminar,

            // Para agregar nuevas imágenes (Lista de archivos)
            @FormDataParam("imagenes") List<FormDataBodyPart> bodyParts
    ) {
        // Asegúrate de tener esta constante definida en tu clase
        final String CARPETA_EVENTOS = "/home/vboxuser/marvinBeak/Eventos/fotos/";

        try {
            // 1. Obtener original
            Evento e = repositorio.obtenerPorId(id);
            if (e == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Evento no encontrado\"}").build();
            }

            // 2. Actualizar Textos
            if (titulo != null && !titulo.isEmpty()) e.setTitulo(titulo);
            if (descripcion != null) e.setDescripcion(descripcion);
            if (lugar != null) e.setLugar(lugar);
            
            // 3. Actualizar Fechas y Horas (Solo si vienen datos)
            if (fechaInicioStr != null && !fechaInicioStr.isEmpty()) 
                e.setFechaInicio(LocalDate.parse(fechaInicioStr));
                
            if (fechaFinStr != null && !fechaFinStr.isEmpty()) 
                e.setFechaFin(LocalDate.parse(fechaFinStr));
                
            if (horaInicioStr != null && !horaInicioStr.isEmpty()) 
                e.setHoraInicio(LocalTime.parse(horaInicioStr));
                
            if (horaFinStr != null && !horaFinStr.isEmpty()) 
                e.setHoraFin(LocalTime.parse(horaFinStr));

            // 4. Actualizar estado verificado
            if (verificado != null) e.setVerificado(verificado);

            // Inicializar lista de imágenes si es nula
            if (e.getImagenes() == null) e.setImagenes(new ArrayList<>());
            List<String> rutasActuales = e.getImagenes();

            // 5. ELIMINAR IMÁGENES SELECCIONADAS
            if (imagenesAEliminar != null && !imagenesAEliminar.isEmpty()) {
                for (int i = rutasActuales.size() - 1; i >= 0; i--) {
                    String rutaCompleta = rutasActuales.get(i);
                    File archivoFisico = new File(rutaCompleta);
                    
                    if (imagenesAEliminar.contains(archivoFisico.getName())) {
                        if (archivoFisico.exists()) archivoFisico.delete(); // Borrar físico
                        rutasActuales.remove(i); // Borrar de lista
                    }
                }
                e.setImagenes(rutasActuales);
            }

            // 6. AGREGAR NUEVAS IMÁGENES
            if (bodyParts != null) {
                File directorio = new File(CARPETA_EVENTOS);
                if (!directorio.exists()) directorio.mkdirs();

                for (FormDataBodyPart part : bodyParts) {
                    ContentDisposition meta = part.getContentDisposition();
                    InputStream fileStream = part.getValueAs(InputStream.class);

                    if (meta.getFileName() != null && !meta.getFileName().isEmpty()) {
                        // Validación simple de extensión (opcional pero recomendada)
                        String nombreOriginal = meta.getFileName().toLowerCase();
                        if (nombreOriginal.endsWith(".jpg") || nombreOriginal.endsWith(".png") || 
                            nombreOriginal.endsWith(".jpeg") || nombreOriginal.endsWith(".webp")) {

                            String nombreArchivo = titulo.replaceAll("\\s+", "_") + "_UPD_" 
                                                 + System.currentTimeMillis() + "_" 
                                                 + meta.getFileName();
                            
                            File destino = new File(CARPETA_EVENTOS + nombreArchivo);
                            Files.copy(fileStream, destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            
                            e.getImagenes().add(nombreArchivo);
                        }
                    }
                }
            }

            // 7. Guardar cambios
            repositorio.actualizar(e);

            return Response.ok(e).build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.serverError().entity("Error: " + ex.getMessage()).build();
        }
    }
	@GET
    @Path("/imagenes/{nombreArchivo}")
    @Produces("image/jpg") // El navegador detectará si es png o jpg
    public Response obtenerImagen(@PathParam("nombreArchivo") String nombreArchivo) {

        File archivo = new File(CARPETA_EVENTOS + nombreArchivo);

        if (!archivo.exists()) {
            // Si no existe, devolvemos 404
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Devolver el archivo binario
        return Response.ok(archivo)
                .header("Content-Disposition", "inline; filename=\"" + nombreArchivo + "\"")
                .build();
    }
    @GET
    @Path("/activos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerEventosActivos() {
        try {
            List<Evento> activos = repositorio.obtenerEventosActivos();
            return Response.ok(activos).build();
        } catch (Exception e) {
            return Response.serverError().entity("{\"message\": \"Error al cargar eventos activos\"}").build();
        }
    }

    @GET
    @Path("/pasados")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerEventosPasados() {
        try {
            List<Evento> pasados = repositorio.obtenerEventosPasados();
            return Response.ok(pasados).build();
        } catch (Exception e) {
            return Response.serverError().entity("{\"message\": \"Error al cargar eventos pasados\"}").build();
        }
    }
}
