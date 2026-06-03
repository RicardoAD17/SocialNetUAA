package mx.edu.uaa.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonFormat;

// ¡SIN ANOTACIONES JPA! Esta clase viaja directo a MongoDB
public class Publicacion {

    private int idPublicacion;
    private Integer idEvento; 
    private String titulo;
    private Integer idAutor;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;

    private List<Integer> intereses;
    private String description;
    private List<String> imagePaths;
    private List<Integer> idComentarios;

    // ==========================================
    // Constructores
    // ==========================================

    public Publicacion() {
        this.createdAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
        this.imagePaths = new ArrayList<>();
        this.intereses = new ArrayList<>();
        this.idComentarios = new ArrayList<>();
    }

    public Publicacion(int idPublicacion, Integer idAutor, Integer idEvento, String titulo, 
                       String description, List<String> imagePaths,
                       List<Integer> idComentarios, List<Integer> intereses) {
        this.idPublicacion = idPublicacion;
        this.idAutor = idAutor;
        this.idEvento = idEvento;
        this.titulo = titulo;
        this.description = description;
        this.intereses = intereses;
        this.imagePaths = imagePaths;
        this.idComentarios = idComentarios;
        this.createdAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }

    // ==========================================
    // Getters y Setters
    // ==========================================

    public int getIdPublicacion() { return idPublicacion; }
    public void setIdPublicacion(int idPublicacion) { this.idPublicacion = idPublicacion; }

    public Integer getIdEvento() { return idEvento; }
    public void setIdEvento(Integer idEvento) { this.idEvento = idEvento; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Integer getIdAutor() { return idAutor; }
    public void setIdAutor(Integer idAutor) { this.idAutor = idAutor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdateAt() { return updateAt; }
    public void setUpdateAt(LocalDateTime updateAt) { this.updateAt = updateAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Integer> getIntereses() { return intereses; }
    public void setIntereses(List<Integer> intereses) { this.intereses = intereses; }

    public List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }

    public List<Integer> getIdComentarios() { return idComentarios; }
    public void setIdComentarios(List<Integer> idComentarios) { this.idComentarios = idComentarios; }
}