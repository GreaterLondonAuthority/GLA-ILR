/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.data

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import uk.gov.london.common.CSVFile
import uk.gov.london.common.CSVRowSource
import uk.gov.london.common.GlaUtils.getActualMonthNameFromAcademicPeriod
import uk.gov.london.common.skills.SkillsGrantType
import uk.gov.london.ilr.environment.Environment
import uk.gov.london.ilr.security.UserService
import java.io.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors
import javax.transaction.Transactional

@Service
@Transactional
class IlrDataService @Autowired constructor(
        val dataImportRepository: DataImportRepository,
        val fundingLearnerRecordRepository: FundingLearnerRecordRepository,
        val fundingSummaryRecordRepository: FundingSummaryRecordRepository,
        val learnerRepository: LearnerRepository,
        val learnerSummaryRepository: LearnerSummaryRepository,
        val occupancyRecordRepository: OccupancyRecordRepository,
        val supplementaryDataRepository: SupplementaryDataRepository,
        val occupancySummaryRepository: OccupancySummaryRepository,
        val healthProblemRepository: HealthProblemRepository,
        val healthProblemCategoryRepository: HealthProblemCategoryRepository,
        var userService: UserService,
        val environment: Environment) {

    internal var log = LoggerFactory.getLogger(javaClass)

    @Throws(Exception::class)
    fun dataImports(): List<DataImport> {
        val foundMap: MutableSet<String> = hashSetOf("")
        var filesSorted = dataImportRepository.findAllByOrderByCreatedOnDesc()
        filesSorted =filesSorted.filter { f -> f.importType != DataImportType.SUPPLEMENTARY_DATA }

        for (file in filesSorted) {
            if (file.importType!!.canSendToOPS) {
                val key = "${file.importType!!.getName()} ${file.academicYear} ${file.period}"
                if (!foundMap.contains(key)) {
                    foundMap.add(key)
                    file.canPushToOPS = true
                }
            }
        }
        return filesSorted
    }

    @Throws(Exception::class)
    fun upload(file: MultipartFile?) : UploadResult {

        var result : UploadResult = upload(file!!.originalFilename, file.inputStream)
        result.numberOfRecords = getNumberOfRecords(file)
        return result
    }

    @Throws(Exception::class)
    fun uploadSupplementalData(file: MultipartFile?) : UploadResult {

        if (!file!!.originalFilename!!.toUpperCase().endsWith(".CSV")) {
            throw RuntimeException("Upload failed: File must be in CSV format, to do this save an excel file as a .CSV")
        }

        var result : UploadResult = upload(file!!.originalFilename, file.inputStream, DataImportType.SUPPLEMENTARY_DATA)
        result.numberOfRecords = getNumberOfRecords(file)
        return result
    }

    fun getNumberOfRecords(file: MultipartFile?) : Long {
        var bis : ByteArrayInputStream = ByteArrayInputStream(file!!.getBytes())
        var br : BufferedReader = BufferedReader(InputStreamReader(bis))
        return br.lines().count()-1;
    }

    fun upload(fileName: String?, inputStream: InputStream): UploadResult {
        val importType = getDataImportTypeFromFileName(fileName)
        return upload(fileName, inputStream, importType)
    }
    fun upload(fileName: String?, inputStream: InputStream, importType: DataImportType): UploadResult {
        var uploadResult : UploadResult = UploadResult()
        uploadResult.originalFilename = fileName!!
        uploadResult.errorMessages = mutableListOf("")


        val dataImport = DataImport(fileName = fileName)
        dataImport.status = DataImportStatus.PENDING
        dataImport.createdOn = environment.now()
        dataImport.importType = importType
        dataImport.status = DataImportStatus.COMPLETE

        if (userService.currentUserName() == null) {
            dataImport.createdBy = "System"
        } else {
            dataImport.createdBy = userService.currentUserName()
        }
        dataImportRepository.save(dataImport)

        try {
            when (importType) {
                DataImportType.LEARNER -> {
                    createFundingLearnerRecords(inputStream)
                }

                DataImportType.FUNDING_SUMMARY -> {
                    val fsrFile = ESFMonthlyRecordFile.parse(fileName)
                    if (!fsrFile.isValid) {
                        uploadResult.errorMessages!!.add("Invalid file format: 'Funding Summary Report YYYY MM.csv'")
                        throw RuntimeException("Invalid file format: 'Funding Summary Report YYYY MM.csv'")
                    }
                    dataImport.academicYear = fsrFile.year
                    dataImport.period = fsrFile.month
                    createSummaryRecords(inputStream, fsrFile.year, fsrFile.month)
                }

                DataImportType.OCCUPANCY_REPORT -> {
                    val fsrFile = ESFMonthlyRecordFile.parse(fileName)
                    if (!fsrFile.isValid) {
                        uploadResult.errorMessages!!.add("Invalid file format: 'Occupancy Report YYYY MM.csv'")
                        throw RuntimeException("Invalid file format: 'Occupancy Report YYYY MM.csv'")
                    }
                    dataImport.academicYear = fsrFile.year
                    dataImport.period = fsrFile.month
                    createOccupancyRecords(inputStream, fsrFile.year, fsrFile.month)
                }

                DataImportType.SUPPLEMENTARY_DATA -> {
                    createSupplementaryDataRecords(inputStream,uploadResult)
                }
                DataImportType.HEALTH_PROBLEM -> {
                    val fsrFile = ESFMonthlyRecordFile.parse(fileName)
                    if (!fsrFile.isValid) {
                        uploadResult.errorMessages!!.add("Invalid file format: 'SILR_YYYY_MM_LLDDHealthProblem.csv'")
                        throw RuntimeException("Invalid file format: 'SILR_YYYY_MM_LLDDHealthProblem.csv'")
                    }
                    dataImport.academicYear = fsrFile.year
                    dataImport.period = fsrFile.month
                    createHealthProblemsRecords(inputStream, fsrFile.year, fsrFile.month)
                }
            }
        } catch (e: IOException) {
            log.error("failed to create records from $fileName due to", e.message)
            dataImport.status = DataImportStatus.FAILED
            uploadResult.errorMessages!!.add("Unable to create records from file: $fileName due to" + e.message)
            throw Exception("Unable to create records from file: $fileName")
        }

        dataImportRepository.save(dataImport)

        return uploadResult
    }
    fun uploadStatic(fileName: String, inputStream: InputStream) {
        try{
            if(fileName.equals("Data Initialiser - Health Problem Category.csv")) {
                    createHealthProblemCategory(inputStream)
            }
        } catch (e :Exception){
            log.error("failed to create records from $fileName due to", e.message)
            throw Exception("Unable to create data initialiser data from file: $fileName")
        }
    }

    private fun createSupplementaryDataRecords(inputStream: InputStream, uploadResult: UploadResult) {
        val csvFile = CSVFile(inputStream)
        val now = environment.now()

        val columns = listOf("Learner reference number", "UKPRN", "Investment priority claimed under", "Has Basic Skills upon joining", "Is homeless (broad definition)").map{i -> i.toUpperCase()}
        val actualColumns = csvFile.headers.map { h -> h.toUpperCase() }

        if (!columns.containsAll(actualColumns)) {
            val missingColumns = HashSet<String>()
            for (column in columns) {
                if (!actualColumns.contains(column)) {
                    missingColumns.add(column)
                }
            }
            throw RuntimeException("Invalid column headers, missing $missingColumns")

        }

        while (csvFile.nextRow()) {
            processSupplementaryDataRecord(csvFile,uploadResult, now)
        }

    }

    private fun processSupplementaryDataRecord(csvRow: CSVFile, uploadResult:UploadResult , now: OffsetDateTime) {

        val learnerReferenceNumber = csvRow.getString("Learner reference number")
        val ukprn = csvRow.getInteger("UKPRN")
        val priority = csvRow.getString("Investment priority claimed under")
        val basicSkills = csvRow.getInteger("Has Basic Skills upon joining")
        val homeless = csvRow.getInteger("Is homeless (broad definition)")



        val supplementaryData = SupplementaryData(SupplementaryDataPK(ukprn, learnerReferenceNumber), priority, basicSkills, homeless, now)

        validateSupplementaryData(supplementaryData)
        validateLearnerRefData(supplementaryData,uploadResult)

        supplementaryDataRepository.save(supplementaryData)

    }

    private fun validateSupplementaryData(supplementaryData: SupplementaryData) {
        val validPriorities = listOf("1.1", "1.2", "2.1")
        if (supplementaryData.hasBasicSkills !in 1..3 ||
              supplementaryData.isHomeless !in 1..3 ||
                supplementaryData.investmentPriorityClaimedUnder !in validPriorities)  {
            throw RuntimeException("File upload failed as one or more cells contain information which is not in the specified format. Amend the content and try again.")
        }


    }

    private fun validateLearnerRefData(supplementaryData: SupplementaryData,uploadResult:UploadResult) {
        if (occupancyRecordRepository.countAllByLearnerReferenceNumberAndUkprn(supplementaryData.id.learnerReferenceNumber, supplementaryData.id.ukprn) == 0) {
            uploadResult.errorMessages.add("A record for Learner reference number " + supplementaryData.id.learnerReferenceNumber +
                    " doesn't exist for UKPRN[" + supplementaryData.id.ukprn + "], valid Learner reference numbers must be uploaded")
        }
    }

    private fun createOccupancyRecords(inputStream: InputStream, academicYear: Int, period: Int) {
        val csvFile = CSVFile(inputStream)
        while (csvFile.nextRow()) {
            processOccupancyRecord(csvFile, academicYear, period)
        }
    }
    private fun createHealthProblemsRecords(inputStream: InputStream, year: Int, period: Int) {
        val csvFile = CSVFile(inputStream)

        val columns = listOf<String>("Year", "SNAPSHOT", "RETURN", "AcMnth", "UKPRN", "PrevUKPRN", "LearnRefNumber", "PrevLearnRefNumber", "ULN", "LLDDCat", "PrimaryLLDD")
        val actualColumns = csvFile.headers

        if (!columns.containsAll(actualColumns)) {
            val expectedColumns = ArrayList<String>()

            for (column in columns) {
                if (!actualColumns.contains(column)) {
                    expectedColumns.add(column)
                }
            }
            throw RuntimeException("column $expectedColumns not found in the file. Acceptable field names (column headings) are $columns")
        }

        deletePreviousFileUpload(year, period);
        while (csvFile.nextRow()) {
            processHealthProblemRecord(csvFile, year, period)
        }
    }

    private fun createHealthProblemCategory(inputStream: InputStream) {
        val csvFile = CSVFile(inputStream)

        while (csvFile.nextRow()) {
            processHealthProblemCategory(csvFile)
        }
    }

    private fun deletePreviousFileUpload (year: Int, period: Int) {
        healthProblemRepository.deleteAllByYearAndReturnNumber(year.toString(), period);
    }

    private fun processOccupancyRecord(csvRow: CSVFile, academicYear: Int, period: Int) {
        val learnerReferenceNumber = csvRow.getString("Learner reference number")
        val postCode = csvRow.getString("Postcode prior to enrolment")

        val learner = learnerRepository.save(
                Learner(
                        csvRow.getString("Unique learner number").toLong(),
                        learnerReferenceNumber,
                        postCode,
                        LocalDate.parse(csvRow.getString("Date of birth"), DateTimeFormatter.ofPattern("d/M/yyyy"))
                )
        )

        occupancyRecordRepository.save(
                OccupancyRecord(
                        learner = learner,
                        ukprn = csvRow.getInteger("UKPRN"),
                        academicYear = academicYear,
                        period = period,
                        learnerReferenceNumber = learnerReferenceNumber,
                        postCodePriorToEnrolment = postCode,
                        aimSequenceNumber = csvRow.getInteger("Aim sequence number"),
                        learningAimReference = csvRow.getString("Learning aim reference"),
                        learningAimTitle = csvRow.getString("Learning aim title"),
                        learningStartDate = LocalDate.parse(csvRow.getString("Learning start date"), DateTimeFormatter.ofPattern("d/M/yyyy")),
                        learningPlannedEndDate = LocalDate.parse(csvRow.getString("Learning planned end date"), DateTimeFormatter.ofPattern("d/M/yyyy")),
                        completionStatus = csvRow.getInteger("Completion status"),
                        learningStartDatePostCode = csvRow.getString("Learning start date postcode"),
                        fundingLineType = csvRow.getString("Funding line type"),
                        grantType = getSkillsGrantType(csvRow.getString("Funding line type")),
                        deliveryLocationPostCode = csvRow.getString("Delivery location postcode"),
                        totalOnProgrammeEarnedCash = getBidDecimal(csvRow, "Total on programme earned cash"),
                        totalBalancingPaymentEarnedCash = getBidDecimal(csvRow, "Total balancing payment earned cash"),
                        totalAimAchievementEarnedCash = getBidDecimal(csvRow, "Total aim achievement earned cash"),
                        totalJobOutcomeEarnedCash = getBidDecimal(csvRow, "Total job outcome earned cash"),
                        totalLearningSupportEarnedCash = getBidDecimal(csvRow, "Total learning support earned cash"),
                        totalEarnedCash = getBidDecimal(csvRow, "Total earned cash"),
                        monthBreakdown = getMonthBreakdown(csvRow, academicYear),
                        fundingModel = csvRow.getString("Funding model"),
                        outcome = csvRow.getString("Outcome"),
                        ldfamTypeDevolvedAreaMonitoringA = csvRow.getString("Learning delivery funding and monitoring type - devolved area monitoring (A)"),
                        ldfamTypeDevolvedAreaMonitoringB = csvRow.getString("Learning delivery funding and monitoring type - devolved area monitoring (B)"),
                        ldfamTypeDevolvedAreaMonitoringC = csvRow.getString("Learning delivery funding and monitoring type - devolved area monitoring (C)"),
                        ldfamTypeDevolvedAreaMonitoringD = csvRow.getString("Learning delivery funding and monitoring type - devolved area monitoring (D)")
                )
        )
    }
    private fun processHealthProblemRecord(csvRow: CSVFile, academicYear: Int, period: Int) {
        healthProblemRepository.save(
                HealthProblemRecord(
                        year = csvRow.getString("Year"),
                        snapshot = csvRow.getInteger("SNAPSHOT"),
                        returnNumber = csvRow.getInteger("RETURN"),
                        month = csvRow.getInteger("AcMnth"),
                        ukprn = csvRow.getInteger("UKPRN"),
                        prevUkprn = csvRow.getInteger("PrevUKPRN"),
                        learnerRefNumber = csvRow.getString("LearnRefNumber"),
                        prevLearnerRefNumber = csvRow.getString("PrevLearnRefNumber"),
                        uniqueLearnerNumber = csvRow.getInteger("ULN"),
                        llddCategory = csvRow.getInteger("LLDDCat"),
                        primaryLldd = csvRow.getInteger("PrimaryLLDD")
                        )
        )
    }
    private fun processHealthProblemCategory(csvRow: CSVFile) {
        healthProblemCategoryRepository.save(
                HealthProblemCategory(
                        code = csvRow.getInteger("Category Code"),
                        description = csvRow.getString("Description")
                )
        )
    }

    private fun getMonthBreakdown(csvRow: CSVFile, academicYear: Int): List<OccupancyRecordMonthBreakdown> {
        val monthBreakdown = ArrayList<OccupancyRecordMonthBreakdown>()
        for (period in 1..12) {
            val actualMonthName = getActualMonthNameFromAcademicPeriod(period).toLowerCase().capitalize()

            monthBreakdown.add(OccupancyRecordMonthBreakdown(
                    academicYear = academicYear,
                    period = period,
                    grantType = SkillsGrantType.AEB_GRANT,
                    onProgrammeEarnedCash = getBidDecimal(csvRow, "$actualMonthName on programme earned cash"),
                    balancingPaymentEarnedCash = getBidDecimal(csvRow, "$actualMonthName balancing payment earned cash"),
                    aimAchievementEarnedCash = getBidDecimal(csvRow, "$actualMonthName aim achievement earned cash"),
                    jobOutcomeEarnedCash = getBidDecimal(csvRow, "$actualMonthName job outcome earned cash"),
                    learningSupportEarnedCash = getBidDecimal(csvRow, "$actualMonthName learning support earned cash"),
                    subContractedOrPartnershipUkprn = csvRow.getString("Sub contracted or partnership UKPRN")
            ))
        }
        return monthBreakdown
    }

    private fun getBidDecimal(csvRow: CSVFile, column: String): BigDecimal? {
        return try {
            BigDecimal(csvRow.getString(column))
        } catch (e: Exception) {
            null
        }
    }


    fun getDataImportTypeFromFileName(fileName: String?) = (DataImportType.getTypeByFilename(fileName!!)
            ?: throw IllegalArgumentException("Unable to identify file type by filename: $fileName"))

    private fun createFundingLearnerRecords(inputStream: InputStream) {
        val csvFile = CSVFile(inputStream)
        while (csvFile.nextRow()) {
            fundingLearnerRecordRepository.saveAll(getFundingLearnerRecords(csvFile))
        }
    }

    private fun createSummaryRecords(inputStream: InputStream, academicYear: Int, period: Int) {

        fundingSummaryRecordRepository.deleteByAcademicYearAndPeriod(academicYear, period)

        val csvFile = CSVFile(inputStream)
        while (csvFile.nextRow()) {
            fundingSummaryRecordRepository.save(getFundingSummaryRecords(csvFile, academicYear, period))
        }
    }

    fun getFundingLearnerRecords(ukprn: Int?, year: Int?, month: Int?, pageable: Pageable?): Page<FundingLearnerRecord> {
        return fundingLearnerRecordRepository.findAll(ukprn, year, month, pageable)
    }

    fun getFundingSummaryRecords(ukprns: Set<Int>?, academicYear: Int?, period: Int?, pageable: Pageable?): Page<FundingSummaryRecord> {
        return fundingSummaryRecordRepository.findAll(ukprns, academicYear, period, pageable)
    }

    fun getLearners(id: Long?, pageable: Pageable?): Page<Learner> {
        return if (id == null && pageable != null) {
            learnerRepository.findAll(pageable)
        } else {
            learnerRepository.findAll(id, pageable)
        }
    }

    fun getLearnersSummaries(learner: String?, ukprn: Set<Int?>?, academicYear: Int?, filterBySupplementaryData: Boolean?, pageable: Pageable?): Page<LearnerSummary> {
        return learnerSummaryRepository.findAll(learner, ukprn, academicYear,filterBySupplementaryData, pageable!!)
    }

    fun getLearnersAcademicYears(): List<Int> {
        return occupancyRecordRepository.findDistinctAcademicYears()
    }

    fun getLearnersPeriods(academicYear: Int?): List<Int> {
        return occupancyRecordRepository.findDistinctPeriods(academicYear)
    }

    fun countDistinctUkprns(): Int {
        return occupancyRecordRepository.countDistinctUkprns()
    }

    fun getLearnerRecord(learner: Long): Learner {
        return learnerRepository.findById(learner).get()
    }

    fun getFundingLearnerRecords(csvRow: CSVRowSource): List<FundingLearnerRecord> {
        val records = ArrayList<FundingLearnerRecord>()
        for (month in 1..12) {
            val monthFundingLearnerRecord = getFundingLearnerRecord(csvRow, month)
            records.add(monthFundingLearnerRecord)
        }
        return records
    }

    fun getFundingLearnerRecord(csvRow: CSVRowSource, month: Int): FundingLearnerRecord {
        return FundingLearnerRecord(
                year = parseYear(csvRow.getString("Year")),
                ukprn = csvRow.getInteger("UKPRN"),
                learnRefNumber = csvRow.getString("LearnRefNumber"),
                month = month,
                totalPayment = csvRow.getCurrencyValue("L_TotalPayment_ACM$month"))
    }

    fun getFundingSummaryRecords(csvRow: CSVRowSource, academicYear: Int, period: Int): FundingSummaryRecord {
        val actualMonth = if (period <= 5) period + 7 else period - 5
        val actualYear = if (period > 5) academicYear + 1 else academicYear
        val monthColumn = DateTimeFormatter.ofPattern("MMM-yy").format(LocalDate.of(actualYear, actualMonth, 1))

        return FundingSummaryRecord(
                academicYear = academicYear,
                period = period,
                actualYear = actualYear,
                actualMonth = actualMonth,
                ukprn = csvRow.getInteger("UKPRN"),
                fundingLine = csvRow.getString("FundingLineType"),
                source = csvRow.getString("Source"),
                category = csvRow.getString("Category"),
                monthTotal = csvRow.getCurrencyValue(monthColumn),
                totalPayment = csvRow.getCurrencyValue("Year To Date"))
    }

    /**
     * @param stringYear financial year format: "YYYY/YY" (ex: 2018/19)
     * @return in the example above 2018
     */
    fun parseYear(stringYear: String): Int? {
        return Integer.valueOf(stringYear.substring(0, 4))
    }

    fun parseSkillsGrantType(type: String): String {
        return if (type.equals(SkillsGrantType.AEB_PROCURED.name)) {
            "Procured"
        } else {
            "Non procured"
        }
    }

    fun dataImportCount(): Long {
        return dataImportRepository.count()
    }

    fun getDataImportRecord(id: Int): DataImport {
        return dataImportRepository.findById(id).orElse(null)
    }

    fun updateDataImportRecord(record: DataImport): DataImport {
        return dataImportRepository.save(record)
    }

    fun fundingLearnerRecordsCount(): Long {
        return fundingLearnerRecordRepository.count()
    }

    fun getOccupancyDataForLearner(learnerId: Long): List<OccupancyRecord> {
        return occupancyRecordRepository.findAllByLearnerId(learnerId)
    }

    fun getOccupancySummaryByYearAndUkprn(year: Int?, ukprns: Set<Int>?): List<OccupancySummary> {
        return if (year == null  && ukprns == null){
            occupancySummaryRepository.findAll()
        } else if (year == null ) {
            occupancySummaryRepository.findByUkprnIn(ukprns)
        } else if (ukprns == null ) {
            occupancySummaryRepository.findAllByAcademicYear(year)
        } else {
            occupancySummaryRepository.findByUkprnIn(ukprns).stream().filter{occ -> occ.academicYear == year}.collect(Collectors.toList<OccupancySummary>())
        }
    }

    fun getUniqueLearners(year: Int?, ukprn: Set<Int>?): Int {
        return if (year == null && ukprn == null) {
            occupancyRecordRepository.findAllUniqueLearners()
        } else if (year == null) {
            occupancyRecordRepository.findAllUniqueLearnersByUkprn(ukprn)
        } else if (ukprn == null) {
            occupancyRecordRepository.findAllUniqueLearnersByYear(year)
        } else {
            occupancyRecordRepository.findAllUniqueLearnersByYearAndUkprn(year, ukprn)
        }
    }

    fun getTotalDeliveryByYearGrantTypeAndUkprn(year: Int?, grantType: SkillsGrantType, period: Int?, ukprn: Set<Int>?): Long? {
        return if (year == null && ukprn == null) {
            occupancyRecordRepository.totalDeliveryByGrantType(grantType.toString())
        } else if (year == null) {
            occupancyRecordRepository.totalDeliveryByGrantTypeAndUkprn(grantType.toString(), ukprn)
        } else if (ukprn == null) {
            occupancyRecordRepository.totalDeliveryByYearAndGrantType(year, grantType.toString())
        } else if (period == null) {
            occupancyRecordRepository.totalDeliveryByYearGrantTypeAndUkprn(year, grantType.toString(), ukprn)
        } else {
            occupancyRecordRepository.totalDeliveryByYearGrantTypePeriodAndUkprn(year, grantType.toString(), period, ukprn)
        }
    }

    fun getCurrentAcademicYear(): Int {
        val year = YearMonth.now().year
        return if (YearMonth.now().monthValue < 8) year - 1 else year
    }

    fun getLearnerLatestHealthProblemRecord(learnerRefNumber: String?): HealthProblemRecord? {

        return healthProblemRepository.findLatestHealthProblemRecord(learnerRefNumber);
    }
}

fun getSkillsGrantType(fundingLine: String): SkillsGrantType {
    return if (fundingLine.contains("non-procured")) SkillsGrantType.AEB_GRANT else SkillsGrantType.AEB_PROCURED
}
