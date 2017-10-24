package jgb.elasticsearch.controllers;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jgb
 * @since 6/15/17 12:03 PM
 */
@RestController
@RequestMapping("/elastic/health")
public class ElasticsearchHealthController {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchHealthController.class);

    @Autowired
    private TransportClient client;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
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
}
