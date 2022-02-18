/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.file

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.london.common.CSVFile
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.lang.StringBuilder

@Service
class SplitByUkprnCsvImporter @Autowired constructor(val fileService: FileService) {

    internal var log = LoggerFactory.getLogger(javaClass)

    fun createSplitByUKPRNFile(dataImport: DataImport, inputStream: InputStream) {
        val csvFile = CSVFile(inputStream)
        createSplitByUKPRNFile(dataImport, csvFile)
    }

    fun createSplitByUKPRNFile(dataImport: DataImport, csvFile: CSVFile) {
        validateCSVFile(csvFile)
        val contentSplitByUKPRN = splitContentPerUKPRN(csvFile)
        saveSplitByUKPRNContent(dataImport, contentSplitByUKPRN)
    }

    private fun validateCSVFile(csvFile: CSVFile) {
        if (!csvFile.headers.contains(UKPRN)) {
            throw IllegalArgumentException("no $UKPRN column found in CSV file!")
        }
    }

    private fun splitContentPerUKPRN(csvFile: CSVFile) : Map<Int, StringBuilder> {
        val contentSplitByUKPRN = mutableMapOf<Int, StringBuilder>()

        while (csvFile.nextRow()) {
            processSplitFile(csvFile, contentSplitByUKPRN)
        }

        return contentSplitByUKPRN
    }

    private fun processSplitFile(csvFile: CSVFile, contentSplitByUKPRN: MutableMap<Int, StringBuilder>) {
        val ukprn = csvFile.getIntegerOrNull(UKPRN)
        if (ukprn != null) {
            appendRowToContent(ukprn, csvFile, contentSplitByUKPRN)
        }
        else {
            log.warn("$UKPRN value {} invalid at row {}!", csvFile.getString(UKPRN), csvFile.rowIndex)
        }
    }

    private fun appendRowToContent(ukprn: Int, csvFile: CSVFile, contentSplitByUKPRN: MutableMap<Int, StringBuilder>) {
        if (contentSplitByUKPRN[ukprn] == null) {
            contentSplitByUKPRN[ukprn] = StringBuilder(csvFile.headers.joinToString(",")).appendln()
        }
        contentSplitByUKPRN[ukprn]?.appendln(csvFile.currentRowAsString)
    }

    private fun saveSplitByUKPRNContent(dataImport: DataImport, contentSplitByUKPRN: Map<Int, StringBuilder>) {
        val fileType = dataImport.importType!!.description
        val fileSuffix = extractFileSuffixFrom(dataImport)

        for ((ukprn, content) in contentSplitByUKPRN) {
            fileService.saveFile(dataImport.id, fileType, fileSuffix, ukprn, content.toString())
        }
    }

    private fun extractFileSuffixFrom(dataImport: DataImport): String {
        val fileName = dataImport.fileName!!
        return if (dataImport.importType!!.isMonthlyFile) {
            fileName.substring(dataImport.importType!!.description.length + 1, fileName.length - 4)
        }
        else {
            ""
        }
    }

}
