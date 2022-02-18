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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import uk.gov.london.ilr.fundingsummary.FundingSummaryRecordRepository
import uk.gov.london.ilr.init.DataInitialiser.SETUP
import uk.gov.london.ilr.init.DataInitialiser.TEARDOWN
import uk.gov.london.ilr.init.DataInitialiserAction
import uk.gov.london.ilr.init.DataInitialiserModule
import uk.gov.london.ilr.security.UserService

@Component
class DataImportInitialiser(@Autowired val dataImportRepository: DataImportRepository,
                            @Autowired val fundingSummaryRecordRepository: FundingSummaryRecordRepository,
                            @Autowired val fileUploadHandler: FileUploadHandler,
                            @Autowired val userService: UserService,
                            @Autowired val jdbcTemplate: JdbcTemplate,
                            @Autowired var fileService: FileService) : DataInitialiserModule {

    internal var log = LoggerFactory.getLogger(this.javaClass)

    override fun actions(): Array<DataInitialiserAction> {
        return arrayOf(
                DataInitialiserAction("Delete ILR data", TEARDOWN, false, Runnable { this.deleteIlrData() }),
                DataInitialiserAction("Delete ILR reference data", TEARDOWN, false, Runnable { this.deleteRefDataMapping() }),
                DataInitialiserAction("Setup Test Funding  Summary Report data", SETUP, false, Runnable { this.createTestFundingSummaryReportData() }),
                DataInitialiserAction("Setup Test Occupancy Report data", SETUP, false, Runnable { this.createTestOccupancyReportData() }),
                DataInitialiserAction("Setup Test Supplemental Data", SETUP, false, Runnable { this.createTestSupplementalData() }),
                DataInitialiserAction("Setup Test Data Validation Issues", SETUP, false, Runnable { this.createTestDataValidationIssues() }),
                DataInitialiserAction("Setup reference data mappings", SETUP, false, Runnable { this.createRefDataMapping() }),
                DataInitialiserAction("Setup test provider allocation data", SETUP, false, Runnable { this.createTestProviderAllocationData() })
        )
    }

    private fun deleteIlrData() {
        log.info("Deleting test ILR data")

        dataImportRepository.deleteAll()
        fundingSummaryRecordRepository.deleteAll()
        deleteOccupancyRecords()
        deleteLearnerRecords()
        deleteRefDataMapping()
        deleteProviderAllocationData()
        fileService.deleteTestData()
    }

    private fun deleteOccupancyRecords() {
        jdbcTemplate.execute("delete from occupancy_record_month_breakdown")
        jdbcTemplate.execute("delete from occupancy_record")
    }

    private fun deleteLearnerRecords() {
        jdbcTemplate.execute("delete from learner")
        jdbcTemplate.execute("delete from learning_delivery")
        jdbcTemplate.execute("delete from earning_period")
        jdbcTemplate.execute("delete from learning_aim")
        jdbcTemplate.execute("delete from supplementary_data")
    }

    private fun deleteRefDataMapping() {
        jdbcTemplate.execute("delete from ref_data_mapping")
    }

    private fun deleteProviderAllocationData() {
        log.info("Deleting provider allocation data")
        jdbcTemplate.execute("delete from provider_allocation")
    }

    private fun createTestOccupancyReportData() {
        log.info("Creating test occupancy report data")
        uploadNewOccupancyReport("")
    }

    private fun createTestSupplementalData() {
        log.info("Creating test Supplemental Report")
        uploadOSupplementalData("")
    }

    private fun uploadNewOccupancyReport(fileName: String) {
        try {
            fileUploadHandler.upload(fileName, this.javaClass.getResourceAsStream(fileName), DataImportType.OCCUPANCY_REPORT)
        } catch (e: Exception) {
            log.error("failed to create test Occupancy Report data: $fileName", e)
        }
    }

    private fun uploadOSupplementalData(fileName: String) {
        try {
            fileUploadHandler.upload(fileName, this.javaClass.getResourceAsStream(fileName), DataImportType.SUPPLEMENTARY_DATA, "")
        } catch (e: Exception) {
            log.error("failed to create test supplement report: $fileName", e)
        }
    }

    private fun createTestDataValidationIssues() {
        log.info("Creating test Data Validation Issues")
        uploadDataValidationIssues("")
        uploadDataValidationIssues("")
        uploadDataValidationIssues("")
        uploadDataValidationIssues("")
    }

    private fun uploadDataValidationIssues(fileName: String) {
        try {
            fileUploadHandler.upload(fileName, this.javaClass.getResourceAsStream(fileName), DataImportType.DATA_VALIDATION_ISSUES)
        } catch (e: Exception) {
            log.error("failed to create test data validation issues: $fileName", e)
        }
    }

    private fun createTestFundingSummaryReportData() {
        log.info("Creating test funding summary report data")

        uploadFundingSummaryReport("")
        uploadFundingSummaryReport("")
        uploadFundingSummaryReport("")
    }

    private fun uploadFundingSummaryReport(fileName: String) {
        try {
            fileUploadHandler.upload(fileName, this.javaClass.getResourceAsStream(fileName), DataImportType.FUNDING_SUMMARY)
        } catch (e: Exception) {
            log.error("failed to create test funding summary report data $fileName", e)
        }
    }

    private fun createRefDataMapping() {
        log.info("Creating reference data mapping test data")

        try {
            val fileName = ""
            fileUploadHandler.upload(fileName, this.javaClass.getResourceAsStream(fileName), DataImportType.ILR_CODE_VALUES)
        } catch (e: Exception) {
            log.error("failed to create reference data mapping test data", e)
        }
    }

    private fun createTestProviderAllocationData() {
        log.info("Creating provider allocation test data")

        try {
            val fileName = ""
            fileUploadHandler.upload(fileName, this.javaClass.getResourceAsStream(fileName), DataImportType.PROVIDER_ALLOCATION)
        } catch (e: Exception) {
            log.error("failed to create provider allocation test data", e)
        }
    }
}
