## Create Quarkus Project

From https://quarkus.io/guides/rest-json

```bash
mvn io.quarkus:quarkus-maven-plugin:1.12.2.Final:create \
    -DprojectGroupId=org.acme \
    -DprojectArtifactId=realworld-fulltext-search \
    -DclassName="PersonResource" \
    -Dpath="/person" 
```

IntelliJ Plugins for Quarkus
- https://plugins.jetbrains.com/plugin/13234-quarkus-tools official RedHat plugin
- https://plugins.jetbrains.com/plugin/14242-quarkus-run-configs very nice for debugging
- https://www.jetbrains.com/help/idea/quarkus.html official JetBrains plugin for Ultimate Version

## Postgresql Database (via Docker Compose)

See official docu at http://postgresguide.com/utilities/psql.html

Login via
```bash
psql -U postgres -h localhost
```
password: `changeme` (defined as environment variable in **docker/docker-compose.yml**)

List tables with `\d`

Show table content with `select * from person;`. Make sure the statement ends with `;`.

## Pulsar (via Docker Compose)
See official docu https://pulsar.apache.org/docs/en/standalone/

### Test general setup
Login to pulsar docker container and create a subscription (and a topic)
```bash
docker exec -it learning_real_world_fulltext_search_pulsar_1 bash
# in docker container
bin/pulsar-client consume my-topic -s "first-subscription"
```

Open a second terminal, login into docker container again. Create a message in the topic
```bash
docker exec -it learning_real_world_fulltext_search_pulsar_1 bash
# in docker container
bin/pulsar-client produce my-topic --messages "hello-pulsar"
```

Should print messages
```bash
# in producer terminal
INFO  org[..]PulsarClientTool - 1 messages successfully produced
# in consumer terminal
INFO  org[..]PulsarClientTool - 1 messages successfully consumed
```

### Create Debezium Source IO
Login to pulsar docker container and create a `source` connector
```bash
docker exec -it learning_real_world_fulltext_search_pulsar_1 bash
bin/pulsar-admin sources create --source-config-file conf/debezium-postgres-source-config.yaml
```

Check the debezium created a new topic and check the `source` config
```bash
bin/pulsar-admin topics list public/default
bin/pulsar-admin source get --tenant public --namespace default --name debezium-postgres-source
bin/pulsar-admin source restart --tenant public --namespace default --name debezium-postgres-source
bin/pulsar-admin persistent subscriptions persistent://public/default/book.public.book
bin/pulsar-admin persistent unsubscribe --force --subscription my-subscription persistent://public/default/book.public.book
```

Subscribe to topic created by debezium
```bash
bin/pulsar-client consume public/default/book.public.book -s "local-debezium-subscription" -n 0
```

Do some update on postgres database
```bash
psql -U postgres -h localhost
update award set name='book';
```

Subscriber prints
```bash
----- got message -----
key:[eyJpZCI6MX0=], properties:[], content:{
  "before":null,
  "after":{"id":1,"name":"bookdwed","person_id":2},
  "source":{
    "version":"1.0.0.Final","connector":"postgresql","name":"foo",
    "ts_ms":1619993589460,"snapshot":"false","db":"postgres","schema":"public",
    "table":"award","txId":589,"lsn":23566248,"xmin":null},
  "op":"u","ts_ms":1619993589464}
```

