package mx.edu.uaa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mx.edu.uaa.data.HCaptchaService;
// Imports correctos
import mx.edu.uaa.data.EmailService;
import mx.edu.uaa.data.TokenService;
import mx.edu.uaa.data.UsuarioRepository;
import mx.edu.uaa.model.Usuario;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.util.ArrayList; 
import java.util.Optional;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@Path("/usuarios")
public class UsuarioResource {

    private static final String CARPETA_FOTOS = "/home/vboxuser/marvinBeak/Usuarios/fotos/";
    
    // Instancias de servicios y repositorios
    private TokenService tokenService = new TokenService();
    private UsuarioRepository usuarioRepo = new UsuarioRepository(); 

    // 1. INICIAR REGISTRO
@POST
@Path("/iniciar-registro")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public Response iniciarRegistro(
    @FormParam("correo") String correo,
    @FormParam("captchaToken") String captchaToken // <--- RECIBE EL TOKEN
) {
	//if (!HCaptchaService.esValido(captchaToken)) {
    //return Response.status(400).entity("Error: Captcha inválido.").build();
    //}

        try {
            // Usamos el repositorio en vez de método local
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
            // ==========================================
            // 1. VALIDACIONES DE SEGURIDAD (¡AGREGA ESTO!)
            // ==========================================
            
            // Validar Nombre
            if (nombre == null || nombre.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Error: El nombre es obligatorio.\"}").build();
            }

            // Validar Correo
            if (correo == null || correo.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Error: El correo es obligatorio.\"}").build();
            }

            // Validar Password (Opcional: puedes exigir mínimo de caracteres)
            if (password == null || password.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Error: La contraseña es obligatoria.\"}").build();
            }

            // Validar Duplicados (Importante para no tener dos usuarios con el mismo correo)
            if (usuarioRepo.obtenerPorCorreo(correo) != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"message\": \"Error: El correo ya está registrado.\"}").build();
            }
            // ==========================================

            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setCorreo(correo);
            u.setPassword(password);
            u.setCorreoValidado(true);
		List<Integer> listaIntereses = new ArrayList<>();
