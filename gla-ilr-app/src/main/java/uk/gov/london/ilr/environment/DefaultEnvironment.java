/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.environment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.TreeMap;

/**
 * Default implementation of Environment interface.
 *
 * Created by sleach on 19/08/2016.
 */
@Component
public class DefaultEnvironment implements Environment, InfoContributor {

    @Value("${ops.base.url}")
    String opsBaseUrl;

    @Value("${env.shortname}")
    String envShortName = null;

    @Value("${env.fullname}")
    String envFullName = null;

    @Value("${app.release}")
    String appRelease = null;

    @Value("${app.build}")
    String appBuild = null;

    @Value("${testdata.def_pw}")
    String defPwHash;

    @Value("${env.isTestEnvironment}")
    String isTestEnvironment;

    @Value("${cloud.console.logon.url}")
    String cloudConsoleLogonUrl = null;

    String profile = null;

    Clock clock = Clock.system(ZoneId.of("Z"));

    final OffsetDateTime appStartupTime = OffsetDateTime.now();

    @Override
    public String shortName() {
        return envShortName;
    }

    @Override
    public String fullName() {
        return envFullName;
    }

    @Override
    public String releaseNumber() {
        return appRelease;
    }

    @Override
    public String buildNumber() {
        return appBuild;
    }

    @Override
    public String getAppVersionAndBuildNumberElement() {
        return String.format("%s.%s", appRelease, appBuild);
    }

    @Override
    public String profileName() {
        return profile;
    }

    @Override
    public String hostName() {
        return System.getenv("HOSTNAME");
    }

    public Clock clock() {
        return clock;
    }

    @Override
    public boolean isTestEnvironment() {
        return isTestEnvironment != null && isTestEnvironment.equalsIgnoreCase("true");
    }

    @Override
    public String defPwHash() {
        return defPwHash;
    }

    @Override
    public OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    public String getCloudConsoleLogonUrl() {
        return cloudConsoleLogonUrl;
    }

    public OffsetDateTime getAppStartupTime() {
        return appStartupTime;
    }

    public String opsBaseUrl() {
        return opsBaseUrl;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> data = new TreeMap<>();

        data.put("release", releaseNumber());
        data.put("build", buildNumber());
        data.put("environment", fullName());
        data.put("cloud-console-logon-url", getCloudConsoleLogonUrl());
        data.put("app-start-time", getAppStartupTime());
        data.put("server-time", now());

        builder.withDetail("opsApplication", data);
    }

}
