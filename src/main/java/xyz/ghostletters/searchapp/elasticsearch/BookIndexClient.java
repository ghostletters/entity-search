package xyz.ghostletters.searchapp.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import xyz.ghostletters.searchapp.pulsar.BookView;
import xyz.ghostletters.webapp.entity.Book;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;

@ApplicationScoped
public class BookIndexClient {

    @Inject
    RestClient restClient;

    @Inject
    ObjectMapper objectMapper;
    public void index(BookView book, Long bookId) throws IOException {
        Request request = new Request("PUT", "/book/_doc/" + bookId);

        String valueAsString = objectMapper.writeValueAsString(book);

        String payload = """
                %s
                """.formatted(valueAsString);

        request.setJsonEntity(payload);

        restClient.performRequest(request);
    }
}