if (bodyParts != null) {
    for (FormDataBodyPart part : bodyParts) {
        // Convertir el valor de cada parte a Integer
        try {
            String valor = part.getValueAs(String.class);
            listaIntereses.add(Integer.parseInt(valor));
        } catch (NumberFormatException e) {
            // Ignorar valores no numéricos
        }
    }
}
u.setIntereses(listaIntereses);
            // --- LÓGICA DE ADMIN ---
            u.setAdmin(admin != null ? admin : false);
            
            // --- LÓGICA DE ROL ---
            String rolFinal = (rol != null && !rol.isEmpty()) ? rol : "invitado";
            u.setRol(rolFinal);

            if ("alumno".equalsIgnoreCase(rolFinal)) {
                if (idCarrera == null || idCarrera <= 0) {
                     // Opcional: Validar que el alumno tenga carrera
                     // return Response.status(Response.Status.BAD_REQUEST).entity("Falta Carrera").build();
                }
                u.setIdCarrera(idCarrera);
                u.setIdDepartamento(null);
            } else if ("profesor".equalsIgnoreCase(rolFinal)) {
                u.setIdDepartamento(idDepartamento);
                u.setIdCarrera(null);
            } else {
                u.setIdCarrera(null);
                u.setIdDepartamento(null);
            }

            // --- LÓGICA DE FOTO ---
            if (fotoDetalles != null && fotoDetalles.getFileName() != null && !fotoDetalles.getFileName().isEmpty()) {
                File carpeta = new File(CARPETA_FOTOS);
                if (!carpeta.exists()) carpeta.mkdirs();

                String nombreArchivo = System.currentTimeMillis() + "_" + fotoDetalles.getFileName();
                File archivoDestino = new File(CARPETA_FOTOS + nombreArchivo);
                Files.copy(fotoStream, archivoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                u.setFotoRuta(nombreArchivo);
            }

            // Guardar
            usuarioRepo.guardar(u);

            // Limpiar token temporal si existe
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
            String fotoUrl = datosGoogle.get("fotoUrl");

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

                if (correo.endsWith("@edu.uaa.mx")) {
                    u.setRol("alumno");
                } else if (correo.endsWith("@uaa.edu.mx")) {
                    u.setRol("profesor");
                } else {
                    u.setRol("invitado");
                }

                usuarioRepo.guardar(u);
                return Response.ok(u).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error en login Google").build();
        }
    }
    
    // 5. LOGIN NORMAL
   // 5. LOGIN NORMAL
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormParam("correo") String correo, @FormParam("password") String password) {
        try {
            // 1. Buscamos al usuario usando tu variable y método correctos
            Usuario usuario = usuarioRepo.obtenerPorCorreo(correo);
            
            // Si el usuario no existe, rechazamos
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"message\": \"Credenciales incorrectas\"}").build();
            }

            // 2. Comparamos la contraseña en texto plano (ya que no la estás encriptando en el registro)
            boolean passwordCorrecta = password.equals(usuario.getPassword());
            
            if (!passwordCorrecta) {
                return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"message\": \"Credenciales incorrectas\"}").build();
            }
            // Le quitamos los intereses para que el convertidor JSON no intente leerlos y explote
            usuario.setIntereses(null); 
            // ---------------------------------
            // 3. Si todo está bien, devuelves el usuario
            return Response.ok(usuario).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
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
            @PathParam("id") Integer idUsuarioAEditar, // El usuario que queremos cambiar
            
            // Parametros de seguridad
            @FormDataParam("idSolicitante") Integer idSolicitante, // Quién intenta hacer el cambio
            
            // Datos a editar
            @FormDataParam("nombre") String nombre,
            @FormDataParam("password") String password,
            @FormDataParam("rol") String rol,
            @FormDataParam("idCarrera") Integer idCarrera,
            @FormDataParam("idDepartamento") Integer idDepartamento,
            @FormDataParam("foto") InputStream fotoStream,
            @FormDataParam("foto") FormDataContentDisposition fotoDetalles
    ) {
        try {
            // 1. Obtener el usuario objetivo
            Usuario target = usuarioRepo.obtenerPorId(idUsuarioAEditar);
            if (target == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Usuario no encontrado\"}").build();
            }

            // 2. Obtener al solicitante para ver permisos
            Usuario solicitante = usuarioRepo.obtenerPorId(idSolicitante);
            if (solicitante == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Solicitante no identificado\"}").build();
            }

            // --- REGLA DE SEGURIDAD ---
            // Solo pasa si: Es Admin O Es el mismo usuario
            boolean esAdmin = solicitante.isAdmin(); // Asumiendo que tienes este getter o verifica el rol
            // Si usas roles String: boolean esAdmin = "admin".equalsIgnoreCase(solicitante.getRol());
            
            boolean esElMismo = solicitante.getIdUsuario().equals(idUsuarioAEditar);

            if (!esAdmin && !esElMismo) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"message\": \"No tienes permiso para editar este usuario\"}").build();
            }
            // ---------------------------

            // 3. Aplicar cambios (Solo si envían datos nuevos)
            if (nombre != null && !nombre.isEmpty()) target.setNombre(nombre);
            if (password != null && !password.isEmpty()) target.setPassword(password);
            
            // Solo un admin debería poder cambiar el rol de alguien más, pero lo dejamos abierto por ahora
            if (rol != null && !rol.isEmpty()) target.setRol(rol); 

            // Actualizar Carrera/Depto
            if (idCarrera != null) target.setIdCarrera(idCarrera > 0 ? idCarrera : null);
            if (idDepartamento != null) target.setIdDepartamento(idDepartamento > 0 ? idDepartamento : null);

            // 4. Actualizar Foto (Si suben nueva)
            if (fotoDetalles != null && fotoDetalles.getFileName() != null && !fotoDetalles.getFileName().isEmpty()) {
                File carpeta = new File(CARPETA_FOTOS);
                if (!carpeta.exists()) carpeta.mkdirs();

                String nombreArchivo = System.currentTimeMillis() + "_UPD_" + fotoDetalles.getFileName();
                File archivoDestino = new File(CARPETA_FOTOS + nombreArchivo);
                Files.copy(fotoStream, archivoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                target.setFotoRuta(nombreArchivo);
            }

            // 5. Guardar
            usuarioRepo.actualizar(target);

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
            @QueryParam("idSolicitante") Integer idSolicitante // Se envía en la URL: ?idSolicitante=1
    ) {
        try {
            // 1. Validaciones de existencia
            Usuario target = usuarioRepo.obtenerPorId(idUsuarioAEliminar);
            Usuario solicitante = usuarioRepo.obtenerPorId(idSolicitante);

            if (target == null) return Response.status(Response.Status.NOT_FOUND).build();
            if (solicitante == null) return Response.status(Response.Status.UNAUTHORIZED).build();

            // 2. REGLA DE SEGURIDAD
            // Ajusta la lógica de "esAdmin" según cómo guardes tus roles (boolean isAdmin o String rol="admin")
            boolean esAdmin = solicitante.isAdmin(); 
            boolean esElMismo = solicitante.getIdUsuario().equals(idUsuarioAEliminar);

            if (!esAdmin && !esElMismo) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"message\": \"No tienes permiso para eliminar este usuario\"}").build();
            }

            // 3. Eliminar
            // Opcional: Podrías borrar también su foto de perfil del disco aquí
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerUsuarios(@QueryParam("buscar") String textoBusqueda) {
        try {
            // 1. Obtener la lista completa del repositorio
            List<Usuario> listaCompleta = usuarioRepo.obtenerTodos();
            
            // Lista que vamos a devolver
            List<Usuario> resultado = new ArrayList<>();

            // 2. Filtrar y Limpiar
            for (Usuario u : listaCompleta) {
                
                // Lógica de búsqueda (opcional)
                // Si 'textoBusqueda' viene vacío, agregamos todos.
                // Si trae texto, solo agregamos los que coincidan en nombre o correo.
                boolean coincide = true;
                if (textoBusqueda != null && !textoBusqueda.isEmpty()) {
                    String texto = textoBusqueda.toLowerCase();
                    String nombre = u.getNombre() != null ? u.getNombre().toLowerCase() : "";
                    String correo = u.getCorreo() != null ? u.getCorreo().toLowerCase() : "";
                    
                    if (!nombre.contains(texto) && !correo.contains(texto)) {
                        coincide = false;
                    }
                }

                if (coincide) {
                    // --- SEGURIDAD IMPORTANTE ---
                    // Creamos una COPIA o limpiamos el password del objeto en memoria
                    // para no enviarlo al frontend.
                    // (OJO: No llamamos a repo.guardar(), solo modificamos lo que se va a enviar)
                    u.setPassword(null); 
                    
                    resultado.add(u);
                }
            }

            return Response.ok(resultado).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"message\": \"Error al obtener usuarios\"}").build();
        }
    }
	// En UsuarioResource.java
