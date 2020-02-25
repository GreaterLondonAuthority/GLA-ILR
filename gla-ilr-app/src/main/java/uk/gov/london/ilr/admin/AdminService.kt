/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.admin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.boot.actuate.info.InfoEndpoint
import org.springframework.boot.actuate.metrics.MetricsEndpoint
import org.springframework.stereotype.Service
import uk.gov.london.ilr.audit.AuditService
import uk.gov.london.ilr.environment.Environment
import uk.gov.london.ilr.security.UserService
import javax.transaction.Transactional

@Service
@Transactional
class AdminService @Autowired constructor(val userService: UserService,
                                          val messageRepository: MessageRepository,
                                          val environment: Environment,
                                          val contributors: List<InfoContributor>,
                                          val infoEndpoint: InfoEndpoint,
                                          val metricsEndpoint: MetricsEndpoint,
                                          val auditService: AuditService) {

    fun getMessages(): List<Message> {
        return messageRepository.findAll()
    }

    fun getBannerMessage(): Message? {
        return messageRepository.findByCode(sgw_system_outage_message_code)
    }

    fun updateMessage(model: MessageModel) {
        val message = messageRepository.getOne(model.code)
        message.text = model.text
        message.enabled = model.enabled
        message.modifiedBy = userService.currentUserName()
        message.modifiedOn = environment.now()
        messageRepository.save(message)

        auditService.auditCurrentUserActivity("Updated banner message. Code: " + model.code
                                                    + " new text: " + model.text)
    }

    fun getInfoDetails(): Map<String, Any> {

        metricsEndpoint.listNames()

        val builder = Info.Builder()
        for (contributor in contributors) {
            contributor.contribute(builder)
        }

        infoEndpoint.info()["build"]

        builder.withDetail("info", infoEndpoint.info())
        builder.withDetail("sysMetrics", getSysMetrics())

        return builder.build().details
    }

    private fun getSysMetrics(): Map<String, Any> {
        val sysMetrics = mutableMapOf<String, Any>()
        for (metricName in metricsEndpoint.listNames().names) {
            sysMetrics[metricName] = metricsEndpoint.metric(metricName, null).measurements[0].value
        }
        return sysMetrics
    }

}
