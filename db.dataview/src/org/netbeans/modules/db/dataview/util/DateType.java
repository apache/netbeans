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
        DateFormat.getDateInstance(),
        DateFormat.getTimeInstance(DateFormat.SHORT),
        DateFormat.getTimeInstance(DateFormat.SHORT, TimestampType.LOCALE),
        new SimpleDateFormat("yyyy-MM-dd"), // NOI18N
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

    private synchronized static java.util.Date doParse (String sVal) {
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
