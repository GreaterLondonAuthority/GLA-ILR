/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.security;

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
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

        try {
            createUser("", defaultAdminPassword);
        } catch (UnsupportedEncodingException e) {
            log.error("failed to create bootstrap admin user", e);
        }
    }

    void createBootstrapRpUsers() {
        log.debug("Creating bootstrap admin users");

        try {
            createRpUser("", defaultAdminPassword);
        } catch (UnsupportedEncodingException e) {
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
            createOrgAdminUser("", defaultAdminPassword);
        } catch (UnsupportedEncodingException e) {
            log.error("failed to create user", e);
        }
    }

    void createUser(String username, String password) throws UnsupportedEncodingException {
        createUserWithRole(username, password, "ROLE_OPS_ADMIN");
    }

    void createOrgAdminUser(String username, String password) throws UnsupportedEncodingException {
        createUserWithRole(username, password, "ROLE_GLA_ORG_ADMIN");
    }

    private void createUserWithRole(String username, String password, String roleName) throws UnsupportedEncodingException {
        if (userService.exists(username)) {
            BaseOrganisationImpl organisation = new BaseOrganisationImpl();
            organisation.setExternalReference("");

            Role role = new Role(roleName);
            role.setOrganisation(organisation);
            role.setApproved(true);
            User user = new User(username, new String(Base64.getDecoder().decode(password), "UTF-8"));
            user.getRoles().add(role);
            userService.save(user);
        }
    }

    void createRpUser(String username, String password) throws UnsupportedEncodingException {
        if (userService.exists(username)) {
            User user = new User(username, new String(Base64.getDecoder().decode(password), StandardCharsets.UTF_8));
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
