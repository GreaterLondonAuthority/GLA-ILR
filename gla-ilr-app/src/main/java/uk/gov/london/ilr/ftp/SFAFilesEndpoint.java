/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.ftp;

import static uk.gov.london.ilr.ftp.FTPUtils.unzip;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.london.ilr.data.DataImportType;
import uk.gov.london.ilr.data.IlrDataService;

@MessageEndpoint
public class SFAFilesEndpoint {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private IlrDataService ilrDataService;

    @Autowired
    private FTPService ftpService;

    @Value("${sfa.ftp.local.directory}")
    private String localDirectory;

    @Value("${sfa.ftp.delete.local.files}")
    private boolean deleteLocalFiles;

    @ServiceActivator(inputChannel = "sftpChannel")
    public String process(File file) throws Exception {
        log.info("processing file {}", file.getName());

        if (file.getName().toLowerCase().endsWith(".zip")) {
            String password = ftpService.getFileDecryptionPassword();

            List<String> files = unzip(file, password, localDirectory);
            for (String fileName : files) {
              if(isValidFileType(fileName)) {
                ilrDataService.upload(fileName, new FileInputStream(file));
                log.info("finished processing unzipped file {}", file.getName());
              }
            }
        }

        if (deleteLocalFiles) {
            if (file.delete()) {
                log.debug("Successfully deleted file {}", file.getName());
            }
            else {
                log.warn("Failed to delete file {} after being processed!", file.getName());
            }
        }

        return null;
    }

  public boolean isValidFileType(String fileName) {
    return DataImportType.getTypeByFilename(fileName) != null;
  }

}
