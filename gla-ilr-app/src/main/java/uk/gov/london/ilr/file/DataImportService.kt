package uk.gov.london.ilr.file

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.london.ilr.audit.AuditService
import javax.transaction.Transactional

@Service
class DataImportService @Autowired constructor(val auditService: AuditService,
                                               val fileService: FileService,
                                               val dataImportRepository: DataImportRepository) {

    @Throws(Exception::class)
    fun dataImports(): List<DataImport> {
        val foundMap: MutableSet<String> = hashSetOf("")
        var filesSorted = dataImportRepository.findAllByOrderByCreatedOnDesc()
        filesSorted =filesSorted.filter { f -> f.importType != DataImportType.SUPPLEMENTARY_DATA }

        for (file in filesSorted) {
            if (file.importType!!.canSendToOPS) {
                val key = "${file.importType!!.description} ${file.academicYear} ${file.period}"
                if (!foundMap.contains(key)) {
                    foundMap.add(key)
                    file.canPushToOPS = true
                }
            }
        }
        return filesSorted
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun saveWithNewTransaction(dataImport: DataImport?): DataImport {
        return dataImportRepository.save(dataImport!!)
    }

    fun dataImportCount(): Long {
        return dataImportRepository.count()
    }

    fun getDataImportRecord(id: Int): DataImport? {
        return dataImportRepository.findById(id).orElse(null)
    }

    fun updateDataImportRecord(record: DataImport): DataImport {
        return dataImportRepository.save(record)
    }

    fun getLatestImportForUser(user: String, fileType: String) : DataImport? {
        return dataImportRepository.findLatestUploadByUserAndType(user, fileType)
    }

    @Transactional
    fun delete(id: Int) {
        val dataImport = dataImportRepository.getOne(id)
        dataImportRepository.delete(dataImport)
        fileService.deleteByDataImportId(id)
        auditService.auditCurrentUserActivity("Deleted file '${dataImport.fileName}'")
    }

}
