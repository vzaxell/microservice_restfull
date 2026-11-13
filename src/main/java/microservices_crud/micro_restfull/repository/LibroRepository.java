package microservices_crud.micro_restfull.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import microservices_crud.micro_restfull.model.Libro;

@Repository
public interface LibroRepository extends MongoRepository<Libro, String> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, String id);
}
