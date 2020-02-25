/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundingLearnerRecordRepository extends JpaRepository<FundingLearnerRecord, Integer>, QuerydslPredicateExecutor<FundingLearnerRecord> {

    List<FundingLearnerRecord> findAll();

    List<FundingLearnerRecord> findByUkprnAndLearnRefNumber(Integer ukprn, String learnerRef);

    default Page<FundingLearnerRecord> findAll(Integer ukprn, Integer year, Integer month, Pageable pageable) {
        QFundingLearnerRecord query = new QFundingLearnerRecord();
        query.andSearch(ukprn,year, month);
        return findAll(query.getPredicate(), pageable);
    }
}
