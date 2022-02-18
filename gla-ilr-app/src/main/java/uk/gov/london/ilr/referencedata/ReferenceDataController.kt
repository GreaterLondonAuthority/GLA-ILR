/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.referencedata

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.london.ilr.web.PagingControls
import javax.servlet.http.HttpServletRequest

@Controller
class ReferenceDataController(private val referenceDataService: ReferenceDataService) {

    @PreAuthorize("authentication.name == '' or hasAnyRole('OPS_ADMIN', 'GLA_ORG_ADMIN', 'TECH_ADMIN')")
    @GetMapping("/codeValues")
    fun codeValues(@RequestParam(required = false) year: Int?,
                   @RequestParam(required = false) attribute: String?,
                   @PageableDefault(size = 50) pageable: Pageable,
                   model: Model,
                   request: HttpServletRequest): String {
        model["academicYears"] = referenceDataService.getReferenceDataAcademicYears()
        model["attributes"] = referenceDataService.getReferenceDataAttributes()
        val selectedAttribute = if (attribute == "") null else attribute
        model["page"] = PagingControls(referenceDataService.getCodeValues(year, selectedAttribute, pageable))
        return "codeValues"
    }
}
