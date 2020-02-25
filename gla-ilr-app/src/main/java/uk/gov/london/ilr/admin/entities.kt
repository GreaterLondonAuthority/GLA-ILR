package uk.gov.london.ilr.admin

import java.time.OffsetDateTime
import javax.persistence.*

const val sgw_system_outage_message_code = "sgw-system-outage"

@Entity
class Message(

        @Id
        val code: String,

        var codeDisplayName: String,

        var text: String? = null,

        var createdBy: String? = null,

        var createdOn: OffsetDateTime? = null,

        var modifiedBy: String? = null,

        var modifiedOn: OffsetDateTime? = null,

        var enabled: Boolean? = null

)

data class MessageModel(

        val code: String,

        var text: String? = null,

        var enabled: Boolean? = null

)
