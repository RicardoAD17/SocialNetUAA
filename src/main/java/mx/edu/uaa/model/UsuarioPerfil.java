package mx.edu.uaa.model;

public class UsuarioPerfil {
    private Integer idUsuario;
    private String nombre;
    private String fotoRuta;
    private Integer idCarrera;
    private Integer idDepartamento;
    private String rol;
    public Integer getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getFotoRuta() {
        return fotoRuta;
    }
    public void setFotoRuta(String fotoRuta) {
        this.fotoRuta = fotoRuta;
    }
    public Integer getIdCarrera() {
        return idCarrera;
    }
    public void setIdCarrera(Integer idCarrera) {
        this.idCarrera = idCarrera;
    }
    public Integer getIdDepartamento() {
        return idDepartamento;
    }
    public void setIdDepartamento(Integer idDepartamento) {
        this.idDepartamento = idDepartamento;
    }
    public String getRol() {
        return rol;
    }
    public void setRol(String rol) {
        this.rol = rol;
    }

    
}