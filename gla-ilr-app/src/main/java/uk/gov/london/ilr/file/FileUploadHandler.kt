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
import uk.gov.london.common.skills.SkillsGrantType
import uk.gov.london.ilr.environment.Environment
import uk.gov.london.ilr.fundingsummary.FundingSummaryService
import uk.gov.london.ilr.learner.OccupancyReportService
import uk.gov.london.ilr.learner.SupplementaryDataService
import uk.gov.london.ilr.providerallocation.ProviderAllocationService
import uk.gov.london.ilr.referencedata.ReferenceDataService
import uk.gov.london.ilr.security.User.SYSTEM_USER
import uk.gov.london.ilr.security.UserService
import java.io.InputStream
import java.util.regex.Pattern
import javax.transaction.Transactional

class UploadResult(var numberOfRecords: Long = 0,
                   var errorMessages: MutableList<String> = mutableListOf())

@Service
@Transactional
class FileUploadHandler @Autowired constructor(
        val dataImportService: DataImportService,
        val fundingSummaryService: FundingSummaryService,
        val occupancyReportService: OccupancyReportService,
        val supplementaryDataService: SupplementaryDataService,
        val providerAllocationService: ProviderAllocationService,
        val splitByUkprnCsvImporter: SplitByUkprnCsvImporter,
        val referenceDataService: ReferenceDataService,
        var userService: UserService,
        val environment: Environment) {

    internal var log = LoggerFactory.getLogger(javaClass)

    @Async
    fun uploadAsync(fileName: String, inputStream: InputStream, importType: DataImportType, userName: String): UploadResult {
        return upload(fileName, inputStream, importType, userName)
    }

    fun upload(fileName: String, inputStream: InputStream, importType: DataImportType, userName: String? = SYSTEM_USER): UploadResult {
        val dataImport = DataImport(fileName = fileName)
        dataImport.status = DataImportStatus.PROCESSING
        dataImport.createdOn = environment.now()
        dataImport.importType = importType
        dataImport.createdBy = userName
        dataImport.academicYear = if (importType.isYearlyFile) extractYearFromFilename(fileName, importType) else null
        dataImport.period = if (importType.isMonthlyFile) extractPeriodFromFilename(fileName, importType) else null
        dataImportService.saveWithNewTransaction(dataImport)
        return upload(dataImport, inputStream)
    }

    fun upload(dataImport: DataImport, inputStream: InputStream): UploadResult {
        var uploadResult = UploadResult()
        try {
            when (dataImport.importType) {
                DataImportType.FUNDING_SUMMARY -> {
                    fundingSummaryService.createSummaryRecords(dataImport, inputStream)
                }

                DataImportType.OCCUPANCY_REPORT -> {
                    occupancyReportService.createNewOccupancyRecords(dataImport, inputStream)
                }

                DataImportType.DATA_VALIDATION_ISSUES -> {
                    splitByUkprnCsvImporter.createSplitByUKPRNFile(dataImport, inputStream)
                }

                DataImportType.PROVIDER_ALLOCATION -> {
                    uploadResult = providerAllocationService.createProviderAllocationRecords(dataImport, inputStream)
                }

                DataImportType.GLA_FSR -> {
                    splitByUkprnCsvImporter.createSplitByUKPRNFile(dataImport, inputStream)
                }

                DataImportType.GLA_OCC -> {
                    splitByUkprnCsvImporter.createSplitByUKPRNFile(dataImport, inputStream)
                }

                DataImportType.SUPPLEMENTARY_DATA -> {
                    supplementaryDataService.createSupplementaryDataRecords(dataImport, inputStream, uploadResult)
                }

                DataImportType.ILR_CODE_VALUES -> {
                    referenceDataService.createReferenceDataRecords(dataImport, inputStream)
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
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly()
        }
        finally {
            dataImportService.saveWithNewTransaction(dataImport)
        }

        return uploadResult
    }

}

fun extractYearFromFilename(fileName: String, importType: DataImportType): Int {
    var year: Int? = null
    var matcher = Pattern.compile("(.*) ([0-9]{4})( |.csv)").matcher(fileName)
    if (matcher.find()) {
        year = matcher.group(2).toInt()
    }

    if (year == null) {
        throw IllegalArgumentException("Invalid file format, expected: '${importType.format}'")
    }

    return year
}

fun extractPeriodFromFilename(fileName: String, importType: DataImportType): Int {
    var period: Int? = null
    if (fileName.matches("(.*) +\\d{4} \\d{2}(.csv)".toRegex())) {
        period = fileName.substring(fileName.length - 6, fileName.length - 4).toInt()
    }

    if (period == null || period < 1 || period > 14) {
        throw IllegalArgumentException("Invalid file format, expected: '${importType.format}'")
    }

    return period
}

fun getSkillsGrantType(fundingLine: String): SkillsGrantType {
    var grantType: SkillsGrantType = SkillsGrantType.AEB_PROCURED
    if (fundingLine.contains("non-procured")) {
        grantType = SkillsGrantType.AEB_GRANT
    } else if (fundingLine.contains("nsct", ignoreCase = true)) {
        grantType = SkillsGrantType.AEB_NSCT
    }
    return grantType
}
