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
 * Evaluates a time of day against a time range.
 * 
 * - hour of day is 24 h based.
 * - minute is 0-59.
 * - second is 0-59.
 * 
 * Handles correctly the situation where a time range passes into next period.
 * Examples:
 * 
 *   - Only minute range is defined. minuteMin=55 minuteMax=05.  This means
 *     a 10 minute interval around every hour change from 5 minutes before
 *     the hour, to 5 minutes after the hour.
 *   - Only second range is defined. secondMin=55 secondMax=05.  This means
 *     a 10 second interval around every minute change, from 5 seconds before
 *     the minute change, to 5 seconds after the minute change.
 *   - Interval 23:35:00 to 00:25:00. This means a 55 minute interval around
 *     every day change.
 * 
 * @author lbruun
 */
class TimeRange {

    private static final int UNDEFINED = -1;
    private final int hourMin;
    private final int hourMax;
    private final int minuteMin;
    private final int minuteMax;
    private final int secondMin;
    private final int secondMax;

    public TimeRange(int hourMin, int hourMax, int minuteMin, int minuteMax, int secondMin, int secondMax) {
        this.hourMin = hourMin;
        this.hourMax = ((hourMax == UNDEFINED) && (hourMin != UNDEFINED)) ? hourMin : hourMax;
        this.minuteMin = minuteMin;
        this.minuteMax = ((minuteMax == UNDEFINED) && (minuteMin != UNDEFINED)) ? minuteMin : minuteMax;
        this.secondMin = secondMin;
        this.secondMax = ((secondMax == UNDEFINED) && (secondMin != UNDEFINED)) ? secondMin : secondMax;
    }

    public boolean isInRange(Calendar cal) {

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        if (hourDefined()) {
            if (hourMin <= hourMax) {
                if (!(hour >= hourMin && hour <= hourMax)) {
                    return false;
                }
            } else {
                if (!(hour >= hourMin || hour <= hourMax)) {
                    return false;
                }
            }
        }
        if (minuteDefined()) {
            if (hourDefined()) {
                if (hour == hourMin) {
                    if (!(minute >= minuteMin)) {
                        return false;
                    }
                }
                if (hour == hourMax) {
                    if (!(minute <= minuteMax)) {
                        return false;
                    }
                }
            } else {
                if (minuteMin <= minuteMax) { // minute range is fully within an hour
                    if (!(minute >= minuteMin && minute <= minuteMax)) {
                        return false;
                    }
                } else {   // minute range is passing into new hour
                    if (!(minute >= minuteMin || minute <= minuteMax)) {
                        return false;
                    }
                }
            }
        } 
        if (secondDefined()) {
            if (minuteDefined()) {
                if (hourDefined()) {
                    if (hour == hourMin && minute == minuteMin) {
                        if (!(second >= secondMin)) {
                            return false;
                        }
                    }
                    if (hour == hourMax && minute == minuteMax) {
                        if (!(second <= secondMax)) {
                            return false;
                        }
                    }
                } else {
                    if (minute == minuteMin) {
                        if (!(second >= secondMin)) {
                            return false;
                        }
                    }
                    if (minute == minuteMax) {
                        if (!(second <= secondMax)) {
                            return false;
                        }
                    }
                }
            } else  {  // only secondMin and secondMax are defined
                if (secondMin <= secondMax) {  // second range is fully within a minute
                    if (!(second >= secondMin && second <= secondMax)) {
                        return false;
                    }
                } else {  // second range is rolling over into next minute
                    if (!(second >= secondMin || second <= secondMax)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private boolean hourDefined() {
        return (!(hourMin == UNDEFINED && hourMax == UNDEFINED));
    }
    private boolean minuteDefined() {
        return (!(minuteMin == UNDEFINED && minuteMax == UNDEFINED));
    }
    private boolean secondDefined() {
        return (!(secondMin == UNDEFINED && secondMax == UNDEFINED));
    }
    
    
    public static TimeRangeBuilder getBuilder() {
        return new TimeRangeBuilder();
    }
    
    public static class TimeRangeBuilder {
        private int hourMin = UNDEFINED;
        private int hourMax = UNDEFINED;
        private int minuteMin = UNDEFINED;
        private int minuteMax = UNDEFINED;
        private int secondMin = UNDEFINED;
        private int secondMax = UNDEFINED;

        private TimeRangeBuilder() {}

        public TimeRangeBuilder setHourMinMax(int hourMin, int hourMax) {
            this.hourMin = hourMin;
            this.hourMax = hourMax;
            return this;
        }

        public TimeRangeBuilder setMinuteMinMax(int minuteMin, int minuteMax) {
            this.minuteMin = minuteMin;
            this.minuteMax = minuteMax;
            return this;
        }

        public TimeRangeBuilder setSecondMinMax(int secondMin, int secondMax) {
            this.secondMin = secondMin;
            this.secondMax = secondMax;
            return this;
        }

        public TimeRange createTimeRange() {
            return new TimeRange(hourMin, hourMax, minuteMin, minuteMax, secondMin, secondMax);
        }
    }
}
