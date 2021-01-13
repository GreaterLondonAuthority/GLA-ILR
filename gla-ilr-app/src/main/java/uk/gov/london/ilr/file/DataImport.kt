/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.file

import java.time.OffsetDateTime
import javax.persistence.*

const val LRN = "Learner reference number"
const val UKPRN = "UKPRN"

enum class DataImportStatus {
    PROCESSING, COMPLETE, FAILED
}

@Entity
data class DataImport(

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_import_seq_gen")
        @SequenceGenerator(name = "data_import_seq_gen", sequenceName = "data_import_seq", initialValue = 100, allocationSize = 1)
        var id: Int? = null,

        var fileName: String? = null,

        @Enumerated(EnumType.STRING)
        var status: DataImportStatus? = null,

        @Enumerated(EnumType.STRING)
        var importType: DataImportType? = null,

        var createdOn: OffsetDateTime? = null,

        var createdBy: String? = null,

        var academicYear: Int? = null,

        var period: Int? = null,

        var rowsProcessed: Int? = null,

        var lastExportDate: OffsetDateTime? = null,

        @Transient
        var canPushToOPS: Boolean = false

)
