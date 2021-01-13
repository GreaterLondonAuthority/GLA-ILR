/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security configuration.
 */
@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsService userService;

    @Autowired
    OPSAuthProvider opsAuthProvider;

    @Autowired
    SGWAuthenticationFailureHandler sgwAuthenticationFailureHandler;

    /**
     * Configure HTTP security.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http
            .authorizeRequests()
                .antMatchers(
                        "/bower_components/**",
                        "/images/**",
                        "/scripts/**",
                        "/styles/**",
                        "/login",
                        "/healthcheck",
                        "/api/v1/features/**",
                        "/swagger-ui.html", "/webjars/**", "/swagger-resources", "/swagger-resources/configuration/ui",
                        "/swagger-resources/configuration/security", "/v2/api-docs").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .failureHandler(sgwAuthenticationFailureHandler)
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login");
    }

    /**
     * Get user account details from the UserService.
     */
    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            //.authenticationProvider(daoAuthenticationProvider())
            .authenticationProvider(opsAuthProvider);
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    /**
     * This will be used to encode passwords, and to check passwords match.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", bcryptPasswordEncoder);

        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("bcrypt", encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(bcryptPasswordEncoder);

        return passwordEncoder;
    }

}
