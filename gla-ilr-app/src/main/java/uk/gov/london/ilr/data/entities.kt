/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.data

import uk.gov.london.common.skills.SkillsGrantType
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import javax.persistence.*


@Entity
data class DataImport(

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_import_seq_gen")
        @SequenceGenerator(name = "data_import_seq_gen", sequenceName = "data_import_seq", initialValue = 100, allocationSize = 1)
        var id: Int? = null,

        var fileName: String? = null,

        @Enumerated(EnumType.STRING)
        var status: DataImportStatus? = null,

        @Enumerated(EnumType.STRING)
        var importType: DataImportType? = null,

        var createdOn: OffsetDateTime? = null,

        var createdBy: String? = null,

        var academicYear: Int? = null,

        var period: Int? = null,

        var lastExportDate: OffsetDateTime? = null,

        @Transient
        var canPushToOPS: Boolean = false

)

@Entity
class Learner (

        @Id
        var id: Long,

        var learnerReferenceNumber: String? = null,

        var postCode: String? = null,

        var dateOfBirth: LocalDate



)

@Entity(name = "v_learner_summary")
class LearnerSummary (
        
        @Id
        var id: Int,
        
        var learnerId: Long,

        var learnerReferenceNumber: String?,
        
        var academicYear: Int?,
        
        var ukprn: Int?,

        var lastSupplementaryDataUpload: OffsetDateTime?

)

@Entity
class OccupancyRecord (

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "occupancy_record_seq_gen")
        @SequenceGenerator(name = "occupancy_record_seq_gen", sequenceName = "occupancy_record_seq", initialValue = 10000, allocationSize = 1)
        var id: Int? = null,

        @ManyToOne
        var learner: Learner,

        var ukprn: Int,

        var academicYear: Int,

        var period: Int,

        var learnerReferenceNumber: String,

        var postCodePriorToEnrolment: String,

        var aimSequenceNumber: Int,

        var learningAimReference: String,

        var learningAimTitle: String,

        var learningStartDate: LocalDate,

        var learningPlannedEndDate: LocalDate,

        var completionStatus: Int,

        var learningStartDatePostCode: String,

        var fundingLineType: String,

        @Enumerated(EnumType.STRING)
        var grantType: SkillsGrantType,

        var deliveryLocationPostCode: String,

        var totalOnProgrammeEarnedCash: BigDecimal? = null,

        var totalBalancingPaymentEarnedCash: BigDecimal? = null,

        var totalAimAchievementEarnedCash: BigDecimal? = null,

        var totalJobOutcomeEarnedCash: BigDecimal? = null,

        var totalLearningSupportEarnedCash: BigDecimal? = null,

        var totalEarnedCash: BigDecimal? = null,

        @OneToMany(cascade = [CascadeType.ALL], targetEntity = OccupancyRecordMonthBreakdown::class)
        @JoinColumn(name = "occupancy_record_id")
        var monthBreakdown: List<OccupancyRecordMonthBreakdown>,

        var fundingModel: String? = null,

        var outcome: String? = null,

        var ldfamTypeDevolvedAreaMonitoringA: String? = null,

        var ldfamTypeDevolvedAreaMonitoringB: String? = null,

        var ldfamTypeDevolvedAreaMonitoringC: String? = null,

        var ldfamTypeDevolvedAreaMonitoringD: String? = null

)

@Entity
class OccupancyRecordMonthBreakdown (

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "occupancy_record_month_breakdown_seq_gen")
        @SequenceGenerator(name = "occupancy_record_month_breakdown_seq_gen", sequenceName = "occupancy_record_month_breakdown_seq", initialValue = 10000, allocationSize = 1)
        var id: Int? = null,

        var academicYear: Int,

        var period: Int,

        @Enumerated(EnumType.STRING)
        var grantType: SkillsGrantType,

        var onProgrammeEarnedCash: BigDecimal? = null,

        var balancingPaymentEarnedCash: BigDecimal? = null,

        var aimAchievementEarnedCash: BigDecimal? = null,

        var jobOutcomeEarnedCash: BigDecimal? = null,

        var learningSupportEarnedCash: BigDecimal? = null,

        var subContractedOrPartnershipUkprn: String? = null

)

@Entity(name = "v_occupancy_summary")
class OccupancySummary(

        @Id
        var id: Int,

        var academicYear: Int,

        var ukprn: Int? = null,

        var period: Int,

        var returnPeriod: Int,

        var grantType: String,

        var sumOnProgrammeEarnedCash: BigDecimal? = null,

        var sumBalancingPaymentEarnedCash: BigDecimal? = null,

        var sumAimAchievementEarnedCash: BigDecimal? = null,

        var sumJobOutcomeEarnedCash: BigDecimal? = null,

        var sumLearningSupportEarnedCash: BigDecimal? = null

)

@Entity
class FundingLearnerRecord (

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funding_learner_record_seq_gen")
        @SequenceGenerator(name = "funding_learner_record_seq_gen", sequenceName = "funding_learner_record_seq", initialValue = 100, allocationSize = 1)
        var id: Int? = null,

        var year: Int? = null,

        var ukprn: Int? = null,

        var learnRefNumber: String? = null,

        var month: Int? = null,

        var totalPayment: BigDecimal? = null

)

@Entity
class FundingSummaryRecord (

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funding_summary_record_seq_gen")
        @SequenceGenerator(name = "funding_summary_record_seq_gen", sequenceName = "funding_summary_record_seq", initialValue = 100, allocationSize = 1)
        var id: Int? = null,

        var academicYear: Int? = null,

        var period: Int? = null,

        var actualYear: Int? = null,

        var actualMonth: Int? = null,

        var ukprn: Int? = null,

        var fundingLine: String? = null,

        var source: String? = null,

        var category: String? = null,

        var monthTotal: BigDecimal? = null,

        var totalPayment: BigDecimal? = null

)


@Entity (name = "health_problem_record")
class HealthProblemRecord (

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "health_problem_record_seq")
        var id: Int? = null,

        var year: String,

        var snapshot: Int? = null,

        var returnNumber: Int,

        var month: Int,

        var ukprn: Int? = null,

        var prevUkprn: Int? = null,

        var learnerRefNumber: String,

        var prevLearnerRefNumber: String? = null,

        var uniqueLearnerNumber : Int? = null,

        var llddCategory: Int,

        var primaryLldd: Int? = null
        )

@Entity (name = "health_problem_category")
class HealthProblemCategory (
        @Id
        var code: Int,
        var description: String
)
class UploadResult {

        lateinit var originalFilename : String

        var numberOfRecords : Long = 0

        var errorMessages: MutableList<String> = mutableListOf()

}

@Entity(name = "supplementary_data")
class SupplementaryData (

        @EmbeddedId
        var id: SupplementaryDataPK,

        var investmentPriorityClaimedUnder: String,

        var hasBasicSkills: Int,

        var isHomeless: Int,

        var lastSupplementaryDataUpload: OffsetDateTime
)

@Embeddable
class SupplementaryDataPK (
        var ukprn: Int,
        var learnerReferenceNumber: String
): Serializable