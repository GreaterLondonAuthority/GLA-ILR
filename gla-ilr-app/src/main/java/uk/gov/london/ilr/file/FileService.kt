/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.file

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.london.ilr.environment.Environment
import uk.gov.london.ilr.security.UserService
import java.util.stream.Collectors
import javax.transaction.Transactional

@Service
@Transactional
class FileService @Autowired constructor(val fileEntityRepository: FileEntityRepository,
                                         val fileSummaryRepository: FileSummaryRepository,
                                         val userService: UserService,
                                         val environment: Environment) {

    internal var log = LoggerFactory.getLogger(javaClass)

    fun getAllFileSummaries() : List<FileSummary> {
        val currentUser = userService.currentUser
        var fileSummaries: MutableList<FileSummary>
        fileSummaries = if (currentUser.isGla) {
            fileSummaryRepository.findAll().toMutableList()
        }
        else {
            fileSummaryRepository.findAllByUkprnIn(currentUser.ukprns).toMutableList()
        }

        if (fileSummaries.size > 0 ) {
            // populate empty files for drop down dates
            val earliestYearMonth = getEarliestYearMonth(fileSummaries)
            val latestYearMonth = getLatestYearMonth(fileSummaries)
            val expectedDates = createExpectedDates(earliestYearMonth!!, latestYearMonth!!)
            val existingNames: List<String> = fileSummaries.stream().map { f -> f.fileSuffix }.collect(Collectors.toList()) as List<String>

            expected@ for (expected in expectedDates) {
                for (name in existingNames) {
                    if (name.contains(expected)) {
                        continue@expected
                    }
                }
                // ukprn and filetype obtained from first entry to prevent blank rows in other dropdowns
                val fakeSummary: FileSummary = FileSummary(fileSuffix = expected, fileType = fileSummaries.get(0).fileType, ukprn = fileSummaries.get(0).ukprn)
                fileSummaries.add(fakeSummary)
            }
        }
        return fileSummaries
    }


    fun filenameListContainsDate(existingList: List<String>, expectedDates: List<String>) : Boolean {
        for (expected in expectedDates) {
            for (name in existingList) {
                if (name.contains(expected)) {
                    return true
                }
            }
        }
        return false
    }

    fun createExpectedDates(earliest: Int, latest: Int): List<String> {
        val names: MutableList<String> = mutableListOf()
        val startYear: Int = Math.floorDiv(earliest, 100)
        val endYear: Int = Math.floorDiv(latest, 100)
        val startMonth = earliest - startYear * 100
        val endMonth = latest - endYear * 100
        for (year in startYear..endYear) {
            for (month in 1..12) {
                if (year == startYear && month < startMonth) {
                    continue
                }
                if (year == endYear && month > endMonth) {
                    break
                }
                val monthString = if (month < 10) {
                  "0$month"
                } else {
                    "$month"
                }
                names.add("$year $monthString")
            }
        }
        return names
    }

    fun getEarliestYearMonth(summaries: List<FileSummary>): Int? {
        var earliestYearMonth: Int? = null
        for (summary in summaries) {
            val fileSuffix: String? = summary.fileSuffix
            val currentYearMonth = convertStringToYearMonth(fileSuffix)
            if (currentYearMonth != null && (earliestYearMonth == null || currentYearMonth < earliestYearMonth)) {
                earliestYearMonth = currentYearMonth
            }
        }
        return earliestYearMonth
    }

    fun getLatestYearMonth(summaries: List<FileSummary>): Int? {
        var latestYearMonth: Int? = null
        for (summary in summaries) {
            val fileSuffix: String? = summary.fileSuffix
            val currentYearMonth = convertStringToYearMonth(fileSuffix)
            if (currentYearMonth != null && (latestYearMonth == null || currentYearMonth > latestYearMonth)) {
                latestYearMonth = currentYearMonth
            }
        }
        return latestYearMonth
    }

    fun convertStringToYearMonth(input: String?): Int? {
        if (input == null) {
            return null
        }
        val list = input.split(" ")
        if (list.isNullOrEmpty() || list.size != 2) {
            return null
        }
        val year = list.get(0).toIntOrNull()
        val month = list.get(1).toIntOrNull()
        return year!! * 100 + month!!
    }

    fun getFileEntity(fileType: String, fileSuffix: String, ukprn: Int) : FileEntity? {
        return fileEntityRepository.findFirstByFileTypeAndFileSuffixAndUkprn(fileType, fileSuffix, ukprn)
    }

    fun saveFile(dataImportId: Int?, fileType: String, fileSuffix: String, ukprn: Int, content: String) {
        fileEntityRepository.save(FileEntity(
                dataImportId = dataImportId,
                fileType = fileType,
                fileSuffix = fileSuffix,
                ukprn = ukprn,
                content = content
        ))
    }

    fun deleteTestData() {
        if (environment.isTestEnvironment) {
            fileEntityRepository.deleteAll()
        }
        else {
            log.warn("attempt to delete data in a non-test environment!")
        }
    }

    fun deleteByDataImportId(dataImportId: Int) {
        fileEntityRepository.deleteByDataImportId(dataImportId)
    }

    fun findByDataImportId(dataImportId: Int) : FileEntity? {
        return fileEntityRepository.findByDataImportId(dataImportId)
    }

}
