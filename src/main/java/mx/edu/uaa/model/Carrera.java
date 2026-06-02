package mx.edu.uaa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "carreras")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Integer idCarrera;

    @Column(name = "id_centro", nullable = false)
    private Integer idCentro; 

    @Column(nullable = false, length = 150)
    private String nombre;

    // ==========================================
    // Constructores
    // ==========================================

    public Carrera() {}

    public Carrera(Integer idCarrera, Integer idCentro, String nombre) {
        this.idCarrera = idCarrera;
        this.idCentro = idCentro;
        this.nombre = nombre;
    }

    // ==========================================
    // Getters y Setters
    // ==========================================

    public Integer getIdCarrera() { return idCarrera; }
    public void setIdCarrera(Integer idCarrera) { this.idCarrera = idCarrera; }
    
    public Integer getIdCentro() { return idCentro; }
    public void setIdCentro(Integer idCentro) { this.idCentro = idCentro; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
