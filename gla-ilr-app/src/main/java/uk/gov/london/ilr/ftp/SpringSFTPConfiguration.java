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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

import java.io.File;

@Configuration
@EnableIntegration
public class SpringSFTPConfiguration {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcLockRegistry lockRegistry;

    @Value("${sfa.ftp.hostname}")
    private String hostname;

    @Value("${sfa.ftp.port}")
    private Integer port;

    @Value("${sfa.ftp.username}")
    private String username;

    @Value("${sfa.ftp.password}")
    private String password;

    @Value("${sfa.ftp.remote.directory}")
    private String remoteDirectory;

    @Value("${sfa.ftp.local.directory}")
    private String localDirectory;

    @Value("${sfa.ftp.timeout}")
    private Integer timeout;

    @Value("${sfa.ftp.delete.remote.files}")
    private boolean deleteRemoteFiles;

    @Value("${sfa.ftp.delete.local.files}")
    private boolean deleteLocalFiles;

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
        factory.setHost(hostname);
        factory.setPort(port);
        factory.setUser(username);
        factory.setPassword(password);
        factory.setTimeout(timeout);
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory);
    }

    @Bean(name = "sfaSynchroniser")
    public SFASynchroniser sfaSynchroniser() {
        SFASynchroniser fileSynchroniser = new SFASynchroniser(sftpSessionFactory(), lockRegistry);
        fileSynchroniser.setDeleteRemoteFiles(deleteRemoteFiles);
        fileSynchroniser.setRemoteDirectory(remoteDirectory);
        fileSynchroniser.setLocalDirectory(localDirectory);
        return fileSynchroniser;
    }

    @Bean(name = "MessageSource")
    @InboundChannelAdapter(channel = "sftpChannel", poller = @Poller(fixedRate = "300000", maxMessagesPerPoll = "-1") )
    public MessageSource<File> sftpMessageSource() {
        SftpInboundFileSynchronizingMessageSource source = new SftpInboundFileSynchronizingMessageSource(sfaSynchroniser());
        source.setLocalDirectory(new File(localDirectory));
        source.setAutoCreateLocalDirectory(true);
        source.setLocalFilter(new AcceptOnceFileListFilter<>());
        log.info("sftpMessageSource created");
        return source;
    }

}
