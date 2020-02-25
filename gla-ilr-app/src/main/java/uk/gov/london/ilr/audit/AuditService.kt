package uk.gov.london.ilr.audit

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.london.ilr.environment.Environment
import uk.gov.london.ilr.security.UserService
import javax.transaction.Transactional

@Service
@Transactional
class AuditService @Autowired constructor(val userService: UserService,
                                          val environment: Environment) {

    internal var log = LoggerFactory.getLogger(this.javaClass)

    fun auditCurrentUserActivity(summary: String) {
        log.info("AUDIT_SGW: " + userService.currentUserName() + " at " + environment.now() + " " + summary)
    }

}