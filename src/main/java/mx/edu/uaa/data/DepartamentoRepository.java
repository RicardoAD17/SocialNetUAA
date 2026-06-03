package mx.edu.uaa.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import mx.edu.uaa.model.Departamento;
import java.util.List;

public class DepartamentoRepository {
    
    // Conexión a la unidad de persistencia definida en persistence.xml
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("SocialNetPU");

    public List<Departamento> obtenerTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT d FROM Departamento d", Departamento.class).getResultList();
        } finally {
            em.close();
        }
    }

    // Filtrado optimizado directamente en la base de datos
    public List<Departamento> obtenerPorCentro(Integer idCentro) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT d FROM Departamento d WHERE d.idCentro = :idCentro", Departamento.class)
                     .setParameter("idCentro", idCentro)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public Departamento guardar(Departamento d) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            // Si el ID es nulo o 0, es un registro nuevo (INSERT)
            if (d.getIdDepartamento() == null || d.getIdDepartamento() == 0) {
                em.persist(d);
            } else {
                // Si ya tiene ID, es una actualización (UPDATE)
                d = em.merge(d);
            }
            
            em.getTransaction().commit();
            return d;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    public Departamento obtenerPorId(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Departamento.class, id);
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Departamento d = em.find(Departamento.class, id);
            
            if (d != null) {
                em.remove(d);
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