// ==========================================
    // OBTENER USUARIO POR NOMBRE O ID (GET)
    // ==========================================
    @GET
    @Path("/buscar/{dato}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerUsuarioPublico(@PathParam("dato") String dato) {
        try {
            Usuario u = null;
            
            // 1. Intentamos buscar por ID numérico
            try {
                int id = Integer.parseInt(dato);
                u = usuarioRepo.obtenerPorId(id);
            } catch (NumberFormatException e) {
                // 2. Si no es número, buscamos por Nombre (o Correo)
                // (Nota: Asegúrate de tener obtenerPorNombre en tu repo)
                u = usuarioRepo.obtenerPorNombre(dato);
            }

            if (u != null) {
                // IMPORTANTE: Por seguridad, borramos el password antes de enviarlo
                u.setPassword(null); 
                return Response.ok(u).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
    @GET
    @Path("/fotos/{nombreArchivo}")
    @Produces("image/jpg")
    public Response obtenerFotoPerfil(@PathParam("nombreArchivo") String nombreArchivo) {
        
        // La misma ruta que usas en CARPETA_FOTOS
        final String RUTA_FOTOS_PERFIL = "/home/vboxuser/marvinBeak/Usuarios/fotos/"; 
        
        File archivo = new File(RUTA_FOTOS_PERFIL + nombreArchivo);

        if (!archivo.exists()) {
            // Si no tiene foto, podrías devolver una por defecto o 404
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(archivo)
                .header("Content-Disposition", "inline; filename=\"" + nombreArchivo + "\"")
                .build();
    }
}
