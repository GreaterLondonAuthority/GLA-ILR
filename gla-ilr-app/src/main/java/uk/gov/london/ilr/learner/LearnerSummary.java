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
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity(name = "v_learner_summary")
@QueryEntity
public class LearnerSummary {

    @Id
    public LearnerPK id;
    public Long learnerId;
    public OffsetDateTime lastSupplementaryDataUpload;
    public String investmentPriorityClaimedUnder;
    public String hasBasicSkills;
    public String homeless;
    public String highestEducationalAttainmentAtEsfStart;
    public String progressingIntoEducationOrTrainingAsEsfResult;
    public LocalDate startDateForEducationOrTrainingEsfResult;
    public String hasLeftEsfProgram;
    public LocalDate esfLeaveDate;

    public LearnerSummary() {}

    public LearnerSummary(LearnerPK id, Long learnerId, OffsetDateTime lastSupplementaryDataUpload,
                          String investmentPriorityClaimedUnder, String hasBasicSkills, String homeless) {
        this.id = id;
        this.learnerId = learnerId;
        this.lastSupplementaryDataUpload = lastSupplementaryDataUpload;
        this.investmentPriorityClaimedUnder = investmentPriorityClaimedUnder;
        this.hasBasicSkills = hasBasicSkills;
        this.homeless = homeless;
    }
}
