/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.ftp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ilr.init.DataInitialiserAction;
import uk.gov.london.ilr.init.DataInitialiserModule;

import static uk.gov.london.ilr.init.DataInitialiser.TEARDOWN;

@Component
public class FTPDataInitialiser implements DataInitialiserModule {

    @Autowired
    private FTPService ftpService;

    @Override
    public DataInitialiserAction[] actions() {
        return new DataInitialiserAction[] {
                new DataInitialiserAction("Initialising test SFA file password ...", TEARDOWN, false, this::initTestSFAFilePassword)
        };
    }

    private void initTestSFAFilePassword() {
        ftpService.saveFileDecryptionPassword("test_sfa_file_password");
    }

}
