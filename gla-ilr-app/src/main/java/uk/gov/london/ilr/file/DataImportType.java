/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.file;

public enum DataImportType {

    LEARNER("Learner",
            "",
            false,
            false,
            false),

    FUNDING_SUMMARY("Funding Summary",
            "",
            true,
            true,
            false),

    OCCUPANCY_REPORT("Occupancy Report",
            "",
            false,
            true,
            false),

    SUPPLEMENTARY_DATA("Supplemental Data",
            "",
            false,
            false,
            false),

    DATA_VALIDATION_ISSUES("Data Validation Issues",
            "",
            false,
            true,
            true);

    final String description;
    final String format;
    final boolean canSendToOPS;
    final boolean isMonthlyFile;
    final boolean isDeletable;

    DataImportType(String description, String format, boolean canSendToOPS, boolean isMonthlyFile, boolean isDeletable) {
        this.description = description;
        this.format = format;
        this.canSendToOPS = canSendToOPS;
        this.isMonthlyFile = isMonthlyFile;
        this.isDeletable = isDeletable;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMonthlyFile() {
        return isMonthlyFile;
    }

    public boolean isDeletable() {
        return isDeletable;
    }

    public static DataImportType getTypeByFilename(String filename) {
        if (!filename.toUpperCase().endsWith(".CSV")) {
            throw new RuntimeException("Upload failed: File must be in CSV format, to do this save an excel file as a .CSV");
        }

        if (filename.matches("Funding Summary Report(.*)+(.csv)")) {
            return DataImportType.FUNDING_SUMMARY;
        }
        if (filename.matches("(.*)[lL]earner(.*)+(.csv)")) {
            return DataImportType.LEARNER;
        }
        if (filename.matches("Occupancy [rR]eport(.*)+(.csv)")) {
            return DataImportType.OCCUPANCY_REPORT;
        }
        if (filename.matches("Supplemental [dD]ata(.*)+(.csv)")) {
            return DataImportType.SUPPLEMENTARY_DATA;
        }
        if (filename.matches("[dD]ata [vV]alidation [iI]ssues(.*)+(.csv)")) {
            return DataImportType.DATA_VALIDATION_ISSUES;
        }

        return null;
    }

}
