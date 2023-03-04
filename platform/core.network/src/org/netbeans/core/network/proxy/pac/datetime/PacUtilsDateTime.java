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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.netbeans.core.network.proxy.pac.PacHelperMethodsNetscape;

/**
 * Methods and constants useful in PAC script evaluation, specifically
 * date/time related.
 * 
 * @author lbruun
 */
public class PacUtilsDateTime {

    /**
     * List of valid weekday names as used in the Netscape specification.
     * <p>
     * Content: {@code  SUN  MON  TUE  WED  THU  FRI  SAT}
     *
     */
    public static final List<String> WEEKDAY_NAMES = Collections.unmodifiableList(
            Arrays.asList("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"));

    /**
     * List of valid month names as used in the Netscape specification.
     * <p>
     * Content: {@code JAN  FEB  MAR  APR  MAY  JUN  JUL  AUG  SEP  OCT  NOV  DEC}
     *
     */
    public static final List<String> MONTH_NAMES = Collections.unmodifiableList(
            Arrays.asList("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"));

    private static final TimeZone UTC_TIME = TimeZone.getTimeZone("UTC");

    
    /**
     * Evaluates if now is within a weekday range. Method arguments are as described
     * for {@link PacHelperMethodsNetscape#weekdayRange(Object...) }
     * 
     * @param now
     * @param args
     * @return true if within range
     * @throws PacUtilsDateTime.PacDateTimeInputException if arguments were invalid
     */
    public static boolean isInWeekdayRange(Date now, Object... args) throws PacDateTimeInputException {
        int params = getNoOfParams(args);
        boolean gmt = usesGMT(args);
        Calendar cal = getCalendar(now, gmt);
        if (gmt) {
            params--;
        }
        if (!(params >=1 && params <=2)) {
            throw new PacDateTimeInputException("invalid number of arguments");
        }
        final int wdNumMin;
        final int wdNumMax;
        if (params == 1) {
            wdNumMin = getWeekday(args[0].toString());
            wdNumMax = wdNumMin;
        } else {
            wdNumMin = getWeekday(args[0].toString());
            wdNumMax = getWeekday(args[1].toString());
        }
        
        int wdNum = cal.get(Calendar.DAY_OF_WEEK);
                
        if (wdNumMin <= wdNumMax) {
            return ( wdNum >= wdNumMin && wdNum <= wdNumMax);
        } else {
            return ( wdNum >= wdNumMin || wdNum <= wdNumMax);
        }        
    }
    
    
    /**
     * Evaluates if now is within a time range. Method arguments are as described
     * for {@link PacHelperMethodsNetscape#timeRange(Object...) }
     * 
     * @param now
     * @param args
     * @return true if within range
     * @throws PacUtilsDateTime.PacDateTimeInputException if arguments were invalid
     */
    public static boolean isInTimeRange(Date now, Object... args) throws PacDateTimeInputException {
        int params = getNoOfParams(args);
        boolean gmt = usesGMT(args);
        Calendar cal = getCalendar(now, gmt);
        if (gmt) {
            params--;
        }
        if (!(params >= 1 && params <= 6) || params == 5 || params == 3) {
            throw new PacDateTimeInputException("invalid number of arguments");
        }

        TimeRange.TimeRangeBuilder trBld = TimeRange.getBuilder();

        if (params == 1) {
            trBld   .setHourMinMax(getHour(args[0]), getHour(args[0]));
        }
        if (params == 2) {
            trBld   .setHourMinMax(getHour(args[0]),getHour(args[1]));
            if (getHour(args[0]) != getHour(args[1])) {
                trBld.setMinuteMinMax(0, 0);
            }
        }
        if (params == 4) {
            trBld   .setHourMinMax(getHour(args[0]), getHour(args[2]))
                    .setMinuteMinMax(getMinute(args[1]),getMinute(args[3]))
                    .setSecondMinMax(0, 0);
        }
        if (params == 6) {
            trBld   .setHourMinMax(getHour(args[0]),getHour(args[3]))
                    .setMinuteMinMax(getMinute(args[1]),getMinute(args[4]))
                    .setSecondMinMax(getSecond(args[2]),getSecond(args[5]));
        }
        TimeRange timeRange = trBld.createTimeRange();
        
        return timeRange.isInRange(cal);
    }


