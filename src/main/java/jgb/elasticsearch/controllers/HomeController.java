package jgb.elasticsearch.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jgb
 * @since 6/15/17 11:49 AM
 */
@RestController
public class HomeController {

    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping("/")
    public String index() {
        LOG.info("Greetings from Spring Boot!");
        return "Greetings from Spring Boot!";
    }

}
