/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.ui.charts;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;


/**
 *
 * @author  Jiri Sedlacek
 */
public class DateTimeAxisUtils {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.charts.Bundle"); // NOI18N
    private static final String DAYS_FORMAT = messages.getString("DateTimeAxisUtils_DaysFormat"); // NOI18N
    private static final String HOURS_FORMAT = messages.getString("DateTimeAxisUtils_HoursFormat"); // NOI18N
    private static final String HOURS_EXT_FORMAT = messages.getString("DateTimeAxisUtils_HoursExtFormat"); // NOI18N
    private static final String MINUTES_FORMAT = messages.getString("DateTimeAxisUtils_MinutesFormat"); // NOI18N
    private static final String MINUTES_EXT_FORMAT = messages.getString("DateTimeAxisUtils_MinutesExtFormat"); // NOI18N
    private static final String SECONDS_FORMAT = messages.getString("DateTimeAxisUtils_SecondsFormat"); // NOI18N
    private static final String SECONDS_EXT_FORMAT = messages.getString("DateTimeAxisUtils_SecondsExtFormat"); // NOI18N
    private static final String MILLIS_FORMAT = messages.getString("DateTimeAxisUtils_MillisFormat"); // NOI18N
    private static final String MILLIS_EXT_FORMAT = messages.getString("DateTimeAxisUtils_MillisExtFormat"); // NOI18N
    private static final String MILLIS_FULL_FORMAT = messages.getString("DateTimeAxisUtils_MillisFullFormat"); // NOI18N
    private static final String MILLIS_ONLY_FORMAT = messages.getString("DateTimeAxisUtils_MillisOnlyFormat"); // NOI18N
                                                                                                               // -----
    public static final int MIN_TIMEMARK_STEP = 100; // The minimal distance between two time marks
    public static final Color BASE_TIMELINE_COLOR = new Color(0, 0, 0);
    public static final Color MAIN_TIMELINE_COLOR = new Color(150, 150, 150);
    public static final Color TICK_TIMELINE_COLOR = new Color(230, 230, 230);
    private static final int TIME_FORMAT_UNKNOWN = -1;
    private static final int TIME_FORMAT_MILLIS = 10;
    private static final int TIME_FORMAT_SECONDS = 20;
    private static final int TIME_FORMAT_MINUTES = 30;
    private static final int TIME_FORMAT_HOURS = 40;
    private static final int TIME_FORMAT_DAYS = 50;
    private static final SimpleDateFormat daysDateFormat = new SimpleDateFormat(DAYS_FORMAT);
    private static final SimpleDateFormat hoursDateFormat = new SimpleDateFormat(HOURS_FORMAT);
    private static final SimpleDateFormat hoursDateFormatD = new SimpleDateFormat(HOURS_EXT_FORMAT);
    private static final SimpleDateFormat minutesDateFormat = new SimpleDateFormat(MINUTES_FORMAT);
    private static final SimpleDateFormat minutesDateFormatD = new SimpleDateFormat(MINUTES_EXT_FORMAT);
    private static final SimpleDateFormat secondsDateFormat = new SimpleDateFormat(SECONDS_FORMAT);
    private static final SimpleDateFormat secondsDateFormatD = new SimpleDateFormat(SECONDS_EXT_FORMAT);
    private static final SimpleDateFormat millisDateFormat = new SimpleDateFormat(MILLIS_FORMAT);
    private static final SimpleDateFormat millisDateFormatD = new SimpleDateFormat(MILLIS_EXT_FORMAT);
    private static final SimpleDateFormat millisDateFormatF = new SimpleDateFormat(MILLIS_FULL_FORMAT);
    private static final SimpleDateFormat onlyMillisDateFormat = new SimpleDateFormat(MILLIS_ONLY_FORMAT);
    private static final long[] timeUnitsGrid = new long[] {
                                                    10, 20, 50, 100, 250, 500, // milliseconds
    1000, 2000, 5000, 10000, 15000, 30000, // seconds
    60000, 120000, 300000, 600000, 900000, 1800000, // minutes
    3600000, 7200000, 10800000, 21600000, 43200000, // hours
    86400000, 172800000, 259200000, 432000000
                                                }; // days
    private static final int[] timeUnitsFormat = new int[] {
                                                     TIME_FORMAT_MILLIS, TIME_FORMAT_MILLIS, TIME_FORMAT_MILLIS,
                                                     TIME_FORMAT_MILLIS, TIME_FORMAT_MILLIS, TIME_FORMAT_MILLIS,
                                                     TIME_FORMAT_SECONDS, TIME_FORMAT_SECONDS, TIME_FORMAT_SECONDS,
                                                     TIME_FORMAT_SECONDS, TIME_FORMAT_SECONDS, TIME_FORMAT_SECONDS,
                                                     TIME_FORMAT_MINUTES, TIME_FORMAT_MINUTES, TIME_FORMAT_MINUTES,
                                                     TIME_FORMAT_MINUTES, TIME_FORMAT_MINUTES, TIME_FORMAT_MINUTES,
                                                     TIME_FORMAT_HOURS, TIME_FORMAT_HOURS, TIME_FORMAT_HOURS, TIME_FORMAT_HOURS,
                                                     TIME_FORMAT_HOURS, TIME_FORMAT_DAYS, TIME_FORMAT_DAYS, TIME_FORMAT_DAYS,
                                                     TIME_FORMAT_DAYS
                                                 };
    private static final HashMap<Long, Integer> timeUnitsToIndex = new HashMap<>();

