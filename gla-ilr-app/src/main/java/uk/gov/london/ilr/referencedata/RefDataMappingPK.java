/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.referencedata;

import com.querydsl.core.annotations.QueryEmbeddable;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

@Embeddable
@QueryEmbeddable
public class RefDataMappingPK implements Serializable {

    public Integer year;
    public String attribute;
    public String code;

    public RefDataMappingPK() {
    }

    public RefDataMappingPK(Integer year, String attribute, String code) {
        this.year = year;
        this.attribute = attribute;
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RefDataMappingPK refDataMappingPK = (RefDataMappingPK) o;
        return Objects.equals(attribute, refDataMappingPK.attribute)
                && Objects.equals(code, refDataMappingPK.code)
                && Objects.equals(year, refDataMappingPK.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, attribute, code);
    }

    public String toString() {
        return String.format("%d-%s-%s", year, attribute, code);
    }

}
