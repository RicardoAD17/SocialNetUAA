package mx.edu.uaa.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.NoResultException;
import mx.edu.uaa.model.Usuario;

import java.util.List;

public class UsuarioRepository {

    // Conexión a la unidad de persistencia en persistence.xml
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("SocialNetPU");

    // --- OBTENER TODOS ---
    public List<Usuario> obtenerTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
        } finally {
            em.close();
        }
    }

    // --- GUARDAR (Crear) ---
    public void guardar(Usuario nuevoUsuario) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            // Si es nulo o 0, es un INSERT
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
    
    // --- ACTUALIZAR ---
    public void actualizar(Usuario usuarioEditado) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(usuarioEditado); // UPDATE automático en MySQL
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

    // --- ELIMINAR ---
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

    // --- BUSCAR POR ID ---
    public Usuario obtenerPorId(Integer id) {
        if (id == null) return null;
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }
    
    // --- BUSCAR POR CORREO ---
    public Usuario obtenerPorCorreo(String correo) {
        if (correo == null) return null;
        EntityManager em = emf.createEntityManager();
        try {
            // Buscamos ignorando mayúsculas/minúsculas directo en la base de datos
            return em.createQuery("SELECT u FROM Usuario u WHERE LOWER(u.correo) = LOWER(:correo)", Usuario.class)
                     .setParameter("correo", correo)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null; // Retorna null si no lo encuentra (igual que el .orElse(null) original)
        } finally {
            em.close();
        }
    }

    // --- BUSCAR POR NOMBRE ---
    public Usuario obtenerPorNombre(String nombre) {
        if (nombre == null) return null;
        EntityManager em = emf.createEntityManager();
        try {
            // Buscamos ignorando mayúsculas/minúsculas
            return em.createQuery("SELECT u FROM Usuario u WHERE LOWER(u.nombre) = LOWER(:nombre)", Usuario.class)
                     .setParameter("nombre", nombre)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}