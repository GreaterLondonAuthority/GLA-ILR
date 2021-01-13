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
        return if (currentUser.isGla) {
            fileSummaryRepository.findAll()
        }
        else {
            fileSummaryRepository.findAllByUkprnIn(currentUser.ukprns)
        }
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
