package jgb.elasticsearch.controllers;

import jgb.elasticsearch.utils.Constants;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

/**
 * @author jgb
 * @since 6/15/17 12:03 PM
 */
@RestController
@RequestMapping("/elastic/authors")
public class ElasticsearchAuthorsController {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchAuthorsController.class);

    @Autowired
    private TransportClient client;

    @RequestMapping(value = "/demo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BulkResponse> createDemoAuthors() {
        try {
            final XContentBuilder clintonGormley = getXContentBuilderAuthor("Clinton", "Gormley");
            final XContentBuilder zacharyTong = getXContentBuilderAuthor("Zachary", "Tong");

            String isbn = "978-1449358549";
            final BulkResponse response = client
                    .prepareBulk()
                    .add(getRequestAuthor(clintonGormley, isbn))
                    .add(getRequestAuthor(zacharyTong, isbn))
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL)
                    .setTimeout(TimeValue.timeValueMillis(500))
                    .get(TimeValue.timeValueSeconds(1));

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            LOG.error("Exception.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private XContentBuilder getXContentBuilderAuthor(String firstName, String lastName) throws IOException {
        return JsonXContent
                .contentBuilder()
                .startObject()
                .field(Constants.Elastic.FIELD_FIRST_NAME, firstName)
                .field(Constants.Elastic.FIELD_LAST_NAME, lastName)
                .endObject();
    }

    private IndexRequest getRequestAuthor(XContentBuilder xContentBuilder, String isbn) {
        return Requests
                .indexRequest(Constants.Elastic.INDEX_CATALOG)
                .type(Constants.Elastic.TYPE_AUTHORS)
                .id(UUID.randomUUID().toString())
                .source(xContentBuilder)
                .parent(isbn)
                .opType(DocWriteRequest.OpType.INDEX);
    }
}