    static {
        for (int i = 0; i < timeUnitsGrid.length; i++) {
            timeUnitsToIndex.put(Long.valueOf(timeUnitsGrid[i]), i);
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static String getDaysValue(long mark, boolean useDayMark) {
        return daysDateFormat.format(new Date(mark));
    }

    public static String getHoursValue(long mark, boolean useDayMark) {
        return (useDayMark ? hoursDateFormatD.format(new Date(mark)) : hoursDateFormat.format(new Date(mark)));
    }

    public static double getMaximumScale(long optimalUnits) {
        return (double) MIN_TIMEMARK_STEP / (double) optimalUnits;
    }

    public static String getMillisValue(long mark, boolean useDayMark) {
        return (useDayMark ? millisDateFormatD.format(new Date(mark)) : millisDateFormat.format(new Date(mark)));
    }

    public static String getMillisValueFull(long mark) {
        return millisDateFormatF.format(new Date(mark));
    }

    public static String getMinutesValue(long mark, boolean useDayMark) {
        return (useDayMark ? minutesDateFormatD.format(new Date(mark)) : minutesDateFormat.format(new Date(mark)));
    }

    public static long getOptimalUnits(double factor) {
        for (int i = 0; i < timeUnitsGrid.length; i++) {
            if ((timeUnitsGrid[i] * factor) >= MIN_TIMEMARK_STEP) {
                return timeUnitsGrid[i];
            }
        }

        return timeUnitsGrid[timeUnitsGrid.length - 1];
    }

    public static String getSecondsValue(long mark, boolean useDayMark) {
        return (useDayMark ? secondsDateFormatD.format(new Date(mark)) : secondsDateFormat.format(new Date(mark)));
    }

    public static String getTimeMarkMillisString(long mark, long optimalUnits) {
        int format = getTimeUnitsFormat(optimalUnits);

        if (format != TIME_FORMAT_MILLIS) {
            return ""; // NOI18N
        }

        return onlyMillisDateFormat.format(new Date(mark));
    }

    public static String getTimeMarkNoMillisString(long mark, long optimalUnits, boolean useDayMark) {
        int format = getTimeUnitsFormat(optimalUnits);

        if (format == TIME_FORMAT_UNKNOWN) {
            return ""; // NOI18N
        }

        if (format == TIME_FORMAT_MILLIS) {
            format = TIME_FORMAT_SECONDS;
        }

        return getTimeMarkStringFromFormat(mark, format, useDayMark);
    }

    public static String getTimeMarkString(long mark, long optimalUnits, boolean useDayMark) {
        int format = getTimeUnitsFormat(optimalUnits);

        if (format == TIME_FORMAT_UNKNOWN) {
            return ""; // NOI18N
        }

        return getTimeMarkStringFromFormat(mark, format, useDayMark);
    }

    private static String getTimeMarkStringFromFormat(long mark, int format, boolean useDayMark) {
        switch (format) {
            case TIME_FORMAT_MILLIS:
                return getMillisValue(mark, useDayMark);
            case TIME_FORMAT_SECONDS:

            //return getSecondsValue(mark, useDayMark);
            case TIME_FORMAT_MINUTES:

            //return getMinutesValue(mark, useDayMark);
            case TIME_FORMAT_HOURS:

                //return getHoursValue(mark, useDayMark);
                return getSecondsValue(mark, useDayMark);
            case TIME_FORMAT_DAYS:
                return getDaysValue(mark, useDayMark);
            default:
                return ""; // NOI18N
        }
    }

    private static int getTimeUnitsFormat(long optimalUnits) {
        int timeUnitsFormatIndex = getUnitsIndex(optimalUnits);

        if (timeUnitsFormatIndex == -1) {
            return TIME_FORMAT_UNKNOWN;
        }

        return timeUnitsFormat[timeUnitsFormatIndex];
    }

    private static int getUnitsIndex(long optimalUnits) {
        Integer oTimeUnitsFormatIndex = timeUnitsToIndex.get(optimalUnits);

        if (oTimeUnitsFormatIndex == null) {
            return -1;
        }

        return oTimeUnitsFormatIndex;
    }
}
