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
import java.util.Date;
import java.util.Locale;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.util.NbBundle;

/**
 * A {@link DataType}representing a timestamp value.
 * 
 * @author Ahimanikya Satapathy
 */
public class TimestampType {
    // Irrespective of the JVM's Locale lets pick a Locale for use on any JVM
    public static final String DEFAULT_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"; // NOI18N
    private static final DateFormat[] TIMESTAMP_PARSING_FORMATS = new DateFormat[]{
        new SimpleDateFormat (DEFAULT_FORMAT_PATTERN),
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()),
        new SimpleDateFormat("yyyy-MM-dd HH:mm"), // NOI18N
        new SimpleDateFormat("yyyy-MM-dd"), // NOI18N
        DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()),
        new SimpleDateFormat("MM-dd-yyyy"), // NOI18N
        new SimpleDateFormat("HH:mm:ss"), // NOI18N
        new SimpleDateFormat("HH:mm"), // NOI18N
        DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
    };

    {
        for (int i = 0; i < TIMESTAMP_PARSING_FORMATS.length; i++) {
            TIMESTAMP_PARSING_FORMATS[i].setLenient(false);
        }
    }

    public static Timestamp convert(Object value) throws DBException {
        if (null == value) {
            return null;
        } else if (value instanceof Timestamp) {
            return (Timestamp) value;
        } else if (value instanceof java.sql.Date) {
            return new Timestamp(((java.sql.Date)value).getTime());
        }  else if (value instanceof java.util.Date) {
            return new Timestamp(((java.util.Date)value).getTime());
        } else if (value instanceof String) {
            Date dVal = doParse ((String) value);
            if (dVal == null) {
                throw new DBException(NbBundle.getMessage(TimestampType.class, "LBL_invalid_timestamp"));
            }
            return new Timestamp(dVal.getTime());
        } else {
            throw new DBException(NbBundle.getMessage(TimestampType.class, "LBL_invalid_timestamp"));
        }
    }

    public static synchronized Date doParse (String sVal) {
        Date dVal = null;
        for (DateFormat format : TIMESTAMP_PARSING_FORMATS) {
            try {
                dVal = format.parse (sVal);
                break;
            } catch (ParseException ex) {
                Logger.getLogger (TimestampType.class.getName ()).log (Level.FINEST, ex.getLocalizedMessage () , ex);
            }
        }
        return dVal;
    }
}
