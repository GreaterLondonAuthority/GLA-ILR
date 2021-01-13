/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.audit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.london.ilr.init.DataInitialiser.SETUP
import uk.gov.london.ilr.init.DataInitialiser.TEARDOWN
import uk.gov.london.ilr.init.DataInitialiserAction
import uk.gov.london.ilr.init.DataInitialiserModule

@Component
class AuditDataInitialiser(@Autowired val auditService: AuditService): DataInitialiserModule {

    override fun actions(): Array<DataInitialiserAction> {
        return arrayOf(
                DataInitialiserAction("Deleting test audit activities", TEARDOWN, false, Runnable { this.deleteTestAuditActivities() }),
                DataInitialiserAction("Creating test audit activities", SETUP, false, Runnable { this.createTestAuditActivities() })
        )
    }

    private fun deleteTestAuditActivities() {
        auditService.deleteAll()
    }

    private fun createTestAuditActivities() {
        auditService.auditActivityForUser("admin@ilr.com", "Test audit activity.")
    }

}
