/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.ftp;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

public class FTPConfig implements Serializable {

    @NotBlank
    private String host;

    @NotBlank
    private String path;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public FTPConfig() {}

    public FTPConfig(String host, String path, String username, String password) {
        this.host = host;
        this.path = path;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FTPConfig ftpConfig = (FTPConfig) o;
        return Objects.equals(host, ftpConfig.host) &&
                Objects.equals(path, ftpConfig.path) &&
                Objects.equals(username, ftpConfig.username) &&
                Objects.equals(password, ftpConfig.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, path, username, password);
    }

}
