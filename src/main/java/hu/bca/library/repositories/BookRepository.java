package hu.bca.library.repositories;

import hu.bca.library.models.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends CrudRepository<Book, Long> {

    @Query(value = "SELECT * FROM Book where year is NULL", nativeQuery = true)
    List<Book> findAllWhereYearIsNull();
}
