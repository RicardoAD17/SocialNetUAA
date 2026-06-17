package mx.edu.uaa.data;

import mx.edu.uaa.model.ComentarioMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComentarioMongoRepository extends MongoRepository<ComentarioMongo, String> {

    // Buscar todos los comentarios de un evento/publicación
    List<ComentarioMongo> findByIdPublicacion(Integer idPublicacion);

    // Buscar un comentario específico por su ID numérico
    Optional<ComentarioMongo> findByIdComentario(Integer idComentario);

    // Eliminar un comentario por su ID numérico
    void deleteByIdComentario(Integer idComentario);
}