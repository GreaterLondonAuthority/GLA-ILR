/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.learner;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.Set;

class SupplementaryDataSummaryQueryBuilder {

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    SupplementaryDataSummaryQueryBuilder withLearner(String learner) {
        if (learner != null && !learner.isEmpty()) {
            BooleanExpression expression = QSupplementaryDataSummary.supplementaryDataSummary.id.learnerReferenceNumber.equalsIgnoreCase(learner);
            predicateBuilder.and(expression);
        }
        return this;
    }

    SupplementaryDataSummaryQueryBuilder withUkprns(Set<Integer> ukprns) {
        if (ukprns != null && !ukprns.isEmpty()) {
            predicateBuilder.and(QSupplementaryDataSummary.supplementaryDataSummary.id.ukprn.in(ukprns));
        }
        return this;
    }

    SupplementaryDataSummaryQueryBuilder withAcademicYear(Integer academicYear) {
        if (academicYear != null) {
            predicateBuilder.and(QSupplementaryDataSummary.supplementaryDataSummary.id.year.eq(academicYear));
        }
        return this;
    }
}
