/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.learner

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.togglz.core.manager.FeatureManager
import uk.gov.london.ilr.feature.IlrFeature
import uk.gov.london.ilr.file.*
import uk.gov.london.ilr.permission.PermissionRequired
import uk.gov.london.ilr.permission.PermissionType.UPLOAD_SUPPLEMENTAL_FILE
import uk.gov.london.ilr.security.UserService
import uk.gov.london.ilr.web.PagingControls
import java.time.format.DateTimeFormatter
import java.util.*

@Controller
class LearnerController(private val fileUploadHandler: FileUploadHandler,
                        private val learnerService: LearnerService,
                        private val supplementaryDataService: SupplementaryDataService,
                        private val userService: UserService,
                        private val dataImportService: DataImportService,
                        private val fileService: FileService,
                        private val featureManager: FeatureManager) {

    @PermissionRequired(UPLOAD_SUPPLEMENTAL_FILE)
    @PostMapping("/uploadSupplementalData")
    fun handleFileUploadSupplementalData(@RequestParam("file") file: MultipartFile, redirectAttributes: RedirectAttributes): String {
        try {
            if (!file.originalFilename!!.toUpperCase().endsWith(".CSV")) {
                throw RuntimeException("Upload failed: File must be in CSV format, to do this save an excel file as a .CSV")
            }

            val result = fileUploadHandler.upload(file.originalFilename, file.inputStream, DataImportType.SUPPLEMENTARY_DATA, userService.currentUserName())

            if (result.errorMessages.isNotEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessageList", result.errorMessages)
            } else {
                redirectAttributes.addFlashAttribute("numberOfRecordsUpdated", "File uploaded and " + result.numberOfRecords.toString() + " learner records updated")
            }
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        }

        return "redirect:/learners"
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'GLA_ORG_ADMIN', 'GLA_SPM', 'GLA_PM', 'GLA_FINANCE', 'GLA_READ_ONLY', 'ORG_ADMIN', 'PROJECT_EDITOR', 'PROJECT_READER')" )
    @GetMapping("/learners")
    @Transactional
    fun learners(@RequestParam(required = false) learner: String?,
                 @RequestParam(required = false) ukprn: Int?,
                 @RequestParam(required = false) academicYear: Int?,
                 @RequestParam(required = false) filterBySupplementaryData: Boolean?,
                 @PageableDefault(size = 50) pageable: Pageable,
                 model: Model): String {
        if (searchNotAllowed(ukprn)) {
            model["noReturnData"] = "You not allowed to see data from UKPRN $ukprn"
        }
        else {
            val ukprns : Set<Int>? = getSearchUkprns(ukprn)
            val latestImportForUser = dataImportService.getLatestImportForUser(userService.currentUserName(), DataImportType.SUPPLEMENTARY_DATA.name)

            model["page"] = PagingControls(learnerService.getLearnersSummaries(learner, ukprns, academicYear, filterBySupplementaryData, pageable))
            model["academicYears"] = learnerService.getLearnersAcademicYears()
            model["learnerDetailsPageEnabled"] = featureManager.isActive(IlrFeature.LEARNER_DETAILS_PAGE)
            if (latestImportForUser != null) {
                model["errorFileExists"] = fileService.findByDataImportId(latestImportForUser.id!!) != null
                model["downloadURL"] = "/downloadErrorFile/" + latestImportForUser.id
            }

        }
        return "learners"
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/downloadErrorFile/{importId}", produces = ["text/csv"])
    fun downloadReport(@PathVariable importId: Int) : ResponseEntity<String> {

        val dataImportRecord = dataImportService.getDataImportRecord(importId)
                ?: throw RuntimeException("no record found with ID $importId")

        if (!dataImportRecord.createdBy.equals(userService.currentUserName())) {
            throw RuntimeException("Unable to retrieve this record as was created by a different user")
        }

        var file: FileEntity? = null

        if (importId != null) {
            file = fileService.findByDataImportId(importId)
        }

        return if (file == null) {
            buildResponseEntity("error.txt", "text/plain", "No error file exists for report")
        }
        else {
            buildResponseEntity(getFileNameForDownload(dataImportRecord), "text/csv", file.content)
        }
    }

    private fun getFileNameForDownload(dataImportRecord: DataImport) : String {
        val date= dataImportRecord.createdOn!!.format(DateTimeFormatter.ofPattern("ddMMyyyyHHmmss"))
        val fileName = dataImportRecord.fileName!!.replace("\\s+\\d+\\.".toRegex(),".");
        val suffix = fileName.substring(fileName.lastIndexOf("."))
        val prefix = fileName.substring(0, fileName.length-suffix.length)
        return "$prefix $date$suffix"

    }

    private fun buildResponseEntity(fileName: String, contentType: String, content: String) : ResponseEntity<String> {
        return ResponseEntity.ok()
                .header("Content-disposition", "attachment;filename=$fileName")
                .contentType(MediaType.parseMediaType(contentType))
                .body(content)
    }

    private fun searchNotAllowed(ukprn: Int?): Boolean {
        val currentUser = userService.currentUser
        return !currentUser.isGla && ukprn != null && !currentUser.ukprns.contains(ukprn)
    }

    private fun getSearchUkprns(ukprn: Int?): Set<Int>? {
        val currentUser = userService.currentUser
        return if (ukprn != null) {
            setOf(ukprn)
        }
        else if (!currentUser.isGla) {
            currentUser.ukprns
        }
        else {
            null
        }
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'GLA_ORG_ADMIN', 'GLA_SPM', 'GLA_PM', 'GLA_FINANCE', 'GLA_READ_ONLY')" )
    @GetMapping("/learners/lrn/{lrn}/ukprn/{ukprn}/year/{year}")
    @Transactional
    fun getLearner(
            @PathVariable lrn: String,
            @PathVariable ukprn: Int,
            @PathVariable year: Int, model: Model): String {

        val id= LearnerPK(lrn, ukprn, year)

        val learnerRecord = learnerService.getLearnerRecord(id)
        model["learner"] = learnerRecord

        var learningDeliveryForLeaner = learnerService.getLearningDeliveryForLeaner(id)
        model["delivery"] = learningDeliveryForLeaner
        // Check if user is allow to see all occupancyDataForLearner for this learner
        val currentUser = userService.currentUser
        if(!currentUser.isGla && learningDeliveryForLeaner.isNotEmpty()) {
            learningDeliveryForLeaner.forEach { occ ->
                if(currentUser.ukprns.contains(occ.id.ukprn)) {
                    learningDeliveryForLeaner = learningDeliveryForLeaner.filter { it.id.ukprn == occ.id.ukprn}
                } else {
                    model["noReturnData"] = "You not allowed to see data from some UKPRNs"
                }
            }
        }
        val learnerSupplementaryData: SupplementaryData? = supplementaryDataService.getLearnerLatestSupplementaryData(learnerRecord.id.learnerReferenceNumber)
        val dateFormat = "dd/MM/yyyy"
        val formatter = DateTimeFormatter.ofPattern(dateFormat, Locale.ENGLISH)
        model["lastSupplementaryDataUpload"] = if (learnerSupplementaryData?.lastSupplementaryDataUpload == null)  "GLA data not uploaded"
                                                else "GLA data last uploaded on " + learnerSupplementaryData.lastSupplementaryDataUpload.format(formatter)
        return "learner"
    }

}
