package uk.gov.london.ilr.data

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface DataImportRepository: JpaRepository<DataImport, Int> {
    fun findAllByOrderByCreatedOnDesc(): List<DataImport>
}

interface LearnerRepository: JpaRepository<Learner, Long> {

    @Query(value = "select * from learner l where ?1 is null or ?1 = l.id", nativeQuery = true)
    fun findAll(id: Long?, pageable: Pageable?): Page<Learner>

    fun findByLearnerReferenceNumber(learner: String): Learner

}

interface LearnerSummaryRepository: JpaRepository<LearnerSummary, Int>, QuerydslPredicateExecutor<LearnerSummary> {

    @JvmDefault
    fun findAll(learner: String?, ukprn: Set<Int?>?, academicYear: Int?, filterBySupplementaryData: Boolean?, pageable: Pageable): Page<LearnerSummary> {
        val query = QLearnerSummary()
        query.andSearch(learner, ukprn, academicYear, filterBySupplementaryData)
        if (query.predicate == null){
            return findAll(pageable)
        } else {
            return findAll(query.predicate, pageable)
        }
    }

}

interface OccupancyRecordRepository: JpaRepository<OccupancyRecord, Int> {

    fun findAllByLearnerId(id: Long) : List<OccupancyRecord>

    @Query(value = "select count(distinct ocr.learner_id) from occupancy_record ocr ", nativeQuery = true)
    fun findAllUniqueLearners(): Int

    @Query(value = "select count(distinct ocr.learner_id) from occupancy_record ocr  where ocr.ukprn in ?1", nativeQuery = true)
    fun findAllUniqueLearnersByUkprn(ukprn: Set<Int>?): Int

    @Query(value = "select count(distinct ocr.learner_id) from occupancy_record ocr  where ocr.academic_year = ?1", nativeQuery = true)
    fun findAllUniqueLearnersByYear(year: Int): Int

    @Query(value = "select count(distinct ocr.learner_id) from occupancy_record ocr  where ocr.academic_year = ?1 and ocr.ukprn in ?2", nativeQuery = true)
    fun findAllUniqueLearnersByYearAndUkprn(year: Int, ukprn: Set<Int>?): Int

    fun countAllByLearnerReferenceNumberAndUkprn(learner: String, ukprn: Int): Int

    @Query(value = "select distinct academic_year from occupancy_record where academic_year is not null order by academic_year", nativeQuery = true)
    fun findDistinctAcademicYears(): List<Int>

    @Query(value = "select distinct period from occupancy_record where (period is not null and academic_year =?1) order by period", nativeQuery = true)
    fun findDistinctPeriods(academicYear: Int?): List<Int>

    @Query(value = "select count(distinct ocr.ukprn) from occupancy_record ocr", nativeQuery = true)
    fun countDistinctUkprns(): Int

    @Query(value = "select sum(ocr.total_earned_cash) from occupancy_record ocr where ocr.academic_year = ?1 and ocr.grant_type = ?2 and ocr.ukprn in ?3", nativeQuery = true)
    fun totalDeliveryByYearGrantTypeAndUkprn(year: Int?, type: String, ukprn: Set<Int>?): Long?

    @Query(value = "select sum(ocr.total_earned_cash) from occupancy_record ocr where ocr.academic_year = ?1 and ocr.grant_type = ?2 and ocr.period = ?3 and ocr.ukprn in ?4", nativeQuery = true)
    fun totalDeliveryByYearGrantTypePeriodAndUkprn(year: Int?, type: String, period: Int?, ukprn: Set<Int>?): Long?

    @Query(value = "select sum(ocr.total_earned_cash) from occupancy_record ocr where ocr.academic_year = ?1 and ocr.grant_type = ?2", nativeQuery = true)
    fun totalDeliveryByYearAndGrantType(year: Int?, type: String): Long?

    @Query(value = "select sum(ocr.total_earned_cash) from occupancy_record ocr where  ocr.grant_type = ?1 and ukprn in ?2", nativeQuery = true)
    fun totalDeliveryByGrantTypeAndUkprn(type: String, ukprn: Set<Int>?): Long?

    @Query(value = "select sum(ocr.total_earned_cash) from occupancy_record ocr where ocr.grant_type = ?1", nativeQuery = true)
    fun totalDeliveryByGrantType(type: String): Long?
}

interface OccupancySummaryRepository: JpaRepository<OccupancySummary, Int> {

    fun findAllByAcademicYear(academicYear: Int): List<OccupancySummary>
    fun findByUkprnIn(ids: Set<Int>?): List<OccupancySummary>

}

interface SupplementaryDataRepository: JpaRepository<SupplementaryData, SupplementaryDataPK> {
}

interface HealthProblemRepository: JpaRepository<HealthProblemRecord, Int> {
    fun deleteAllByYearAndReturnNumber(year: String, returnNumber: Int?): Int

    @Query("select * from " +
                       "(SELECT * FROM HEALTH_PROBLEM_RECORD  where learner_ref_number =?1\n" +
                         "order by year desc, return_number desc, month desc\n" +
                       ") healthRecord  limit 1\n", nativeQuery = true)
    fun findLatestHealthProblemRecord(learnerRefNumber: String?): HealthProblemRecord?
}

interface HealthProblemCategoryRepository: JpaRepository<HealthProblemCategory, Int> {
}

