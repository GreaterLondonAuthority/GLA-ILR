/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.learner

import org.apache.commons.io.input.BOMInputStream
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
import java.time.OffsetDateTime
import java.util.*
import kotlin.collections.HashSet
import kotlin.collections.set


const val INVEST_PRIOR_CLAIM_UNDER = "Investment priority claimed under"
const val HAS_BASIC_SKILLS = "Has Basic Skills upon joining"
const val IS_HOMELESS = "Is homeless (broad definition)"
const val HIGHEST_EDUCATIONAL_ATTAINMENT_AT_ESF_START = "Highest educational attainment at ESF start"
const val PROGRESSING_INTO_EDUCATION_OR_TRAINING_AS_ESF_RESULT = "Progressing into education or training as ESF result"
const val START_DATE_FOR_EDUCATION_OR_TRAINING_ESF_RESULT = "Start date for education or training ESF result"
const val HAS_LEFT_ESF_PROGRAM = "Has left ESF program"
const val ESF_LEAVE_DATE = "ESF leave date"
const val ERROR_COLUMN = "Error column"

val SUPPLEMENTARY_DATA_COLUMNS = listOf(
        LRN,
        UKPRN,
        INVEST_PRIOR_CLAIM_UNDER,
        HAS_BASIC_SKILLS,
        IS_HOMELESS,
        HIGHEST_EDUCATIONAL_ATTAINMENT_AT_ESF_START,
        PROGRESSING_INTO_EDUCATION_OR_TRAINING_AS_ESF_RESULT,
        START_DATE_FOR_EDUCATION_OR_TRAINING_ESF_RESULT,
        HAS_LEFT_ESF_PROGRAM,
        ESF_LEAVE_DATE)
        .map{ i -> i.toUpperCase()}

val validSupplementaryDataValues = mapOf(
        INVEST_PRIOR_CLAIM_UNDER to setOf("1.1", "1.2", "2.1"),
        HAS_BASIC_SKILLS to setOf("1", "2", "3"),
        IS_HOMELESS to setOf("1", "2", "3"),
        HIGHEST_EDUCATIONAL_ATTAINMENT_AT_ESF_START to setOf("1", "2", "3", "7", "9", "10", "11", "12", "13", "97", "98", "99"),
        PROGRESSING_INTO_EDUCATION_OR_TRAINING_AS_ESF_RESULT to setOf("1", "2"),
        HAS_LEFT_ESF_PROGRAM to setOf("1", "2")
)

@Service
class SupplementaryDataService(val supplementaryDataRepository: SupplementaryDataRepository,
                               val learnerDeliveryRepository: LearningDeliveryRepository,
                               val userService: UserService,
                               val fileService: FileService,
                               val auditService: AuditService,
                               val environment: Environment) {

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
            val ukprn = csvFile.getInteger(UKPRN)

            validateUserAccessToUpload(csvFile.getInteger(UKPRN), errorMessages)
            validateSupplementaryData(ukprn, learnerReferenceNumber, csvFile, errorMessages)

            if (errorMessages.isEmpty()) {
                processSupplementaryData(csvFile, ukprn, learnerReferenceNumber, now)
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
        }
    }


    private fun processSupplementaryData(csvRow: CSVFile, ukprn: Int, learnerReferenceNumber: String, now: OffsetDateTime) {
        supplementaryDataRepository.save(SupplementaryData(
                id = SupplementaryDataPK(ukprn, learnerReferenceNumber),
                investmentPriorityClaimedUnder = csvRow.getString(INVEST_PRIOR_CLAIM_UNDER),
                hasBasicSkills = csvRow.getInteger(HAS_BASIC_SKILLS),
                isHomeless = csvRow.getInteger(IS_HOMELESS),
                highestEducationalAttainmentAtEsfStart = csvRow.getInteger(HIGHEST_EDUCATIONAL_ATTAINMENT_AT_ESF_START),
                progressingIntoEducationOrTrainingAsEsfResult = csvRow.getInteger(PROGRESSING_INTO_EDUCATION_OR_TRAINING_AS_ESF_RESULT),
                startDateForEducationOrTrainingEsfResult = csvRow.getDate(START_DATE_FOR_EDUCATION_OR_TRAINING_ESF_RESULT, "yyyy-MM-dd"),
                hasLeftEsfProgram = csvRow.getInteger(HAS_LEFT_ESF_PROGRAM),
                esfLeaveDate = csvRow.getDate(ESF_LEAVE_DATE, "yyyy-MM-dd"),
                lastSupplementaryDataUpload= now))
    }

    private fun validateSupplementaryData(ukprn: Int , learnerReferenceNumber: String, csvRow: CSVFile, errorMessages: MutableMap<String, String>) {
        validateFieldContent(csvRow, INVEST_PRIOR_CLAIM_UNDER, errorMessages)
        validateFieldContent(csvRow, HAS_BASIC_SKILLS, errorMessages)
        validateFieldContent(csvRow, IS_HOMELESS, errorMessages)
        validateFieldContent(csvRow, HIGHEST_EDUCATIONAL_ATTAINMENT_AT_ESF_START, errorMessages)
        validateFieldContent(csvRow, PROGRESSING_INTO_EDUCATION_OR_TRAINING_AS_ESF_RESULT, errorMessages)
        validateDateField(csvRow, START_DATE_FOR_EDUCATION_OR_TRAINING_ESF_RESULT, errorMessages)
        validateFieldContent(csvRow, HAS_LEFT_ESF_PROGRAM, errorMessages)
        validateDateField(csvRow, ESF_LEAVE_DATE, errorMessages)

        if (learnerDeliveryRepository.countAllByIdUkprnAndIdLearnerReferenceNumber(ukprn, learnerReferenceNumber) == 0) {
            errorMessages["Learner reference number"] = "A record for Learner reference number " + learnerReferenceNumber +
                    " doesn't exist for UKPRN " + ukprn + ", valid Learner reference numbers must be uploaded"
        }
    }

    fun validateFieldContent(csvRow: CSVFile, columnName: String, errorMessages: MutableMap<String, String>) {
        val value = csvRow.getString(columnName)
        val validValues = validSupplementaryDataValues[columnName]
        if (value !in validValues!!) {
            addErrorMessage(columnName, validValues.joinToString(), errorMessages)
        }
    }

    fun validateDateField(csvRow: CSVFile, columnName: String, errorMessages: MutableMap<String, String>) {
        val value = csvRow.getString(columnName)
        try {
            SimpleDateFormat("yyyy-MM-dd").parse(value)
        } catch (e: ParseException) {
            addErrorMessage(columnName, "YYYY-MM-DD", errorMessages)
        }
    }

    private fun addErrorMessage(columnName: String, expectedFormat: Any, errorMessages: MutableMap<String,String>) {
        errorMessages[columnName] = "File upload failed as one or more cells in \"$columnName\" column contain(s) information " +
                "which is not in the specified format \"$expectedFormat\"."
    }
}
