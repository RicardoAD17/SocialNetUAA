package mx.edu.uaa.model;
import org.springframework.data.annotation.Id;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

// ¡SIN ANOTACIONES JPA! Esta clase viaja directo a MongoDB
public class Publicacion {
	@Id
	private String id; // <-- CRÍTICO: Debe ser String para que soporte el "$oid" de Mongo
	
	private Integer idPublicacion;
    private Integer idEvento;
    private String titulo;
    private Integer idAutor;

    // Magia: Usamos LocalDate (Solo fecha) y el patrón exacto que dejaste en Mongo
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate updateAt;

    private List<Integer> intereses;
    private String description;
    private List<String> imagePaths;
    private List<Integer> idComentarios;
    private String nombreEvento;

    // Getters y Setters
    public String getNombreEvento() { 
        return nombreEvento; 
    }

    public void setNombreEvento(String nombreEvento) { 
        this.nombreEvento = nombreEvento; 
    }
    // ==========================================
    // Constructores
    // ==========================================

    public Publicacion() {
        // Ahora usamos LocalDate.now()
        this.createdAt = LocalDate.now();
        this.updateAt = LocalDate.now();
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
        this.createdAt = LocalDate.now();
        this.updateAt = LocalDate.now();
    }

    // ==========================================
    // Getters y Setters
    // ==========================================
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
    public int getIdPublicacion() { return idPublicacion; }
    public void setIdPublicacion(int idPublicacion) { this.idPublicacion = idPublicacion; }

    public Integer getIdEvento() { return idEvento; }
    public void setIdEvento(Integer idEvento) { this.idEvento = idEvento; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Integer getIdAutor() { return idAutor; }
    public void setIdAutor(Integer idAutor) { this.idAutor = idAutor; }

    // ¡Asegúrate de que los Getters y Setters también digan LocalDate!
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDate getUpdateAt() { return updateAt; }
    public void setUpdateAt(LocalDate updateAt) { this.updateAt = updateAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Integer> getIntereses() { return intereses; }
    public void setIntereses(List<Integer> intereses) { this.intereses = intereses; }

    public List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }

    public List<Integer> getIdComentarios() { return idComentarios; }
    public void setIdComentarios(List<Integer> idComentarios) { this.idComentarios = idComentarios; }
}
