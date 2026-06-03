package mx.edu.uaa.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import mx.edu.uaa.model.Publicacion;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PublicacionRepository {

    // ==========================================
    // CONEXIÓN A MONGODB (EL NODO 2)
    // ==========================================
    private static final String URI_MONGODB = "mongodb:// 172.25.219.232:27017"; 
    
    private static final MongoClient mongoClient = MongoClients.create(URI_MONGODB);
    private static final MongoDatabase database = mongoClient.getDatabase("socialnet_db");
    private static final MongoCollection<Document> collection = database.getCollection("publicaciones");

    // Mapper configurado para manejar Fechas y para IGNORAR el "_id" propio de MongoDB
    private static final ObjectMapper jsonMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // --- GUARDAR (CREAR) ---
    public Publicacion guardar(Publicacion nuevaPublicacion) throws Exception {
        
        // 1. Lógica ID Autoincrementable buscando el número mayor directamente en Mongo
        Document maxDoc = collection.find().sort(new Document("idPublicacion", -1)).first();
        int nuevoId = (maxDoc != null) ? maxDoc.getInteger("idPublicacion") + 1 : 1;
        nuevaPublicacion.setIdPublicacion(nuevoId);

        // 2. Convertir Objeto Java -> JSON String -> Documento BSON de Mongo
        String json = jsonMapper.writeValueAsString(nuevaPublicacion);
        Document doc = Document.parse(json);

        // 3. Insertar en MongoDB
        collection.insertOne(doc);
        
        return nuevaPublicacion;
    }

    // --- OBTENER TODAS ---
    public List<Publicacion> obtenerTodas() throws Exception {
        List<Publicacion> lista = new ArrayList<>();
        
        // Recorremos la colección de MongoDB
        for (Document doc : collection.find()) {
            // Documento BSON -> JSON String -> Objeto Java
            Publicacion p = jsonMapper.readValue(doc.toJson(), Publicacion.class);
            lista.add(p);
        }
        return lista;
    }

    // --- OBTENER POR ID ---
    public Publicacion obtenerPorId(int id) throws Exception {
        Document doc = collection.find(Filters.eq("idPublicacion", id)).first();
        if (doc != null) {
            return jsonMapper.readValue(doc.toJson(), Publicacion.class);
        }
        return null;
    }

    // --- OBTENER POR EVENTO (Filtrar en Base de Datos) ---
    public List<Publicacion> obtenerPorEvento(Integer idEvento) throws Exception {
        List<Publicacion> lista = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("idEvento", idEvento))) {
            lista.add(jsonMapper.readValue(doc.toJson(), Publicacion.class));
        }
        return lista;
    }

    // --- OBTENER POR INTERÉS (Filtrar en Base de Datos) ---
    public List<Publicacion> obtenerPorInteres(Integer idInteres) throws Exception {
        List<Publicacion> lista = new ArrayList<>();
        // Magia de MongoDB: Sabe buscar dentro de arreglos automáticamente
        for (Document doc : collection.find(Filters.eq("intereses", idInteres))) {
            lista.add(jsonMapper.readValue(doc.toJson(), Publicacion.class));
        }
        return lista;
    }

    // --- ACTUALIZAR ---
    public void actualizar(Publicacion publicacionEditada) throws Exception {
        // Convertimos la nueva versión a Documento
        String json = jsonMapper.writeValueAsString(publicacionEditada);
        Document doc = Document.parse(json);

        // Reemplazamos el documento en Mongo que coincida con el idPublicacion
        collection.replaceOne(Filters.eq("idPublicacion", publicacionEditada.getIdPublicacion()), doc);
    }

    // --- ELIMINAR ---
    public boolean eliminar(int id) throws Exception {
        // Ejecutamos el DELETE directamente en Mongo
        DeleteResult result = collection.deleteOne(Filters.eq("idPublicacion", id));
        
        // Devuelve true si se eliminó al menos un registro
        return result.getDeletedCount() > 0;
    }
}