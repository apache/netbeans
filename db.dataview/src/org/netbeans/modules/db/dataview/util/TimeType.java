/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        DateFormat.getTimeInstance(DateFormat.LONG, TimestampType.LOCALE),
        DateFormat.getTimeInstance(DateFormat.SHORT, TimestampType.LOCALE),
        new SimpleDateFormat("HH:mm:ss", TimestampType.LOCALE), // NOI18N
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

    private synchronized static Date doParse (String sVal) {
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
