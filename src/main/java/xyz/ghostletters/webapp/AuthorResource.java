package xyz.ghostletters.webapp;

import xyz.ghostletters.webapp.entity.Author;
import xyz.ghostletters.webapp.entity.Book;
import xyz.ghostletters.webapp.repo.AuthorRepo;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/author")
public class AuthorResource {

    @Inject
    AuthorRepo authorRepo;

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public Author getStevenKing() {
//        Book dark_tower = new Book("Dark Tower", 400);
//        Book it = new Book("It", 366);
//
//        Author steven_king = new Author("Steven King", List.of(dark_tower, it));
//        return steven_king;
//    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Author getAuthor() {
        return authorRepo.findById(3L).get();
    }
}