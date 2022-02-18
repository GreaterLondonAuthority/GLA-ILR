/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.referencedata;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

class RefDataMappingQueryBuilder {

    private final BooleanBuilder predicateBuilder = new BooleanBuilder();

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    RefDataMappingQueryBuilder withYear(Integer year) {
        if (year != null) {
            predicateBuilder.and(QRefDataMapping.refDataMapping.id.year.eq(year));
        }
        return this;
    }

    RefDataMappingQueryBuilder withAttribute(String attribute) {
        if (attribute != null) {
            predicateBuilder.and(QRefDataMapping.refDataMapping.id.attribute.eq(attribute));
        }
        return this;
    }

}
