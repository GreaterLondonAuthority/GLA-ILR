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
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.togglz.core.manager.FeatureManager;

import java.io.File;
import java.util.concurrent.locks.Lock;

import static uk.gov.london.ilr.feature.IlrFeature.SFA_FTP_SYNC;

public class SFASynchroniser extends SftpInboundFileSynchronizer {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcLockRegistry lockRegistry;

    @Autowired
    private FeatureManager features;

    @Value("${sfa.ftp.error-logging-interval}")
    private Integer errorLoggingInterval = 30;

    private final SessionFactory<ChannelSftp.LsEntry> sessionFactory;

    private String remoteDirectory = null;
    private String localDirectory = null;
    private int syncCount = 0;

    public SFASynchroniser(SessionFactory<ChannelSftp.LsEntry> sessionFactory, JdbcLockRegistry lockRegistry) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
        this.lockRegistry = lockRegistry;
    }

    public String getRemoteDirectory() {
        return this.remoteDirectory;
    }

    @Override
    public void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = remoteDirectory;
        super.setRemoteDirectory(remoteDirectory);
    }

    public String getLocalDirectory() {
        return this.localDirectory;
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    /**
     * Returns the number of times the synchronisation has occurred.
     */
    public int getSyncCount() {
        return syncCount;
    }

    private int localFileCount() {
        File[] files = new File(localDirectory).listFiles();
        return files != null ? files.length : -1;
    }

    @Override
    public void synchronizeToLocalDirectory(File localDirectory) {
        synchronizeToLocalDirectory(localDirectory, Integer.MIN_VALUE);
    }

    @Override
    public void synchronizeToLocalDirectory(final File localDirectory, final int maxFetchSize) {
        if (!features.isActive(SFA_FTP_SYNC)) {
            log.info("SFA sync is disabled");
        } else {
            Lock lock = lockRegistry.obtain("SFA_FTP_SYNC");
            try {
                if (lock != null && lock.tryLock()) {
                    log.debug("Synchronising...");
                    super.synchronizeToLocalDirectory(localDirectory, maxFetchSize);
                    syncCount++;
                    log.info("Local files = {}", localFileCount());
                    log.debug("Synchronisation complete");
                } else {
                    log.info("Could not acquire lock");
                }
            } catch (Exception e) {
                log.error("Error synchronising SFA files", e);
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * Initiates an immediate sync, as long as the synchronizer is not paused.
     */
    public void sync() {
        this.synchronizeToLocalDirectory( new File(localDirectory) );
    }

}
