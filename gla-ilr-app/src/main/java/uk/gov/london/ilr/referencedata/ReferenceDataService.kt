/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.referencedata

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.london.common.CSVFile
import uk.gov.london.ilr.file.DataImport
import java.io.InputStream
import java.time.LocalDateTime

const val ATTRIBUTE_COLUMN = "ATTRIBUTE"
const val CODE_COLUMN = "CODE"
const val HEADLINE_VALUE_COLUMN = "HEADLINE VALUE"
const val DETAILED_VALUE_COLUMN = "DETAILED VALUE"

val REF_DATA_COLUMNS = listOf(ATTRIBUTE_COLUMN, CODE_COLUMN, HEADLINE_VALUE_COLUMN, DETAILED_VALUE_COLUMN)

@Service
class ReferenceDataService(val referenceDataRepository: ReferenceDataRepository) {

    fun getReferenceDataAcademicYears(): List<Int> {
        return referenceDataRepository.findDistinctAcademicYears()
    }

    fun getReferenceDataAttributes(): List<String> {
        return referenceDataRepository.findDistinctAttributes()
    }

    fun getCodeValues(year: Int?, attribute: String?, pageable: Pageable): Page<RefDataMapping> {
        return referenceDataRepository.findAll(year, attribute, pageable)
    }

    fun createReferenceDataRecords(dataImport: DataImport, inputStream: InputStream) {
        val csvFile = CSVFile(inputStream)
        validateColumnHeaders(csvFile)
        val year = dataImport.academicYear
        val createdBy = dataImport.createdBy
        referenceDataRepository.deleteAllByIdYear(year)
        while (csvFile.nextRow()) {
            processReferenceDataRecord(csvFile, year, createdBy)
        }
    }

    private fun validateColumnHeaders(csvFile: CSVFile) {
        val actualColumns = csvFile.headers.map { h -> h.toUpperCase() }
        if (!actualColumns.containsAll(REF_DATA_COLUMNS)) {
            throw RuntimeException("Invalid column headers, expected $REF_DATA_COLUMNS")
        }
    }

    private fun processReferenceDataRecord(csvRow: CSVFile, year: Int?, createdBy: String?) {
        val attribute = csvRow.getString(ATTRIBUTE_COLUMN)
        val code = csvRow.getString(CODE_COLUMN)
        val headlineValue = csvRow.getStringIfPresent(HEADLINE_VALUE_COLUMN)
        val detailedValue = csvRow.getStringIfPresent(DETAILED_VALUE_COLUMN)
        if (attribute.isNullOrBlank() || headlineValue.isNullOrBlank()) {
            throw RuntimeException("File upload failed as one or more cells in \"Attribute\"/\"Headline Value\" column contain(s) no information. Fill in all the required content and try again.")
        }
        val id = RefDataMappingPK(year, attribute, code)
        referenceDataRepository.save(RefDataMapping(id, headlineValue, detailedValue, LocalDateTime.now(), createdBy))
    }

}
