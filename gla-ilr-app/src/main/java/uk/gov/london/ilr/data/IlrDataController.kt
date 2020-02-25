/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.data

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.london.common.GlaUtils
import uk.gov.london.common.skills.SkillsGrantType
import uk.gov.london.ilr.web.PagingControls
import java.math.BigDecimal
import javax.servlet.http.HttpServletRequest


@Controller
class IlrDataController(private val ilrDataService: IlrDataService) {

    @PreAuthorize("authentication.name == '' or hasRole('OPS_ADMIN') or hasRole('GLA_ORG_ADMIN') " +
            "or hasRole('GLA_SPM') or hasRole('GLA_PM') or hasRole('GLA_FINANCE') or hasRole('GLA_READ_ONLY')")
    @PostMapping("/upload")
    fun handleFileUpload(@RequestParam("file") file: MultipartFile, redirectAttributes: RedirectAttributes): String {
        try {
            var result : UploadResult = ilrDataService.upload(file)
            redirectAttributes.addFlashAttribute("successMessage", "Successfully uploaded " + result.originalFilename)
            redirectAttributes.addFlashAttribute("numberOfRecordsUpdated", result.numberOfRecords.toString() + " records updated")

        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload due to: " + e.message)
        }

        return "redirect:/files"
    }

    @PreAuthorize("authentication.name == '' or hasRole('OPS_ADMIN') or hasRole('GLA_ORG_ADMIN') " +
            "or hasRole('GLA_SPM') or hasRole('GLA_PM') or hasRole('GLA_FINANCE') or hasRole('GLA_READ_ONLY')")    @PostMapping("/uploadSupplementalData")
    fun handleFileUploadSupplementalData(@RequestParam("file") file: MultipartFile, redirectAttributes: RedirectAttributes): String {
        try {
            var result : UploadResult = ilrDataService.uploadSupplementalData(file)
            if (result.errorMessages.isNotEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessageList", result.errorMessages)
            } else {
                redirectAttributes.addFlashAttribute("numberOfRecordsUpdated", "File uploaded and " + result.numberOfRecords.toString() + " learner records updated")
            }
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        }

        return "redirect:/learners"
    }

    @PreAuthorize("authentication.name == '' or hasRole('OPS_ADMIN') or hasRole('GLA_ORG_ADMIN') " +
            "or hasRole('GLA_SPM') or hasRole('GLA_PM') or hasRole('GLA_FINANCE') or hasRole('GLA_READ_ONLY')")
    @GetMapping("/files")
    fun filesPage(model: Model): String {
        model["dataImports"] = ilrDataService.dataImports()
        return "files"
    }

    @PreAuthorize("authentication.name == '' or hasRole('OPS_ADMIN') or hasRole('GLA_ORG_ADMIN') " +
            "or hasRole('GLA_SPM') or hasRole('GLA_PM') or hasRole('GLA_FINANCE') or hasRole('GLA_READ_ONLY')" +
            "or hasRole('ORG_ADMIN') or hasRole('PROJECT_EDITOR') or hasRole('PROJECT_READER')" )
    @GetMapping("/learners")
    fun learners(@RequestParam(required = false) learner: String?,
                 @RequestParam(required = false) ukprn: Int?,
                 @RequestParam(required = false) academicYear: Int?,
                 @RequestParam(required = false) filterBySupplementaryData: Boolean?,
                 @PageableDefault(size = 50) pageable: Pageable,
                 model: Model): String {
        // Check if user has access to see learners data with this specific ukprn
        var currentUser = ilrDataService.userService.currentUser
        if(!currentUser.isGla && ukprn != null) {
            if(!currentUser.ukprns.contains(ukprn)) {
                model["noReturnData"] = "You not allowed to see data from UKPRN $ukprn"
                return "learners"
            }
        }
        var validUkprns : Set<Int> = (if(!currentUser.isGla) currentUser.ukprns else setOf(ukprn)) as Set<Int>
        model["page"] = PagingControls(ilrDataService.getLearnersSummaries(learner, validUkprns, academicYear, filterBySupplementaryData, pageable))
        model["academicYears"] = ilrDataService.getLearnersAcademicYears()
        return "learners"
    }

