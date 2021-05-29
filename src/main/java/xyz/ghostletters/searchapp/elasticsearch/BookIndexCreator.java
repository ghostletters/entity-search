package xyz.ghostletters.searchapp.elasticsearch;

import io.quarkus.runtime.StartupEvent;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;

@ApplicationScoped
public class BookIndexCreator {

    @Inject
    RestClient restClient;

    public void onStart(@Observes StartupEvent startupEvent) throws IOException {
        Request request = new Request("PUT", "/book");

        if (isIndexPresent("/book")) {
            return;
        }

        String payload = """
                    {
                      "settings" : {
                        "analysis" : {
                          "analyzer" : {
                      	    "my_text_field_analyzer" : {
                        		  "tokenizer" : "standard",
                        	    "filter" : [
                        	      "lowercase",
                          		  "my_text_asciifolding_filter"
                        		  ]
                      	    }
                      	  },
                          "filter" : {
                        	  "my_text_asciifolding_filter" : {
                        	    "type" : "asciifolding",
                              "preserve_original" : true
                        	  }
                        	}
                      	}
                      },
                      "mappings": {
                       "properties" : {
                          "title" : {
                            "type" : "text",
                            "analyzer" : "my_text_field_analyzer"
                          },
                          "author" : {
                            "type" : "text",
                            "analyzer" : "my_text_field_analyzer"
                          }
                        }
                      }
                    }""";

        request.setJsonEntity(payload);

        restClient.performRequest(request);
    }

    private boolean isIndexPresent(String indexName) throws IOException {
        return restClient.performRequest(new Request("HEAD", indexName))
                .getStatusLine()
                .getStatusCode() == 200;
    }
}
