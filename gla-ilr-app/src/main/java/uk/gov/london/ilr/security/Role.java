/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.security;

import org.springframework.security.core.GrantedAuthority;
import uk.gov.london.common.organisation.BaseOrganisationImpl;
import uk.gov.london.common.user.BaseRole;

import javax.persistence.Transient;

public class Role extends BaseRole implements GrantedAuthority {

    private String name;

    @Transient
    private boolean approved;

    @Transient
    private BaseOrganisationImpl organisation;

    public Role() {}

    public Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isApproved() {
        return approved;
    }

    @Override
    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Override
    public BaseOrganisationImpl getOrganisation() {
        return organisation;
    }

    public void setOrganisation(BaseOrganisationImpl organisation) {
        this.organisation = organisation;
    }

    @Override
    public String getAuthority() {
        return name;
    }

}
