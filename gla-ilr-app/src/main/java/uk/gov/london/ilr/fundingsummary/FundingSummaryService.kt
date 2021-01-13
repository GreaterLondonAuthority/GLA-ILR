/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.fundingsummary

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.london.common.CSVFile
import uk.gov.london.common.CSVRowSource
import uk.gov.london.ilr.file.DataImport
import uk.gov.london.ilr.file.UKPRN
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.ArrayList

@Service
class FundingSummaryService(val fundingSummaryRecordRepository: FundingSummaryRecordRepository) {

    fun getFundingAcademicYears(): List<Int> {
        return fundingSummaryRecordRepository.findDistinctAcademicYears()
    }

    fun getFundingSummaryRecords(ukprns: Set<Int>?, academicYear: Int?, period: Int?, pageable: Pageable): Page<FundingSummaryRecord> {
        return fundingSummaryRecordRepository.findAll(ukprns, academicYear, period, pageable)
    }

    fun createSummaryRecords(dataImport: DataImport, inputStream: InputStream) {
        createSummaryRecords(inputStream, dataImport.academicYear!!, dataImport.period!!)
    }

    fun createSummaryRecords(inputStream: InputStream, academicYear: Int, period: Int) {
        val actualMonth = if (period <= 5) period + 7 else period - 5
        val actualYear = if (period > 5) academicYear + 1 else academicYear
        val actualMonthName = if (period <= 12) getMonthName(actualYear, actualMonth) else null

        val csvFile = CSVFile(inputStream)

        val expectedColumns = mutableSetOf(UKPRN, "Funding Line", "Source", "Category", "Year to date", "Previous collection year to date", "Total")
        if (actualMonthName != null) {
            expectedColumns.add(actualMonthName)
        }

        val actualColumns = csvFile.headers

        validateColumnHeader(expectedColumns, actualColumns)

        fundingSummaryRecordRepository.deleteByAcademicYearAndPeriod(academicYear, period)

        while (csvFile.nextRow()) {
            fundingSummaryRecordRepository.save(createFundingSummaryRecord(csvFile, academicYear, period, actualYear, actualMonth, actualMonthName))
        }
    }

    private fun getMonthName(year: Int, monthName: Int): String {
        return DateTimeFormatter.ofPattern("MMM-yy").format(LocalDate.of(year, monthName, 1))
    }

    fun validateColumnHeader(expectedColumnHeader:Set<String>, csvColumnHeader:Set<String>){
        if (!csvColumnHeader.containsAll(expectedColumnHeader)) {
            val expectedColumns = ArrayList<String>()

            for (column in expectedColumnHeader) {
                if (!csvColumnHeader.contains(column) ) {
                    expectedColumns.add(column)
                }
            }

            val errorMessage = "column $expectedColumns not found in the file. Acceptable column headings are $expectedColumnHeader"

            throw RuntimeException(errorMessage)
        }
    }

    fun createFundingSummaryRecord(csvRow: CSVRowSource, academicYear: Int, period: Int, actualYear: Int, actualMonth: Int, actualMonthName: String?): FundingSummaryRecord {
        return FundingSummaryRecord(
                academicYear,
                period,
                actualYear,
                actualMonth,
                csvRow.getInteger(UKPRN),
                csvRow.getString("Funding Line"),
                csvRow.getString("Source"),
                csvRow.getString("Category"),
                if (actualMonthName != null) csvRow.getCurrencyValue(actualMonthName) else null,
                csvRow.getCurrencyValue("Year to date"))
    }

}
