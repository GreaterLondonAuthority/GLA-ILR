/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.learner

import io.swagger.annotations.Api
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.transaction.Transactional

@RestController
@RequestMapping("/api/v1/learner/")
@Api
class LearnerAPI(private val supplementaryDataService: SupplementaryDataService) {

    @PreAuthorize("authentication.name == '' or hasAnyRole('OPS_ADMIN')")
    @RequestMapping(value = ["/supplementaryData/year/{year}/period/{period}"], method = [RequestMethod.DELETE])
    @Transactional
    fun deleteSupplementaryData(@PathVariable year: Int?, @PathVariable period: Int?): String {
        if (year != null && period != null) {
            supplementaryDataService.deleteSupplementaryDataForYearPeriod(year, period)
        } else {
            throw RuntimeException("Invalid request, year and period must be specified.")
        }
        return "Supplementary data deleted for year $year and period $period"
    }

}