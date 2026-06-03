package mx.edu.uaa.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "usuarios") // Nombre de la tabla en MySQL
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincrementable
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(unique = true, nullable = false, length = 100)
    private String correo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String rol;

    @Column(name = "is_admin")
    private boolean isAdmin;

    @Column(name = "token_validacion")
    private String tokenValidacion;

    @Column(name = "correo_validado")
    private boolean correoValidado;

    @Column(name = "id_carrera")
    private Integer idCarrera;      // Solo para Alumnos

    @Column(name = "id_departamento")
    private Integer idDepartamento; // Solo para Profesores

    @Lob
    @Column(name = "foto_ruta", columnDefinition = "LONGTEXT")
    private String fotoRuta;

    @Column(name = "es_google")
    private boolean esGoogle;

    // MAGIA JPA: Crea una tabla extra para guardar los múltiples intereses del usuario
    @ElementCollection
    @CollectionTable(
        name = "usuario_intereses", 
        joinColumns = @JoinColumn(name = "id_usuario")
    )
    @Column(name = "id_interes")
    private List<Integer> intereses;

    // ==========================================
    // Constructores
    // ==========================================
    
    public Usuario() {
        this.intereses = new ArrayList<>();
    }

    public Usuario(int idUsuario, String correo, String nombre, String password, String rol, boolean isAdmin) {
        this.idUsuario = idUsuario;
        this.correo = correo;
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
        this.isAdmin = isAdmin;
        this.intereses = new ArrayList<>();
    }

    // ==========================================
    // Getters y Setters
    // ==========================================

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public String getTokenValidacion() { return tokenValidacion; }
    public void setTokenValidacion(String tokenValidacion) { this.tokenValidacion = tokenValidacion; }

    public boolean isCorreoValidado() { return correoValidado; }
    public void setCorreoValidado(boolean correoValidado) { this.correoValidado = correoValidado; }

    public Integer getIdCarrera() { return idCarrera; }
    public void setIdCarrera(Integer idCarrera) { this.idCarrera = idCarrera; }

    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getFotoRuta() { return fotoRuta; }
    public void setFotoRuta(String fotoRuta) { this.fotoRuta = fotoRuta; }

    public boolean isEsGoogle() { return esGoogle; }
    public void setEsGoogle(boolean esGoogle) { this.esGoogle = esGoogle; }

    public List<Integer> getIntereses() { return intereses; }
    public void setIntereses(List<Integer> intereses) { this.intereses = intereses; }
}
