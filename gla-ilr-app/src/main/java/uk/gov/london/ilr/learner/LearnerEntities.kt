/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.learner

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import javax.persistence.*

@Embeddable
data class ProviderPK (
        var year: Int,
        var ukprn: Int
) : Serializable

@Embeddable
data class LearningDeliveryPK (
        var ukprn: Int,
        var learnerReferenceNumber: String,
        var aimSequenceNumber: Int,
        var year: Int

        )   : Serializable {

        val keyString: String
                get() = learnerReferenceNumber + ':' + ukprn + ':' + aimSequenceNumber + ':' + year

}

@Embeddable
data class EarningPeriodPK (
        var ukprn: Int,
        var learnerReferenceNumber: String,
        var aimSequenceNumber: Int,
        var year: Int,
        var month: Int
) : Serializable

@Embeddable
data class LearningAimPK (
        var aimReference: String,
        var year: Int
) : Serializable

@Entity
class Provider(
        @Id
        var id: ProviderPK,
        var providerName : String? = null
)

@Entity
class Learner(
        @Id
        var id: LearnerPK,
        var uniqueLearnerNumber: Long? = null,
        var dateOfBirth: LocalDate? = null,
        var llddHealthProblem	: Int? = null,
        var ethnicity	: Int? = null,
        var gender	: String? = null,
        var priorAttainment	: Int? = null,
        var postcodePriorToEnrollment	: String? = null,
        @Column(name = "return")
        var returnPeriod	: Int? = null,
        @Column(name = "provider_specified_learner_monitoring_a")
        var providerSpecifiedLearnerMonitoringA : String? = null,
        @Column(name = "provider_specified_learner_monitoring_b")
        var providerSpecifiedLearnerMonitoringB : String? = null,
        var familyName : String? = null,
        var givenName : String? = null
)

@Entity
class LearningDelivery (
        @Id
        var id: LearningDeliveryPK,
        var aimReference: String? = null,
        var startDate: LocalDate? = null,
        var plannedEndDate: LocalDate? = null,
        var actualEndDate: LocalDate? = null,
        var outcome : Int? = null,
        var notionalNvqLevel : String? = null,
        var tierTwoSectorSubjectArea : String? = null,
        var fundingModel : Int? = null,
        var completionStatus : Int? = null,
        var learnerEmploymentStatus : Int? = null,
        var esmTypeBenefitStatus : Int? = null,
        @Column(name = "return")
        var returnPeriod	: Int? = null,
        var fundingLineType : String? = null,
        var partnerUkprn : Int? = null,
        var ldfamTypeFundingIndicator : Int? = null,
        @Column(name = "ldfam_type_ldm_a")
        var ldfamTypeLdmA : Int? = null,
        @Column(name = "ldfam_type_ldm_b")
        var ldfamTypeLdmB : Int? = null,
        @Column(name = "ldfam_type_ldm_c")
        var ldfamTypeLdmC : Int? = null,
        @Column(name = "ldfam_type_ldm_d")
        var ldfamTypeLdmD : Int? = null,
        @Column(name = "ldfam_type_ldm_e")
        var ldfamTypeLdmE : Int? = null,
        @Column(name = "ldfam_type_ldm_f")
        var ldfamTypeLdmF : Int? = null,
        @Column(name = "ldfam_type_dam_a")
        var ldfamTypeDamA : Int? = null,
        @Column(name = "ldfam_type_dam_b")
        var ldfamTypeDamB : Int? = null,
        @Column(name = "ldfam_type_dam_c")
        var ldfamTypeDamC : Int? = null,
        @Column(name = "ldfam_type_dam_d")
        var ldfamTypeDamD : Int? = null,
        @Column(name = "ldfam_type_dam_E")
        var ldfamTypeDamE : String? = null,
        @Column(name = "ldfam_type_dam_F")
        var ldfamTypeDamF : String? = null,
        var ldfamCommunityLearningProvisionType : Int? = null,
        @Column(name = "ldfam_type_household_situation_a")
        var ldfamTypeHouseholdSituationA : String? = null,
        @Column(name = "ldfam_type_household_situation_b")
        var ldfamTypeHouseholdSituationB : String? = null,
        var localAuthorityCode : String? = null,
        var partnerUkprnName : String? = null,
        var esmTypeEmploymentIntensity : Int? = null,
        var startForFundingPurposes : Int? = null,
        var tierTwoSectorSubjectAreaName : String? = null) {

        constructor(id: LearningDeliveryPK) : this(id, null, null, null,
                null, null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null,
                null, null, null, null,
                null ,null, null, null,
                null,null)

        @Transient
        var aim : LearningAim? = null
}

@Entity
class EarningPeriod (

        @Id
        var id: EarningPeriodPK,
        var onProgrammeEarnedCash: BigDecimal? = null,
        var balancingPaymentEarnedCash: BigDecimal? = null,
        var aimAchievementEarnedCash: BigDecimal? = null,
        var jobOutcomeEarnedCash: BigDecimal? = null,
        var learningSupportEarnedCash: BigDecimal? = null,
        @Column(name = "return")
        var returnPeriod	: Int? = null

)

@Entity
class LearningAim (
        @Id
        var id: LearningAimPK,
        var title: String
)

@Entity(name = "supplementary_data")
class SupplementaryData(

        @EmbeddedId
        var id: SupplementaryDataPK,
        var investmentPriorityClaimedUnder: String,
        var hasBasicSkills: Int,
        var isHomeless: Int,
        var highestEducationalAttainmentAtEsfStart: Int,
        var progressingIntoEducationOrTrainingAsEsfResult: Int,
        var startDateForEducationOrTrainingEsfResult: LocalDate,
        var hasLeftEsfProgram: Int,
        var esfLeaveDate: LocalDate,
        var lastSupplementaryDataUpload: OffsetDateTime
)

@Embeddable
class SupplementaryDataPK(
        var ukprn: Int,
        var learnerReferenceNumber: String
) : Serializable


@Entity(name = "ref_data_mapping")
class RefDataMapping(
        @EmbeddedId
        var id: RefDataMappingPK,
        var headlineValue: String?,
        var detailedValue: String?,
        var addedOn: LocalDateTime,
        var addedBy: String
)

@Embeddable
class RefDataMappingPK(
        var year: Int?,
        var attribute: String,
        var code: String
) : Serializable
