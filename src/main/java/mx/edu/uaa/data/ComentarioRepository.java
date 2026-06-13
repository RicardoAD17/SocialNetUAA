package mx.edu.uaa.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import mx.edu.uaa.model.Comentario;
import java.util.List;
import org.springframework.stereotype.Repository;
@Repository
public class ComentarioRepository {

    // Conexión a la unidad de persistencia en persistence.xml
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("SocialNetPU");

    // --- GUARDAR (Crear o Actualizar) ---
    public Comentario guardar(Comentario c) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            // Si el ID es nulo o 0, es un registro nuevo (INSERT)
            if (c.getIdComentario() == null || c.getIdComentario() == 0) {
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

    // --- OBTENER TODOS ---
    public List<Comentario> obtenerTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Comentario c", Comentario.class).getResultList();
        } finally {
            em.close();
        }
    }

    // --- OBTENER POR PUBLICACIÓN (Filtrado directamente en la BD) ---
    public List<Comentario> obtenerPorPublicacion(Integer idPublicacion) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Comentario c WHERE c.idPublicacion = :idPub", Comentario.class)
                     .setParameter("idPub", idPublicacion)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    // --- OBTENER POR ID ---
    public Comentario obtenerPorId(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Comentario.class, id);
        } finally {
            em.close();
        }
    }

    // --- ELIMINAR (Desvinculando hijos de forma óptima) ---
    public boolean eliminar(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // PASO 1: Buscar hijos y quitarles la referencia (JPQL Bulk Update)
            // Esto le dice a MySQL que busque todos los comentarios que tengan este padre y los ponga en NULL
            em.createQuery("UPDATE Comentario c SET c.idComentarioPadre = null WHERE c.idComentarioPadre = :padreId")
              .setParameter("padreId", id)
              .executeUpdate();

            // PASO 2: Borrar SOLO el comentario objetivo
            Comentario c = em.find(Comentario.class, id);
            if (c != null) {
                em.remove(c);
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
