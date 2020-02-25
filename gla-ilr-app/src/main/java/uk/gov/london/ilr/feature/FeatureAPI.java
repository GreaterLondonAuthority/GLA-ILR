/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.feature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.togglz.core.manager.FeatureManager;

import static uk.gov.london.ilr.feature.IlrFeature.OPS_CONNECTION;

@RestController
@RequestMapping("/api/v1")
public class FeatureAPI {

    @Autowired
    private FeatureManager featureManager;

    @Value("${togglz.features.OPS_CONNECTION.enabled}")
    boolean opsConnectionDefaultStatus;

    @RequestMapping(value = "/features/{feature}", method = RequestMethod.GET)
    public boolean getFeatureStatus(@PathVariable IlrFeature feature) {
        return featureManager.isActive(feature);
    }

    @RequestMapping(value = "/features/{feature}", method = RequestMethod.PUT)
    public void setFeatureStatus(@PathVariable IlrFeature feature, @RequestParam boolean enabled) {
        featureManager.getFeatureState(feature).setEnabled(enabled);
    }

    @RequestMapping(value = "/features/resetDefaults", method = RequestMethod.POST)
    public void resetDefaults() {
        featureManager.getFeatureState(OPS_CONNECTION).setEnabled(opsConnectionDefaultStatus);
    }

}
