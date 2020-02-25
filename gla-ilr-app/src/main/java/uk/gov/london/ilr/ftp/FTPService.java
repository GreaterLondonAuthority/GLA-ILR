/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.ftp;

import com.jcraft.jsch.ChannelSftp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.stereotype.Service;
import uk.gov.london.ilr.encryption.EncryptedTextService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static uk.gov.london.ilr.encryption.EncryptedTextType.SFAFileDecryptionPassword;

@Service
public class FTPService {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private SFASynchroniser sfaSynchroniser;

    @Autowired
    private EncryptedTextService encryptedTextService;

    @Value("${sfa.ftp.hostname}")
    private String hostname;

    @Value("${sfa.ftp.remote.directory}")
    private String remoteDirectory;

    @Value("${sfa.ftp.local.directory}")
    private String localDirectory;

    @Value("${sfa.ftp.username}")
    private String username;

    @Value("${sfa.ftp.password}")
    private String password;

    public FTPConfig getFTPConfig() {
        return new FTPConfig(hostname, remoteDirectory, username, password);
    }

    public void runSFASynchroniser() {
        sfaSynchroniser.sync();
    }

    public List<String> listLocalFiles() {
        return Arrays.asList(new File(localDirectory).list());
    }

    public List<String> listRemoteFiles() throws IOException {
        return listRemoteFiles(hostname, remoteDirectory, username, password);
    }

    public List<String> listRemoteFiles(FTPConfig ftpConfig) throws IOException {
        return listRemoteFiles(ftpConfig.getHost(), ftpConfig.getPath(), ftpConfig.getUsername(), ftpConfig.getPassword());
    }

    public List<String> listRemoteFiles(String hostname, String remoteDirectory, String username, String password) throws IOException {
        Session<ChannelSftp.LsEntry> session = sftpSessionFactory(hostname, username, password).getSession();
        return Arrays.asList(session.listNames(remoteDirectory));
    }

    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory(String host, String username, String password) {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
        factory.setHost(host);
        factory.setPort(22);
        factory.setUser(username);
        factory.setPassword(password);
        factory.setTimeout(50000);
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory);
    }

    public String getFileDecryptionPassword() {
        return encryptedTextService.getText(SFAFileDecryptionPassword);
    }

    public void saveFileDecryptionPassword(String password) {
        encryptedTextService.save(SFAFileDecryptionPassword, password);
    }

}
