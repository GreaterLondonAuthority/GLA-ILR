/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.data;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import java.util.Set;

public class QFundingSummaryRecord extends EntityPathBase<FundingSummaryRecord> {
    public static final QFundingSummaryRecord qFundingSummaryRecord = new QFundingSummaryRecord();

    public QFundingSummaryRecord() {
        super(FundingSummaryRecord.class, forVariable("qFundingSummaryRecord"));
    }

    public final NumberPath<Integer> academicYear = createNumber("academicYear", Integer.class);
    public final NumberPath<Integer> ukprn = createNumber("ukprn", Integer.class);
    public final NumberPath<Integer> period = createNumber("period", Integer.class);

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    public void andSearch(Set<Integer> ukprns, Integer academicYear, Integer period){
        if (ukprns != null && !ukprns.isEmpty()) {
            ukprns.forEach(ukprnValue -> {
                if(ukprnValue != null) {
                    predicateBuilder.and(this.ukprn.eq(ukprnValue));
                }
            });
        }

        if (academicYear != null){
            predicateBuilder.and(this.academicYear.eq(academicYear));
        }

        if (period != null){
            predicateBuilder.and(this.period.eq(period));
        }

    }

    public Predicate getPredicate() {
        return predicateBuilder.getValue();
    }
}
