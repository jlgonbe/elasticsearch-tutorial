package jgb.elasticsearch.controllers;

import org.apache.log4j.Logger;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

/**
 * @author jgb
 * @since 6/15/17 12:03 PM
 */
@RestController
@RequestMapping("/elastic")
public class ElasticsearchController {

    private static final Logger LOG = Logger.getLogger(ElasticsearchController.class);
    private static final String INDEX_CATALOG = "catalog";
    private static final String TYPE_BOOKS = "books";
    private static final String TYPE_AUTHORS = "authors";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_CATEGORIES = "categories";
    private static final String FIELD_PUBLISHER = "publisher";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_PUBLISHED_DATE = "published_date";
    private static final String FIELD_ISBN = "isbn";
    private static final String FIELD_RATING = "rating";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_FIRST_NAME = "first_name";
    private static final String FIELD_LAST_NAME = "last_name";
    public static final String FIELD_CATEGORIES_NAME = "categories.name";

    private String isbn = "978-1449358549";

    @Autowired
    private TransportClient client;
    @Value("classpath:elasticsearch/catalog-index.json")
    private Resource index;

    @RequestMapping(value = "/health", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClusterHealthResponse> getClusterHealth() {
        final ClusterHealthResponse response = client
                .admin()
                .cluster()
                .prepareHealth()
                .setWaitForGreenStatus()
                .setTimeout(TimeValue.timeValueSeconds(5))
                .execute()
                .actionGet();

        LOG.info("Cluster health: " + response.getStatus().name());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/index/{indexName}/exists", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IndicesExistsResponse> indexExists(@PathVariable String indexName) {
        final IndicesExistsResponse response = client
                .admin()
                .indices()
                .prepareExists(indexName)
                .get(TimeValue.timeValueMillis(100));

        LOG.info("Index " + indexName + " exists: " + response.isExists());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/index/catalog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateIndexResponse> createIndexCatalog() {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Streams.copy(index.getInputStream(), out);

            final CreateIndexResponse response = client
                    .admin()
                    .indices()
                    .prepareCreate(INDEX_CATALOG)
                    .setSource(out.toByteArray())
                    .setTimeout(TimeValue.timeValueSeconds(1))
                    .get(TimeValue.timeValueSeconds(2));

            LOG.info("Success creating index: " + response.isShardsAcked());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            LOG.error("Exception.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/books/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> getNumberOfBooks() {
        final SearchResponse response = client
                .prepareSearch(INDEX_CATALOG)
                .setTypes(TYPE_BOOKS)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchAllQuery())
                .setFrom(0)
                .setSize(10)
                .setTimeout(TimeValue.timeValueMillis(100))
                .get(TimeValue.timeValueMillis(200));

        LOG.info("Books count: " + response.getHits().getTotalHits());
        return new ResponseEntity<>(response.getHits().getTotalHits(), HttpStatus.OK);
    }

    @RequestMapping(value = "/books/count/rating/{rating}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> countBooksByRating(@PathVariable Float rating) {
        final QueryBuilder query = QueryBuilders
                .boolQuery()
                .must(QueryBuilders
                        .rangeQuery(FIELD_RATING)
                        .gte(rating)
                )
                .must(QueryBuilders
                        .nestedQuery(
                                FIELD_CATEGORIES,
                                QueryBuilders.matchQuery(FIELD_CATEGORIES_NAME, "analytics"),
                                ScoreMode.Total
                        )
                )
                .must(QueryBuilders
                        .hasChildQuery(
                                TYPE_AUTHORS,
                                QueryBuilders.termQuery(FIELD_LAST_NAME, "Gormley"),
                                ScoreMode.Total
                        )
                );

        final SearchResponse response = client
                .prepareSearch(INDEX_CATALOG)
                .setTypes(TYPE_BOOKS)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(query)
                .setFrom(0)
                .setSize(10)
                .setFetchSource(
                        new String[]{FIELD_TITLE, FIELD_PUBLISHER}, /* includes */
                        new String[0] /* excludes */
                )
                .setTimeout(TimeValue.timeValueMillis(100))
                .get(TimeValue.timeValueMillis(200));

        return new ResponseEntity<>(response.getHits().getTotalHits(), HttpStatus.OK);
    }

    @RequestMapping(value = "/books/demo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IndexResponse> createDemoBook() {
        try {
            final XContentBuilder source = JsonXContent
                    .contentBuilder()
                    .startObject()
                    .field(FIELD_TITLE, "Elasticsearch: The Definitive Guide. ...")
                    .startArray(FIELD_CATEGORIES)
                    .startObject().field(FIELD_NAME, "analytics").endObject()
                    .startObject().field(FIELD_NAME, "search").endObject()
                    .startObject().field(FIELD_NAME, "database store").endObject()
                    .endArray()
                    .field(FIELD_PUBLISHER, "O'Reilly")
                    .field(FIELD_DESCRIPTION, "Whether you need full-text search or ...")
                    .field(FIELD_PUBLISHED_DATE, new LocalDate(2015, 2, 7).toDate())
                    .field(FIELD_ISBN, isbn)
                    .field(FIELD_RATING, 4)
                    .endObject();
            client
                    .prepareIndex(INDEX_CATALOG, TYPE_BOOKS)
                    .setId(isbn)
                    .setSource(source)
                    .setOpType(DocWriteRequest.OpType.INDEX)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL)
                    .setTimeout(TimeValue.timeValueMillis(100))
                    .execute(new ActionListener<IndexResponse>() {
                        @Override
                        public void onResponse(IndexResponse indexResponse) {
                            LOG.info("The document has been indexed with the result: " + indexResponse.getResult());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            LOG.error("The document has been not been indexed", e);
                        }
                    });

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            LOG.error("Exception.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/authors/demo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BulkResponse> createDemoAuthors() {
        try {
            final XContentBuilder clintonGormley = JsonXContent
                    .contentBuilder()
                    .startObject()
                    .field(FIELD_FIRST_NAME, "Clinton")
                    .field(FIELD_LAST_NAME, "Gormley")
                    .endObject();

            final XContentBuilder zacharyTong = JsonXContent
                    .contentBuilder()
                    .startObject()
                    .field(FIELD_FIRST_NAME, "Zachary")
                    .field(FIELD_LAST_NAME, "Tong")
                    .endObject();

            final BulkResponse response = client
                    .prepareBulk()
                    .add(Requests
                            .indexRequest(INDEX_CATALOG)
                            .type(TYPE_AUTHORS)
                            .id(UUID.randomUUID().toString())
                            .source(clintonGormley)
                            .parent(isbn)
                            .opType(DocWriteRequest.OpType.INDEX)
                    )
                    .add(Requests
                            .indexRequest(INDEX_CATALOG)
                            .type(TYPE_AUTHORS)
                            .id(UUID.randomUUID().toString())
                            .source(zacharyTong)
                            .parent(isbn)
                            .opType(DocWriteRequest.OpType.INDEX)
                    )
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL)
                    .setTimeout(TimeValue.timeValueMillis(500))
                    .get(TimeValue.timeValueSeconds(1));

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            LOG.error("Exception.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
