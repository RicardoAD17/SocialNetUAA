package mx.edu.uaa.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import mx.edu.uaa.model.Interes;

import java.util.ArrayList;
import java.util.List;

public class InteresRepository {
    
    // Conexión a la unidad de persistencia en persistence.xml
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("SocialNetPU");

    public List<Interes> obtenerTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT i FROM Interes i", Interes.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Interes guardar(Interes interes) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            // Si el ID es nulo o 0, insertamos un registro nuevo
            if (interes.getIdInteres() == null || interes.getIdInteres() == 0) {
                em.persist(interes);
            } else {
                // Si ya existe, lo actualizamos
                interes = em.merge(interes);
            }
            
            em.getTransaction().commit();
            return interes;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    
    // Método optimizado para obtener los intereses dado una lista de IDs
    public List<Interes> obtenerPorListaIds(List<Integer> idsBuscados) {
        // Validación de seguridad para no mandar listas vacías a la BD
        if (idsBuscados == null || idsBuscados.isEmpty()) {
            return new ArrayList<>();
        }
        
        EntityManager em = emf.createEntityManager();
        try {
            // Utilizamos la cláusula IN de JPQL
            return em.createQuery("SELECT i FROM Interes i WHERE i.idInteres IN :ids", Interes.class)
                     .setParameter("ids", idsBuscados)
                     .getResultList();
        } finally {
            em.close();
        }
    }
}