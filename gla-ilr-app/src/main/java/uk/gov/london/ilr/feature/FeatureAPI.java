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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.togglz.core.manager.FeatureManager;

@RestController
@RequestMapping("/api/v1")
public class FeatureAPI {

    private final FeatureManager featureManager;

    @Value("${togglz.features.OPS_CONNECTION.enabled}")
    boolean opsConnectionDefaultStatus;

    public FeatureAPI(FeatureManager featureManager) {
        this.featureManager = featureManager;
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN')")
    @RequestMapping(value = "/features/{feature}", method = RequestMethod.GET)
    public boolean getFeatureStatus(@PathVariable IlrFeature feature) {
        return featureManager.isActive(feature);
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN')")
    @RequestMapping(value = "/features/{feature}", method = RequestMethod.PUT)
    public void setFeatureStatus(@PathVariable IlrFeature feature, @RequestParam boolean enabled) {
        featureManager.getFeatureState(feature).setEnabled(enabled);
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN')")
    @RequestMapping(value = "/features/resetDefaults", method = RequestMethod.POST)
    public void resetDefaults() {
        featureManager.getFeatureState(OPS_CONNECTION).setEnabled(opsConnectionDefaultStatus);
    }

}
