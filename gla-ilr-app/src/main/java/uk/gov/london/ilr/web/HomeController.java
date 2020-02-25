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
import uk.gov.london.ilr.data.IlrDataService;
import uk.gov.london.ilr.environment.Environment;

import java.security.Principal;

@Controller
class HomeController {

    @Autowired
    private IlrDataService ilrDataService;

    @Autowired
    private Environment environment;

    @GetMapping("/")
    String index(Principal principal, Model model) {
        model.addAttribute("dataImportCount", ilrDataService.dataImportCount());
        model.addAttribute("fundingLearnerRecordsCount", ilrDataService.fundingLearnerRecordsCount() / 12);
        model.addAttribute("isTestEnvironment", environment.isTestEnvironment());
        return "redirect:/occupancySummary";
    }

    @GetMapping(value = "/login")
    public String loginPage() {
        // TODO : redirect to home page if logged in
//        return "redirect:/";
        return "login";
    }

}