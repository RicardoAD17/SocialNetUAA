package mx.edu.uaa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "departamentos")
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departamento")
    private Integer idDepartamento;

    // Llave foránea hacia la tabla centros
    @Column(name = "id_centro", nullable = false)
    private Integer idCentro; 

    @Column(nullable = false, length = 150)
    private String nombre;

    // ==========================================
    // Constructores
    // ==========================================

    public Departamento() {}

    public Departamento(Integer idDepartamento, Integer idCentro, String nombre) {
        this.idDepartamento = idDepartamento;
        this.idCentro = idCentro;
        this.nombre = nombre;
    }

    // ==========================================
    // Getters y Setters
    // ==========================================

    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }
    
    public Integer getIdCentro() { return idCentro; }
    public void setIdCentro(Integer idCentro) { this.idCentro = idCentro; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
