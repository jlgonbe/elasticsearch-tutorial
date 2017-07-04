package jgb.elasticsearch.main;

import net.minidev.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.jayway.jsonpath.Configuration.defaultConfiguration;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

/**
 * @author jgb
 * @since 6/15/17 2:00 PM
 */
public class ElasticsearchRestApiTest {

    private final RestClient restClient = RestClient
            .builder(new HttpHost("localhost", 9200))
            .setRequestConfigCallback(builder -> builder
                    .setConnectTimeout(1000)
                    .setSocketTimeout(5000))
            .build();

    @Test
    public void testClusterHealthIsGreen() throws Exception {
        final Response response = restClient
                .performRequest(HttpGet.METHOD_NAME, "_cluster/health", emptyMap());

        final LinkedHashMap json = (LinkedHashMap) defaultConfiguration()
                .jsonProvider().parse(EntityUtils.toString(response.getEntity()));

        assertEquals(json.get("status"), "green");
    }

    @Test
    public void testQueryToElastic() throws Exception {
        final Map<String, Object> authors = new LinkedHashMap<>();
        authors.put("type", "authors");
        authors.put("query",
                singletonMap("term",
                        singletonMap("last_name", "Gormley")
                )
        );

        final Map<String, Object> categories = new LinkedHashMap<>();
        categories.put("path", "categories");
        categories.put("query",
                singletonMap("match",
                        singletonMap("categories.name", "search")
                )
        );

        final Map<String, Object> query = new LinkedHashMap<>();
        query.put("size", 10);
        query.put("_source", new String[]{"title", "publisher"});
        query.put("query",
                singletonMap("bool",
                        singletonMap("must", new Map[]{
                                singletonMap("range",
                                        singletonMap("rating",
                                                singletonMap("gte", 4)
                                        )
                                ),
                                singletonMap("has_child", authors),
                                singletonMap("nested", categories)
                        })
                )
        );

        final HttpEntity payload = new NStringEntity(JSONObject.toJSONString(query),
                ContentType.APPLICATION_JSON);

        final Response response = restClient
                .performRequest(HttpPost.METHOD_NAME, "catalog/books/_search",
                        emptyMap(), payload);

        final LinkedHashMap json = (LinkedHashMap) defaultConfiguration()
                .jsonProvider().parse(EntityUtils.toString(response.getEntity()));

        assertEquals(1, ((LinkedHashMap) json.get("hits")).get("total"));
    }
}
