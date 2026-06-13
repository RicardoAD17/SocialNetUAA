package mx.edu.uaa.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "comentarios")
public class ComentarioMongo {

    @Id
    private String id; // El ID alfanumérico propio de MongoDB
    
    private Integer idComentario;
    private Integer idUsuario;
    private Integer idPublicacion;
    private Integer idComentarioPadre;
    private String description; // Atributo mapeado para la descripción del post
    private String descripcion;  // Atributo mapeado para el comentario
    private LocalDateTime fechaComentario; // Usamos LocalDateTime igual que en tu clase MySQL

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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
