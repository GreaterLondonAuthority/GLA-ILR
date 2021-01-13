/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

@Component
public class KeyDataInfoContributor implements InfoContributor {

    @Autowired
    JdbcTemplate jdbc;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("keyData", getSummaryOfKeyDataEntityCounts());
    }

    /**
     * Returns the summary of key data entity counts to be displayed on dash board page.
     */
    private Map<String, String> getSummaryOfKeyDataEntityCounts() {

        final Map<String, String> keyDataEntityCounts = new TreeMap<>();

        RowCallbackHandler rowCallbackHandler = rs -> keyDataEntityCounts.put(rs.getString("key"), rs.getString("value"));

        jdbc.query("SELECT key, value FROM v_dashboard_key_data_entity_counts", rowCallbackHandler);

        return keyDataEntityCounts;
    }

}
