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
    BookIndexClient bookIndexClient;

    @Inject
    BookListenerService bookListenerService;

    @Inject
    PulsarSubscriber pulsarSubscriber;

    MessageListener messageListener = (consumer, msg) -> {
        try {
            // Do something with the message
            String jsonPayload = new String(msg.getData());
            System.out.println("Message received: " + jsonPayload);

            BookEvent bookEvent = objectMapper.readValue(jsonPayload, BookEvent.class);
            System.out.println(bookEvent.getAfter().getName());

            bookListenerService.handleBookChange(bookEvent);

            bookIndexClient.index(bookEvent.getAfter());

            // Acknowledge the message so that it can be deleted by the message broker
            consumer.acknowledge(msg);
        } catch (Exception e) {
            // Message failed to process, redeliver later
            consumer.negativeAcknowledge(msg);
            throw new RuntimeException(e);
        }
    };

    public void onStart(@Observes StartupEvent event) throws PulsarClientException {
        pulsarSubscriber.initConsumerWithListener(topicName, messageListener);
    }
}
