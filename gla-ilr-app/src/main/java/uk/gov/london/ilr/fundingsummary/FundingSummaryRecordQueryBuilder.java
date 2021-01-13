/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.fundingsummary;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import java.util.Set;

class FundingSummaryRecordQueryBuilder {

    private final BooleanBuilder predicateBuilder = new BooleanBuilder();

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    FundingSummaryRecordQueryBuilder withUkprns(Set<Integer> ukprns) {
        if (ukprns != null && !ukprns.isEmpty()) {
            ukprns.forEach(ukprnValue -> {
                if (ukprnValue != null) {
                    predicateBuilder.and(QFundingSummaryRecord.fundingSummaryRecord.ukprn.eq(ukprnValue));
                }
            });
        }
        return this;
    }

    FundingSummaryRecordQueryBuilder withAcademicYear(Integer academicYear) {
        if (academicYear != null) {
            predicateBuilder.and(QFundingSummaryRecord.fundingSummaryRecord.academicYear.eq(academicYear));
        }
        return this;
    }

    FundingSummaryRecordQueryBuilder withPeriod(Integer period) {
        if (period != null) {
            predicateBuilder.and(QFundingSummaryRecord.fundingSummaryRecord.period.eq(period));
        }
        return this;
    }

}
