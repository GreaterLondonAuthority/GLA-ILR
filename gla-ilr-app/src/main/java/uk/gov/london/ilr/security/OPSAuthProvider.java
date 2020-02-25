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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import uk.gov.london.ilr.ops.OpsService;

import static uk.gov.london.common.organisation.OrganisationType.LEARNING_PROVIDER;

@Component
public class OPSAuthProvider implements AuthenticationProvider {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private OpsService opsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        User user = opsService.authenticate((String) authentication.getPrincipal(), (String) authentication.getCredentials());

        boolean anySkillsUsers = user.getRoles().stream().anyMatch(r -> r.getOrganisation().getEntityType() == LEARNING_PROVIDER.id());
        if (!(user.isGla() || anySkillsUsers)) {
            throw new BadCredentialsException("");
        }

        return new UsernamePasswordAuthenticationToken(user, null, user.getRoles());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
