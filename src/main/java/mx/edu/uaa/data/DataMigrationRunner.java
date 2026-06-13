package mx.edu.uaa.data;

import mx.edu.uaa.model.Comentario;
import mx.edu.uaa.model.ComentarioMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataMigrationRunner implements CommandLineRunner {

    @Autowired
    private ComentarioRepository sqlRepository; // Tu repositorio nativo con EntityManager

    @Autowired
    private ComentarioMongoRepository mongoRepository; // Tu repositorio NoSQL

    @Override
    public void run(String... args) throws Exception {
        // Validación para ejecutar la migración únicamente si MongoDB está vacío
        if (mongoRepository.count() == 0) {
            System.out.println(">>> Iniciando migración de comentarios de MySQL a MongoDB...");

            // Usamos tu método nativo personalizado en lugar de findAll()
            List<Comentario> comentariosSQL = sqlRepository.obtenerTodos();

            for (Comentario sqlBuck : comentariosSQL) {
                ComentarioMongo mongoDoc = new ComentarioMongo();
                
                // Mapeo uno a uno respetando tus estructuras exactas
                mongoDoc.setIdComentario(sqlBuck.getIdComentario());
                mongoDoc.setIdPublicacion(sqlBuck.getIdPublicacion());
                mongoDoc.setIdUsuario(sqlBuck.getIdUsuario());
                mongoDoc.setIdComentarioPadre(sqlBuck.getIdComentarioPadre());
                mongoDoc.setDescripcion(sqlBuck.getDescripcion());
                
                // Usamos tu getter nativo de LocalDateTime directamente
                mongoDoc.setFechaComentario(sqlBuck.getFechaComentario());

                mongoRepository.save(mongoDoc);
            }

            System.out.println(">>> ¡Migración completada con éxito! Registros movidos: " + comentariosSQL.size());
        } else {
            System.out.println(">>> MongoDB ya contiene datos de comentarios. Saltando migración.");
        }
    }
}
