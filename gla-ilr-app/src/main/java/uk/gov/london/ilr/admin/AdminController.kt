/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.admin

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import uk.gov.london.ilr.audit.AuditService
import uk.gov.london.ilr.web.PagingControls
import javax.validation.Valid

@Controller
class AdminController(val adminService: AdminService,
                      val auditService: AuditService) {

    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'TECH_ADMIN')")
    @GetMapping("/messages")
    fun messages(model: Model): String {
        model["messages"] = adminService.getMessages()
        model["pageTitle"] = "System Messages"
        return "admin/messages"
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'TECH_ADMIN')")
    @PostMapping("/messages")
    fun updateMessage(@Valid @ModelAttribute("message") message: MessageModel): String {
        adminService.updateMessage(message)
        return "redirect:/messages"
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'TECH_ADMIN')")
    @GetMapping("/systemDashboard")
    fun systemDashboard(model: Model): String {
        model["infoDetails"] = adminService.getInfoDetails()
        model["pageTitle"] = "System Dashboard"
        return "admin/systemDashboard"
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'TECH_ADMIN')")
    @GetMapping("/auditHistory")
    fun auditHistory(@PageableDefault(size = 50, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable,
                     model: Model): String {
        model["page"] = PagingControls(auditService.findAll(pageable))
        model["pageTitle"] = "Audit History"
        return "admin/auditHistory"
    }

}