To fill the `"before"` value increase `REPLICA IDENTITY` in postgres
```bash
ALTER TABLE public.book REPLICA IDENTITY FULL;
```
For details on `REPLICA IDENTITY` see
[Postgres docu](https://www.postgresql.org/docs/current/sql-altertable.html#SQL-CREATETABLE-REPLICA-IDENTITY)
and [Debezium docu](https://debezium.io/documentation/reference/1.0/connectors/postgresql.html#replica-identity)


### Notes

Delete a source (from within pulsar container)
```bash
bin/pulsar-admin source delete --tenant public --namespace default --name debezium-postgres-source
bin/pulsar-admin persistent delete persistent://public/default/debezium-postgres-source-debezium-offset-topic
bin/pulsar-admin persistent delete persistent://public/default/debezium-postgres-topic
bin/pulsar-admin persistent delete persistent://public/default/book.public.book
```

## Elsaticsearch + Kibana

Quarkus advocates against the elastic high level client, because it [pulls in heavy dependencies](https://quarkus.io/guides/elasticsearch#using-the-high-level-rest-client).

Open Kibana at http://localhost:5601/

Kibana asks for you to create a `index pattern`.


## Additional Notes
Had various problems to read `date` data from pulsar json payload into an object
Several steps to fix it:
- avoid table columns just named as lowercase of Java property name
    - instead use snake case via `quarkus.hibernate-orm.physical-naming-strategy=CustomPhysicalNamingStrategy`
- add snakecase to `ObjectMapper` so json from pulsar is correctly transformed to `Award`
    - see `RegisterCustomModuleCustomizer`
- use `@Inject ObjectMapper objectMapper` in `PulsarSubscriber` to create Schema.
    - requires use of unshaded dependency `pulsar-client-original` in `pom.xml`
    - if not, explodes on creating date object from timestamp
    - not sure why it still explodes on `LocalDateTime`, but works with `ZonedDateTime`...
- use `@Inject ObjectMapper objectMapper` to create payload for elasticsearch call


## Elasticsearchs index setup
```
DELETE book

PUT book
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
            "preserve_original" : false
    	  }
    	}
  	}
  },
  "mappings": {
   "properties" : {
      "my_text_field" : {
        "type" : "text",
        "analyzer" : "my_text_field_analyzer"
      }
    }
  }
}

GET book/_analyze
{
  "field" : "name",
  "text": "açaí à la Carte"
}

PUT book/_doc/1
{
  "my_text_field": "açaí à la Carte"
}

GET book/_doc/1

GET book/_search
{
  "query": {
    "match": {
      "my_text_field": {
        "query": "acai"
      }
    }
  }
}

GET book/_search
{
  "query": {
    "match": {
      "my_text_field": {
        "query": "carte"
      }
    }
  }
}

GET book/_search
{
  "query": {
    "match": {
      "my_text_field": {
        "query": "CARTEee",
        "fuzziness": "AUTO"
      }
    }
  }
}
```

## simple query example + asciifolding + copy_to

```
DELETE book

PUT book
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
    "properties": {
      "author": {
        "type": "text",
        "analyzer" : "my_text_field_analyzer",
        "copy_to": "_all_content" 
      },
      "title": {
        "type": "text",
        "analyzer" : "my_text_field_analyzer",
        "copy_to": "_all_content" 
      },
      "_all_content": {
        "type": "text",
        "store": true
      }
    }
  }
}

PUT book/_doc/1
{
  "title": "The King and the Horse",
  "author": "Jon Doe",
  "isbn": "111"
}

PUT book/_doc/2
{
  "title": "The King and the Horse",
  "author": "Steven Açaí",
  "isbn": "222"
}

PUT book/_doc/3
{
  "title": "The King Man",
  "author": "Horse Steven",
  "isbn": "333"
}

PUT book/_doc/4
{
  "author": "Steven King",
  "title": "Dark Tower",
  "isbn": "444"
}

GET book/_search
{
  "stored_fields": ["_all_content"]
}


GET book/_search
{
  "query": {
    "bool": {
      "should": [
        {
          "query_string" : {
            "query": "horse steven king",
            "fields": ["title^2", "author", "_all_content^0.5"],
            "default_operator": "and"
          }
        },
        {
          "match": {
            "isbn": {
              "query": "horse steven king",
              "boost": 5
            }
          }
        },
        {
          "multi_match" : {
            "query": "horse steven king", 
            "fields": [ "title^2", "author" ],
            "fuzziness": "AUTO",
            "operator": "or",
            "boost": 0.4,
            "type": "most_fields"
          }
        }
      ]
    }
  }
}

