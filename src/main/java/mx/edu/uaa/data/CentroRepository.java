package mx.edu.uaa.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import mx.edu.uaa.model.Centro;
import java.util.List;

public class CentroRepository {
    
    // Conexión a la unidad de persistencia definida en persistence.xml
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("SocialNetPU");

    public List<Centro> obtenerTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            // JPQL para traer todos los centros
            return em.createQuery("SELECT c FROM Centro c", Centro.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Centro guardar(Centro c) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            // Si no tiene ID o es 0, es un nuevo registro (INSERT)
            if (c.getIdCentro() == null || c.getIdCentro() == 0) {
                em.persist(c);
            } else {
                // Si ya tiene ID, lo actualiza (UPDATE)
                c = em.merge(c);
            }
            
            em.getTransaction().commit();
            return c;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    public Centro obtenerPorId(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            // find() busca automáticamente por la llave primaria en la tabla
            return em.find(Centro.class, id);
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            // Primero buscamos si el centro existe
            Centro c = em.find(Centro.class, id);
            
            if (c != null) {
                em.remove(c); // Ejecuta el DELETE en MySQL
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
}