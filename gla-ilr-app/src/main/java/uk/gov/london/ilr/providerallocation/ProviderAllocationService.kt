/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.providerallocation

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.london.common.CSVFile
import uk.gov.london.common.CSVRowSource
import uk.gov.london.common.GlaUtils
import uk.gov.london.ilr.file.DataImport
import uk.gov.london.ilr.file.UKPRN
import uk.gov.london.ilr.file.UploadResult
import java.io.InputStream

const val YEAR = "Academic Year"
const val OPS_PROJECT_TYPE = "OPS Project type"
const val ALLOCATION_TYPE = "Allocation type"
const val FULL_TERM_ALLOCATION = "Full term allocation £"
const val YEARLY_ALLOCATION = "FY allocation £"
const val ALLOCATION_R01 = "YTD allocation @R01 £"
const val ALLOCATION_R02 = "YTD allocation @R02 £"
const val ALLOCATION_R03 = "YTD allocation @R03 £"
const val ALLOCATION_R04 = "YTD allocation @R04 £"
const val ALLOCATION_R05 = "YTD allocation @R05 £"
const val ALLOCATION_R06 = "YTD allocation @R06 £"
const val ALLOCATION_R07 = "YTD allocation @R07 £"
const val ALLOCATION_R08 = "YTD allocation @R08 £"
const val ALLOCATION_R09 = "YTD allocation @R09 £"
const val ALLOCATION_R10 = "YTD allocation @R10 £"
const val ALLOCATION_R11 = "YTD allocation @R11 £"
const val ALLOCATION_R12 = "YTD allocation @R12 £"
const val ALLOCATION_R13 = "YTD allocation @R13 £"
const val ALLOCATION_R14 = "YTD allocation @R14 £"

const val BLANK = "blank"

val PROVIDER_ALLOCATION_DATA_COLUMNS = listOf(
        YEAR,
        UKPRN,
        OPS_PROJECT_TYPE,
        ALLOCATION_TYPE,
        FULL_TERM_ALLOCATION,
        YEARLY_ALLOCATION,
        ALLOCATION_R01,
        ALLOCATION_R02,
        ALLOCATION_R03,
        ALLOCATION_R04,
        ALLOCATION_R05,
        ALLOCATION_R06,
        ALLOCATION_R07,
        ALLOCATION_R08,
        ALLOCATION_R09,
        ALLOCATION_R10,
        ALLOCATION_R11,
        ALLOCATION_R12,
        ALLOCATION_R13,
        ALLOCATION_R14)
        .map { i -> i.toUpperCase() }


@Service
class ProviderAllocationService(val providerAllocationRepository: ProviderAllocationRepository) {

    fun createProviderAllocationRecords(dataImport: DataImport, inputStream: InputStream): UploadResult {
        val uploadResult = UploadResult()
        val csvFile = CSVFile(inputStream)
        createProviderAllocationCsvFile(dataImport, csvFile, uploadResult)
        return uploadResult
    }

    private fun createProviderAllocationCsvFile(dataImport: DataImport, csvFile: CSVFile, uploadResult: UploadResult) {
        validateColumnHeaders(csvFile)
        var allYears = mutableSetOf<Int>()
        while (csvFile.nextRow()) {
            validateProviderAllocationRow(csvFile, uploadResult)
            if (!allYears.contains(GlaUtils.parseYear(csvFile.getString(YEAR)))) {
                providerAllocationRepository.deleteAllByYear(GlaUtils.parseYear(csvFile.getString(YEAR)))
                allYears.add(GlaUtils.parseYear(csvFile.getString(YEAR)))
            }
            providerAllocationRepository.save(createProviderAllocationRecord(csvFile))
        }
    }

    private fun validateColumnHeaders(csvFile: CSVFile) {
        val actualColumns = csvFile.headers.map { h -> h.toUpperCase() }

        if (!actualColumns.containsAll(PROVIDER_ALLOCATION_DATA_COLUMNS)) {
            val missingColumns = HashSet<String>()
            for (column in PROVIDER_ALLOCATION_DATA_COLUMNS) {
                if (!actualColumns.contains(column)) {
                    missingColumns.add(column)
                }
            }
            throw RuntimeException("Invalid column headers, missing $missingColumns.")
        }
    }

    private fun validateProviderAllocationRow(csvRow: CSVFile, uploadResult: UploadResult) {

        if (csvRow.getString(YEAR).isNullOrBlank() ||
                csvRow.getString(UKPRN).isNullOrBlank() ||
                csvRow.getString(OPS_PROJECT_TYPE).isNullOrBlank() ||
                csvRow.getString(ALLOCATION_TYPE).isNullOrBlank()) {
            val errorMessage = "one or more cells in 'Academic year', 'UKPRN', 'OPS Project type', 'Allocation type' column contain(s) no information. " +
                    "Fill in all the required content and try again.";
            uploadResult.errorMessages.add(errorMessage);
            throw RuntimeException(errorMessage);
        }
    }

    private fun createProviderAllocationRecord(csvRow: CSVRowSource): ProviderAllocation {
        return ProviderAllocation(
                ProviderAllocationPK(GlaUtils.parseYear(csvRow.getString(YEAR)), csvRow.getInteger(UKPRN), csvRow.getString(OPS_PROJECT_TYPE), csvRow.getString(ALLOCATION_TYPE)),
                csvRow.getCurrencyValue(FULL_TERM_ALLOCATION),
                csvRow.getCurrencyValue(YEARLY_ALLOCATION),
                csvRow.getCurrencyValue(ALLOCATION_R01),
                csvRow.getCurrencyValue(ALLOCATION_R02),
                csvRow.getCurrencyValue(ALLOCATION_R03),
                csvRow.getCurrencyValue(ALLOCATION_R04),
                csvRow.getCurrencyValue(ALLOCATION_R05),
                csvRow.getCurrencyValue(ALLOCATION_R06),
                csvRow.getCurrencyValue(ALLOCATION_R07),
                csvRow.getCurrencyValue(ALLOCATION_R08),
                csvRow.getCurrencyValue(ALLOCATION_R09),
                csvRow.getCurrencyValue(ALLOCATION_R10),
                csvRow.getCurrencyValue(ALLOCATION_R11),
                csvRow.getCurrencyValue(ALLOCATION_R12),
                csvRow.getCurrencyValue(ALLOCATION_R13),
                csvRow.getCurrencyValue(ALLOCATION_R14))
    }

    fun getProviderAllocations(year: Int?, ukprns: Set<Int?>?, pageable: Pageable): Page<ProviderAllocation> {
        return providerAllocationRepository.findAll(ukprns, year, pageable)
    }

    fun findDistinctAcademicYears(): List<Int> {
        return providerAllocationRepository.findDistinctAcademicYears()
    }
}
