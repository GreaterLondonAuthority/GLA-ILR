/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.data;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QFundingLearnerRecord extends EntityPathBase<FundingLearnerRecord> {
    public static final QFundingLearnerRecord qFundingLearnerRecord = new QFundingLearnerRecord();

    public QFundingLearnerRecord() {
        super(FundingLearnerRecord.class, forVariable("qFundingLearnerRecord"));
    }

    public final NumberPath<Integer> year = createNumber("year", Integer.class);
    public final NumberPath<Integer> ukprn = createNumber("ukprn", Integer.class);
    public final NumberPath<Integer> month = createNumber("month", Integer.class);

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    public void andSearch(Integer ukprn, Integer year, Integer month){
        if (ukprn != null){
            predicateBuilder.and(this.ukprn.eq(ukprn));
        }

        if (year != null){
            predicateBuilder.and(this.year.eq(year));
        }

        if (month != null){
            predicateBuilder.and(this.month.eq(month));
        }

    }

    public Predicate getPredicate() {
        return predicateBuilder.getValue();
    }
}
