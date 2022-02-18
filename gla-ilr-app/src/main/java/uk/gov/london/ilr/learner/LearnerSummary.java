/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.learner;

import com.querydsl.core.annotations.QueryEntity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "v_learner_summary")
@QueryEntity
public class LearnerSummary {

    @Id
    public LearnerPK id;
    public Long learnerId;

    public LearnerSummary() {}

    public LearnerSummary(LearnerPK id, Long learnerId) {
        this.id = id;
        this.learnerId = learnerId;
    }
}