    /**
     * Evaluates if now is within a date range. Method arguments are as described
     * for {@link PacHelperMethodsNetscape#dateRange(Object...)  }
     * 
     * @param now 
     * @param args arguments
     * @return true if within range
     * @throws PacUtilsDateTime.PacDateTimeInputException if arguments were invalid
     */
    public static boolean isInDateRange(Date now, Object... args) throws PacDateTimeInputException {
        int params = getNoOfParams(args);
        boolean gmt = usesGMT(args);
        Calendar cal = getCalendar(now, gmt);
        if (gmt) {
            params--;
        }
        if (!(params >= 1 && params <= 6) || params == 5 || params == 3) {
            throw new PacDateTimeInputException("invalid number of arguments");
        }
    
        DateRange.DateRangeBuilder drBld = DateRange.getBuilder();
        if (params == 1) {
            if (isYear(args[0])) {
                int y = getYear(args[0]);
                drBld.setYear(y, y);
            } else if (isMonth(args[0])) {
                int m = getMonth(args[0].toString());
                drBld.setMonth(m, m);
            } else if (isDate(args[0])) {
                int d = getDate(args[0]);
                drBld.setDate(d, d);
            } else {
                throw new PacDateTimeInputException("invalid argument : " + args[0].toString());
            }
        }
        if (params == 2) {
            if (isYear(args[0])) {
                drBld.setYear(getYear(args[0]), getYear(args[1]));
            } else if (isMonth(args[0])) {
                drBld.setMonth(getMonth(args[0].toString()), getMonth(args[1].toString()));
            } else if (isDate(args[0])) {
                drBld.setDate(getDate(args[0]), getDate(args[1]));
            } else {
                throw new PacDateTimeInputException("invalid argument : " + args[0].toString());
            }
        }
        if (params == 4) {
            if (isMonth(args[0])) {
                drBld   .setYear(getYear(args[1]), getYear(args[3]))
                        .setMonth(getMonth(args[0].toString()), getMonth(args[2].toString()));
            } else if (isDate(args[0])) {
                drBld   .setMonth(getMonth(args[1].toString()), getMonth(args[3].toString()))
                        .setDate(getDate(args[0]), getDate(args[2]));
            } else {
               throw new PacDateTimeInputException("invalid argument : " + args[0].toString()); 
            }
        }
        if (params == 6) {
            drBld   .setYear(getYear(args[2]), getYear(args[5]))
                    .setMonth(getMonth(args[1].toString()), getMonth(args[4].toString()))
                    .setDate(getDate(args[0]), getDate(args[3]));
        }
        
        DateRange dateRange = drBld.createDateRange();
        
        return dateRange.isInRange(cal);
    }



    private static boolean isMonth(Object obj) {
        return (obj instanceof CharSequence);
    }
    
    private static boolean isYear(Object obj) {
        try {
            int val = getInteger(obj);
            return (val >= 1000);
        } catch (PacDateTimeInputException ex) {
            return false;
        }
    }
    
    private static boolean isDate(Object obj) {
        try {
            int val = getInteger(obj);
            return (val >= 1 && val <= 31);
        } catch (PacDateTimeInputException ex) {
            return false;
        }
    }

    private static int getDate(Object obj) throws PacDateTimeInputException {
        if  (!isDate(obj)) {
            throw new PacDateTimeInputException("value " + obj.toString() + " is not a valid day of month");
        }
        return getInteger(obj);
    }
    
    private static int getYear(Object obj) throws PacDateTimeInputException {
        if  (!isYear(obj)) {
            throw new PacDateTimeInputException("value " + obj.toString() + " is not a valid year");
        }
        return getInteger(obj);
    }
    
