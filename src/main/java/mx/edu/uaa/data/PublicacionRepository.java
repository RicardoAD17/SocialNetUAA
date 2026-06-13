package mx.edu.uaa.data;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import jakarta.validation.constraints.Pattern;
import mx.edu.uaa.model.Publicacion;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.model.Sorts; // Agrega este import
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PublicacionRepository {

    // ==========================================
    // CONEXIÓN A MONGODB (EL NODO 2)
    // ==========================================
    private static final String URI_MONGODB = "mongodb://172.25.219.232:27017";

    private static final MongoClient mongoClient = MongoClients.create(URI_MONGODB);
    private static final MongoDatabase database = mongoClient.getDatabase("socialnet_db");
    private static final MongoCollection<Document> collection = database.getCollection("publicaciones");

    // --- GUARDAR (CREAR) ---
    public Publicacion guardar(Publicacion nuevaPublicacion) throws Exception {

        // 1. Lógica ID Autoincrementable buscando el número mayor directamente en Mongo
        Document maxDoc = collection.find().sort(new Document("idPublicacion", -1)).first();
        int nuevoId = (maxDoc != null) ? maxDoc.getInteger("idPublicacion") + 1 : 1;
        nuevaPublicacion.setIdPublicacion(nuevoId);

        // 2. Armamos el documento BSON manualmente
        Document doc = new Document("idPublicacion", nuevaPublicacion.getIdPublicacion())
                .append("idEvento", nuevaPublicacion.getIdEvento())
                .append("titulo", nuevaPublicacion.getTitulo())
                .append("idAutor", nuevaPublicacion.getIdAutor())
                .append("description", nuevaPublicacion.getDescription())
                .append("intereses", nuevaPublicacion.getIntereses() != null ? nuevaPublicacion.getIntereses() : new ArrayList<>())
                .append("imagePaths", nuevaPublicacion.getImagePaths() != null ? nuevaPublicacion.getImagePaths() : new ArrayList<>())
                .append("idComentarios", nuevaPublicacion.getIdComentarios() != null ? nuevaPublicacion.getIdComentarios() : new ArrayList<>());

        // Guardamos las fechas como objetos Date nativos en Mongo
        if (nuevaPublicacion.getCreatedAt() != null) {
            doc.append("createdAt", Date.from(nuevaPublicacion.getCreatedAt().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (nuevaPublicacion.getUpdateAt() != null) {
            doc.append("updateAt", Date.from(nuevaPublicacion.getUpdateAt().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        // 3. Insertar en MongoDB
        collection.insertOne(doc);
        
        // Recuperar el _id alfanumérico generado
        if (doc.getObjectId("_id") != null) {
            nuevaPublicacion.setId(doc.getObjectId("_id").toHexString());
        }

        return nuevaPublicacion;
    }

    // --- OBTENER TODAS ---
    // Dentro de tu PublicacionRepository.jav

    public List<Publicacion> obtenerTodas() throws Exception {
        List<Publicacion> lista = new ArrayList<>();
        
        // .sort(Sorts.descending("createdAt")) pone lo más nuevo al principio
        for (Document doc : collection.find().sort(Sorts.descending("createdAt"))) {
            lista.add(mapearDocumentoAObjeto(doc));
        }
        return lista;
    }

    // --- OBTENER POR ID ---
    public Publicacion obtenerPorId(int id) throws Exception {
        Document doc = collection.find(Filters.eq("idPublicacion", id)).first();
        return doc != null ? mapearDocumentoAObjeto(doc) : null;
    }

    // --- OBTENER POR EVENTO ---
    public List<Publicacion> obtenerPorEvento(Integer idEvento) throws Exception {
        List<Publicacion> lista = new ArrayList<>();
        for (Document doc : collection.find()) {
            try {
                lista.add(mapearDocumentoAObjeto(doc));
            } catch (Exception e) {
                System.err.println(" Error procesando la publicación en obtenerPorEvento.");
            }
        }
        return lista;
    }

    // --- OBTENER POR INTERÉS ---
    public List<Publicacion> obtenerPorInteres(Integer idInteres) throws Exception {
        List<Publicacion> lista = new ArrayList<>();
        // Magia de MongoDB: Sabe buscar dentro de arreglos automáticamente
        for (Document doc : collection.find()) {
            try {
                lista.add(mapearDocumentoAObjeto(doc));
            } catch (Exception e) {
                System.err.println(" Error procesando la publicación en obtenerPorInteres.");
            }
        }
        return lista;
    }

    // --- ACTUALIZAR ---
    public void actualizar(Publicacion p) throws Exception {
        Document doc = new Document("idPublicacion", p.getIdPublicacion())
                .append("idEvento", p.getIdEvento())
                .append("titulo", p.getTitulo())
                .append("idAutor", p.getIdAutor())
                .append("description", p.getDescription())
                .append("intereses", p.getIntereses() != null ? p.getIntereses() : new ArrayList<>())
                .append("imagePaths", p.getImagePaths() != null ? p.getImagePaths() : new ArrayList<>())
                .append("idComentarios", p.getIdComentarios() != null ? p.getIdComentarios() : new ArrayList<>());

        if (p.getCreatedAt() != null) {
            doc.append("createdAt", Date.from(p.getCreatedAt().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (p.getUpdateAt() != null) {
            doc.append("updateAt", Date.from(p.getUpdateAt().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        // Reemplazamos el documento en Mongo que coincida con el idPublicacion
        collection.replaceOne(Filters.eq("idPublicacion", p.getIdPublicacion()), doc);
    }

    // --- ELIMINAR ---
    public boolean eliminar(int id) throws Exception {
        // Ejecutamos el DELETE directamente en Mongo
        DeleteResult result = collection.deleteOne(Filters.eq("idPublicacion", id));
        // Devuelve true si se eliminó al menos un registro
        return result.getDeletedCount() > 0;
    }

    // ==========================================
    // MÉTODO AUXILIAR: Mapeo Manual Seguro
    // ==========================================
    private Publicacion mapearDocumentoAObjeto(Document doc) {
        Publicacion p = new Publicacion();

        // 1. Extraer ID de Mongo sin importar si es String u ObjectId
        Object rawId = doc.get("_id");
        if (rawId != null) {
            p.setId(rawId.toString());
        }

        // 2. Extraer tu idPublicacion asegurando que no explote si viene nulo
        Integer idPub = doc.getInteger("idPublicacion");
        p.setIdPublicacion(idPub != null ? idPub : 0);

        p.setIdEvento(doc.getInteger("idEvento"));
        p.setTitulo(doc.getString("titulo"));
        p.setIdAutor(doc.getInteger("idAutor"));
        p.setDescription(doc.getString("description"));

        p.setIntereses(obtenerListaEnteros(doc, "intereses"));
        p.setIdComentarios(obtenerListaEnteros(doc, "idComentarios"));

        List<String> images = doc.getList("imagePaths", String.class);
        p.setImagePaths(images != null ? images : new ArrayList<>());

        p.setCreatedAt(extraerFechaSegura(doc, "createdAt"));
        p.setUpdateAt(extraerFechaSegura(doc, "updateAt"));

        return p;
    }

    // Lector súper seguro de arreglos
    private List<Integer> obtenerListaEnteros(Document doc, String clave) {
        List<Integer> lista = new ArrayList<>();
        try {
            List<Number> raw = doc.getList(clave, Number.class);
            if (raw != null) {
                for (Number n : raw) {
                    if (n != null) { // Evita NullPointerException
                        lista.add(n.intValue());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Aviso: El campo " + clave + " no se pudo leer correctamente.");
        }
        return lista;
    }

    // Lector seguro que evita el error de ClassCastException de String a Date
    private java.time.LocalDate extraerFechaSegura(Document doc, String clave) {
        Object valor = doc.get(clave);
        if (valor == null) return null;

        try {
            // Escenario 1: Mongo lo tiene guardado correctamente como Date nativo
            if (valor instanceof Date) {
                return ((Date) valor).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } 
            // Escenario 2: Mongo lo tiene guardado como Texto (El causante del error)
            else if (valor instanceof String) {
                String texto = (String) valor;
                // Tomamos los primeros 10 caracteres (YYYY-MM-DD)
                if (texto.length() >= 10) {
                    return java.time.LocalDate.parse(texto.substring(0, 10));
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudo leer la fecha de " + clave + ": " + valor);
        }
        
        // Fallback de seguridad
        return java.time.LocalDate.now();
    }
    // --- BUSCADOR DE TEXTO EN MONGODB ---
// --- BUSCADOR DE TEXTO EN MONGODB ---
    public List<Publicacion> buscarPorTexto(String terminoBusqueda) throws Exception {
        List<Publicacion> lista = new ArrayList<>();

        // La "i" al final le indica a MongoDB que ignore mayúsculas y minúsculas
        Bson filtro = Filters.or(
                Filters.regex("titulo", terminoBusqueda, "i"),
                Filters.regex("description", terminoBusqueda, "i")
        );

        for (Document doc : collection.find(filtro)) {
            try {
                lista.add(mapearDocumentoAObjeto(doc));
            } catch (Exception e) {
                System.err.println("Error procesando publicación: " + e.getMessage());
            }
        }
        return lista;
    }
}
