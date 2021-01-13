/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.permission

enum class PermissionType(val permissionKey: String, permissionDescription: String) {
    DEFAULT_PERMISSION("default.permission", "Default permission"),
    UPLOAD_SUPPLEMENTAL_FILE("upload.supplemental.file", "Upload supplementary data file");

    private val permissionDescription: String?

    init {
        this.permissionDescription = permissionDescription
    }
}