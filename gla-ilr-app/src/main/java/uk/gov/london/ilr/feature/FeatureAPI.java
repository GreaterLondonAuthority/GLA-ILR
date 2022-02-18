/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.feature;

import static uk.gov.london.ilr.feature.IlrFeature.OPS_CONNECTION;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.togglz.core.manager.FeatureManager;
import uk.gov.london.ilr.environment.Environment;

@RestController
@RequestMapping("/api/v1")
public class FeatureAPI {

    private final FeatureManager featureManager;
    private final Environment environment;

    @Value("${togglz.features.OPS_CONNECTION.enabled}")
    boolean opsConnectionDefaultStatus;

    public FeatureAPI(FeatureManager featureManager, Environment environment) {
        this.featureManager = featureManager;
        this.environment = environment;
    }

    @RequestMapping(value = "/features/{feature}", method = RequestMethod.GET)
    public boolean getFeatureStatus(@PathVariable IlrFeature feature) {
        validateInTestEnvironment();
        return featureManager.isActive(feature);
    }

    @RequestMapping(value = "/features/{feature}", method = RequestMethod.PUT)
    public void setFeatureStatus(@PathVariable IlrFeature feature, @RequestParam boolean enabled) {
        validateInTestEnvironment();
        featureManager.getFeatureState(feature).setEnabled(enabled);
    }

    @RequestMapping(value = "/features/resetDefaults", method = RequestMethod.POST)
    public void resetDefaults() {
        validateInTestEnvironment();
        featureManager.getFeatureState(OPS_CONNECTION).setEnabled(opsConnectionDefaultStatus);
    }

    void validateInTestEnvironment() {
        if (!environment.isTestEnvironment()) {
            throw new RuntimeException("API available in test environment only!");
        }
    }

}
