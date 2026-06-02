package mx.edu.uaa.model;

import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Integer idEvento;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(length = 255)
    private String lugar;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "id_creador", nullable = false)
    private Integer idCreador;

    // MAGIA JPA: Crea una tabla para la lista de rutas/nombres de imágenes
    @ElementCollection
    @CollectionTable(
        name = "evento_imagenes", 
        joinColumns = @JoinColumn(name = "id_evento")
    )
    @Column(name = "ruta_imagen")
    private List<String> imagenes;

    @Column(nullable = false)
    private Boolean verificado;

    // ==========================================
    // Constructores
    // ==========================================
    
    public Evento() {
        this.imagenes = new ArrayList<>();
        this.verificado = false;
    }

    public Evento(Integer idEvento, String titulo, String descripcion, String lugar,
                  LocalDate fechaInicio, LocalDate fechaFin,
                  LocalTime horaInicio, LocalTime horaFin, Integer idCreador) {
        this.idEvento = idEvento;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.idCreador = idCreador;
        this.imagenes = new ArrayList<>();
        this.verificado = false;
    }

    // ==========================================
    // Getters y Setters
    // ==========================================

    public Integer getIdEvento() { return idEvento; }
    public void setIdEvento(Integer idEvento) { this.idEvento = idEvento; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public Integer getIdCreador() { return idCreador; }
    public void setIdCreador(Integer idCreador) { this.idCreador = idCreador; }

    public List<String> getImagenes() { return imagenes; }
    public void setImagenes(List<String> imagenes) { this.imagenes = imagenes; }

    public Boolean getVerificado() { return verificado; }
    public void setVerificado(Boolean verificado) { this.verificado = verificado; }
}
