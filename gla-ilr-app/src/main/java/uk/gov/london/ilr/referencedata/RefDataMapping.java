/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.referencedata;

import com.querydsl.core.annotations.QueryEntity;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "ref_data_mapping")
@QueryEntity
public class RefDataMapping {

    @Id
    public RefDataMappingPK id;
    public String headlineValue;
    public String detailedValue;
    public LocalDateTime addedOn;
    public String addedBy;

    public RefDataMapping() {
    }

    public RefDataMapping(RefDataMappingPK id, String headlineValue, String detailedValue, LocalDateTime addedOn,
            String addedBy) {
        this.id = id;
        this.headlineValue = headlineValue;
        this.detailedValue = detailedValue;
        this.addedOn = addedOn;
        this.addedBy = addedBy;
    }
}
