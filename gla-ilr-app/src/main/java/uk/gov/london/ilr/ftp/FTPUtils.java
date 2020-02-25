/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.ftp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPUtils {

    private static final Logger log = LoggerFactory.getLogger(FTPUtils.class);

    public static List<String> unzip(File file, String password, String destPath) {
        List<String> fileNames = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(file);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
            List<FileHeader> fileHeaderList = zipFile.getFileHeaders();
            for (FileHeader fileHeader : fileHeaderList) {
                fileNames.add(fileHeader.getFileName());

                zipFile.extractFile(fileHeader, destPath);
                log.info("extracted zip file {}", file.getName());
            }
        } catch (Exception e) {
            log.error("failed to extract zip file "+file.getName(), e);
        }

        return fileNames;
    }

}
