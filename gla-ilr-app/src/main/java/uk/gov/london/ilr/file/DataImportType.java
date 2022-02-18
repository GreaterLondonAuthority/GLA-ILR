/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.file;

public enum DataImportType {

    FUNDING_SUMMARY("Funding Summary",
            "",
            true,
            true,
            true,
            false),

    OCCUPANCY_REPORT("Occupancy Report",
            "",
            false,
            true,
            true,
            false),

    SUPPLEMENTARY_DATA("Supplemental Data",
            "",
            false,
            true,
            true,
            false),

    PROVIDER_ALLOCATION("Provider Allocations",
            "",
            false,
            true,
            false,
            false),

    DATA_VALIDATION_ISSUES("Data Validation Issues",
            "",
            false,
            true,
            true,
            true),

    GLA_FSR("GLA FSR",
            "",
            false,
            true,
            true,
            true,
            true),

    GLA_OCC("GLA OCC",
            "",
            false,
            true,
            true,
            true,
            true),

    ILR_CODE_VALUES("ILR Code Values",
            "",
            false,
            true,
            false,
            false);

    final String description;
    final String format;
    final boolean canSendToOPS;
    final boolean isYearlyFile;
    final boolean isMonthlyFile;
    final boolean isDeletable;
    final boolean clearPreviousData;

    DataImportType(String description, String format, boolean canSendToOPS, boolean isYearlyFile, boolean isMonthlyFile,
            boolean isDeletable) {
        this(description, format, canSendToOPS, isYearlyFile, isMonthlyFile, isDeletable, false);

    }

    DataImportType(String description, String format, boolean canSendToOPS, boolean isYearlyFile, boolean isMonthlyFile,
                   boolean isDeletable, boolean clearPreviousData) {
        this.description = description;
        this.format = format;
        this.canSendToOPS = canSendToOPS;
        this.isYearlyFile = isYearlyFile;
        this.isMonthlyFile = isMonthlyFile;
        this.isDeletable = isDeletable;
        this.clearPreviousData = clearPreviousData;
    }

    public String getDescription() {
        return description;
    }

    public boolean isYearlyFile() {
        return isYearlyFile;
    }

    public boolean isMonthlyFile() {
        return isMonthlyFile;
    }

    public boolean isDeletable() {
        return isDeletable;
    }

    public boolean isClearPreviousData() {
        return clearPreviousData;
    }

    public boolean shouldClearPreviousData() {
        return isDeletable && clearPreviousData && isYearlyFile && isMonthlyFile;
    }

    public static DataImportType getByDescription(String desc) {
        for (DataImportType value : DataImportType.values()) {
            if (value.getDescription().equals(desc)) {
                return value;
            }
        }
        return null;
    }

    public static DataImportType getTypeByFilename(String filename) {
        if (!filename.toUpperCase().endsWith(".CSV")) {
            throw new RuntimeException("File must be in CSV format, to do this save an excel file as a .CSV");
        }

        if (filename.matches("Funding Summary Report(.*)+(.csv)")) {
            return DataImportType.FUNDING_SUMMARY;
        }
        if (filename.matches("Occupancy [rR]eport(.*)+(.csv)")) {
            return DataImportType.OCCUPANCY_REPORT;
        }
        if (filename.matches("(.*)Supp [dD]ata(.*)+(.csv)")) {
            return DataImportType.SUPPLEMENTARY_DATA;
        }
        if (filename.matches("[pP]rovider [aA]llocation(.*)+(.csv)")) {
            return DataImportType.PROVIDER_ALLOCATION;
        }
        if (filename.matches("[dD]ata [vV]alidation [iI]ssues(.*)+(.csv)")) {
            return DataImportType.DATA_VALIDATION_ISSUES;
        }
        if (filename.matches("ILR [cC]ode [vV]alue(.*)+(.csv)")) {
            return DataImportType.ILR_CODE_VALUES;
        }
        if (filename.matches("GLA FSR(.*)+(.csv)")) {
            return DataImportType.GLA_FSR;
        }
        if (filename.matches("GLA OCC(.*)+(.csv)")) {
            return DataImportType.GLA_OCC;
        }

        return null;
    }

}
