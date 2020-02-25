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
    public String fYear(Integer year) {
        if(year == null){
            return null;
        }
        return year + "/" + ((year % 100) + 1);
    }

    public String valueTypeText(String type) {
        if(type.equalsIgnoreCase(SkillsGrantType.AEB_PROCURED.name())){
            return "Procured";
        } else {
            return "Non Procured";
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
        String monthString;
        switch (month) {
            case 1:
            case 13: monthString = "September";     break;
            case 2:
            case 14: monthString = "October";       break;
            case 3:  monthString = "November";      break;
            case 4:  monthString = "December";      break;
            case 5:  monthString = "January";       break;
            case 6:  monthString = "February";      break;
            case 7:  monthString = "March";         break;
            case 8:  monthString = "April";         break;
            case 9:  monthString = "May";           break;
            case 10: monthString = "June";          break;
            case 11: monthString = "July";          break;
            case 12: monthString = "August";        break;
            default: monthString = "Invalid month"; break;
        }
        return monthString.substring(0,3);
    }
}
