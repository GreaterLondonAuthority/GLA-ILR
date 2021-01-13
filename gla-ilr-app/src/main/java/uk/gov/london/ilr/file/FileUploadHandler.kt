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
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.interceptor.TransactionInterceptor
import uk.gov.london.common.CSVFile
import uk.gov.london.common.skills.SkillsGrantType
import uk.gov.london.ilr.file.ESFMonthlyRecordFile.validateESFMonthlyRecordFile
import uk.gov.london.ilr.environment.Environment
import uk.gov.london.ilr.fundingsummary.FundingSummaryService
import uk.gov.london.ilr.learner.*
import uk.gov.london.ilr.security.User.SYSTEM_USER
import uk.gov.london.ilr.security.UserService
import java.io.InputStream
import java.time.LocalDateTime
import javax.transaction.Transactional

class UploadResult(var numberOfRecords: Long = 0,
                   var errorMessages : MutableList<String> = mutableListOf())

@Service
@Transactional
class FileUploadHandler @Autowired constructor(
        val dataImportService: DataImportService,
        val fundingSummaryService: FundingSummaryService,
        val occupancyReportService: OccupancyReportService,
        val supplementaryDataService: SupplementaryDataService,
        val splitByUkprnCsvImporter: SplitByUkprnCsvImporter,
        val refDataMappingRepository: RefDataMappingRepository,
        var userService: UserService,
        val environment: Environment) {

    internal var log = LoggerFactory.getLogger(javaClass)

    @Async
    fun uploadAsync(fileName: String?, inputStream: InputStream, importType: DataImportType, userName: String): UploadResult {
        return upload(fileName, inputStream, importType, userName)
    }

    fun upload(fileName: String?, inputStream: InputStream, importType: DataImportType, userName: String? = SYSTEM_USER): UploadResult {
        val dataImport = DataImport(fileName = fileName)
        dataImport.status = DataImportStatus.PROCESSING
        dataImport.createdOn = environment.now()
        dataImport.importType = importType
        dataImport.createdBy = userName
        dataImportService.saveWithNewTransaction(dataImport)

        return upload(dataImport, inputStream)
    }

    fun upload(dataImport: DataImport, inputStream: InputStream): UploadResult {
        validateFileName(dataImport)
        extractYearAndPeriodIfAvailable(dataImport)

        val uploadResult = UploadResult()
        try {
            when (dataImport.importType) {
                DataImportType.FUNDING_SUMMARY -> {
                    fundingSummaryService.createSummaryRecords(dataImport, inputStream)
                }

                DataImportType.OCCUPANCY_REPORT -> {
                    occupancyReportService.createNewOccupancyRecords(dataImport, inputStream)
                }

                DataImportType.DATA_VALIDATION_ISSUES -> {
                    splitByUkprnCsvImporter.createDataValidationIssueRecords(dataImport, inputStream)
                }

                DataImportType.SUPPLEMENTARY_DATA -> {
                    supplementaryDataService.createSupplementaryDataRecords(dataImport, inputStream, uploadResult)
                }
            }

            dataImport.status = DataImportStatus.COMPLETE
        }
        catch (e: Exception) {
            log.error("failed to create records from ${dataImport.fileName} due to", e.message)
            dataImport.status = DataImportStatus.FAILED
            if (uploadResult.errorMessages.isEmpty()) {
                uploadResult.errorMessages.add("Unable to create records from file: ${dataImport.fileName} due to " + e.message)
            }
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
        }
        finally {
            dataImportService.saveWithNewTransaction(dataImport)
        }

        return uploadResult
    }

    private fun validateFileName(dataImport: DataImport) {
        if (!dataImport.fileName!!.toUpperCase().endsWith(".CSV")) {
            throw RuntimeException("Upload failed: File must be in CSV format, to do this save an excel file as a .CSV")
        }

        if (dataImport.importType!!.isMonthlyFile) {
            validateESFMonthlyRecordFile(dataImport.fileName, dataImport.importType)
        }
    }

    private fun extractYearAndPeriodIfAvailable(dataImport: DataImport) {
        if (dataImport.importType!!.isMonthlyFile) {
            val fsrFile = ESFMonthlyRecordFile.parse(dataImport.fileName)
            dataImport.academicYear = fsrFile.year
            dataImport.period = fsrFile.month
        }
    }

    fun uploadStatic(fileName: String, inputStream: InputStream) {
        try{
            if(fileName == "Data Initialiser - Ref Data Mapping.csv") {
                createRefDataMapping(inputStream)
            }
        } catch (e :Exception){
            log.error("failed to create records from $fileName due to", e.message)
            throw Exception("Unable to create data initialiser data from file: $fileName")
        }
    }

    private fun createRefDataMapping(inputStream: InputStream) {
        val csvFile = CSVFile(inputStream)

        while (csvFile.nextRow()) {
            processRefDataMapping(csvFile)
        }
    }

    private fun processRefDataMapping(csvRow: CSVFile) {
        refDataMappingRepository.save(
                RefDataMapping(
                        RefDataMappingPK(
                                year = parseYear(csvRow.getString("year")),
                                attribute = csvRow.getString("attribute"),
                                code = csvRow.getString("code")
                        ),
                        headlineValue = csvRow.getStringIfPresent("headlineValue"),
                        detailedValue = csvRow.getStringIfPresent("detailedValue"),
                        addedOn = LocalDateTime.now(),
                        addedBy = csvRow.getString("addedBy")
                )
        )
    }
        /**
     * @param stringYear financial year format: "YYYY/YY" (ex: 2018/19)
     * @return in the example above 2018
     */
    fun parseYear(stringYear: String): Int? {
        return Integer.valueOf(stringYear.substring(0, 4))
    }

}

fun getSkillsGrantType(fundingLine: String): SkillsGrantType {
    return if (fundingLine.contains("non-procured")) SkillsGrantType.AEB_GRANT else SkillsGrantType.AEB_PROCURED
}
