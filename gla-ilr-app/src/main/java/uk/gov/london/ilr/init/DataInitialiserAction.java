/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.init;

/**
 * An action to be performed by the DataInitialiser framework on application startup.
 *
 * Actions are created by DataInitialiserModule classes.
 */
public class DataInitialiserAction {

    private final String name;
    private final int order;
    private final boolean runInAllEnvironments;
    private final Runnable action;

    /**
     * Defines a DataInitialiser action.
     *
     * @param name - the name of the action
     * @param order - defines the order in which it should be run
     * @param runInAllEnvironments - whether the action should run in environments without managed test data
     * @param action - the code to be executed
     */
    public DataInitialiserAction(String name, int order, boolean runInAllEnvironments, Runnable action) {
        this.name = name;
        this.order = order;
        this.runInAllEnvironments = runInAllEnvironments;
        this.action = action;
    }

    public int executionOrder() {
        return order;
    }

    public String name() {
        return name;
    }
    
    public boolean runInAllEnvironments() {
        return runInAllEnvironments;
    }

    public void execute() {
        action.run();
    }
}
