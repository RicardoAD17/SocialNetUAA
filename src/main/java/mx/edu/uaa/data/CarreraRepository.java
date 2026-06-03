package mx.edu.uaa.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import mx.edu.uaa.model.Carrera;
import java.util.List;

public class CarreraRepository {
    
    // Apunta al persistence-unit que creaste en tu persistence.xml
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("SocialNetPU");

    public List<Carrera> obtenerTodas() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Carrera c", Carrera.class).getResultList();
        } finally {
            em.close();
        }
    }
    
    // Obtener carreras filtradas por Centro (Para los selects en cascada)
    public List<Carrera> obtenerPorCentro(Integer idCentro) {
        EntityManager em = emf.createEntityManager();
        try {
            // Usamos JPQL para filtrar directamente en la base de datos
            return em.createQuery("SELECT c FROM Carrera c WHERE c.idCentro = :idCentro", Carrera.class)
                     .setParameter("idCentro", idCentro)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public Carrera guardar(Carrera c) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            // Si el ID es nulo o 0, es un registro nuevo (INSERT)
            if (c.getIdCarrera() == null || c.getIdCarrera() == 0) {
                em.persist(c); 
            } else {
                // Si ya tiene ID, es una actualización (UPDATE)
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

    public Carrera obtenerPorId(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            // JPA busca automáticamente por la Llave Primaria
            return em.find(Carrera.class, id);
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Carrera c = em.find(Carrera.class, id);
            
            if (c != null) {
                em.remove(c); // Lo borra de la BD
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