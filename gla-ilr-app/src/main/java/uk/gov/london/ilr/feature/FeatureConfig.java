/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.feature;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.spi.FeatureProvider;

/**
 * Configuration of the Togglz feature toggle framework.
 *
 * See also the togglz entries in the application.properties file
 */
@Component
public class FeatureConfig {

    @Bean
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider(IlrFeature.class);
    }

}
