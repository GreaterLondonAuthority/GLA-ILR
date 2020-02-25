/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import org.springframework.security.core.userdetails.UserDetails;
import uk.gov.london.common.user.BaseUser;

@Entity(name = "users")
public class User extends BaseUser implements UserDetails, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq_gen")
    @SequenceGenerator(name = "users_seq_gen", sequenceName = "users_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column
    private String username;

    @Column
    private String password;

    @Transient
    private Set<Role> roles = new HashSet<>();

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<Role> getAuthorities() {
        return Arrays.asList(new Role("ROLE_ADMIN"));
    }

    public void setAuthorities(Collection<Role> roles) {
        setRoles(new HashSet<>(roles));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    public boolean hasRole(String... rolesToCheck) {
        return roles.stream().map(Role::getName).anyMatch(Arrays.asList(rolesToCheck)::contains);
    }

    public Set<Integer> getUkprns() {
        Set<Integer> ukprns = new HashSet<>();
        roles.stream().forEach(role -> {
            if (role.getOrganisation() != null) {
                if(role.getOrganisation().getExternalReference() != null) {
                    ukprns.add(Integer.parseInt(role.getOrganisation().getExternalReference()));
                }
            }
        });
        return ukprns;
    }
}
