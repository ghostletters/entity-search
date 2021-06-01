package xyz.ghostletters.webapp;

import xyz.ghostletters.webapp.entity.Author;
import xyz.ghostletters.webapp.entity.Book;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

@Path("/author")
public class AuthorResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Author getStevenKing() {
        Book dark_tower = new Book("Dark Tower", 400);
        Book it = new Book("It", 366);

        Author steven_king = new Author("Steven King", Set.of(dark_tower, it));
        return steven_king;
    }
}