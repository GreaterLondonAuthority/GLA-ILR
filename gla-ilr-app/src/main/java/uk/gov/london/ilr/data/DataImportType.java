/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.data;

public enum DataImportType {
    LEARNER("Learner", "Learner", false),
    FUNDING_SUMMARY("Funding Summary", "Funding Summary Report", true),
    OCCUPANCY_REPORT("Occupancy Report", "Occupancy Report", false),
    SUPPLEMENTARY_DATA("Supplemental Data", "Supplemental Data", false),
    HEALTH_PROBLEM("Health Problem", "Health Problem", false),
    HEALTH_PROBLEM_CATEGORY("Health Problem Category", "Health Problem Category", false);

    String name;
    String filenameIdentifier;
    boolean canSendToOPS;

    DataImportType(String name, String filenameIdentifier, boolean canSendToOPS) {
        this.name = name;
        this.filenameIdentifier = filenameIdentifier;
        this.canSendToOPS = canSendToOPS;
    }

    public String getName() {
        return name;
    }

    public String getFilenameIdentifier() {
        return filenameIdentifier;
    }

    public boolean isCanSendToOPS() {
        return canSendToOPS;
    }

    public static DataImportType getTypeByFilename(String filename){

        if (filename.matches("Funding Summary Report(.*)+(.csv)"))
            return DataImportType.FUNDING_SUMMARY;
        if (filename.matches("(.*)[lL]earner(.*)+(.csv)"))
            return DataImportType.LEARNER;
        if (filename.matches("Occupancy [rR]eport(.*)+(.csv)"))
            return DataImportType.OCCUPANCY_REPORT;
        if (filename.matches("Supplemental [dD]ata(.*)+(.csv)"))
            return DataImportType.SUPPLEMENTARY_DATA;
        if (filename.matches("SILR_(.*)_LLDDHealthProblem+(.csv)"))
            return DataImportType.HEALTH_PROBLEM;

        return null;
    }

}
