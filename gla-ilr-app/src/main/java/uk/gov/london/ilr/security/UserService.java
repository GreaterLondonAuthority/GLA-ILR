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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uk.gov.london.common.organisation.BaseOrganisationImpl;
import uk.gov.london.common.organisation.OrganisationType;

import static uk.gov.london.common.organisation.OrganisationType.LEARNING_PROVIDER;
import static uk.gov.london.common.organisation.OrganisationType.MANAGING_ORGANISATION;

@Service
public class UserService implements UserDetailsService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =  userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("user not found")
        );
        return getInitialisedTestUser(user);
    }

    public User getCurrentUser() throws UsernameNotFoundException {
        return ((User) getAuthentication().getPrincipal());
    }

    public boolean exists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User getInitialisedTestUser(User user) {
        Role role = new Role("ROLE_OPS_ADMIN");
        OrganisationType learningProvider = MANAGING_ORGANISATION;
        BaseOrganisationImpl organisation = new BaseOrganisationImpl();

        if (user.getUsername().startsWith("")) {
            learningProvider = LEARNING_PROVIDER;
            role = new Role("ROLE_ORG_ADMIN");
            organisation.setExternalReference("10000000");
        }
        organisation.setType(learningProvider);
        organisation.setEntityType(organisation.getType().id());
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
            return ((org.springframework.security.core.userdetails.UserDetails)principal).getUsername();
        }

        return null;
    }

    Authentication getAuthentication() {
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

}
