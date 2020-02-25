/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.data;

public class ESFMonthlyRecordFile {

    private String name;
    private boolean valid;
    private Integer year;
    private Integer month;

    public String getName() {
        return name;
    }

    public boolean isValid() {
        return valid;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    public static ESFMonthlyRecordFile parse(String name) {
        ESFMonthlyRecordFile esfMonthlyRecordFile = new ESFMonthlyRecordFile();
        esfMonthlyRecordFile.name = name;
        if (name.matches("(.*) Report(.*)+\\d{4} \\d{2}(.csv)")) {
            esfMonthlyRecordFile.year = Integer.parseInt(name.substring(name.length() - 11, name.length() - 7));
            esfMonthlyRecordFile.month = Integer.parseInt(name.substring(name.length() - 6, name.length() - 4));
            esfMonthlyRecordFile.valid = esfMonthlyRecordFile.month >= 1 && esfMonthlyRecordFile.month <=14;
        }
        else if (name.matches("SILR_+\\d{4}_\\d{2}+_LLDDHealthProblem(.csv)")) {
            esfMonthlyRecordFile.year = Integer.parseInt(name.substring(5, 9));
            esfMonthlyRecordFile.month = Integer.parseInt(name.substring(10,12));
            esfMonthlyRecordFile.valid = esfMonthlyRecordFile.month >= 1 && esfMonthlyRecordFile.month <=14;
            esfMonthlyRecordFile.valid = esfMonthlyRecordFile.valid? esfMonthlyRecordFile.year >= 1900 && esfMonthlyRecordFile.year <=3000:esfMonthlyRecordFile.valid;
        }
        return esfMonthlyRecordFile;
    }

}
