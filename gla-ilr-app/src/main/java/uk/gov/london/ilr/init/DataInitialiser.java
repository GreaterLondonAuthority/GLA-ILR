/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.init;

import static uk.gov.london.ilr.security.User.SYSTEM_USER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ilr.environment.Environment;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import uk.gov.london.ilr.security.UserService;

/**
 * Framework for applications to initialise their own data.
 */
@Component
public class DataInitialiser {

    Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Default execution order for actions that clean up old data.
     */
    public static final int TEARDOWN = -50;

    /**
     * Default execution order for actions that setup new data.
     */
    public static final int SETUP = 50;

    int actionsExecuted = 0;
    int errors = 0;

    @Autowired
    Environment environment;

    @Autowired
    UserService userService;

    // Spring will autowire in any @Components that implement the DataInitialiserModule interface
    @Autowired
    DataInitialiserModule[] modules;

    List<DataInitialiserAction> actions = new LinkedList<>();

    /**
     * Application startup entry point.
     */
    @PostConstruct
    public void initialiseData() {
        log.info("Starting data initialisation for {} modules...", modules.length);

        createSystemUser();

        buildActionsList();

        executeActions();

        log.info("Data initialisation complete.");
    }

    void executeActions() {
        actionsExecuted = 0;
        errors = 0;

        for (DataInitialiserAction action : actions) {
            if (shouldRunAction(action)) {
                executeAction(action);
            }
        }
    }

    /**
     * Returns true if the current environment should have test data, or if the action should run in all environments.
     */
    boolean shouldRunAction(DataInitialiserAction action) {
        return environment.isTestEnvironment() || action.runInAllEnvironments();
    }

    /**
     * Executes an action, and catches any runtime exceptions it might throw.
     */
    void executeAction(DataInitialiserAction action) {
        try {
            action.execute();
            actionsExecuted++;
        } catch (RuntimeException e) {
            errors++;
            log.error("Error executing data initialiser action " + action.name(), e);
        }
    }

    /**
     * Asks each module for the actions they wish to be executed.
     */
    void buildActionsList() {
        actions.clear();
        for (DataInitialiserModule module : modules) {
            addActionsFromModule(module);
        }
        actions.sort(Comparator.comparingInt(DataInitialiserAction::executionOrder));
    }

    void addActionsFromModule(DataInitialiserModule module) {
        actions.addAll(Arrays.asList(module.actions()));
    }

    /**
     * Creates system user for the data initialiser.
     */
    private void createSystemUser() {
        userService.createSystemUser();
        userService.withLoggedInUser(SYSTEM_USER);
    }
}
