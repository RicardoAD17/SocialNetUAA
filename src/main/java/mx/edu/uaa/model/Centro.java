package mx.edu.uaa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "centros")
public class Centro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_centro")
    private Integer idCentro;

    @Column(nullable = false, length = 150)
    private String nombre;

    // ==========================================
    // Constructores
    // ==========================================

    public Centro() {}

    public Centro(Integer idCentro, String nombre) {
        this.idCentro = idCentro;
        this.nombre = nombre;
    }

    // ==========================================
    // Getters y Setters
    // ==========================================

    public Integer getIdCentro() { return idCentro; }
    public void setIdCentro(Integer idCentro) { this.idCentro = idCentro; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
