/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.audit

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.london.ilr.environment.Environment
import uk.gov.london.ilr.security.UserService
import javax.transaction.Transactional

private const val MAX_SUMMARY_SIZE = 4000

@Service
@Transactional
class AuditService @Autowired constructor(val userService: UserService,
                                          val auditRepository: AuditRepository,
                                          val environment: Environment) {

    internal var log = LoggerFactory.getLogger(this.javaClass)

    fun findAll(pageable: Pageable): Page<AuditActivity> {
        return auditRepository.findAll(pageable)
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun auditCurrentUserActivity(summary: String) {
        auditActivityForUser(userService.currentUser.username, summary)
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun auditActivityForUser(username: String, summary: String) {
        val auditActivity = AuditActivity(
                username = username,
                timestamp = environment.now(),
                summary = summary.take(MAX_SUMMARY_SIZE)
        )
        auditRepository.save(auditActivity)
    }

    fun deleteAll() {
        if (environment.isTestEnvironment) {
            auditRepository.deleteAll()
        }
        else {
            log.warn("attempted to delete audit activities in a non-test env!")
        }
    }

}