    @PreAuthorize("authentication.name == '' or hasRole('OPS_ADMIN') or hasRole('GLA_ORG_ADMIN') " +
            "or hasRole('GLA_SPM') or hasRole('GLA_PM') or hasRole('GLA_FINANCE') or hasRole('GLA_READ_ONLY')" +
            "or hasRole('ORG_ADMIN') or hasRole('PROJECT_EDITOR') or hasRole('PROJECT_READER')" )
    @GetMapping("/learners/{id}")
    fun getLearner(@PathVariable id: Long, @RequestParam(required = false) year: Int?, model: Model): String {

        val learnerRecord = ilrDataService.getLearnerRecord(id)
        model["learner"] = learnerRecord

        var occupancyDataForLearner = ilrDataService.getOccupancyDataForLearner(id)
        // Check if user is allow to see all occupancyDataForLearner for this learner
        var currentUser = ilrDataService.userService.currentUser
        if(!currentUser.isGla && occupancyDataForLearner != null && occupancyDataForLearner.isNotEmpty()) {
            occupancyDataForLearner.forEach { occ ->
                if(currentUser.ukprns.contains(occ.ukprn)) {
                    occupancyDataForLearner = occupancyDataForLearner.filter { it -> it.ukprn == occ.ukprn}
                } else {
                    model["noReturnData"] = "You not allowed to see data from some UKPRNs"
                }
            }
        }

        model["years"] = occupancyDataForLearner.map { o -> o.learningStartDate.year }.toSortedSet()
        model["occupancy"] = if (year == null) occupancyDataForLearner else occupancyDataForLearner.filter { y -> y.learningStartDate.year == year }
        model["selectedYear"] = if (year == null) "" else year
        val healthProblemRecord: HealthProblemRecord? = ilrDataService.getLearnerLatestHealthProblemRecord(learnerRecord.learnerReferenceNumber)
        model["llddCategory"] = healthProblemRecord?.llddCategory?:"-"
        model["primaryLldd"] = when (healthProblemRecord?.primaryLldd) {
                                    1 -> "Yes"
                                    0 -> "No"
                                    else -> "-"
                                }
        return "learner"
    }

    @PreAuthorize("authentication.name == '' or hasRole('OPS_ADMIN') or hasRole('GLA_ORG_ADMIN') " +
            "or hasRole('GLA_SPM') or hasRole('GLA_PM') or hasRole('GLA_FINANCE') or hasRole('GLA_READ_ONLY')" +
            "or hasRole('ORG_ADMIN') or hasRole('PROJECT_EDITOR') or hasRole('PROJECT_READER')" )
    @GetMapping("/fundingSummary")
    fun fundingSummary(@RequestParam(required = false) ukprn: Int?,
                       @RequestParam(required = false) academicYear: Int?,
                       @RequestParam(required = false) period: Int?,
                       @PageableDefault(size = 50) pageable: Pageable,
                       model: Model): String {
        // Check if user has access to see learners data with this specific ukprn
        var currentUser = ilrDataService.userService.currentUser
        if(!currentUser.isGla && ukprn != null) {
            if(!currentUser.ukprns.contains(ukprn)) {
                model["noReturnData"] = "You not allowed to see data from UKPRN $ukprn"
                return "fundingSummary"
            }
        }
        var validUkprns : Set<Int> = (if(!currentUser.isGla) currentUser.ukprns else setOf(ukprn)) as Set<Int>
        val page = PagingControls(ilrDataService.getFundingSummaryRecords(validUkprns, academicYear, period, pageable))
        model["page"] = page
        return "fundingSummary"
    }

