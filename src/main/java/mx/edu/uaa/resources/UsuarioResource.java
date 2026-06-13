package mx.edu.uaa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mx.edu.uaa.data.EmailService;
import mx.edu.uaa.data.TokenService;
import mx.edu.uaa.data.UsuarioRepository;
import mx.edu.uaa.model.Usuario;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/usuarios")
public class UsuarioResource {

    // Instancias de servicios y repositorios
    private TokenService tokenService = new TokenService();
    private UsuarioRepository usuarioRepo = new UsuarioRepository(); 

    // 1. INICIAR REGISTRO
    @POST
    @Path("/iniciar-registro")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response iniciarRegistro(
        @FormParam("correo") String correo,
        @FormParam("captchaToken") String captchaToken
    ) {
        try {
            if (usuarioRepo.obtenerPorCorreo(correo) != null) {
                return Response.status(Response.Status.CONFLICT).entity("El correo ya está registrado.").build();
            }

            String token = String.valueOf((int) ((Math.random() * (999999 - 100000)) + 100000));
            tokenService.guardarToken(correo, token);
            
            EmailService emailService = new EmailService();
            emailService.enviarCorreoValidacion(correo, token);

            return Response.ok("Código enviado").build();
        } catch (Exception e) {
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }

    // 2. VERIFICAR CÓDIGO
    @POST
    @Path("/verificar-codigo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response verificarCodigo(@FormParam("correo") String correo, @FormParam("codigo") String codigo) {
        try {
            if (tokenService.validarToken(correo, codigo)) {
                return Response.ok("Código correcto").build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Código incorrecto").build();
            }
        } catch (Exception e) {
            return Response.serverError().entity("Error").build();
        }
    }

    // 3. REGISTRO FINAL
    @POST
    @Path("/registro-final")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response registroFinal(
            @FormDataParam("nombre") String nombre,
            @FormDataParam("correo") String correo,
            @FormDataParam("password") String password,
            @FormDataParam("rol") String rol,
            @FormDataParam("idCarrera") Integer idCarrera,
            @FormDataParam("idDepartamento") Integer idDepartamento,
            @FormDataParam("admin") Boolean admin,
            @FormDataParam("intereses") List<FormDataBodyPart> bodyParts,
            @FormDataParam("foto") InputStream fotoStream,
            @FormDataParam("foto") FormDataContentDisposition fotoDetalles) {
        try {
            // Validaciones
            if (nombre == null || nombre.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Error: El nombre es obligatorio.\"}").build();
            }
            if (correo == null || correo.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Error: El correo es obligatorio.\"}").build();
            }
            if (password == null || password.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Error: La contraseña es obligatoria.\"}").build();
            }
            if (usuarioRepo.obtenerPorCorreo(correo) != null) {
                return Response.status(Response.Status.CONFLICT).entity("{\"message\": \"Error: El correo ya está registrado.\"}").build();
            }

            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setCorreo(correo);
            u.setCorreoValidado(true);
            
            // Encriptar contraseña
            String passwordEncriptada = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
            u.setPassword(passwordEncriptada);

            List<Integer> listaIntereses = new ArrayList<>();
            if (bodyParts != null) {
                for (FormDataBodyPart part : bodyParts) {
                    try {
                        String valor = part.getValueAs(String.class);
                        listaIntereses.add(Integer.parseInt(valor));
                    } catch (NumberFormatException e) { }
                }
            }
            u.setIntereses(listaIntereses);

            u.setAdmin(admin != null ? admin : false);
            String rolFinal = (rol != null && !rol.isEmpty()) ? rol : "invitado";
            u.setRol(rolFinal);

            if ("alumno".equalsIgnoreCase(rolFinal)) {
                u.setIdCarrera(idCarrera);
                u.setIdDepartamento(null);
            } else if ("profesor".equalsIgnoreCase(rolFinal)) {
                u.setIdDepartamento(idDepartamento);
                u.setIdCarrera(null);
            } else {
                u.setIdCarrera(null);
                u.setIdDepartamento(null);
            }

