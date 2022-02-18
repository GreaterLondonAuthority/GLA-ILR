/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.fundingsummary

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.london.ilr.security.UserService
import uk.gov.london.ilr.web.PagingControls
import java.time.YearMonth
import javax.servlet.http.HttpServletRequest

@Controller
class FundingSummaryController(private val fundingSummaryService: FundingSummaryService,
                               private val userService: UserService) {

    @PreAuthorize("authentication.name == '' or hasAnyRole('OPS_ADMIN', 'GLA_ORG_ADMIN', 'GLA_SPM', 'GLA_PM', 'GLA_FINANCE', 'GLA_READ_ONLY')" )
    @GetMapping("/fundingSummary")
    fun fundingSummary(@RequestParam(required = false) ukprn: Int?,
                       @RequestParam(required = false) academicYear: Int?,
                       @RequestParam(required = false) period: Int?,
                       @PageableDefault(size = 50) pageable: Pageable,
                       model: Model,
                       request: HttpServletRequest): String {
        model["pageTitle"] = "Funding Summary"
        // Check if user has access to see learners data with this specific ukprn
        val currentUser = userService.currentUser
        if(!currentUser.isGla && ukprn != null) {
            if(!currentUser.ukprns.contains(ukprn)) {
                model["noReturnData"] = "You not allowed to see data from UKPRN $ukprn"
                return "fundingSummary"
            }
        }
        model["academicYears"] = fundingSummaryService.getFundingAcademicYears()
        val selectedYear = if(request.getParameter("academicYear") == null ) getCurrentAcademicYear() else academicYear
        model["selectedYear"] = selectedYear ?: ""

        val validUkprns : Set<Int> = (if(!currentUser.isGla) currentUser.ukprns else setOf(ukprn)) as Set<Int>
        val page = PagingControls(fundingSummaryService.getFundingSummaryRecords(validUkprns, academicYear, period, pageable))
        model["page"] = page
        return "fundingSummary"
    }

    fun getCurrentAcademicYear(): Int {
        val year = YearMonth.now().year
        return if (YearMonth.now().monthValue < 8) year - 1 else year
    }

}
