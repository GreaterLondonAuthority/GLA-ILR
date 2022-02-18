/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.learner

import liquibase.util.StringUtils
import org.apache.commons.io.input.BOMInputStream
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.london.common.CSVFile
import uk.gov.london.ilr.audit.AuditService
import uk.gov.london.ilr.environment.Environment
import uk.gov.london.ilr.file.*
import uk.gov.london.ilr.security.User.SYSTEM_USER
import uk.gov.london.ilr.security.UserService
import java.io.InputStream
import java.io.StringWriter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*
import javax.transaction.Transactional
import kotlin.collections.HashSet
import kotlin.collections.set


const val INVEST_PRIOR_CLAIM_UNDER = "Investment priority claimed under"
const val IS_HOMELESS = "Is homeless (broad definition)"
const val HIGHEST_LITERARY_ATTAINMENT_AT_ESF_START = "Highest attainment in literacy/ESOL at ESF start"
const val HIGHEST_NUMERACY_ATTAINMENT_AT_ESF_START = "Highest attainment in numeracy at ESF start"
const val HIGHEST_EDUCATIONAL_ATTAINMENT_AT_ESF_START = "Highest educational attainment at ESF start"
const val PROGRESSING_INTO_EDUCATION_OR_TRAINING_AS_ESF_RESULT = "Progressing into education or training as ESF result"
const val START_DATE_FOR_EDUCATION_OR_TRAINING_ESF_RESULT = "Start date for education or training ESF result"
const val HAS_LEFT_ESF_PROGRAMME = "Has left ESF programme"
const val ESF_RETURNER = "ESF Returner"
const val ESF_LEAVE_DATE = "ESF leave date"
const val ERROR_COLUMN = "Error column"

const val BLANK = "blank"

val SUPPLEMENTARY_DATA_COLUMNS = listOf(
        LRN,
        UKPRN,
        INVEST_PRIOR_CLAIM_UNDER,
        IS_HOMELESS,
        HIGHEST_EDUCATIONAL_ATTAINMENT_AT_ESF_START,
        HIGHEST_LITERARY_ATTAINMENT_AT_ESF_START,
        HIGHEST_NUMERACY_ATTAINMENT_AT_ESF_START,
        PROGRESSING_INTO_EDUCATION_OR_TRAINING_AS_ESF_RESULT,
        START_DATE_FOR_EDUCATION_OR_TRAINING_ESF_RESULT,
        HAS_LEFT_ESF_PROGRAMME,
        ESF_RETURNER,
        ESF_LEAVE_DATE)
        .map{ i -> i.toUpperCase()}

val validSupplementaryDataValues = mapOf(
        INVEST_PRIOR_CLAIM_UNDER to setOf("1.1", "1.2", "2.1"),
        IS_HOMELESS to setOf("1", "2", "3"),
        HIGHEST_EDUCATIONAL_ATTAINMENT_AT_ESF_START to setOf("1", "2", "3", "7", "9", "10", "11", "12", "13", "97", "98", "99"),
        PROGRESSING_INTO_EDUCATION_OR_TRAINING_AS_ESF_RESULT to setOf("1", BLANK),
        HAS_LEFT_ESF_PROGRAMME to setOf("1", BLANK),
        ESF_RETURNER to setOf("1", BLANK),
        HIGHEST_LITERARY_ATTAINMENT_AT_ESF_START to setOf("E0", "E1", "E2", "E3", "L1", "L2"),
        HIGHEST_NUMERACY_ATTAINMENT_AT_ESF_START to setOf("E0", "E1", "E2", "E3", "L1", "L2")
)