            // --- LÓGICA DE FOTO EN BASE64 ---
            if (fotoDetalles != null && fotoDetalles.getFileName() != null && !fotoDetalles.getFileName().isEmpty()) {
                String base64Foto = convertirImagenABase64(fotoStream, fotoDetalles.getFileName());
                u.setFotoRuta(base64Foto);
            }

            usuarioRepo.guardar(u);
            tokenService.eliminarToken(correo);

            return Response.ok("{\"message\": \"Usuario registrado exitosamente\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error al registrar: " + e.getMessage()).build();
        }
    }

    // 4. LOGIN GOOGLE
    @POST
    @Path("/login-google")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginGoogle(Map<String, String> datosGoogle) {
        try {
            String correo = datosGoogle.get("correo");
            String nombre = datosGoogle.get("nombre");
            String fotoUrl = datosGoogle.get("fotoUrl"); // URL directa de Google (es válida como String)

            if (correo == null || correo.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            Usuario u = usuarioRepo.obtenerPorCorreo(correo);

            if (u != null) {
                return Response.ok(u).build();
            } else {
                u = new Usuario();
                u.setNombre(nombre);
                u.setCorreo(correo);
                u.setPassword(""); 
                u.setCorreoValidado(true);
                u.setEsGoogle(true); 
                u.setFotoRuta(fotoUrl); 

                if (correo.endsWith("@edu.uaa.mx")) u.setRol("alumno");
                else if (correo.endsWith("@uaa.edu.mx")) u.setRol("profesor");
                else u.setRol("invitado");

                usuarioRepo.guardar(u);
                return Response.ok(u).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error en login Google").build();
        }
    }
    
    // 5. LOGIN NORMAL
    // 5. LOGIN NORMAL FRAGMENTADO
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormParam("correo") String correo, @FormParam("password") String password) {
        try {
            // 1. Buscamos SOLO en la vista de autenticación (Súper rápido y seguro)
            mx.edu.uaa.model.UsuarioAuth auth = usuarioRepo.buscarParaLogin(correo);
            
            if (auth == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Credenciales incorrectas\"}").build();
            }

            // 2. Comparamos contraseñas
            boolean passwordCorrecta = org.mindrot.jbcrypt.BCrypt.checkpw(password, auth.getPassword());
            if (!passwordCorrecta) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Credenciales incorrectas\"}").build();
            }
            
            // 3. Si todo es correcto, devolvemos SOLO el perfil público (Sin contraseña, sin tokens)
            mx.edu.uaa.model.UsuarioPerfil perfil = usuarioRepo.obtenerPerfilPublico(auth.getIdUsuario());
            return Response.ok(perfil).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"message\": \"Error interno\"}").build();
        }
    }
    // ==========================================
    // ACTUALIZAR USUARIO (PUT)
    // ==========================================
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response actualizarUsuario(
            @PathParam("id") Integer idUsuarioAEditar,
            @FormDataParam("idSolicitante") Integer idSolicitante,
            @FormDataParam("nombre") String nombre,
            @FormDataParam("password") String password,
            @FormDataParam("rol") String rol,
            @FormDataParam("idCarrera") Integer idCarrera,
            @FormDataParam("idDepartamento") Integer idDepartamento,
            @FormDataParam("foto") InputStream fotoStream,
            @FormDataParam("foto") FormDataContentDisposition fotoDetalles
    ) {
        try {
            Usuario target = usuarioRepo.obtenerPorId(idUsuarioAEditar);
            if (target == null) return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Usuario no encontrado\"}").build();

            Usuario solicitante = usuarioRepo.obtenerPorId(idSolicitante);
            if (solicitante == null) return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Solicitante no identificado\"}").build();

            boolean esAdmin = solicitante.isAdmin(); 
            boolean esElMismo = solicitante.getIdUsuario().equals(idUsuarioAEditar);

            if (!esAdmin && !esElMismo) {
                return Response.status(Response.Status.FORBIDDEN).entity("{\"message\": \"No tienes permiso para editar este usuario\"}").build();
            }

            if (nombre != null && !nombre.isEmpty()) target.setNombre(nombre);
            if (password != null && !password.isEmpty()) {
                String passEnc = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
                target.setPassword(passEnc);
            }
            
            if (rol != null && !rol.isEmpty()) target.setRol(rol); 

            if (idCarrera != null) target.setIdCarrera(idCarrera > 0 ? idCarrera : null);
            if (idDepartamento != null) target.setIdDepartamento(idDepartamento > 0 ? idDepartamento : null);

            // --- LÓGICA DE FOTO EN BASE64 ---
            if (fotoDetalles != null && fotoDetalles.getFileName() != null && !fotoDetalles.getFileName().isEmpty()) {
                String base64Foto = convertirImagenABase64(fotoStream, fotoDetalles.getFileName());
                target.setFotoRuta(base64Foto);
            }

            usuarioRepo.actualizar(target);
            target.setPassword(null); // Seguridad al devolver
            
            return Response.ok(target).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error al actualizar").build();
        }
    }

    // ==========================================
    // ELIMINAR USUARIO (DELETE)
    // ==========================================
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarUsuario(
            @PathParam("id") Integer idUsuarioAEliminar,
            @QueryParam("idSolicitante") Integer idSolicitante
    ) {
        try {
            Usuario target = usuarioRepo.obtenerPorId(idUsuarioAEliminar);
            Usuario solicitante = usuarioRepo.obtenerPorId(idSolicitante);

            if (target == null) return Response.status(Response.Status.NOT_FOUND).build();
            if (solicitante == null) return Response.status(Response.Status.UNAUTHORIZED).build();

            boolean esAdmin = solicitante.isAdmin(); 
            boolean esElMismo = solicitante.getIdUsuario().equals(idUsuarioAEliminar);

            if (!esAdmin && !esElMismo) {
                return Response.status(Response.Status.FORBIDDEN).entity("{\"message\": \"No tienes permiso\"}").build();
            }

            if (usuarioRepo.eliminar(idUsuarioAEliminar)) {
                return Response.ok("{\"message\": \"Usuario eliminado correctamente\"}").build();
            }
            
            return Response.serverError().build();

        } catch (Exception e) {
            return Response.serverError().entity("Error al eliminar").build();
        }
    }

    // ==========================================
    // OBTENER TODOS LOS USUARIOS (GET)
    // ==========================================
    // ==========================================
    // DIRECTORIO: ALUMNOS
    // ==========================================
    @GET
    @Path("/directorio/alumnos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerDirectorioAlumnos() {
        try {
            List<mx.edu.uaa.model.UsuarioPerfil> alumnos = usuarioRepo.obtenerDirectorioAlumnos();
            return Response.ok(alumnos).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"message\": \"Error al cargar alumnos\"}").build();
        }
    }

    // ==========================================
    // DIRECTORIO: PROFESORES
    // ==========================================
    @GET
    @Path("/directorio/profesores")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerDirectorioProfesores() {
        try {
            // Nota: Debes asegurar que este método exista en tu UsuarioRepository
            List<mx.edu.uaa.model.UsuarioPerfil> profesores = usuarioRepo.obtenerDirectorioProfesores();
            return Response.ok(profesores).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"message\": \"Error al cargar profesores\"}").build();
        }
    }
    // ==========================================
    // OBTENER USUARIO POR NOMBRE O ID (GET)
    // ==========================================
   // ==========================================
    // OBTENER PERFIL DE USUARIO
    // ==========================================
    @GET
    @Path("/{id}/perfil")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPerfilUsuario(@PathParam("id") Integer idUsuario) {
        try {
            // Lee directamente de vista_usuario_perfil
            mx.edu.uaa.model.UsuarioPerfil perfil = usuarioRepo.obtenerPerfilPublico(idUsuario);
            
            if (perfil != null) {
                return Response.ok(perfil).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Usuario no encontrado\"}").build();
            }
        } catch (Exception e) {
            return Response.serverError().build();
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