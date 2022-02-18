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
    var reportFirstFormatChangeYear: Int = 2020

    var reportSecondFormatChangeYear: Int = 2021

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
        val expectedColumns = mutableSetOf(LRN, UKPRN, "Return", "Unique learner number", "Aim sequence number",
                "Date of birth", "LLDD and health problem", "Ethnicity", "Sex", "Postcode prior to enrolment", "Prior attainment", "Provider specified learner monitoring (A)", "Provider specified learner monitoring (B)", // Learner
                "Learning aim reference", "Outcome", "Notional NVQ level", "Tier 2 sector subject area", "Funding model", "Completion status", "ESM Type - benefit status indicator", "Learner employment status", // Learner Delivery
                "Funding line type", "ESFA Funding line type", "Partner UKPRN", "LDFAM type - full or co funding indicator", "LDFAM type - LDM (A)", "LDFAM type - LDM (B)", "LDFAM type - LDM (C)", "LDFAM type - LDM (D)", // Learenr Delivery
                "LDFAM type - LDM (E)", "LDFAM type - LDM (F)", "LDFAM type - DAM (A)", "LDFAM type - DAM (B)", "LDFAM type - DAM (C)", "LDFAM type - DAM (D)", "LDFAM type - Community Learning provision type",// Learenr Delivery
                "Learning start date", "Learning planned end date", "Learning actual end date") // Learenr Delivery

        if ( academicYear >= reportFirstFormatChangeYear ) {
            expectedColumns.addAll(mutableSetOf("Provider name","Family name", "Given names", "Tier 2 sector subject area name", "Local authority code", "LDFAM type - DAM (E)", "LDFAM type - DAM (F)",
                    "LDFAM type - household situation (A)", "LDFAM type - household situation (B)", "ESM type - employment intensity indicator", "Start for funding purposes", "Partner UKPRN name"))
        }

        if ( academicYear >= reportSecondFormatChangeYear ) {
            expectedColumns.addAll(mutableSetOf("Policy uplift rate","Age at start", "Basic skills type", "Policy uplift category", "ESM type - Length of unemployment"))
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
            val providerName = getString(csvRow,"Provider name")
            providerRepository.save(Provider(id, providerName)
            )
        }
    }

    private fun createLearningAim(csvRow: CSVFile,aimReference: String,  academicYear: Int) {
        val id = LearningAimPK(aimReference, academicYear)
        if (!learningAimRepository.existsById(id)) {
            val title = getString(csvRow, "Learning aim title").orEmpty()
            learningAimRepository.save(LearningAim(id, title)
            )
        }
    }

    private fun createLearner(csvRow: CSVFile, learnerReferenceNumber: String, ukprn: Int, academicYear: Int, uln: Long, returnPeriod: Int) {
        val id = LearnerPK(learnerReferenceNumber, ukprn, academicYear)
        if (!learnerRepository.existsById(id)) {
            val dateOfBirth: LocalDate? = getDateFromField(csvRow, "Date of birth")
            val lldd = getInteger(csvRow,"LLDD and health problem")
            val ethnicity = getInteger(csvRow,"Ethnicity")
            val priorAttainment = getInteger(csvRow,"Prior attainment")
            val gender = getString(csvRow,"Sex")
            val postcode = getString(csvRow,"Postcode prior to enrolment")
            val monA = getString(csvRow,"Provider specified learner monitoring (A)")
            val monB = getString(csvRow,"Provider specified learner monitoring (B)")
            val familyName = getString(csvRow,"Family name")
            val givenName = getString(csvRow,"Given names")
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
        val aimReference = getString(csvRow,"Learning aim reference")
        val outcome = getInteger(csvRow,"Outcome")
        val nvq = getString(csvRow,"Notional NVQ level")
        val tier2 = getString(csvRow,"Tier 2 sector subject area")
        val funding = getInteger(csvRow,"Funding model")
        val completion = getInteger(csvRow,"Completion status")
        val esm = getInteger(csvRow,"ESM Type - benefit status indicator")
        val empStatus = getInteger(csvRow,"Learner employment status")
        val fundingLineType = getString(csvRow,"Funding line type")
        val esfaFundingLineType = getString(csvRow,"ESFA Funding line type")
        val partnerUkprn = getInteger(csvRow,"Partner UKPRN")
        val ldfamTypeFundingIndicator = getInteger(csvRow,"LDFAM type - full or co funding indicator")
        val ldfamTypeLdmA = getInteger(csvRow,"LDFAM type - LDM (A)")
        val ldfamTypeLdmB = getInteger(csvRow,"LDFAM type - LDM (B)")
        val ldfamTypeLdmC = getInteger(csvRow,"LDFAM type - LDM (C)")
        val ldfamTypeLdmD = getInteger(csvRow,"LDFAM type - LDM (D)")
        val ldfamTypeLdmE = getInteger(csvRow,"LDFAM type - LDM (E)")
        val ldfamTypeLdmF = getInteger(csvRow,"LDFAM type - LDM (F)")
        val ldfamTypeDamA = getInteger(csvRow,"LDFAM type - DAM (A)")
        val ldfamTypeDamB = getInteger(csvRow,"LDFAM type - DAM (B)")
        val ldfamTypeDamC = getInteger(csvRow,"LDFAM type - DAM (C)")
        val ldfamTypeDamD = getInteger(csvRow,"LDFAM type - DAM (D)")
        val ldfamTypeDamE = getString(csvRow,"LDFAM type - DAM (E)")
        val ldfamTypeDamF = getString(csvRow,"LDFAM type - DAM (F)")
        val ldfamCommunityLearningProvisionType = getInteger(csvRow,"LDFAM type - Community Learning provision type")
        val ldfamTypeHouseholdSituationA = getString(csvRow,"LDFAM type - household situation (A)")
        val ldfamTypeHouseholdSituationB = getString(csvRow,"LDFAM type - household situation (B)")
        val localAuthorityCode = getString(csvRow,"Local authority code")
        val partnerUkprnName = getString(csvRow,"Partner UKPRN name")
        val esmTypeEmploymentIntensity = getInteger(csvRow,"ESM type - employment intensity indicator")
        val startForFundingPurposes = getInteger(csvRow,"Start for funding purposes")
        val tierTwoSectorSubjectAreaName = getString(csvRow,"Tier 2 sector subject area name")
        val policyUpliftRate = getInteger(csvRow,"Policy uplift rate")
        val ageAtStart = getInteger(csvRow,"Age at start")
        val basicSkillsType = getString(csvRow,"Basic skills type")
        val policyUpliftCategory = getString(csvRow,"Policy uplift category")
        val esmTypeLengthOfUnemployment = getInteger(csvRow,"ESM type - Length of unemployment")

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
                        esfaFundingLineType,
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
                        tierTwoSectorSubjectAreaName,
                        policyUpliftRate,
                        ageAtStart,
                        basicSkillsType,
                        policyUpliftCategory,
                        esmTypeLengthOfUnemployment
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
                if (!csvColumnHeader.stream().anyMatch { o -> stripStr(o).equals(stripStr(column)) }) {
                    expectedColumns.add(column)
                }
            }
            if (expectedColumns.size > 0) {
                var errorMessage = "column $expectedColumns not found in the file."
                if (listAcceptableColumn) {
                    errorMessage += " Acceptable column headings are $expectedColumnHeader"
                }

                throw RuntimeException(errorMessage)
            }
        }
    }

    private fun stripStr(str : String ):String{
        return str.replace(Regex("\\s+"),"").toLowerCase();
    }

    private fun getString(csvRow: CSVFile, heading : String) : String? {
        return csvRow.getStringIfPresent(getHeaderKey(csvRow, heading))
    }

    private fun getInteger(csvRow: CSVFile, heading : String) : Int? {
        return csvRow.getIntegerIfPresent(getHeaderKey(csvRow, heading))
    }

    private fun getHeaderKey(csvRow: CSVFile, heading : String) :String? {
        return if (csvRow.headers.contains(heading)) heading
        else csvRow.headers.find {h -> return if (stripStr(h).equals(stripStr(heading))) h else heading }
    }


    fun validateOccupancyRecordExists(ukprn: Int, learnerReferenceNumber: String, year: Int?) : Boolean {
        return learnerRepository.recordExists(ukprn, learnerReferenceNumber, year)
    }

}
