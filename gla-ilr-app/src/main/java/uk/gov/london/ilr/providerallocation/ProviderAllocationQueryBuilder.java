/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.providerallocation;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import java.util.Set;

class ProviderAllocationQueryBuilder {

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    ProviderAllocationQueryBuilder withUkprns(Set<Integer> ukprns) {
        if (ukprns != null && !ukprns.isEmpty()) {
            predicateBuilder.and(QProviderAllocation.providerAllocation.id.ukprn.in(ukprns));
        }
        return this;
    }

    ProviderAllocationQueryBuilder withAcademicYear(Integer academicYear) {
        if (academicYear != null) {
            predicateBuilder.and(QProviderAllocation.providerAllocation.id.year.eq(academicYear));
        }
        return this;
    }
}
