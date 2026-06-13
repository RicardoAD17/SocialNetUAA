package mx.edu.uaa.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comentario")
    private Integer idComentario;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;      // FK: Quién comentó

    @Column(name = "id_publicacion", nullable = false)
    private Integer idPublicacion;  // FK: En qué publicación

    // Nullable por defecto: Si es null, es un comentario principal. Si tiene valor, es una respuesta.
    @Column(name = "id_comentario_padre")
    private Integer idComentarioPadre; 

    // Usamos TEXT en lugar de VARCHAR estándar para permitir comentarios largos
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "fecha_comentario", nullable = false)
    private LocalDateTime fechaComentario;

    // ==========================================
    // Constructores
    // ==========================================

    public Comentario() {
        this.fechaComentario = LocalDateTime.now();
    }

    // ==========================================
    // Getters y Setters
    // ==========================================

    public Integer getIdComentario() { return idComentario; }
    public void setIdComentario(Integer idComentario) { this.idComentario = idComentario; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdPublicacion() { return idPublicacion; }
    public void setIdPublicacion(Integer idPublicacion) { this.idPublicacion = idPublicacion; }

    public Integer getIdComentarioPadre() { return idComentarioPadre; }
    public void setIdComentarioPadre(Integer idComentarioPadre) { this.idComentarioPadre = idComentarioPadre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaComentario() { return fechaComentario; }
    public void setFechaComentario(LocalDateTime fechaComentario) { this.fechaComentario = fechaComentario; }
}
