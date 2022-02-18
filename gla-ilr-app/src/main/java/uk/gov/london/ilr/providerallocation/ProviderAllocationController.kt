/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.providerallocation

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
import javax.servlet.http.HttpServletRequest

@Controller
class ProviderAllocationController(private val providerAllocationService: ProviderAllocationService,
                                   private val userService: UserService) {

    @PreAuthorize("authentication.name == '' or hasAnyRole('OPS_ADMIN', 'GLA_ORG_ADMIN', 'TECH_ADMIN')")
    @GetMapping("/providerAllocation")
    fun providerAllocation(@RequestParam(required = false) year: Int?,
                           @RequestParam(required = false) ukprn: Int?,
                           @PageableDefault(size = 50) pageable: Pageable,
                           model: Model,
                           request: HttpServletRequest): String {
        if (isSearchByUkprnAllowed(ukprn)) {
            model["noReturnData"] = "You not allowed to see data from UKPRN $ukprn"
        }
        model["academicYears"] = providerAllocationService.findDistinctAcademicYears()
        model["pageTitle"] = "Provider Allocation Data"
        val ukprns: Set<Int>? = getSearchUkprns(ukprn)
        model["page"] = PagingControls(providerAllocationService.getProviderAllocations(year, ukprns, pageable))
        return "providerAllocation"
    }

    private fun isSearchByUkprnAllowed(ukprn: Int?): Boolean {
        val currentUser = userService.currentUser
        return !currentUser.isGla && ukprn != null && !currentUser.ukprns.contains(ukprn)
    }

    private fun getSearchUkprns(ukprn: Int?): Set<Int>? {
        val currentUser = userService.currentUser
        return if (ukprn != null) {
            setOf(ukprn)
        } else if (!currentUser.isGla) {
            currentUser.ukprns
        } else {
            null
        }
    }
}
