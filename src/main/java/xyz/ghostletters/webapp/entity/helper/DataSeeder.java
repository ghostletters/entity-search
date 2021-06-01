package xyz.ghostletters.webapp.entity.helper;

import io.quarkus.runtime.StartupEvent;
import xyz.ghostletters.webapp.entity.Author;
import xyz.ghostletters.webapp.entity.Book;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@ApplicationScoped
@Transactional
public class DataSeeder {

    @Inject
    EntityManager entityManager;

    public void onStart(@Observes StartupEvent startupEvent) {
        Book kingAndHorseOne = new Book("The King and the Horse", 111);
        Author jon_doe = new Author("Jon Doe", Set.of(kingAndHorseOne));
        entityManager.persist(jon_doe);

        Book kingAndHorseTwo = new Book("The King and the Horse", 222);
        Author steven_acai = new Author("Steven Açaí", Set.of(kingAndHorseTwo));
        entityManager.persist(steven_acai);

        Book king = new Book("The King Man", 333);
        Author horse_steven = new Author("Horse Steven", Set.of(king));
        entityManager.persist(horse_steven);
    }
}
