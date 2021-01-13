/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.fundingsummary;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FundingSummaryRecordRepository extends JpaRepository<FundingSummaryRecord, Integer>,
        QuerydslPredicateExecutor<FundingSummaryRecord> {

    default Page<FundingSummaryRecord> findAll(Set<Integer> ukprns, Integer academicYear, Integer period, Pageable pageable) {
        Predicate predicate = new FundingSummaryRecordQueryBuilder()
                .withUkprns(ukprns)
                .withAcademicYear(academicYear)
                .withPeriod(period)
                .getPredicate();
        if (predicate == null) {
            return findAll(pageable);
        } else {
            return findAll(predicate, pageable);
        }
    }

    List<FundingSummaryRecord> findAllByAcademicYearAndPeriod(Integer academicYear, Integer period);

    void deleteByAcademicYearAndPeriod(Integer academicYear, Integer period);

    @Query(value = "select distinct academic_year from funding_summary_record "
        + "where academic_year is not null order by academic_year desc", nativeQuery = true)
    List<Integer> findDistinctAcademicYears();

}
