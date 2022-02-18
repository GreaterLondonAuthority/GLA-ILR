/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.providerallocation;

import com.querydsl.core.annotations.QueryEmbeddable;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@QueryEmbeddable
public class ProviderAllocationPK implements Serializable {

    public Integer year;
    public Integer ukprn;
    public String opsProjectType;
    public String allocationType;

    public ProviderAllocationPK() {
    }

    public ProviderAllocationPK(Integer year, Integer ukprn, String opsProjectType, String allocationType) {
        this.year = year;
        this.ukprn = ukprn;
        this.opsProjectType = opsProjectType;
        this.allocationType = allocationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProviderAllocationPK that = (ProviderAllocationPK) o;
        return Objects.equals(year, that.year) &&
                Objects.equals(ukprn, that.ukprn) &&
                Objects.equals(opsProjectType, that.opsProjectType) &&
                Objects.equals(allocationType, that.allocationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, ukprn, opsProjectType, allocationType);
    }

    @Override
    public String toString() {
        return "ProviderAllocationPK{" +
                "year=" + year +
                ", ukprn=" + ukprn +
                ", opsProjectType='" + opsProjectType + '\'' +
                ", allocationType='" + allocationType + '\'' +
                '}';
    }
}
