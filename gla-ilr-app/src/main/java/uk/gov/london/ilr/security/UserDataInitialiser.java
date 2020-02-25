/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.security;

import static uk.gov.london.common.organisation.OrganisationType.MANAGING_ORGANISATION;
import static uk.gov.london.ilr.init.DataInitialiser.SETUP;
import static uk.gov.london.ilr.init.DataInitialiser.TEARDOWN;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uk.gov.london.common.organisation.BaseOrganisationImpl;
import uk.gov.london.ilr.init.DataInitialiserAction;
import uk.gov.london.ilr.init.DataInitialiserModule;

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
                new DataInitialiserAction("Setup bootstrap rp user", SETUP, true, this::createBootstrapRPUsers),
                new DataInitialiserAction("Delete users", TEARDOWN, false, this::deleteUsers),
                new DataInitialiserAction("Setup users", SETUP, false, this::createUsers),
        };
    }

    void createBootstrapAdminUsers() {
        log.debug("Creating bootstrap admin users");

        try {
            createUser("", defaultAdminPassword);
        }
        catch (UnsupportedEncodingException e) {
            log.error("failed to create bootstrap admin user", e);
        }
    }

    void createBootstrapRPUsers() {
        log.debug("Creating bootstrap admin users");

        try {
            createRPUser("", defaultAdminPassword);
        }
        catch (UnsupportedEncodingException e) {
            log.error("failed to create bootstrap rp user", e);
        }
    }

    /**
     * Creates test users.
     */
    void createUsers() {
        log.debug("Creating test users");

        try {
            createUser("", defaultAdminPassword);
        }
        catch (UnsupportedEncodingException e) {
            log.error("failed to create user", e);
        }
    }

    void createUser(String username, String password) throws UnsupportedEncodingException {
        if (!userService.exists(username)) {
            User user = new User(username, new String(Base64.getDecoder().decode(password), "UTF-8"));
            Role role = new Role("ROLE_OPS_ADMIN");
            BaseOrganisationImpl organisation = new BaseOrganisationImpl();
            organisation.setEntityType(MANAGING_ORGANISATION.id());
            organisation.setType(MANAGING_ORGANISATION);
            organisation.setExternalReference("10000000");
            role.setOrganisation(organisation);
            role.setApproved(true);
            user.getRoles().add(role);
            userService.save(user);
        }
    }

    void createRPUser(String username, String password) throws UnsupportedEncodingException {
        if (!userService.exists(username)) {
            User user = new User(username, new String(Base64.getDecoder().decode(password), "UTF-8"));


            userService.save(user);

        }
    }

    /**
     * Deletes all users in the environment.
     */
    void deleteUsers() {
        log.debug("Deleting test users");
        userService.deleteAllUsers();
    }
}
