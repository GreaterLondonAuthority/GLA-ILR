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

@Entity(name = "v_supplementary_data_summary")
@QueryEntity
public class SupplementaryDataSummary {

    @Id
    public SupplementaryDataPK id;
    public Integer period;
    public OffsetDateTime lastSupplementaryDataUpload;
    public String investmentPriorityClaimedUnder;
    public String homeless;
    public String highestEducationalAttainmentAtEsfStart;
    public String highestLiteracyAttainmentAtEsfStart;
    public String highestNumeracyAttainmentAtEsfStart;
    public String progressingIntoEducationOrTrainingAsEsfResult;
    public LocalDate startDateForEducationOrTrainingEsfResult;
    public String hasLeftEsfProgramme;
    public String esfReturner;
    public LocalDate esfLeaveDate;

    public SupplementaryDataSummary() {}

    public SupplementaryDataSummary(SupplementaryDataPK id, Integer period, OffsetDateTime lastSupplementaryDataUpload,
                                    String investmentPriorityClaimedUnder, String homeless) {
        this.id = id;
        this.period = period;
        this.lastSupplementaryDataUpload = lastSupplementaryDataUpload;
        this.investmentPriorityClaimedUnder = investmentPriorityClaimedUnder;
        this.homeless = homeless;
    }
}
