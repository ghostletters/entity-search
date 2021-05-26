package xyz.ghostletters.searchapp.pulsar;

import xyz.ghostletters.searchapp.eventchange.BookEvent;
import xyz.ghostletters.webapp.entity.Author;
import xyz.ghostletters.webapp.repo.AuthorRepo;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class BookListenerService {

    @Inject
    AuthorRepo authorRepo;

    public Optional<Author> handleBookChange(BookEvent bookEvent) {
        Optional<Author> optionalAuthor = authorRepo.findAuthorByBook(bookEvent.getAfter());

        optionalAuthor.ifPresent(author -> {
            System.out.println(author.getName());
            System.out.println("Wrote " + author.getBooks().size() + " books.");
        });


        return optionalAuthor;
    }
}
