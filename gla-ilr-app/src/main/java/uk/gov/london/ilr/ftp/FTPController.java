/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.ftp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Arrays;

@Controller
public class FTPController {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    FTPService ftpService;

    FTPConfig testFtpConfig = new FTPConfig();

    @GetMapping("/ftpConfig")
    public String ftpConfig(Model model) {
        model.addAttribute("ftpConfig", ftpService.getFTPConfig());
        model.addAttribute("fileDecryptionPassword", ftpService.getFileDecryptionPassword());
        try {
            model.addAttribute("localFiles", ftpService.listLocalFiles());
            model.addAttribute("remoteFiles", ftpService.listRemoteFiles());
        } catch (Exception e) {
            log.error("could not list files", e);
            model.addAttribute("listFilesMessage", "Failed to list files: "+e.getMessage());
        }
        return "admin/ftpConfig";
    }

    @PostMapping("/ftpSync")
    public String ftpSync(RedirectAttributes redirectAttributes) {
        ftpService.runSFASynchroniser();
        redirectAttributes.addFlashAttribute("runSynchroniserMessage", "Successfully run file synchroniser");
        return "redirect:/ftpConfig";
    }

    @PostMapping("/saveFileDecryptionPassword")
    public String saveFileDecryptionPassword(@RequestParam String password, RedirectAttributes redirectAttributes) {
        ftpService.saveFileDecryptionPassword(password);
        redirectAttributes.addFlashAttribute("saveMessage", "Successfully saved");
        return "redirect:/ftpConfig";
    }

    @GetMapping("/ftpTest")
    public String ftpTest(@RequestParam(required = false) boolean listFiles, Model model) {
        model.addAttribute("ftpConfig", testFtpConfig);
        if (listFiles) {
            try {
                if (testFtpConfig.getHost().contains("mock")) {
                    model.addAttribute("files", Arrays.asList("Mock File 1", "Mock File 2", "Mock File 3"));
                }
                else {
                    model.addAttribute("files", ftpService.listRemoteFiles(testFtpConfig));
                }
            } catch (Exception e) {
                log.error("could not list files", e);
                model.addAttribute("runTestMessage", "FTP connection test failed: "+e.getMessage());
            }
        }
        return "admin/ftpTest";
    }

    @PostMapping("/ftpTest")
    public String ftpTest(@Valid @ModelAttribute("ftpConfig") FTPConfig ftpConfig, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/ftpTest";
        }
        this.testFtpConfig = ftpConfig;
        redirectAttributes.addAttribute("listFiles", true);
        return "redirect:/ftpTest";
    }

}
