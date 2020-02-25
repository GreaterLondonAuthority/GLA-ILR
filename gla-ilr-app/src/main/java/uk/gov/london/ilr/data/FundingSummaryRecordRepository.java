/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.data;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FundingSummaryRecordRepository extends JpaRepository<FundingSummaryRecord, Integer>, QuerydslPredicateExecutor<FundingSummaryRecord> {

    List<FundingSummaryRecord> findAll();

    List<FundingSummaryRecord> findAllByAcademicYearAndPeriod(Integer academicYear, Integer period);

    default Page<FundingSummaryRecord> findAll(Set<Integer> ukprns, Integer academicYear, Integer period, Pageable pageable) {
        QFundingSummaryRecord query = new QFundingSummaryRecord();
        query.andSearch(ukprns,academicYear, period);
        return findAll(query.getPredicate(), pageable);
    }

    void deleteByAcademicYearAndPeriod(Integer academicYear, Integer period);

}