    private static int getWeekday(String wd) throws PacDateTimeInputException { 
        int indexOf = WEEKDAY_NAMES.indexOf(wd);
        if (indexOf == -1) {
            throw new PacDateTimeInputException("Unknown weekday name : \"" + wd + "\"");
        }
        return indexOf+1;  // In Calendar, the first weekday (Sunday) is 1
    }

    private static int getMonth(String month) throws PacDateTimeInputException { 
        int indexOf = MONTH_NAMES.indexOf(month);
        if (indexOf == -1) {
            throw new PacDateTimeInputException("Unknown month name : \"" + month + "\"");
        }
        return indexOf;  // In Calendar, January is 0
    }
    
    
    private static int getInteger(Object obj) throws PacDateTimeInputException {
        if (obj instanceof Integer || obj instanceof Long) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException ex) {
            }
        }
        throw new PacDateTimeInputException("value is " + obj + " is not an integer");
    }
    
    private static int getHour(Object obj) throws PacDateTimeInputException {
        int hour = getInteger(obj);
        if (!(hour >= 0 && hour <= 23)) {
            throw new PacDateTimeInputException("value is " + hour + " is not a valid hour of day (0-23)");
        }
        return hour;
    }
    
    private static int getMinute(Object obj) throws PacDateTimeInputException {
        int min = getInteger(obj);
        if (!(min >= 0 && min <= 59)) {
            throw new PacDateTimeInputException("value is " + min + " is not a valid minute (0-59)");
        }
        return min;
    }
    
    private static int getSecond(Object obj) throws PacDateTimeInputException {
        int sec = getInteger(obj);
        if (!(sec >= 0 && sec <= 59)) {
            throw new PacDateTimeInputException("value is " + sec + " is not a valid second (0-59)");
        }
        return sec;
    }
    
    private static Calendar getCalendar(Date now, boolean useGMT) {        
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        if (useGMT) {
            cal.setTimeZone(UTC_TIME);
        }
        return cal;
    }
    
    
    /**
     * Gets the number of true arguments passed to a JavaScript
     * function. This is done by counting the number of arguments of type 
     * {@code Number} or {@code CharSequence}. 
     * 
     * <p>
     * This is a convenience method useful when implementing
     * {@link PacHelperMethodsNetscape#dateRange(java.lang.Object...) dateRange()}
     * ,
     * {@link PacHelperMethodsNetscape#timeRange(java.lang.Object...) timeRange()}
     * or
     * {@link PacHelperMethodsNetscape#weekdayRange(java.lang.Object...) weekdayRange()}
     * 
     * <p>
     * Note: In some engines, JavaScript function arguments that are not used in the
     * call will have a type of {@code Undefined}.
     *
     * @param objs
     * @return 
     */
    public static int getNoOfParams(Object... objs) {
        int params=0;
        for(Object obj : objs) {
            if (obj == null) {  // don't really know if parameters 
                                // will ever be null when the Java method is 
                                // is invoked from JavaScript. I believe not, i.e
                                // they will be of class Undefined rather than null.
                continue;
            }
            // Only parameters of type CharSequence (String) and 
            // Number (Integer, Long, etc) are relevant.
            if ((obj instanceof Number) || (obj instanceof CharSequence)) {
                params++;
            }
        }
        return params;
    }
    
    /**
     * Evaluates if the last parameter of a parameter array is "GMT".
     * @param args parameters
     * @return 
     */
    public static boolean usesGMT(Object... args) {
        int params = getNoOfParams(args);
        if (args[params - 1] instanceof CharSequence) {
            String p = args[params - 1].toString();
            if (p.equals("GMT")) {
                return true;
            }
        }
        return false;
    }


    /**
     * Validation errors on input to {@code weekdayRange()}, 
     * {@code timeRange()} and {@code dateRange()}.
     */
    public static class PacDateTimeInputException extends Exception  {
        public PacDateTimeInputException(String msg) {
            super(msg);
        }
    }
    
}
