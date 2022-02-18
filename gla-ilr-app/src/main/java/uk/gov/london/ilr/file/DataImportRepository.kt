package uk.gov.london.ilr.file

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DataImportRepository: JpaRepository<DataImport, Int> {
    fun findAllByOrderByCreatedOnDesc(): List<DataImport>

    @Query("select * from data_import where created_by = ?1 and import_type = ?2 order by created_on desc LIMIT 1", nativeQuery = true)
    fun findLatestUploadByUserAndType(user: String, type: String): DataImport?

    fun findByImportTypeAndAcademicYearAndPeriod(type: DataImportType, academicYear: Int, period: Int): DataImport?
}
