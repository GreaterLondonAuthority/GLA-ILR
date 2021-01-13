/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.file

import javax.persistence.*

/**
 * Full file entity.
 */
const val ERROR_FILE_TYPE = "Error File"


@Entity
@Table(name = "file")
data class FileEntity(


        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_seq_gen")
        @SequenceGenerator(name = "file_seq_gen", sequenceName = "file_seq", initialValue = 100, allocationSize = 1)
        var id: Int? = null,

        /**
         * Reference to the DataImport entry which resulted in this file being stored in the DB.
         */
        var dataImportId: Int? = null,

        /**
         * If the file uploaded was "Data Validation Issues 2019 11.csv" this would be "Data Validation Issues".
         */
        @Column(name = "file_type")
        var fileType: String? = null,

        /**
         * If the file uploaded was "Data Validation Issues 2019 11.csv" this would be "2019 11".
         */
        @Column(name = "file_suffix")
        var fileSuffix: String? = null,

        /**
         * Optional, for cases where the file contains data for a specific learning provider.
         */
        @Column(name = "ukprn")
        var ukprn: Int? = null,

        @Column(name = "file_content")
        var content: String

)

/**
 * Summarised representation of the file entity without the content for fast loading meta data / displaying in list.
 */
@Entity
@Table(name = "file")
data class FileSummary(

        @Id
        var id: Int? = null,

        @Column(name = "file_type")
        var fileType: String? = null,

        @Column(name = "file_suffix")
        var fileSuffix: String? = null,

        @Column(name = "ukprn")
        var ukprn: Int? = null

)
