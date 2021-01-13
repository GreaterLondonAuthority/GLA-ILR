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

class LearnerSummaryQueryBuilder {

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    LearnerSummaryQueryBuilder withLearner(String learner) {
        if (learner != null && !learner.isEmpty()) {
            BooleanExpression expression = QLearnerSummary.learnerSummary.id.learnerReferenceNumber.equalsIgnoreCase(learner);
            if (learner.matches("\\d+")) {
                expression = expression.or(QLearnerSummary.learnerSummary.learnerId.eq(Long.valueOf(learner)));
            }
            predicateBuilder.and(expression);
        }
        return this;
    }

    LearnerSummaryQueryBuilder withUkprns(Set<Integer> ukprns) {
        if (ukprns != null && !ukprns.isEmpty()) {
            predicateBuilder.and(QLearnerSummary.learnerSummary.id.ukprn.in(ukprns));
        }
        return this;
    }

    LearnerSummaryQueryBuilder withAcademicYear(Integer academicYear) {
        if (academicYear != null) {
            predicateBuilder.and(QLearnerSummary.learnerSummary.id.year.eq(academicYear));
        }
        return this;
    }

    LearnerSummaryQueryBuilder withFilterBySupplementaryData(Boolean filterBySupplementaryData) {
        if (filterBySupplementaryData != null) {
            if (filterBySupplementaryData) {
                predicateBuilder.and(QLearnerSummary.learnerSummary.lastSupplementaryDataUpload.isNotNull());
            } else {
                predicateBuilder.and(QLearnerSummary.learnerSummary.lastSupplementaryDataUpload.isNull());
            }
        }
        return this;
    }

}
