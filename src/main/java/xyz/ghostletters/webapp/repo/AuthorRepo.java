package xyz.ghostletters.webapp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.ghostletters.webapp.entity.Author;
import xyz.ghostletters.webapp.entity.Book;

import java.util.Optional;

public interface AuthorRepo extends JpaRepository<Author, Long> {

    @Query("select a from Author a join a.books b where b = ?1")
    Optional<Author> findAuthorByBook(Book book);
}
