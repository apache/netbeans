/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.db.dataview.util;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.util.NbBundle;

/**
 * Implements a date type which can generate instances of java.sql.Date and other JDBC
 * date-related types.
 * 
 * @author Ahimanikya Satapathy
 */
public class TimeType {

    public static final String DEFAULT_FOMAT_PATTERN = "HH:mm:ss"; // NOI18N
    private static final DateFormat[] TIME_PARSING_FORMATS = new DateFormat[]{
        new SimpleDateFormat (DEFAULT_FOMAT_PATTERN),
        DateFormat.getTimeInstance(),
        DateFormat.getTimeInstance(DateFormat.SHORT),
        new SimpleDateFormat("HH:mm"), // NOI18N
    };

    {
        for (int i = 0; i < TIME_PARSING_FORMATS.length; i++) {
            TIME_PARSING_FORMATS[i].setLenient(false);
        }
    }

    public static final TimeZone TIME_ZONE = TimeZone.getDefault();

    /* Increment to use in computing a successor value. */
    // One day = 1 day x 24 hr/day x 60 min/hr x 60 sec/min x 1000 ms/sec
    private static final long INCREMENT_DAY = 1 * 24 * 60 * 60 * 1000;
    
    public static long normalizeTime(long rawTimeMillis) {
        int dstOffset = (TIME_ZONE.inDaylightTime(new java.util.Date(rawTimeMillis))) ? TIME_ZONE.getDSTSavings() : 0;
        return (rawTimeMillis < INCREMENT_DAY) ? rawTimeMillis : (rawTimeMillis % INCREMENT_DAY) + dstOffset;
    }

    private static Time getNormalizedTime(long time) {
        Time ret = null;
        ret = new Time(normalizeTime(time));
        return ret;
    }

    public static Time convert(Object value) throws DBException {
        if (null == value) {
            return null;
        } else if (value instanceof java.sql.Time) {
            return (Time) value;
        } else if (value instanceof String) {
            Date dVal = doParse ((String) value);
            if (dVal == null) {
                throw new DBException(NbBundle.getMessage(TimeType.class,"LBL_invalid_time"));
            }
            return getNormalizedTime(dVal.getTime());
        } else {
            throw new DBException(NbBundle.getMessage(TimeType.class,"LBL_invalid_time"));
        }
    }

    private static synchronized Date doParse (String sVal) {
        Date dVal = null;
        for (DateFormat format : TIME_PARSING_FORMATS) {
            try {
                dVal = format.parse (sVal);
                break;
            } catch (ParseException ex) {
                Logger.getLogger (TimeType.class.getName ()).log (Level.FINEST, ex.getLocalizedMessage () , ex);
            }
        }
        return dVal;
    }
}
