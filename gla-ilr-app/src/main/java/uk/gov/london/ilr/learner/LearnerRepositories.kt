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
}

interface RefDataMappingRepository: JpaRepository<RefDataMapping, Int>
