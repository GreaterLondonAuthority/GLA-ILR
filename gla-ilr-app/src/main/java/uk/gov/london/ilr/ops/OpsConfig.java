/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.ops;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpsConfig {

    @Value("${ops.api.username}")
    private String opsApiUsername;

    @Value("${ops.api.password}")
    private String opsApiPassword;

    @Bean
    public RestTemplate opsRestTemplate(RestTemplateBuilder builder) {
        return builder.basicAuthentication(opsApiUsername, opsApiPassword).build();
    }

}
