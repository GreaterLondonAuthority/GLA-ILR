/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.security;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uk.gov.london.common.organisation.BaseOrganisationImpl;
import uk.gov.london.ilr.init.DataInitialiserAction;
import uk.gov.london.ilr.init.DataInitialiserModule;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import static uk.gov.london.common.organisation.OrganisationType.MANAGING_ORGANISATION;
import static uk.gov.london.ilr.init.DataInitialiser.SETUP;
import static uk.gov.london.ilr.init.DataInitialiser.TEARDOWN;

/**
 * Initialises user data in the environment.
 *
 * @author Steve Leach
 */
@Component
public class UserDataInitialiser implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${default.admin.password}")
    String defaultAdminPassword;

    @Override
    public DataInitialiserAction[] actions() {
        return new DataInitialiserAction[] {
                new DataInitialiserAction("Setup bootstrap admin users", SETUP, true, this::createBootstrapAdminUsers),
                new DataInitialiserAction("Setup bootstrap rp user", SETUP, false, this::createBootstrapRpUsers),
                new DataInitialiserAction("Delete users", TEARDOWN, false, this::deleteUsers),
                new DataInitialiserAction("Setup users", SETUP, false, this::createUsers),
        };
    }

    void createBootstrapAdminUsers() {
        log.debug("Creating bootstrap admin users");
    }

    void createBootstrapRpUsers() {
        log.debug("Creating bootstrap admin users");
    }

    /**
     * Creates test users.
     */
    void createUsers() {
        log.debug("Creating test users");
    }

    /**
     * Deletes all users in the environment.
     */
    void deleteUsers() {
        log.debug("Deleting test users");
        userService.deleteAllUsers();
    }
}
