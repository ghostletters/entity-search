package xyz.ghostletters.searchapp.pulsar;

import xyz.ghostletters.searchapp.elasticsearch.BookIndexClient;
import xyz.ghostletters.searchapp.eventchange.BookEvent;
import xyz.ghostletters.webapp.entity.Author;
import xyz.ghostletters.webapp.entity.Book;
import xyz.ghostletters.webapp.repository.AuthorRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Optional;

@Transactional
@ApplicationScoped
public class BookService {

    @Inject
    AuthorRepository authorRepository;

    @Inject
    BookIndexClient bookIndexClient;

    public void handleBookChange(BookEvent bookEvent) throws IOException {
        Book book = bookEvent.getAfter();
        System.out.println(book.getTitle());
        System.out.println(book.getPageCount());

        String author = authorRepository.findByBook(book)
                .map(Author::getName)
                .orElse("");


        BookView bookView = new BookView(book.getTitle(), author, book.getPageCount());

        bookIndexClient.index(bookView, book.getId());
    }
}