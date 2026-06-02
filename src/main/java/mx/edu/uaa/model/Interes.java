package mx.edu.uaa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "intereses")
public class Interes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interes")
    private Integer idInteres; // Equivale a IdArea

    @Column(nullable = false, length = 100)
    private String nombre;     // Ej: "Ciencia", "Deportes", "Becas"

    // ==========================================
    // Constructores
    // ==========================================

    public Interes() {}

    public Interes(Integer idInteres, String nombre) {
        this.idInteres = idInteres;
        this.nombre = nombre;
    }

    // ==========================================
    // Getters y Setters
    // ==========================================

    public Integer getIdInteres() { return idInteres; }
    public void setIdInteres(Integer idInteres) { this.idInteres = idInteres; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
