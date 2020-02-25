/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import uk.gov.london.ilr.admin.AdminService
import uk.gov.london.ilr.environment.Environment
import uk.gov.london.ilr.security.UserService

/**
 * Spring MVC controller advice that adds some default values to the model for all controllers.
 *
 * @author Steve Leach
 */
@ControllerAdvice
class HeaderFooterControllerAdvice(@Autowired val  environment: Environment,
                                   @Autowired val  userService: UserService,
                                   @Autowired val adminService: AdminService) {

    @ModelAttribute
    fun addTitle(model: Model) {
        model.addAttribute("title", "ILR Gateway")
        val currentUserName = userService.currentUserName()
        model.addAttribute("userLoggedIn", currentUserName != null)
        model.addAttribute("opsBaseUrl", environment!!.opsBaseUrl())
        model.addAttribute("username", currentUserName)
        if (currentUserName != null) {
            model.addAttribute("user", userService.currentUser)
        }
        model.addAttribute("bannerMessage", adminService.getBannerMessage())
    }

    @ModelAttribute
    fun footerDetails(model: Model) {
        model.addAttribute("envShortName", environment!!.shortName())
        model.addAttribute("appVersionAndBuildNumberElement", environment.appVersionAndBuildNumberElement)
    }

}
