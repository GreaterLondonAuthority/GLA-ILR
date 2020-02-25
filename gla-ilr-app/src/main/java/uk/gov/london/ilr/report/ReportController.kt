/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.report

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.http.HttpServletResponse
import kotlin.text.Charsets.UTF_8

@Controller
class ReportController(val reportService: ReportService) {

    @GetMapping("/reports")
    fun messages(model: Model): String {
        return "reports"
    }

    @PostMapping("/reports", produces = ["application/csv"])
    fun updateMessage(@RequestParam(required = false) fileName: String,
                      @RequestParam sql: String,
                      response: HttpServletResponse) {
        OutputStreamWriter(response.outputStream, UTF_8).use { out ->
            try {
                reportService.generateReport(sql, out)
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
