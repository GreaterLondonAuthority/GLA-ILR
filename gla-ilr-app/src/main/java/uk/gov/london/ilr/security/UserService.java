/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uk.gov.london.common.organisation.BaseOrganisationImpl;
import uk.gov.london.ilr.environment.Environment;

import java.util.Optional;

import static uk.gov.london.ilr.security.User.SYSTEM_USER;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Environment environment;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("user not found")
        );
        if (environment.isTestEnvironment()) {
            return getInitialisedTestUser(user);
        } else {
            return user;
        }
    }

    public User getCurrentUser() throws UsernameNotFoundException {
        return ((User) getAuthentication().getPrincipal());
    }

    boolean exists(String username) {
        return !userRepository.findByUsername(username).isPresent();
    }

    private User getInitialisedTestUser(User user) {
        Role role = new Role("ROLE_OPS_ADMIN");
        BaseOrganisationImpl organisation = new BaseOrganisationImpl();

        if (user.getUsername().startsWith("rp")) {
            role = new Role("ROLE_ORG_ADMIN");
            organisation.setExternalReference("");
        } else if (user.getUsername().startsWith("org")) {
            role = new Role("ROLE_GLA_ORG_ADMIN");
        }

        role.setOrganisation(organisation);
        user.getRoles().add(role);
        role.setApproved(true);
        return user;
    }

    /**
     * Returns the username of the active user, or null if there is no authenticated user.
     */
    public String currentUserName() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            return null;
        }

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }

        return null;
    }

    private Authentication getAuthentication() {
        // This only exists to support mocking in unit tests
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Saves (creates or updates) a user in the database.
     */
    public void save(User user) {
        userRepository.saveAndFlush(user);
    }

    /**
     * Deletes all the users in the database. TEST ONLY.
     */
    void deleteAllUsers() {
        userRepository.deleteAll();
    }

    /**
     * Creates a disable system user.
     */
    private void createSystemUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            user = Optional.of(new User(username, "-"));
        }
        user.get().setPassword("-");
        userRepository.save(user.get());
    }

    public void createSystemUser() {
        createSystemUser(SYSTEM_USER);
    }

    private static void withLoggedInUser(User user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    public void withLoggedInUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        withLoggedInUser(user.get());
    }

}
