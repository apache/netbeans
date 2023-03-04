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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
public class DateType {

    public static final String DEFAULT_FOMAT_PATTERN = "yyyy-MM-dd"; // NOI18N
    private static final DateFormat[] DATE_PARSING_FORMATS = new DateFormat[]{
        new SimpleDateFormat (DEFAULT_FOMAT_PATTERN),
        DateFormat.getDateInstance(DateFormat.MEDIUM),
        DateFormat.getDateInstance(DateFormat.SHORT),
        new SimpleDateFormat("MM-dd-yyyy"), // NOI18N
    };

    {
        for (int i = 0; i < DATE_PARSING_FORMATS.length; i++) {
            DATE_PARSING_FORMATS[i].setLenient(false);
        }
    }
    

    public static java.sql.Date convert(Object value) throws DBException {
        Calendar cal = Calendar.getInstance();

        if (null == value) {
            return null;
        } else if (value instanceof Timestamp) {
            cal.setTimeInMillis(((Timestamp) value).getTime());
        } else if (value instanceof java.util.Date) {
            cal.setTimeInMillis(((java.util.Date) value).getTime());
        }else if (value instanceof String) {
            java.util.Date dVal = doParse ((String) value);
            if (dVal == null) {
                throw new DBException(NbBundle.getMessage(DateType.class,
                    "MSG_failure_convert_date", value.getClass().getName(), value.toString())); // NOI18N
            }
            cal.setTimeInMillis(dVal.getTime());
        } else {
            throw new DBException(NbBundle.getMessage(DateType.class,
                    "MSG_failure_convert_date", value.getClass().getName(), value.toString())); // NOI18N
        }

        // Normalize to 0 hour in default time zone.
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new java.sql.Date(cal.getTimeInMillis());
    }

    private static synchronized java.util.Date doParse (String sVal) {
        java.util.Date dVal = null;
        for (DateFormat format : DATE_PARSING_FORMATS) {
            try {
                dVal = format.parse (sVal);
                break;
            } catch (ParseException ex) {
                Logger.getLogger (DateType.class.getName ()).log (Level.FINEST, ex.getLocalizedMessage () , ex);
            }
        }
        return dVal;
    }
}
