/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.learner;

import com.querydsl.core.annotations.QueryEmbeddable;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@QueryEmbeddable
public class LearnerPK implements Serializable {

    public String learnerReferenceNumber;
    public Integer ukprn;
    public Integer year;

    public LearnerPK() {}

    public LearnerPK(String learnerReferenceNumber, Integer ukprn, Integer year) {
        this.learnerReferenceNumber = learnerReferenceNumber;
        this.ukprn = ukprn;
        this.year = year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LearnerPK learnerPK = (LearnerPK) o;
        return Objects.equals(learnerReferenceNumber, learnerPK.learnerReferenceNumber)
            && Objects.equals(ukprn, learnerPK.ukprn)
            && Objects.equals(year, learnerPK.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(learnerReferenceNumber, ukprn, year);
    }

    public String toString() {
        return String.format("%s-%d-%d", learnerReferenceNumber, ukprn, year);
    }

}
