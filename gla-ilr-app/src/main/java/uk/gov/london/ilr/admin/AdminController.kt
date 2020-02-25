/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.admin

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import javax.validation.Valid

@Controller
class AdminController(val adminService: AdminService) {

    @GetMapping("/messages")
    fun messages(model: Model): String {
        model["messages"] = adminService.getMessages()
        return "admin/messages"
    }

    @PostMapping("/messages")
    fun updateMessage(@Valid @ModelAttribute("message") message: MessageModel): String {
        adminService.updateMessage(message)
        return "redirect:/messages"
    }

    @GetMapping("/systemDashboard")
    fun systemDashboard(model: Model): String {
        model["infoDetails"] = adminService.getInfoDetails()
        return "admin/systemDashboard"
    }

}
