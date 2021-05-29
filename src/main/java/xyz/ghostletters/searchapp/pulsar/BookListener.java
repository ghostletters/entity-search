package xyz.ghostletters.searchapp.pulsar;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.PulsarClientException;
import xyz.ghostletters.searchapp.elasticsearch.BookIndexClient;
import xyz.ghostletters.searchapp.eventchange.BookEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class BookListener {

    private static final String topicName = "persistent://public/default/foobar.public.book";

    @Inject
    ObjectMapper objectMapper;

    @Inject
    BookService bookService;

    MessageListener messageListener = (consumer, msg) -> {
        try {
            // Do something with the message
            String jsonPayload = new String(msg.getData());
            System.out.println("Message received: " + jsonPayload);

            BookEvent bookEvent = objectMapper.readValue(jsonPayload, BookEvent.class);

            bookService.handleBookChange(bookEvent);

            // Acknowledge the message so that it can be deleted by the message broker
            consumer.acknowledge(msg);
        } catch (Exception e) {
            // Message failed to process, redeliver later
            consumer.negativeAcknowledge(msg);
        }
    };

    public void onStart(@Observes StartupEvent event) throws PulsarClientException {
        PulsarSubscriber.initConsumerWithListener(topicName, messageListener);
    }
}
