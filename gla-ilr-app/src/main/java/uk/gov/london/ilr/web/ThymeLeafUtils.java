/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ilr.web;

import uk.gov.london.common.skills.SkillsGrantType;

public class ThymeLeafUtils {
    public String financialYear(Integer year) {
        if (year == null) {
            return null;
        }
        return year + "/" + ((year % 100) + 1);
    }

    public String valueTypeText(String type) {
        if (type.equalsIgnoreCase(SkillsGrantType.AEB_PROCURED.name())) {
            return "Procured";
        } else {
            return "Grant";
        }
    }

    /**
     * Returns a label for return period with month, academic year and return number, given a calendar year & month.
     * For example 01 2019 -> Sep 19 R01
     * @return
     */
    public String returnPeriodLabel(int period, int year) {
        if (period < 5) {
            return getMonthForInt(period) + " " + String.valueOf(year).substring(2) + " R" + String.format("%02d", period);
        } else {
            return getMonthForInt(period) + " " + String.valueOf(year + 1).substring(2) + " R" + String.format("%02d", period);
        }
    }

    public String getMonthForInt(int month) {
        switch (month) {
            case 1:
            case 13: return "Sep";
            case 2:
            case 14: return "Oct";
            case 3:  return "Nov";
            case 4:  return "Dec";
            case 5:  return "Jan";
            case 6:  return "Feb";
            case 7:  return "Mar";
            case 8:  return "Apr";
            case 9:  return "May";
            case 10: return "Jun";
            case 11: return "Jul";
            case 12: return "Aug";
            default: return "Inv";
        }
    }
}