@Service
@Transactional
class SupplementaryDataService(val supplementaryDataRepository: SupplementaryDataRepository,
                               val learnerDeliveryRepository: LearningDeliveryRepository,
                               val supplementaryDataSummaryRepository: SupplementaryDataSummaryRepository,
                               val learningAimRepository: LearningAimRepository,
                               val learnerService: LearnerService,
                               val userService: UserService,
                               val fileService: FileService,
                               val auditService: AuditService,
                               val occupancyReportService: OccupancyReportService,
                               val environment: Environment) {

    fun validateFileAvailableForUpload(year: Int, period: Int) {
        if (!learnerService.learnerRecordByYearAndPeriodExists(year, period)) {
            throw RuntimeException("Supplementary Data for year " + year + " and period " + period + " is not yet available for upload. "
                    + "Please wait to hear from the GLA when the data becomes available, or update the file name and try again.")
        }
    }

    @Transactional
    fun deleteSupplementaryDataForYearPeriod(year: Int, period: Int) {
        supplementaryDataRepository.deleteAllByYearAndPeriod(year, period)
    }

    fun getLearnerLatestSupplementaryData(learnerRefNumber: String?): SupplementaryData? {
        return supplementaryDataRepository.findLatestSupplementaryDataRecord(learnerRefNumber)
    }

    fun createSupplementaryDataRecords(dataImport: DataImport,  inputStream: InputStream, uploadResult: UploadResult) {
        // https://en.wikipedia.org/wiki/Byte_order_mark
        val bomFilteredInputStream = BOMInputStream(inputStream)
        val csvFile = CSVFile(bomFilteredInputStream)
        createSupplementaryDataRecords(dataImport, csvFile, uploadResult)
    }

    fun createSupplementaryDataRecords(dataImport: DataImport, csvFile: CSVFile, uploadResult: UploadResult) {
        validateColumnHeaders(csvFile)
        processSupplementaryDataRows(dataImport, csvFile, uploadResult)
    }

    private fun validateColumnHeaders(csvFile: CSVFile) {
        val actualColumns = csvFile.headers.minusElement(ERROR_COLUMN).map { h -> h.toUpperCase() }

        if (!SUPPLEMENTARY_DATA_COLUMNS.containsAll(actualColumns)) {
            val missingColumns = HashSet<String>()
            for (column in SUPPLEMENTARY_DATA_COLUMNS) {
                if (!actualColumns.contains(column)) {
                    missingColumns.add(column)
                }
            }
            throw RuntimeException("Invalid column headers, missing $missingColumns")
        }
    }

    private fun validateUserAccessToUpload(ukprn: Int, errorMessages: MutableMap<String, String>) {
        val currentUser = userService.currentUser

        if(environment.isTestEnvironment && currentUser.username == SYSTEM_USER) return

        if(!currentUser.isGla && !currentUser.ukprns.contains(ukprn)) {
            auditService.auditCurrentUserActivity("Supplementary file upload failed for UKPRN ${ukprn} because user ${currentUser.username} " +
                    "doesn't have access to the organisation(s) with stated UKPRN.")
            errorMessages["UKPRN"] = "File upload failed as you do not have access to the organisation(s) with stated UKPRN number(s)."
        }
    }

    private fun createErrorCSVFile(originalHeaders: Set<String>, writer : StringWriter) : CSVFile {
        val headers = LinkedHashSet<String>()
        headers.addAll(originalHeaders)
        headers.add(ERROR_COLUMN)
        return CSVFile(headers, writer)
    }

    private fun processSupplementaryDataRows(dataImport: DataImport, csvFile: CSVFile, uploadResult: UploadResult) {
        val supplementaryDataEntries = mutableListOf<SupplementaryData>()
        val ukprns = mutableSetOf<Int>()
        val now = environment.now()
        val allErrorMessages = mutableSetOf<String>()
        val headers = csvFile.headers.minusElement(ERROR_COLUMN)
        val writer = StringWriter()
        val errorCSVFile = createErrorCSVFile(headers, writer)

        while (csvFile.nextRow()) {
            val errorFileColumns = mutableMapOf<String, Any>()
            val errorMessages = mutableMapOf<String, String>()

            for (header in headers) {
                errorFileColumns[header] = csvFile.getString(header)
            }

            val learnerReferenceNumber = csvFile.getString(LRN)
            val ukprn = if (StringUtils.isNotEmpty(csvFile.getString(UKPRN))) csvFile.getInteger(UKPRN) else null
            val year: Int? = dataImport.academicYear

            if (ukprn == null) {
                addErrorMessage(UKPRN, "Numeric", errorMessages)
            } else {
                ukprns.add(ukprn)
                validateUserAccessToUpload(ukprn, errorMessages)
                validateSupplementaryData(ukprn, learnerReferenceNumber, year, csvFile, errorMessages)
            }

            if (errorMessages.isEmpty()) {
                supplementaryDataEntries.add(readSupplementaryData(csvFile, ukprn!!, dataImport.academicYear!!, dataImport.period!!, learnerReferenceNumber, now))
                uploadResult.numberOfRecords++
            } else {
                allErrorMessages.addAll(errorMessages.values)
                errorFileColumns[ERROR_COLUMN] = errorMessages.keys.joinToString(separator = ", ")
            }
            errorCSVFile.writeValues(errorFileColumns)
        }
        if (allErrorMessages.isNotEmpty()) {
            uploadResult.errorMessages.addAll(allErrorMessages)
            fileService.saveFile(dataImport.id, ERROR_FILE_TYPE, ".csv", -1, writer.toString())
        } else {
            supplementaryDataRepository.deleteAllByUkprnAndYear(ukprns, dataImport.academicYear!!)
            supplementaryDataRepository.saveAll(supplementaryDataEntries)
        }
    }


    private fun readSupplementaryData(csvRow: CSVFile, ukprn: Int, year: Int, period: Int, learnerReferenceNumber: String, now: OffsetDateTime): SupplementaryData {
        val progressingIntoEducationOrTrainingAsEsfResult = csvRow.getIntegerIfPresent(PROGRESSING_INTO_EDUCATION_OR_TRAINING_AS_ESF_RESULT)
        val startDateForEducationOrTrainingEsfResult = if (progressingIntoEducationOrTrainingAsEsfResult != null) {
            csvRow.getDate(START_DATE_FOR_EDUCATION_OR_TRAINING_ESF_RESULT, "yyyy-MM-dd")
        } else {
            null
        }
        val hasLeftEsfProgramme = csvRow.getIntegerIfPresent(HAS_LEFT_ESF_PROGRAMME)
        val esfLeaveDate = if (hasLeftEsfProgramme != null) {
            csvRow.getDate(ESF_LEAVE_DATE, "yyyy-MM-dd")
        } else {
            null
        }
        return SupplementaryData(
                id = SupplementaryDataPK(ukprn, year, learnerReferenceNumber),
                period = period,
                investmentPriorityClaimedUnder = csvRow.getString(INVEST_PRIOR_CLAIM_UNDER),
                isHomeless = csvRow.getInteger(IS_HOMELESS),
                highestEducationalAttainmentAtEsfStart = csvRow.getInteger(HIGHEST_EDUCATIONAL_ATTAINMENT_AT_ESF_START),
                highestLiteracyAttainmentAtEsfStart = csvRow.getString(HIGHEST_LITERARY_ATTAINMENT_AT_ESF_START),
                highestNumeracyAttainmentAtEsfStart = csvRow.getString(HIGHEST_NUMERACY_ATTAINMENT_AT_ESF_START),
                progressingIntoEducationOrTrainingAsEsfResult = progressingIntoEducationOrTrainingAsEsfResult,
                startDateForEducationOrTrainingEsfResult = startDateForEducationOrTrainingEsfResult,
                hasLeftEsfProgramme = hasLeftEsfProgramme,
                esfReturner = csvRow.getIntegerIfPresent(ESF_RETURNER),
                esfLeaveDate = esfLeaveDate,
                lastSupplementaryDataUpload = now)
    }

    private fun validateSupplementaryData(ukprn: Int , learnerReferenceNumber: String, year: Int?, csvRow: CSVFile, errorMessages: MutableMap<String, String>) {
        validateOccupancyRecordExists(ukprn, learnerReferenceNumber, year, errorMessages)
        validateFieldContent(csvRow, INVEST_PRIOR_CLAIM_UNDER, errorMessages)
        validateFieldContent(csvRow, IS_HOMELESS, errorMessages)
        validateFieldContent(csvRow, HIGHEST_EDUCATIONAL_ATTAINMENT_AT_ESF_START, errorMessages)
        validateFieldContent(csvRow, HIGHEST_NUMERACY_ATTAINMENT_AT_ESF_START, errorMessages)
        validateFieldContent(csvRow, HIGHEST_LITERARY_ATTAINMENT_AT_ESF_START, errorMessages)
        val progressingIntoEducationOrTrainingAsEsfResult = validateFieldContent(csvRow, PROGRESSING_INTO_EDUCATION_OR_TRAINING_AS_ESF_RESULT, errorMessages)
        if (progressingIntoEducationOrTrainingAsEsfResult != null && progressingIntoEducationOrTrainingAsEsfResult.isNotBlank()) {
            validateDateField(csvRow, START_DATE_FOR_EDUCATION_OR_TRAINING_ESF_RESULT, errorMessages)
        } else {
            validateDependantFieldBlank(csvRow, START_DATE_FOR_EDUCATION_OR_TRAINING_ESF_RESULT, PROGRESSING_INTO_EDUCATION_OR_TRAINING_AS_ESF_RESULT, errorMessages)
        }
        val hasLeftEsfProgramme = validateFieldContent(csvRow, HAS_LEFT_ESF_PROGRAMME, errorMessages)
        if (hasLeftEsfProgramme != null && hasLeftEsfProgramme.isNotBlank()) {
            validateDateField(csvRow, ESF_LEAVE_DATE, errorMessages)
        } else {
            validateDependantFieldBlank(csvRow, ESF_LEAVE_DATE, HAS_LEFT_ESF_PROGRAMME, errorMessages)
        }

        validateFieldContent(csvRow, ESF_RETURNER, errorMessages)

        if (learnerDeliveryRepository.countAllByIdUkprnAndIdLearnerReferenceNumber(ukprn, learnerReferenceNumber) == 0) {
            errorMessages["Learner reference number"] = "A record for Learner reference number " + learnerReferenceNumber +
                    " doesn't exist for UKPRN " + ukprn + ", valid Learner reference numbers must be uploaded"
        }
    }

    fun validateFieldContent(csvRow: CSVFile, columnName: String, errorMessages: MutableMap<String, String>): String? {
        val value = csvRow.getString(columnName)
        val validValues = validSupplementaryDataValues[columnName]
        val isValueBlankAndBlankNotAllowed = (value == null || value.isEmpty()) && BLANK !in validValues!!
        val valueNotBlankAndNotInAllowedValues = (value != null && value.isNotEmpty()) && value !in validValues!!
        if (valueNotBlankAndNotInAllowedValues || isValueBlankAndBlankNotAllowed) {
            addFormatErrorMessage(columnName, validValues!!.joinToString(), errorMessages)
        }
        return value
    }

    fun validateDateField(csvRow: CSVFile, columnName: String, errorMessages: MutableMap<String, String>) {
        val value = csvRow.getString(columnName)
        try {
            val date = SimpleDateFormat("yyyy-MM-dd").parse(value)
            if (date.after(Date.from(Instant.now()))) {
                addErrorMessage(columnName, "dates cannot be in the future", errorMessages)
            }
        } catch (e: ParseException) {
            addFormatErrorMessage(columnName, "YYYY-MM-DD", errorMessages)
        }
    }

    fun validateDependantFieldBlank(csvRow: CSVFile, columnName: String, dependantOnColumnName: String, errorMessages: MutableMap<String, String>) {
        val value = csvRow.getString(columnName)
        if (value != null && value.isNotBlank()) {
            addErrorMessage(columnName, "should be blank if \"$dependantOnColumnName\" is blank", errorMessages)
        }
    }

    fun validateOccupancyRecordExists(ukprn: Int, learnerReferenceNumber: String, year: Int?, errorMessages: MutableMap<String, String>){
        val exists : Boolean = occupancyReportService.validateOccupancyRecordExists(ukprn, learnerReferenceNumber, year)
        if(!exists) {
            errorMessages["UKPRN, Learner reference number"] = "No records found for ukprn \"$ukprn\" and learner reference \"$learnerReferenceNumber\". " +
                    "Valid previous occupancy records must exist to be able to upload this supplementary data."
        }
    }

    private fun addErrorMessage(columnName: String, message: String, errorMessages: MutableMap<String, String>) {
        errorMessages[columnName] = """File upload failed as one or more cells in "$columnName" column are invalid: $message"""
    }

    private fun addFormatErrorMessage(columnName: String, expectedFormat: Any, errorMessages: MutableMap<String,String>) {
        errorMessages[columnName] = "File upload failed as one or more cells in \"$columnName\" column contain(s) information " +
                "which is not in the specified format \"$expectedFormat\". Amend the content and try again."
    }

    fun getSupplementaryDataSummaries(learner: String?, ukprns: Set<Int>?, academicYear: Int?, pageable: Pageable?): Page<SupplementaryDataSummary> {
        return supplementaryDataSummaryRepository.findAll(learner, ukprns, academicYear, pageable!!)
    }

    fun getDistinctAcademicYears(): List<Int> {
        return supplementaryDataRepository.findDistinctAcademicYears()
    }

    fun getLearningDeliveryForLeaner(learnerId: LearnerPK): List<LearningDelivery> {
        val learningDelivery = learnerDeliveryRepository.findAllByIdUkprnAndIdLearnerReferenceNumberAndIdYearOrderByIdAimSequenceNumber(learnerId.ukprn, learnerId.learnerReferenceNumber, learnerId.year)
        for (delivery in learningDelivery) {
            delivery.aim = learningAimRepository.findById(LearningAimPK(delivery.aimReference!!, learnerId.year)).orElse(null)
        }
        return learningDelivery
    }
}
