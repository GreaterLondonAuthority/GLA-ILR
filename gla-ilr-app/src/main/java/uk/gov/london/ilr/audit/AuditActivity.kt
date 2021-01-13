/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.audit

import java.time.OffsetDateTime
import javax.persistence.*

@Entity(name = "audit_activity")
class AuditActivity (

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq_gen")
    @SequenceGenerator(name = "audit_seq_gen", sequenceName = "audit_activity_seq", initialValue = 100001, allocationSize = 1)
    var id: Int? = null,

    @Column(name = "username")
    var username: String? = null,

    @Column(name = "activity_time")
    var timestamp: OffsetDateTime? = null,

    @Column(name = "summary")
    var summary: String? = null

)
