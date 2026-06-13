package mx.edu.uaa.data;

import mx.edu.uaa.model.ComentarioMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioMongoRepository extends MongoRepository<ComentarioMongo, String> {

    List<ComentarioMongo> findByIdPublicacion(Integer idPublicacion);

}
