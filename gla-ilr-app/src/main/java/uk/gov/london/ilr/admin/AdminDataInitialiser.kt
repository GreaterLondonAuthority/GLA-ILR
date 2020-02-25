/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.admin

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.london.ilr.environment.Environment
import uk.gov.london.ilr.init.DataInitialiser.SETUP
import uk.gov.london.ilr.init.DataInitialiserAction
import uk.gov.london.ilr.init.DataInitialiserModule

@Component
class AdminDataInitialiser(@Autowired val messageRepository: MessageRepository,
                           @Autowired val environment: Environment) : DataInitialiserModule {

    internal var log = LoggerFactory.getLogger(this.javaClass)

    override fun actions(): Array<DataInitialiserAction> {
        return arrayOf(
                DataInitialiserAction("Setup System Messages", SETUP, true, Runnable { this.initSystemMessages() })
        )
    }

    private fun initSystemMessages() {
        log.info("Initialising system messages ...")
        initSystemMessage(sgw_system_outage_message_code, "Banner message", "", false)
    }

    private fun initSystemMessage(code: String, codeDisplayName: String, defaultText: String, enabled: Boolean) {
        var message = messageRepository.findById(code).orElse(null)
        if (message == null) {
            message = Message(code = code, codeDisplayName = codeDisplayName, text = defaultText, enabled = enabled)
        }

        if (environment.isTestEnvironment) {
            message.text = defaultText
        }

        messageRepository.save(message)
    }

}
