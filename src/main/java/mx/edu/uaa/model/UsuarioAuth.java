package mx.edu.uaa.model;
public class UsuarioAuth {
    private Integer idUsuario;
    public Integer getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    private String correo;
    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    private String password;
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    private Boolean correoValidado;
    public Boolean getCorreoValidado() {
        return correoValidado;
    }
    public void setCorreoValidado(Boolean correoValidado) {
        this.correoValidado = correoValidado;
    }
    private Boolean esGoogle;
    public void setEsGoogle(Boolean esGoogle) {
        this.esGoogle = esGoogle;
    }
    public Boolean getEsGoogle() {
        return esGoogle;
    }

   
}