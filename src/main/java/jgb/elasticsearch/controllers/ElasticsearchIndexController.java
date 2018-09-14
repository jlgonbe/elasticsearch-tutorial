package jgb.elasticsearch.controllers;

import jgb.elasticsearch.utils.Constants;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
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

/**
 * @author jgb
 * @since 6/15/17 12:03 PM
 */
@RestController
@RequestMapping("/elastic/index")
public class ElasticsearchIndexController {

    private static final Logger LOG = Logger.getLogger(ElasticsearchIndexController.class);

    @Autowired
    private TransportClient client;
    @Value("classpath:elasticsearch/authors-index.json")
    private Resource authorsIndex;
    @Value("classpath:elasticsearch/books-index.json")
    private Resource booksIndex;

    @RequestMapping(value = "/{indexName}/exists", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IndicesExistsResponse> indexExists(@PathVariable String indexName) {
        final IndicesExistsResponse response = client
                .admin()
                .indices()
                .prepareExists(indexName)
                .get(TimeValue.timeValueMillis(100));

        LOG.info("Index " + indexName + " exists: " + response.isExists());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/authors", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateIndexResponse> createIndexAuthors() {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Streams.copy(authorsIndex.getInputStream(), out);

            final CreateIndexResponse responseAuthorsIndex = client
                    .admin()
                    .indices()
                    .prepareCreate(Constants.Elastic.INDEX_AUTHORS)
                    .setSource(out.toByteArray(), XContentType.JSON)
                    .setTimeout(TimeValue.timeValueSeconds(1))
                    .get(TimeValue.timeValueSeconds(2));

            LOG.info("Success creating authorsIndex: " + responseAuthorsIndex.isAcknowledged());
            return new ResponseEntity<>(responseAuthorsIndex, HttpStatus.CREATED);
        } catch (Exception e) {
            LOG.error("Exception.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/books", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateIndexResponse> createIndexBooks() {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Streams.copy(booksIndex.getInputStream(), out);

            final CreateIndexResponse responseBooksIndex = client
                    .admin()
                    .indices()
                    .prepareCreate(Constants.Elastic.INDEX_BOOKS)
                    .setSource(out.toByteArray(), XContentType.JSON)
                    .setTimeout(TimeValue.timeValueSeconds(1))
                    .get(TimeValue.timeValueSeconds(2));

            LOG.info("Success creating booksIndex: " + responseBooksIndex.isAcknowledged());
            return new ResponseEntity<>(responseBooksIndex, HttpStatus.CREATED);
        } catch (Exception e) {
            LOG.error("Exception.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
