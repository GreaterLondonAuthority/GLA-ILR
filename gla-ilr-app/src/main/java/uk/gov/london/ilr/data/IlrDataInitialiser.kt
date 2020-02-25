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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import uk.gov.london.ilr.init.DataInitialiser.SETUP
import uk.gov.london.ilr.init.DataInitialiser.TEARDOWN
import uk.gov.london.ilr.init.DataInitialiserAction
import uk.gov.london.ilr.init.DataInitialiserModule

@Component
class IlrDataInitialiser(@Autowired val dataImportRepository: DataImportRepository,
                         @Autowired val fundingLearnerRecordRepository: FundingLearnerRecordRepository,
                         @Autowired val fundingSummaryRecordRepository: FundingSummaryRecordRepository,
                         @Autowired val learnerRepository: LearnerRepository,
                         @Autowired val supplementaryDataRepository: SupplementaryDataRepository,
                         @Autowired val occupancyRecordRepository: OccupancyRecordRepository,
                         @Autowired val healthProblemRepository: HealthProblemRepository,
                         @Autowired val healthProblemCategoryRepository: HealthProblemCategoryRepository,
                         @Autowired val ilrDataService: IlrDataService,
                         @Autowired val jdbcTemplate: JdbcTemplate) : DataInitialiserModule {

    internal var log = LoggerFactory.getLogger(this.javaClass)

    override fun actions(): Array<DataInitialiserAction> {
        return arrayOf(
                DataInitialiserAction("Delete ILR data", TEARDOWN, false, Runnable { this.deleteIlrData() }),
                DataInitialiserAction("Setup Test Funding  Summary Report data", SETUP, false, Runnable { this.createTestFundingSummaryReportData() }),
                DataInitialiserAction("Setup Test Occupancy Report data", SETUP, false, Runnable { this.createTestOccupancyReportData() }),
                DataInitialiserAction("Setup Test Funding Learner data", SETUP, false, Runnable { this.createTestFundingLearnerData() }),
                DataInitialiserAction("Populating null occupancy record grant type entries", SETUP, true, Runnable { this.populateOccupancyReportGrantType() }),
                DataInitialiserAction("Setup Test Supplemental Data", SETUP, false, Runnable { this.createTestSupplementalData() }),
                DataInitialiserAction("Setup Test health problems Data", SETUP, false, Runnable { this.createTestHealthProblemData() }),
                DataInitialiserAction("Setup Test health problems category Data", SETUP, true, Runnable { this.createTestHealthProblemCategoryData() })
        )
    }

    // TODO : add task / tech debt to remove this in 1.2
    private fun populateOccupancyReportGrantType() {
        for (occRec in occupancyRecordRepository.findAll()) {
            occRec.grantType = getSkillsGrantType(occRec.fundingLineType)
        }
    }

    private fun deleteIlrData() {
        log.info("Deleting test ILR data")

        dataImportRepository.deleteAll()
        fundingLearnerRecordRepository.deleteAll()
        fundingSummaryRecordRepository.deleteAll()
        supplementaryDataRepository.deleteAll()
        deleteOccupancyRecords()
        learnerRepository.deleteAll()
        healthProblemRepository.deleteAll()
        healthProblemCategoryRepository.deleteAll()
    }

    private fun deleteOccupancyRecords() {
        jdbcTemplate.execute("delete from occupancy_record_month_breakdown")
        jdbcTemplate.execute("delete from occupancy_record")
    }

    private fun createTestFundingLearnerData() {
        log.info("Creating test funding learner data")

        try {
            val fileName = "SILR1718_Funding_Learner_FM35_SN_Test100.csv"
            ilrDataService.upload(fileName, this.javaClass.getResourceAsStream(fileName))
        } catch (e: Exception) {
            log.error("failed to create test funding learner data", e)
        }
    }

    private fun createTestOccupancyReportData() {
        log.info("Creating test occupancy report data")
        uploadOccupancyReport("Occupancy Report 2019 01.csv")
        uploadOccupancyReport("Occupancy Report 2020 11.csv")
        uploadOccupancyReport("Occupancy Report 2020 02.csv")
        uploadOccupancyReport("Occupancy Report 2021 04.csv")
    }
     private fun createTestSupplementalData() {
        log.info("Creating test Supplemental Report")
         uploadOSupplementalData("Supplemental Data.csv")
    }

    private fun uploadOccupancyReport(fileName: String) {
        try {
            ilrDataService.upload(fileName, this.javaClass.getResourceAsStream(fileName))
        } catch (e: Exception) {
            log.error("failed to create test occupancy report data: $fileName", e)
        }
    }

    private fun uploadOSupplementalData(fileName: String) {
        try {
            ilrDataService.upload(fileName, this.javaClass.getResourceAsStream(fileName))
        } catch (e: Exception) {
            log.error("failed to create test supplement report: $fileName", e)
        }
    }

    private fun createTestFundingSummaryReportData() {
        log.info("Creating test funding summary report data")

        try {
            val fileName = "Funding Summary Report 2019 01.csv"
            ilrDataService.upload(fileName, this.javaClass.getResourceAsStream(fileName))
        } catch (e: Exception) {
            log.error("failed to create test funding summary report data", e)
        }
    }

    private fun createTestHealthProblemData() {
        log.info("Creating health problems test data")

        try {
            val fileName = "SILR_2019_11_LLDDHealthProblem.csv"
            ilrDataService.upload(fileName, this.javaClass.getResourceAsStream(fileName))
        } catch (e: Exception) {
            log.error("failed to create  health problems test data", e)
        }
    }

    private fun createTestHealthProblemCategoryData() {
        log.info("Creating health problems category test data")

        try {
            val fileName = "Data Initialiser - Health Problem Category.csv"
            ilrDataService.uploadStatic(fileName, this.javaClass.getResourceAsStream(fileName))
        } catch (e: Exception) {
            log.error("failed to create  health problems category test data", e)
        }
    }
}
