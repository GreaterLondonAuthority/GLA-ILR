/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.report

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.london.ilr.file.ERROR_FILE_TYPE
import uk.gov.london.ilr.file.FileService
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.http.HttpServletResponse
import kotlin.text.Charsets.UTF_8

@Controller
class ReportController(val reportService: ReportService,
                       val fileService: FileService) {

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reports")
    fun getReportsPage(model: Model): String {
        val fileSummaries = fileService.getAllFileSummaries().filter { !it.fileType.equals(ERROR_FILE_TYPE) }
        model["fileTypes"] = fileSummaries.map { it.fileType }.toSet()
        model["fileSuffixes"] = fileSummaries.map { it.fileSuffix }.toSet()
        model["ukprns"] = fileSummaries.map { it.ukprn }.toSet()
        return "reports"
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/download", produces = ["text/csv"])
    fun downloadReport(@RequestParam fileType: String,
                       @RequestParam fileSuffix: String,
                       @RequestParam ukprn: Int) : ResponseEntity<String> {

        val file = fileService.getFileEntity(fileType, fileSuffix, ukprn)
        return if (file == null) {
            buildResponseEntity("error.txt", "text/plain", "No data validation issues for the selected ILR Return period")
        }
        else {
            buildResponseEntity("$fileType $ukprn $fileSuffix.csv", "text/csv", file.content)
        }
    }

    private fun buildResponseEntity(fileName: String, contentType: String, content: String) : ResponseEntity<String> {
        return ResponseEntity.ok()
                .header("Content-disposition", "attachment;filename=$fileName")
                .contentType(MediaType.parseMediaType(contentType))
                .body(content)
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'GLA_ORG_ADMIN', 'GLA_SPM', 'GLA_PM', 'GLA_FINANCE', 'GLA_READ_ONLY')")
    @PostMapping("/adhocReport", produces = ["application/csv"])
    fun generateAdhocReport(@RequestParam(required = false) fileName: String,
                            @RequestParam sql: String,
                            response: HttpServletResponse) {
        OutputStreamWriter(response.outputStream, UTF_8).use { out ->
            try {
                reportService.generateAdhocReport(sql, out)
                val csvFileName = generateFileName(fileName, "csv")
                response.addHeader("Content-disposition", "attachment;filename=$csvFileName")
                response.contentType = "text/csv"
            } catch (e: Exception) {
                out.write(e.toString())
                val errorFileName = generateFileName("error", "txt")
                response.addHeader("Content-disposition", "attachment;filename=$errorFileName")
                response.contentType = "text/plain"
            } finally {
                response.flushBuffer()
            }
        }
    }

    private fun generateFileName(prefix: String, extension: String): String {
        val dateAsString = SimpleDateFormat("ddMMyyyyHHmmss").format(Date())
        return "$prefix$dateAsString.$extension"
    }

}
