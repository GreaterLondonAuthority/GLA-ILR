/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.report

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.rowset.SqlRowSet
import org.springframework.stereotype.Service
import uk.gov.london.common.CSVFile
import java.io.OutputStreamWriter
import java.util.HashMap
import java.util.LinkedHashSet
import javax.transaction.Transactional

@Service
@Transactional
class ReportService @Autowired constructor(val jdbcTemplate: JdbcTemplate) {

    private var log = LoggerFactory.getLogger(javaClass)

    fun generateReport(sql: String, out: OutputStreamWriter) {
        log.info("request to execute SQL: $sql")

        // "queryForRowSet" does not allow execution of modifying SQL scripts
        val rowSet = jdbcTemplate.queryForRowSet(sql)

        val headers = rowSetHeaders(rowSet)

        val csvFile = CSVFile(headers, out)

        while (rowSet.next()) {
            val row = rowSetCsvValues(rowSet, headers)
            csvFile.writeValues(row)
        }
    }

    private fun rowSetHeaders(rowSet: SqlRowSet): Set<String> {
        val headers = LinkedHashSet<String>()
        for (column in rowSet.metaData.columnNames) {
            headers.add(column.toLowerCase())
        }
        return headers
    }

    private fun rowSetCsvValues(rowSet: SqlRowSet, headers: Set<String>): Map<String, Any> {
        val csvRow = HashMap<String, Any>()
        for (header in headers) {
            csvRow[header.toLowerCase()] = rowSet.getString(header)
        }
        return csvRow
    }

}
