package mx.edu.uaa.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.NoResultException;
import mx.edu.uaa.model.Usuario;
import mx.edu.uaa.model.UsuarioAuth;
import mx.edu.uaa.model.UsuarioPerfil;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    // Conexión a la unidad de persistencia en persistence.xml
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("SocialNetPU");

    // =============================================================
    // 1. MÉTODOS DE ESCRITURA Y VALIDACIÓN (Usan la entidad completa)
    // =============================================================

    public List<Usuario> obtenerTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void guardar(Usuario nuevoUsuario) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (nuevoUsuario.getIdUsuario() == null || nuevoUsuario.getIdUsuario() == 0) {
                em.persist(nuevoUsuario);
            } else {
                em.merge(nuevoUsuario);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    public void actualizar(Usuario usuarioEditado) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(usuarioEditado); 
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Usuario u = em.find(Usuario.class, id);
            
            if (u != null) {
                em.remove(u);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().commit();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    public Usuario obtenerPorId(Integer id) {
        if (id == null) return null;
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }
    
    public Usuario obtenerPorCorreo(String correo) {
        if (correo == null) return null;
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE LOWER(u.correo) = LOWER(:correo)", Usuario.class)
                     .setParameter("correo", correo)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null; 
        } finally {
            em.close();
        }
    }

    public Usuario obtenerPorNombre(String nombre) {
        if (nombre == null) return null;
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE LOWER(u.nombre) = LOWER(:nombre)", Usuario.class)
                     .setParameter("nombre", nombre)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    // =============================================================
    // 2. MÉTODOS DE LECTURA RÁPIDA (Usan las vistas fragmentadas)
    // =============================================================

    // --- FUNCIÓN DE LOGIN (Aísla contraseñas) ---
    public UsuarioAuth buscarParaLogin(String correo) {
        EntityManager em = emf.createEntityManager();
        try {
            Object[] row = (Object[]) em.createNativeQuery(
                "SELECT id_usuario, correo, password, correo_validado, es_google FROM vista_usuario_auth WHERE correo = :correo")
                .setParameter("correo", correo)
                .getSingleResult();
            
            return mapearAuth(row);
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    // --- FUNCIÓN DE PERFIL PÚBLICO (Feed y Muro) ---
    public UsuarioPerfil obtenerPerfilPublico(int idUsuario) {
        EntityManager em = emf.createEntityManager();
        try {
            Object[] row = (Object[]) em.createNativeQuery(
                "SELECT id_usuario, nombre, foto_ruta, id_carrera, id_departamento, rol FROM vista_usuario_perfil WHERE id_usuario = :id")
                .setParameter("id", idUsuario)
                .getSingleResult();
            
            return mapearPerfil(row);
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    // --- DIRECTORIO DE ALUMNOS ---
    public List<UsuarioPerfil> obtenerDirectorioAlumnos() {
        EntityManager em = emf.createEntityManager();
        List<UsuarioPerfil> lista = new ArrayList<>();
        try {
            List<Object[]> rows = em.createNativeQuery(
                "SELECT id_usuario, nombre, foto_ruta, id_carrera, id_departamento, rol FROM vista_alumnos")
                .getResultList();
            
            for (Object[] row : rows) {
                lista.add(mapearPerfil(row));
            }
            return lista;
        } finally {
            em.close();
        }
    }

    // --- DIRECTORIO DE PROFESORES ---
    public List<UsuarioPerfil> obtenerDirectorioProfesores() {
        EntityManager em = emf.createEntityManager();
        List<UsuarioPerfil> lista = new ArrayList<>();
        try {
            List<Object[]> rows = em.createNativeQuery(
                "SELECT id_usuario, nombre, foto_ruta, id_carrera, id_departamento, rol FROM vista_profesores")
                .getResultList();
            
            for (Object[] row : rows) {
                lista.add(mapearPerfil(row));
            }
            return lista;
        } finally {
            em.close();
        }
    }

    // =============================================================
    // 3. RUTINAS DE MAPEO SEGURO (Defensa contra Nulls y Casteos)
    // =============================================================

    private UsuarioAuth mapearAuth(Object[] row) {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setIdUsuario((Integer) row[0]);
        auth.setCorreo((String) row[1]);
        auth.setPassword((String) row[2]);
        auth.setCorreoValidado(extraerBooleano(row[3]));
        auth.setEsGoogle(extraerBooleano(row[4]));
        return auth;
    }

    private UsuarioPerfil mapearPerfil(Object[] row) {
        UsuarioPerfil perfil = new UsuarioPerfil();
        perfil.setIdUsuario((Integer) row[0]);
        perfil.setNombre((String) row[1]);
        perfil.setFotoRuta((String) row[2]);
        perfil.setIdCarrera((Integer) row[3]);
        perfil.setIdDepartamento((Integer) row[4]);
        perfil.setRol((String) row[5]);
        return perfil;
    }

    // Evita el clásico error de MySQL donde los BITs a veces llegan como Integer o Byte
    private Boolean extraerBooleano(Object valor) {
        if (valor == null) return false;
        if (valor instanceof Boolean) return (Boolean) valor;
        if (valor instanceof Number) return ((Number) valor).intValue() == 1;
        if (valor instanceof byte[]) return ((byte[]) valor)[0] == 1;
        return false;
    }
    // Método para extraer números de forma segura, sin importar si Mongo los ve como Double o Integer
    private Integer extraerEnteroSeguro(Document doc, String clave) {
        Object valor = doc.get(clave);
        if (valor == null) return null;
        if (valor instanceof Number) {
            return ((Number) valor).intValue();
        }
        return null;
    }
    
}