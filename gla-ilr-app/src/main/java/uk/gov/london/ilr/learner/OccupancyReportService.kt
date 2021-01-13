package uk.gov.london.ilr.learner

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import uk.gov.london.common.CSVFile
import uk.gov.london.ilr.file.DataImport
import uk.gov.london.ilr.file.DataImportService
import uk.gov.london.ilr.file.LRN
import uk.gov.london.ilr.file.UKPRN
import java.io.InputStream
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.transaction.Transactional

@Service
@Transactional
class OccupancyReportService(val dataImportService: DataImportService,
                             val providerRepository: ProviderRepository,
                             var learnerRepository: LearnerRepository,
                             val learnerDeliveryRepository: LearningDeliveryRepository,
                             val learningAimRepository: LearningAimRepository,
                             val earningPeriodRepository: EarningPeriodRepository,
                             val txManager: PlatformTransactionManager) {

    internal var log = LoggerFactory.getLogger(javaClass)

    @Value("\${ilr.occupancy-report.format-change-year}")
    var reportFormatChangeYear: Int = 2020

    val months : Array<String> = DateFormatSymbols(Locale.ENGLISH).months

    fun createNewOccupancyRecords(dataImport: DataImport, inputStream: InputStream) {
        createNewOccupancyRecords(dataImport, inputStream, dataImport.academicYear!!)
    }

    fun createNewOccupancyRecords(dataImport: DataImport, inputStream: InputStream, academicYear: Int) {
        val csvFile = CSVFile(inputStream)

        validateOccupancyFields(academicYear, csvFile)

        cleanOccupancyDataForYear(academicYear)

        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        var status = txManager.getTransaction(def)
        try {
            var loopCount = 0
            while (csvFile.nextRow()) {
                processNewOccupancyRecord(csvFile, academicYear)

                if (++loopCount % 1000 == 0) {
                    log.debug("processsed: $loopCount")
                    dataImport.rowsProcessed = loopCount
                    dataImportService.updateDataImportRecord(dataImport)
                    txManager.commit(status)
                    status = txManager.getTransaction(def)

                }
            }
            log.debug("finished processing all rows: $loopCount")
            dataImport.rowsProcessed
            dataImportService.updateDataImportRecord(dataImport)
            txManager.commit(status)
        } catch ( ex: Exception) {
            log.error("Failed due to : $ex.localizedMessage")
            txManager.rollback(status)
            cleanOccupancyDataForYear(academicYear)
            throw ex
        }
    }

    private fun validateOccupancyFields(academicYear: Int, csvFile: CSVFile) {
        log.debug("academicYear:"+academicYear +"reportFormatChangeYear:"+reportFormatChangeYear)
        val expectedColumns = mutableSetOf(LRN, UKPRN, "Return", "Unique learner number", "Aim sequence number",
                "Date of birth", "LLDD and health problem", "Ethnicity", "Sex", "Postcode prior to enrolment", "Prior attainment", "Provider specified learner monitoring (A)", "Provider specified learner monitoring (B)", // Learner
                "Learning aim reference", "Outcome", "Notional NVQ level", "Tier 2 sector subject area", "Funding model", "Completion status", "ESM Type - benefit status indicator", "Learner employment status", // Learner Delivery
                "Funding line type", "Partner UKPRN", "LDFAM type - full or co funding indicator", "LDFAM type - LDM (A)", "LDFAM type - LDM (B)", "LDFAM type - LDM (C)", "LDFAM type - LDM (D)", // Learenr Delivery
                "LDFAM type - LDM (E)", "LDFAM type - LDM (F)", "LDFAM type - DAM (A)", "LDFAM type - DAM (B)", "LDFAM type - DAM (C)", "LDFAM type - DAM (D)", "LDFAM type - Community Learning provision type",// Learenr Delivery
                "Learning start date", "Learning planned end date", "Learning actual end date") // Learenr Delivery

        if ( academicYear >= reportFormatChangeYear ) {
            expectedColumns.addAll(mutableSetOf("Provider name","Family name", "Given names", "Tier 2 sector subject area name", "Local authority code", "LDFAM type - DAM (E)", "LDFAM type - DAM (F)",
            "LDFAM type - household situation (A)", "LDFAM type - household situation (B)", "ESM type - employment intensity indicator", "Start for funding purposes", "Partner UKPRN name"))
        }

        for (i in 0..11) { // earning
            val monthString = months[i] + " "
            expectedColumns.add(monthString + "on programme earned cash")
            expectedColumns.add(monthString + "balancing payment earned cash")
            expectedColumns.add(monthString + "aim achievement earned cash")
            expectedColumns.add(monthString + "learning support earned cash")
        }

        val actualColumns = csvFile.headers

        validateColumnHeader(expectedColumns, actualColumns, false)
    }

    private fun cleanOccupancyDataForYear(year: Int) {
        log.debug("Removing occupancy data for year: $year")
        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        val status = txManager.getTransaction(def)
        try {
            providerRepository.deleteAllByIdYear(year)
            learnerRepository.deleteAllByIdYear(year)
            learnerDeliveryRepository.deleteAllByIdYear(year)
            earningPeriodRepository.deleteAllByIdYear(year)
            txManager.commit(status)
            log.debug("Successfully removed occupancy data for year: $year")
        } catch ( ex: Exception) {
            log.error( "Failed to remove occupancy data for : $year $ex.localizedMessage")
            throw ex
        }
    }

    private fun processNewOccupancyRecord(csvRow: CSVFile, academicYear: Int) {
        val learnerReferenceNumber = csvRow.getString(LRN)
                ?: throw RuntimeException("$LRN must be supplied for all rows")
        val ukprn = csvRow.getIntegerOrNull(UKPRN)
                ?: throw RuntimeException("$UKPRN must be supplied for row with Learner Reference Number: $learnerReferenceNumber")
        val returnPeriod = csvRow.getIntegerOrNull("Return")
                ?: throw RuntimeException("Return must be supplied for row with Learner Reference Number: $learnerReferenceNumber")
        val ulnString = csvRow.getString("Unique learner number")
                ?: throw RuntimeException("Unique learner number must be supplied for row with Learner Reference Number: $learnerReferenceNumber")
        val aimSequenceNumber = csvRow.getIntegerOrNull("Aim sequence number")
                ?: throw RuntimeException("Aim sequence number must be supplied for row with Learner Reference Number: $learnerReferenceNumber")
        val aimReference = csvRow.getString("Learning aim reference")
                ?: throw RuntimeException("Learning aim reference must be supplied for row with Learner Reference Number: $learnerReferenceNumber")

        val uln = ulnString.toLong()

        createProvider(csvRow, academicYear, ukprn)

        createLearner(csvRow, learnerReferenceNumber, ukprn, academicYear, uln, returnPeriod)

        createLearnerDelivery(csvRow, ukprn, learnerReferenceNumber, aimSequenceNumber, academicYear, returnPeriod)

        createEarningPeriods(csvRow, ukprn, learnerReferenceNumber, aimSequenceNumber, academicYear, returnPeriod)

        createLearningAim(csvRow,aimReference, academicYear)
    }

    private fun createProvider(csvRow: CSVFile,  academicYear: Int, ukprn: Int) {
        val id = ProviderPK(academicYear,ukprn)
        if (!providerRepository.existsById(id)) {
            val providerName = csvRow.getStringIfPresent("Provider name")
            providerRepository.save(Provider(id, providerName)
            )
        }
    }

    private fun createLearningAim(csvRow: CSVFile,aimReference: String,  academicYear: Int) {
        val id = LearningAimPK(aimReference, academicYear)
        if (!learningAimRepository.existsById(id)) {
            val title = csvRow.getString("Learning aim title")
            learningAimRepository.save(LearningAim(id, title)
            )
        }
    }

    private fun createLearner(csvRow: CSVFile, learnerReferenceNumber: String, ukprn: Int, academicYear: Int, uln: Long, returnPeriod: Int) {
        val id = LearnerPK(learnerReferenceNumber, ukprn, academicYear)
        if (!learnerRepository.existsById(id)) {
            val dateOfBirth: LocalDate? = getDateFromField(csvRow, "Date of birth")
            val lldd = csvRow.getInteger("LLDD and health problem")
            val ethnicity = csvRow.getInteger("Ethnicity")
            val priorAttainment = csvRow.getIntegerOrNull("Prior attainment")
            val gender = csvRow.getString("Sex")
            val postcode = csvRow.getString("Postcode prior to enrolment")
            val monA = csvRow.getString("Provider specified learner monitoring (A)")
            val monB = csvRow.getString("Provider specified learner monitoring (B)")
            val familyName = csvRow.getStringIfPresent("Family name")
            val givenName = csvRow.getStringIfPresent("Given names")
            learnerRepository.save(
                    Learner(id,
                            uln,
                            dateOfBirth,
                            lldd,
                            ethnicity,
                            gender,
                            priorAttainment,
                            postcode,
                            returnPeriod,
                            monA,
                            monB,
                            familyName,
                            givenName
                    )
            )
        }
    }

    private fun createLearnerDelivery(csvRow: CSVFile, ukprn: Int, learnerReferenceNumber: String,  aimSequenceNumber: Int, academicYear: Int, returnPeriod: Int) {
        val aimReference = csvRow.getString("Learning aim reference")
        val outcome = csvRow.getIntegerOrNull("Outcome")
        val nvq = csvRow.getString("Notional NVQ level")
        val tier2 = csvRow.getString("Tier 2 sector subject area")
        val funding = csvRow.getIntegerOrNull("Funding model")
        val completion = csvRow.getIntegerOrNull("Completion status")
        val esm = csvRow.getIntegerOrNull("ESM Type - benefit status indicator")
        val empStatus = csvRow.getIntegerOrNull("Learner employment status")
        val fundingLineType = csvRow.getString("Funding line type")
        val partnerUkprn = csvRow.getIntegerOrNull("Partner UKPRN")
        val ldfamTypeFundingIndicator = csvRow.getIntegerOrNull("LDFAM type - full or co funding indicator")
        val ldfamTypeLdmA = csvRow.getIntegerOrNull("LDFAM type - LDM (A)")
        val ldfamTypeLdmB = csvRow.getIntegerOrNull("LDFAM type - LDM (B)")
        val ldfamTypeLdmC = csvRow.getIntegerOrNull("LDFAM type - LDM (C)")
        val ldfamTypeLdmD = csvRow.getIntegerOrNull("LDFAM type - LDM (D)")
        val ldfamTypeLdmE = csvRow.getIntegerOrNull("LDFAM type - LDM (E)")
        val ldfamTypeLdmF = csvRow.getIntegerOrNull("LDFAM type - LDM (F)")
        val ldfamTypeDamA = csvRow.getIntegerOrNull("LDFAM type - DAM (A)")
        val ldfamTypeDamB = csvRow.getIntegerOrNull("LDFAM type - DAM (B)")
        val ldfamTypeDamC = csvRow.getIntegerOrNull("LDFAM type - DAM (C)")
        val ldfamTypeDamD = csvRow.getIntegerOrNull("LDFAM type - DAM (D)")
        val ldfamTypeDamE = csvRow.getStringIfPresent("LDFAM type - DAM (E)")
        val ldfamTypeDamF = csvRow.getStringIfPresent("LDFAM type - DAM (F)")
        val ldfamCommunityLearningProvisionType = csvRow.getIntegerOrNull("LDFAM type - Community Learning provision type")
        val ldfamTypeHouseholdSituationA = csvRow.getStringIfPresent("LDFAM type - household situation (A)")
        val ldfamTypeHouseholdSituationB = csvRow.getStringIfPresent("LDFAM type - household situation (B)")
        val localAuthorityCode = csvRow.getStringIfPresent("Local authority code")
        val partnerUkprnName = csvRow.getStringIfPresent("Partner UKPRN name")
        val esmTypeEmploymentIntensity = csvRow.getIntegerIfPresent("ESM type - employment intensity indicator")
        val startForFundingPurposes = csvRow.getIntegerIfPresent("Start for funding purposes")
        val tierTwoSectorSubjectAreaName = csvRow.getStringIfPresent("Tier 2 sector subject area name")

        learnerDeliveryRepository.save(
                LearningDelivery(LearningDeliveryPK(ukprn, learnerReferenceNumber, aimSequenceNumber, academicYear),
                        aimReference,
                        getDateFromField(csvRow, "Learning start date"),
                        getDateFromField(csvRow, "Learning planned end date"),
                        getDateFromField(csvRow, "Learning actual end date"),
                        outcome,
                        nvq,
                        tier2,
                        funding,
                        completion,
                        empStatus,
                        esm,
                        returnPeriod,
                        fundingLineType,
                        partnerUkprn,
                        ldfamTypeFundingIndicator,
                        ldfamTypeLdmA,
                        ldfamTypeLdmB,
                        ldfamTypeLdmC,
                        ldfamTypeLdmD,
                        ldfamTypeLdmE,
                        ldfamTypeLdmF,
                        ldfamTypeDamA,
                        ldfamTypeDamB,
                        ldfamTypeDamC,
                        ldfamTypeDamD,
                        ldfamTypeDamE,
                        ldfamTypeDamF,
                        ldfamCommunityLearningProvisionType,
                        ldfamTypeHouseholdSituationA,
                        ldfamTypeHouseholdSituationB,
                        localAuthorityCode,
                        partnerUkprnName,
                        esmTypeEmploymentIntensity,
                        startForFundingPurposes,
                        tierTwoSectorSubjectAreaName
                )
        )
    }

    private fun createEarningPeriods(csvRow: CSVFile, ukprn: Int, learnerReferenceNumber: String, aimSequenceNumber: Int, academicYear: Int, returnPeriod: Int) {
        for (i in 0..11) {
            val monthString = months[i] + " "
            val actual = if (i < 7) i + 13 % 7 else i - 6

            earningPeriodRepository.save(EarningPeriod(EarningPeriodPK(ukprn, learnerReferenceNumber, aimSequenceNumber, academicYear, actual),
                    csvRow.getCurrencyValue(monthString + "on programme earned cash"),
                    csvRow.getCurrencyValue(monthString + "balancing payment earned cash"),
                    csvRow.getCurrencyValue(monthString + "aim achievement earned cash"),
                    csvRow.getCurrencyValue(monthString + "job outcome earned cash"),
                    csvRow.getCurrencyValue(monthString + "learning support earned cash"),
                    returnPeriod))

        }
    }

    private fun getDateFromField(csvRow: CSVFile, dateString: String): LocalDate? {
        val dateOfBirthString = csvRow.getString(dateString)
        var dateOfBirth: LocalDate? = null
        if (dateOfBirthString != null && dateOfBirthString.isNotBlank()) {
            dateOfBirth = LocalDate.parse(dateOfBirthString, DateTimeFormatter.ofPattern("d/M/yyyy"))
        }
        return dateOfBirth
    }

    fun validateColumnHeader(expectedColumnHeader:Set<String>, csvColumnHeader:Set<String>, listAcceptableColumn:Boolean){
        if (!csvColumnHeader.containsAll(expectedColumnHeader)) {
            val expectedColumns = ArrayList<String>()

            for (column in expectedColumnHeader) {
                if (!csvColumnHeader.contains(column) ) {
                    expectedColumns.add(column)
                }
            }

            var errorMessage = "column $expectedColumns not found in the file."
            if (listAcceptableColumn) {
                errorMessage += " Acceptable column headings are $expectedColumnHeader"
            }

            throw RuntimeException(errorMessage)
        }
    }

}