    /**
     * Returns a string with the money amount and specific cash suffix for the number passed.
     * A cash suffixed is determined by the amount of zeros, as follows:
     *  k - Thousand, M - Million, B - Billion, T - Trillion
     *  Example:  5100 -> 5,1k
     *           5500 000 -> 5.5M
     * */
    fun withSuffix(count: Long): String {
        if (count < 1000) return "" + count
        val exp = (Math.log(count.toDouble()) / Math.log(1000.0)).toInt()
        return String.format("%.1f %c", count / Math.pow(1000.0, exp.toDouble()), "kMBTqs"[exp - 1])
    }

    @PreAuthorize("authentication.name == '' or hasRole('OPS_ADMIN') or hasRole('GLA_ORG_ADMIN') " +
            "or hasRole('GLA_SPM') or hasRole('GLA_PM') or hasRole('GLA_FINANCE') or hasRole('GLA_READ_ONLY') " +
            "or hasRole('ORG_ADMIN') or hasRole('PROJECT_EDITOR') or hasRole('PROJECT_READER')" )
    @GetMapping("/occupancySummary")
    fun occupancySummary(model: Model,
                         @RequestParam(required = false) year: Int?,
                         @RequestParam(required = false) type: String?,
                         @RequestParam(required = false) ukprn: Set<Int>?,
                         @RequestParam(required = false) period: Int?,
                         request: HttpServletRequest): String {
        val onProgrammeEarnedCashTotals = mutableListOf(BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0))
        val balancingPaymentEarnedCashTotals = mutableListOf(BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0))
        val aimAchievementEarnedCashTotals = mutableListOf(BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0))
        val jobOutcomeEarnedCashTotals = mutableListOf(BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0))
        val learningSupportEarnedCashTotals = mutableListOf(BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0))

        val occupancySummaryMap = mapOf(
                "onProgrammeEarnedCash" to onProgrammeEarnedCashTotals,
                "balancingPaymentEarnedCash" to balancingPaymentEarnedCashTotals,
                "aimAchievementEarnedCash" to aimAchievementEarnedCashTotals,
                "jobOutcomeEarnedCash" to jobOutcomeEarnedCashTotals,
                "learningSupportEarnedCash" to learningSupportEarnedCashTotals
        )

        model["academicYears"] = ilrDataService.getLearnersAcademicYears()
        model["valueTypes"] = listOf(SkillsGrantType.AEB_PROCURED.name, SkillsGrantType.AEB_GRANT.name)

        val selectedYear = if(request.getParameter("year") == null ) ilrDataService.getCurrentAcademicYear() else year
        model["selectedYear"] = selectedYear ?: ""
        model["selectedType"] = type ?: ""
        model["selectedTypeText"] = if(type.isNullOrEmpty()) "Total" else ilrDataService.parseSkillsGrantType(type!!)

        val returns = ilrDataService.getLearnersPeriods(selectedYear)
        model["returnPeriods"] = returns
        var selectedReturnPeriod: Int? = -1
        if (returns.isNotEmpty() && (period == null || !returns.contains(period))) {
            selectedReturnPeriod = returns[returns.size-1]
        }
        else {
            selectedReturnPeriod = period
        }
        if (selectedReturnPeriod != null) {
            model["selectedPeriod"] = selectedReturnPeriod
        }

        // Check if user have access to see data of this specific ukprn
        var currentUser = ilrDataService.userService.currentUser
        if(!currentUser.isGla && ukprn != null && ukprn.isNotEmpty()) {
            if(!currentUser.ukprns.containsAll(ukprn)) {
                model["noReturnData"] = "You not allowed to see data from UKPRN $ukprn"
                return "occupancySummary"
            }
        }
        var validUkprns = if(!currentUser.isGla) currentUser.ukprns else ukprn
        model["numberOfUkprns"] = currentUser.ukprns.size
        model["validUkprn"] = if(currentUser.ukprns.size == 1) currentUser.ukprns.first() else ""

        var occupancySummaryList : List<OccupancySummary> = ilrDataService.getOccupancySummaryByYearAndUkprn(selectedYear, validUkprns)
        occupancySummaryList = if(type.isNullOrEmpty()) occupancySummaryList else occupancySummaryList.filter{ occ -> occ.grantType.equals(type)}
        if (selectedReturnPeriod != null) {
            occupancySummaryList = occupancySummaryList.filter{ occ -> occ.returnPeriod == selectedReturnPeriod }
        }
        model["noReturnData"] = if(occupancySummaryList.isEmpty() && validUkprns != null && validUkprns.isNotEmpty()) "No return uploaded for this period" else ""

        val uploadedUKPRNs = occupancySummaryList.distinctBy { it.ukprn }.size
        val unuploadedUKPRNs = ilrDataService.countDistinctUkprns() - uploadedUKPRNs
        model["returnDataMetrics"] = if(occupancySummaryList.isNotEmpty() && ukprn == null) "Displaying returns from $uploadedUKPRNs UKPRNs, $unuploadedUKPRNs haven't uploaded a return for this period" else ""

        for (entry in occupancySummaryList) {
            onProgrammeEarnedCashTotals[entry.period - 1] = GlaUtils.nullSafeAdd(onProgrammeEarnedCashTotals[entry.period - 1], entry.sumOnProgrammeEarnedCash)
            balancingPaymentEarnedCashTotals[entry.period - 1] = GlaUtils.nullSafeAdd(balancingPaymentEarnedCashTotals[entry.period - 1], entry.sumBalancingPaymentEarnedCash)
            aimAchievementEarnedCashTotals[entry.period - 1] = GlaUtils.nullSafeAdd(aimAchievementEarnedCashTotals[entry.period - 1], entry.sumAimAchievementEarnedCash)
            jobOutcomeEarnedCashTotals[entry.period - 1] = GlaUtils.nullSafeAdd(jobOutcomeEarnedCashTotals[entry.period - 1], entry.sumJobOutcomeEarnedCash)
            learningSupportEarnedCashTotals[entry.period - 1] = GlaUtils.nullSafeAdd(learningSupportEarnedCashTotals[entry.period - 1], entry.sumLearningSupportEarnedCash)
        }

        model["occupancySummaryMap"] = occupancySummaryMap
        model["uniqueLearners"] = ilrDataService.getUniqueLearners(selectedYear, validUkprns)
        var totalDeliveryProcured = ilrDataService.getTotalDeliveryByYearGrantTypeAndUkprn(selectedYear, SkillsGrantType.AEB_PROCURED, selectedReturnPeriod, validUkprns)
        model["totalDeliveryProcured"] = if (totalDeliveryProcured == null) "-" else "£ " + withSuffix(totalDeliveryProcured!!)
        var totalDeliveryNonProcured = ilrDataService.getTotalDeliveryByYearGrantTypeAndUkprn(selectedYear, SkillsGrantType.AEB_GRANT, selectedReturnPeriod, validUkprns)
        model["totalDeliveryNonProcured"] = if (totalDeliveryNonProcured == null) "-" else "£ " + withSuffix(totalDeliveryNonProcured!!)

        return "occupancySummary"
    }

    @PreAuthorize("authentication.name == '' or hasRole('OPS_ADMIN') or hasRole('GLA_ORG_ADMIN') " +
            "or hasRole('GLA_SPM') or hasRole('GLA_PM') or hasRole('GLA_FINANCE') or hasRole('GLA_READ_ONLY')")
    @GetMapping("/fundingLearner")
    fun fundingLearner(@RequestParam(required = false) ukprn: Int?,
                       @RequestParam(required = false) year: Int?,
                       @RequestParam(required = false) month: Int?,
                       @PageableDefault(size = 50) pageable: Pageable,
                       model: Model): String {
        val page = PagingControls(ilrDataService.getFundingLearnerRecords(ukprn, year, month, pageable))
        model["page"] = page
        return "fundingLearner"
    }

}
