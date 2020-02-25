/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.ops;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OpsController {

    @Autowired
    private OpsService opsService;

    @PostMapping(value = "/pushFundingSummaryToOps")
    public String pushFundingSummaryToOps(RedirectAttributes redirectAttributes, @RequestParam("id") Integer id) {
        try {
            opsService.pushFundingSummaryToOps(id);
            redirectAttributes.addFlashAttribute("opsPushMessage", "Successfully pushed data to OPS");
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("opsPushMessage", "Failed to push to OPS: "+e.getMessage());
        }

        return "redirect:/files";
    }

}
