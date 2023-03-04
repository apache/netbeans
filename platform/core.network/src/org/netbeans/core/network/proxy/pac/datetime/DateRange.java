/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.core.network.proxy.pac.datetime;

import java.util.Calendar;

/**
 * Evaluates a date against a date range.
 *
 * - Years are 4-digit.
 * - Months are zero-based (January = 0).
 * - day of month is 1-31. The range for this value is deliberately not
 *    validated against date, meaning 31st of February is valid. This
 *    creates a lenient evaluation.
 * 
 * Handles correctly the situation where a date range passes into next period.
 * Examples:
 * 
 *   - Only month range is defined. monthMin=11 monthMax=02.  The period from 
 *     December 1st to end of February.
 *   - Only day of month range is defined. dateMin=27 dateMax=5. The days from the 
 *     27th of each month to the 5th of the next month.
 * 
 * @author lbruun
 */
class DateRange {

    private static final int UNDEFINED = -1;
    private final int yearMin;
    private final int yearMax;
    private final int monthMin;
    private final int monthMax;
    private final int dateMin;
    private final int dateMax;

    public DateRange(int yearMin, int yearMax, int monthMin, int monthMax, int dateMin, int dateMax) {
        this.yearMin = yearMin;
        this.yearMax = ((yearMax == UNDEFINED) && (yearMin != UNDEFINED)) ? yearMin : yearMax;
        this.monthMin = monthMin;
        this.monthMax = ((monthMax == UNDEFINED) && (monthMin != UNDEFINED)) ? monthMin : monthMax;
        this.dateMin = dateMin;
        this.dateMax = ((dateMax == UNDEFINED) && (dateMin != UNDEFINED)) ? dateMin : dateMax;
    }

    public boolean isInRange(Calendar cal) {

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date = cal.get(Calendar.DATE);
        if (yearDefined()) {
            if (!(year >= yearMin && year <= yearMax)) {
                return false;
            }
        }
        if (monthDefined()) {
            if (yearDefined()) {
                if (year == yearMin) {
                    if (!(month >= monthMin)) {
                        return false;
                    }
                }
                if (year == yearMax) {
                    if (!(month <= monthMax)) {
                        return false;
                    }
                }
            } else {
                if (monthMin <= monthMax) { // month range is fully within a year
                    if (!(month >= monthMin && month<= monthMax)) {
                        return false;
                    }
                } else {   // month range is rolling over into next year
                    if (!(month >= monthMin || month<= monthMax)) {
                        return false;
                    }
                }
            }
        } 
        if (dateDefined()) {
            boolean checkDateRange = false;
            if (monthDefined()) {
                if (yearDefined()) {
                    if ((year == yearMin && month == monthMin) || (year == yearMax && month == monthMax)) {
                        checkDateRange = true;
                    }
                } else {
                    if (month == monthMin || month == monthMax) {
                        checkDateRange = true;
                    }
                }
            } else {
                checkDateRange = true;
            }
            if (checkDateRange) {
                if (dateMin <= dateMax) {  // date range is fully within a month
                    if (!(date >= dateMin && date <= dateMax)) {
                        return false;
                    }
                } else {  // date range is rolling over into next month
                    if (!(date >= dateMin || date <= dateMax)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private boolean yearDefined() {
        return (!(yearMin == UNDEFINED && yearMax == UNDEFINED));
    }
    private boolean monthDefined() {
        return (!(monthMin == UNDEFINED && monthMax == UNDEFINED));
    }
    private boolean dateDefined() {
        return (!(dateMin == UNDEFINED && dateMax == UNDEFINED));
    }
    
    
    public static DateRangeBuilder getBuilder() {
        return new DateRangeBuilder();
    }
    
    public static class DateRangeBuilder {
        private int yearMin = UNDEFINED;
        private int yearMax = UNDEFINED;
        private int monthMin = UNDEFINED;
        private int monthMax = UNDEFINED;
        private int dateMin = UNDEFINED;
        private int dateMax = UNDEFINED;

        private DateRangeBuilder() {
        }

        public DateRangeBuilder setYear(int yearMin, int yearMax) {
            this.yearMin = yearMin;
            this.yearMax = yearMax;
            return this;
        }

        public DateRangeBuilder setMonth(int monthMin, int monthMax) {
            this.monthMin = monthMin;
            this.monthMax = monthMax;
            return this;
        }

        /**
         * Sets the day of month restrictions. These values are generally
         * between 1 and 31 but are not enforced by this class.
         */
        public DateRangeBuilder setDate(int dateMin, int dateMax) {
            this.dateMin = dateMin;
            this.dateMax = dateMax;
            return this;
        }

        public DateRange createDateRange() {
            return new DateRange(yearMin, yearMax, monthMin, monthMax, dateMin, dateMax);
        }

    }

}
