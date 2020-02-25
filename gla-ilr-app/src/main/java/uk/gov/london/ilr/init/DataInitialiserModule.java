/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.init;

/**
 * Interface to be implemented by code that wants to provide DataInitialiser actions.
 */
public interface DataInitialiserModule {

    /**
     * Returns an array of actions to be executed on startup.
     */
    DataInitialiserAction[] actions();

}
