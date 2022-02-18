/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.london.ilr.environment.Environment;
import uk.gov.london.ilr.file.DataImportService;
import uk.gov.london.ilr.security.User;
import uk.gov.london.ilr.security.UserService;

import java.security.Principal;

import static uk.gov.london.common.user.BaseRole.TECH_ADMIN;

@Controller
class HomeController {

    @Autowired
    private DataImportService dataImportService;


    @Autowired
    private UserService userService;

    @Autowired
    private Environment environment;

    @GetMapping("/")
    String index(Principal principal, Model model) {
        model.addAttribute("dataImportCount", dataImportService.dataImportCount());
        model.addAttribute("isTestEnvironment", environment.isTestEnvironment());

        User currentUser = userService.getCurrentUser();
        if (currentUser.hasRole(TECH_ADMIN)) {
            return "redirect:/systemDashboard";
        } else {
            return "redirect:/learners";
        }
    }

    @GetMapping(value = "/login")
    public String loginPage(Model model) {
        model.addAttribute("pageTitle", "Login");
        return "login";
    }

}
