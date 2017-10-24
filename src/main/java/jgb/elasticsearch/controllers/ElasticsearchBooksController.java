package jgb.elasticsearch.controllers;

import jgb.elasticsearch.utils.Constants;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author jgb
 * @since 6/15/17 12:03 PM
 */
@RestController
@RequestMapping("/elastic/books")
public class ElasticsearchBooksController {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchBooksController.class);

    @Autowired
    private TransportClient client;

    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> getNumberOfBooks() {
        final SearchResponse response = client
                .prepareSearch(Constants.Elastic.INDEX_CATALOG)
                .setTypes(Constants.Elastic.TYPE_BOOKS)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchAllQuery())
                .setFrom(0)
                .setSize(10)
                .setTimeout(TimeValue.timeValueMillis(100))
                .get(TimeValue.timeValueMillis(200));

        LOG.info("Books count: " + response.getHits().getTotalHits());
        return new ResponseEntity<>(response.getHits().getTotalHits(), HttpStatus.OK);
    }

    @RequestMapping(value = "/count/rating/{rating}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> countBooksByRating(@PathVariable Float rating) {
        final QueryBuilder query = QueryBuilders
                .boolQuery()
                .must(QueryBuilders
                        .rangeQuery(Constants.Elastic.FIELD_RATING)
                        .gte(rating)
                )
                .must(QueryBuilders
                        .nestedQuery(
                                Constants.Elastic.FIELD_CATEGORIES,
                                QueryBuilders.matchQuery(Constants.Elastic.FIELD_CATEGORIES_NAME, "analytics"),
                                ScoreMode.Total
                        )
                )
                .must(QueryBuilders
                        .hasChildQuery(
                                Constants.Elastic.TYPE_AUTHORS,
                                QueryBuilders.termQuery(Constants.Elastic.FIELD_LAST_NAME, "Gormley"),
                                ScoreMode.Total
                        )
                );

        final SearchResponse response = client
                .prepareSearch(Constants.Elastic.INDEX_CATALOG)
                .setTypes(Constants.Elastic.TYPE_BOOKS)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(query)
                .setFrom(0)
                .setSize(10)
                .setFetchSource(
                        new String[]{Constants.Elastic.FIELD_TITLE, Constants.Elastic.FIELD_PUBLISHER}, /* includes */
                        new String[0] /* excludes */
                )
                .setTimeout(TimeValue.timeValueMillis(100))
                .get(TimeValue.timeValueMillis(200));

        return new ResponseEntity<>(response.getHits().getTotalHits(), HttpStatus.OK);
    }

    @RequestMapping(value = "/demo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IndexResponse> createDemoBook() {
        try {
            final String isbn = "978-1449358549";

            final HttpStatus[] finalStatus = new HttpStatus[1];
            final XContentBuilder source = getSource(isbn);

            client
                    .prepareIndex(Constants.Elastic.INDEX_CATALOG, Constants.Elastic.TYPE_BOOKS)
                    .setId(isbn)
                    .setSource(source)
                    .setOpType(DocWriteRequest.OpType.INDEX)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL)
                    .setTimeout(TimeValue.timeValueMillis(100))
                    .execute(new ActionListener<IndexResponse>() {
                        @Override
                        public void onResponse(IndexResponse indexResponse) {
                            LOG.info("The document has been indexed with the result: " + indexResponse.getResult());
                            finalStatus[0] = HttpStatus.CREATED;
                        }

                        @Override
                        public void onFailure(Exception e) {
                            LOG.error("The document has been not been indexed", e);
                            finalStatus[0] = HttpStatus.INTERNAL_SERVER_ERROR;
                        }
                    });

            return new ResponseEntity<>(finalStatus[0]);
        } catch (Exception e) {
            LOG.error("Exception.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private XContentBuilder getSource(String isbn) throws IOException {
        return JsonXContent
                        .contentBuilder()
                        .startObject()
                        .field(Constants.Elastic.FIELD_TITLE, "Elasticsearch: The Definitive Guide. ...")
                        .startArray(Constants.Elastic.FIELD_CATEGORIES)
                            .startObject().field(Constants.Elastic.FIELD_NAME, "analytics").endObject()
                            .startObject().field(Constants.Elastic.FIELD_NAME, "search").endObject()
                            .startObject().field(Constants.Elastic.FIELD_NAME, "database store").endObject()
                        .endArray()
                        .field(Constants.Elastic.FIELD_PUBLISHER, "O'Reilly")
                        .field(Constants.Elastic.FIELD_DESCRIPTION, "Whether you need full-text search or ...")
                        .field(Constants.Elastic.FIELD_PUBLISHED_DATE, new LocalDate(2015, 2, 7).toDate())
                        .field(Constants.Elastic.FIELD_ISBN, isbn)
                        .field(Constants.Elastic.FIELD_RATING, 4)
                        .endObject();
    }
}
