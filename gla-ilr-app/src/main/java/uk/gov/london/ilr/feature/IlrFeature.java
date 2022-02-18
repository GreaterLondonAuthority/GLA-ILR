/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.feature;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;

/**
 * Feature toggles used by the application.
 */
public enum IlrFeature implements Feature {

    @Label("OPS Connection") OPS_CONNECTION,
    @Label("Learner details page") LEARNER_DETAILS_PAGE,
    @Label("Accessibility URL") ACCESSIBILITY_URL,

}
