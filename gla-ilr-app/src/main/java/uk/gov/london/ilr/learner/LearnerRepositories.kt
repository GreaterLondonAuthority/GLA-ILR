package uk.gov.london.ilr.learner

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface LearnerRepository: JpaRepository<Learner, LearnerPK>{
    @Query(value = "delete from learner where year = ?1", nativeQuery = true)
    @Modifying
    fun deleteAllByIdYear(year: Int)

    @Query(value = "select distinct year from learner where year is not null order by year", nativeQuery = true)
    fun findDistinctAcademicYears(): List<Int>

    @Query(value = "SELECT COUNT(1) > 0 FROM learner WHERE ukprn = ?1 and learner_reference_number = ?2 and year <= ?3", nativeQuery = true)
    fun recordExists(ukprn: Int, learnerRefNumber: String?, year: Int?) : Boolean

    @Query(value = "SELECT COUNT(1) > 0 FROM learner WHERE return = ?2 and year = ?1", nativeQuery = true)
    fun recordByYearAndPeriodExists(year: Int, period: Int): Boolean
}
interface LearningDeliveryRepository : JpaRepository<LearningDelivery, LearningDeliveryPK> {
    fun findAllByIdUkprnAndIdLearnerReferenceNumberAndIdYearOrderByIdAimSequenceNumber(ukprn: Int,lrn: String,  year: Int): List<LearningDelivery>
    fun countAllByIdUkprnAndIdLearnerReferenceNumber(ukprn: Int,lrn: String): Int

    @Query(value = "delete from learning_delivery where year = ?1", nativeQuery = true)
    @Modifying
    fun deleteAllByIdYear(year: Int)


}
interface EarningPeriodRepository: JpaRepository<EarningPeriod, EarningPeriodPK> {
    @Query(value = "delete from earning_period where year = ?1", nativeQuery = true)
    @Modifying
    fun deleteAllByIdYear(year: Int)
}

interface ProviderRepository: JpaRepository<Provider, ProviderPK>{
    @Query(value = "delete from provider where year = ?1", nativeQuery = true)
    @Modifying
    fun deleteAllByIdYear(year: Int)
}

interface LearningAimRepository: JpaRepository<LearningAim, LearningAimPK>

interface SupplementaryDataRepository: JpaRepository<SupplementaryData, SupplementaryDataPK> {
    @Query("select * from " +
            "(select * from supplementary_data  where learner_reference_number =?1\n" +
            "order by last_supplementary_data_upload desc\n" +
            ") supplementaryData  limit 1\n", nativeQuery = true)
    fun findLatestSupplementaryDataRecord(learnerRefNumber: String?): SupplementaryData?

    @Query(value = "delete from supplementary_data where ukprn in (?1) and year = ?2", nativeQuery = true)
    @Modifying
    fun deleteAllByUkprnAndYear(ukprns: Set<Int>, year: Int)

    @Query(value = "delete from supplementary_data where year = ?1 and period = ?2", nativeQuery = true)
    @Modifying
    fun deleteAllByYearAndPeriod(year: Int, period: Int)

    @Query(value = "select distinct sd.year from supplementary_data sd join learner l on l.learner_reference_number = sd.learner_reference_number and l.ukprn = sd.ukprn and l.year = sd.year where sd.year is not null order by sd.year", nativeQuery = true)
    fun findDistinctAcademicYears(): List<Int>
}