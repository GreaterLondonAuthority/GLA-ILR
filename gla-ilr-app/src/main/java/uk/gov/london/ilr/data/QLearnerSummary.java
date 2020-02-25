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
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import java.time.OffsetDateTime;
import java.util.Set;

public class QLearnerSummary extends EntityPathBase<LearnerSummary> {
    public static final QLearnerSummary qLearnerSummary = new QLearnerSummary();

    public QLearnerSummary() {
        super(LearnerSummary.class, forVariable("qLearnerSummary"));
    }

    public final NumberPath<Long> learnerId = createNumber("learnerId", Long.class);
    public final StringPath learnerReferenceNumber = createString("learnerReferenceNumber");
    public final NumberPath<Integer> ukprn = createNumber("ukprn", Integer.class);
    public final NumberPath<Integer> academicYear = createNumber("academicYear", Integer.class);
    public final DateTimePath<OffsetDateTime> supplementaryDataUpload = createDateTime("lastSupplementaryDataUpload", OffsetDateTime.class);

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    public void andSearch(String learner, Set<Integer> ukprn, Integer academicYear, Boolean filterBySupplementaryData) {
        if (learner != null && !learner.isEmpty()) {
            BooleanExpression booleanExpression = learnerReferenceNumber.equalsIgnoreCase(learner);
            if (learner.matches("\\d+")) {
                booleanExpression = booleanExpression.or(learnerId.eq(Long.valueOf(learner)));
            }
            ;
            predicateBuilder.and(booleanExpression);
        }

        if (ukprn != null && !ukprn.isEmpty()) {
            ukprn.forEach(ukprnValue -> {
                if(ukprnValue != null) {
                    predicateBuilder.and(this.ukprn.eq(ukprnValue));
                }
            });
        }

        if (academicYear != null) {
            predicateBuilder.and(this.academicYear.eq(academicYear));
        }

        if (filterBySupplementaryData != null && filterBySupplementaryData) {
            predicateBuilder.and(this.supplementaryDataUpload.isNotNull());
        }

    }

    public Predicate getPredicate() {
        return predicateBuilder.getValue();
    }
}
