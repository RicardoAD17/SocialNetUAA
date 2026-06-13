package mx.edu.uaa.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import mx.edu.uaa.model.Evento;

import java.util.ArrayList;
import java.util.List;

public class EventoRepository {

    // Conexión a la unidad de persistencia en persistence.xml
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("SocialNetPU");

    // --- GUARDAR (CREAR) ---
    public void guardarEvento(Evento nuevoEvento) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            // Si no tiene ID o es 0, es un evento nuevo
            if (nuevoEvento.getIdEvento() == null || nuevoEvento.getIdEvento() == 0) {
                em.persist(nuevoEvento); 
            } else {
                em.merge(nuevoEvento); 
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e; // Lanzamos la excepción para que el Resource devuelva un 500
        } finally {
            em.close();
        }
    }

    // --- OBTENER TODOS ---
    public List<Evento> obtenerTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Evento e", Evento.class).getResultList();
        } finally {
            em.close();
        }
    }

    // --- OBTENER POR ID ---
    public Evento obtenerPorId(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Evento.class, id);
        } finally {
            em.close();
        }
    }

    // --- ELIMINAR ---
    public boolean eliminarEvento(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Evento e = em.find(Evento.class, id);
            if (e != null) {
                em.remove(e);
                em.getTransaction().commit();
                return true;
            }
            
            em.getTransaction().commit();
            return false;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    // --- ACTUALIZAR ---
    public void actualizar(Evento eventoEditado) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            // em.merge toma el objeto modificado y actualiza el registro correspondiente en la BD
            em.merge(eventoEditado); 
            
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
    // --- OBTENER EVENTOS ACTIVOS (Vigentes y Futuros) ---
    public List<Evento> obtenerEventosActivos() {
        EntityManager em = emf.createEntityManager();
        try {
            // Consulta nativa a la vista del fragmento 1
            return em.createNativeQuery("SELECT * FROM fragmento_eventos_activos", Evento.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    // --- OBTENER EVENTOS PASADOS (Archivo Histórico) ---
    public List<Evento> obtenerEventosPasados() {
        EntityManager em = emf.createEntityManager();
        try {
            // Consulta nativa a la vista del fragmento 2
            return em.createNativeQuery("SELECT * FROM fragmento_eventos_pasados", Evento.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
}