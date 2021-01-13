/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.file

import org.springframework.data.jpa.repository.JpaRepository

interface FileEntityRepository : JpaRepository<FileEntity, Int> {

    fun findFirstByFileTypeAndFileSuffixAndUkprn(fileType: String, fileSuffix: String, ukprn: Int) : FileEntity?

    fun deleteByDataImportId(dataImportId: Int)

    fun findByDataImportId(dataImportId: Int) : FileEntity?

}

interface FileSummaryRepository : JpaRepository<FileSummary, Int> {

    fun findAllByUkprnIn(ukprns: Set<Int>) : List<FileSummary>

}
