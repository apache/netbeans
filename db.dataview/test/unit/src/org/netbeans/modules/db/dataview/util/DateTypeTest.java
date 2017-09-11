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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.dataview.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author navaneeth
 */
public class DateTypeTest extends NbTestCase {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", TimestampType.LOCALE);
    private long now = System.currentTimeMillis();
    private long expectedMillis = 0l;
    private DateType type = null;
    
    public DateTypeTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DateTypeTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        type = new DateType();

        Calendar dt = Calendar.getInstance();
        dt.set(Calendar.MILLISECOND, 0);
        dt.set(Calendar.SECOND, 0);
        dt.set(Calendar.MINUTE, 0);
        dt.set(Calendar.HOUR_OF_DAY, 0);
        expectedMillis = dt.getTimeInMillis();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        type = null;
    }

    /**
     * Test of convert method, of class DateType.
     */
    public void testConvertObject() {
        try {
            DateType.convert(new Object());
            fail("Expected Exception");
        } catch (Exception e) {
            // expected
        }
        try {
            DateType.convert("this is not a date");
            fail("Expected Exception");
        } catch (Exception e) {
            // expected
        }
    }

    public void testToDate() throws Exception {
        Object result = DateType.convert(new Date(now));
        assertNotNull("Should get object back", result);
        assertTrue("Should get Date back", result instanceof java.sql.Date);
    }
    
    public void testToTimestamp() throws Exception {
        Object result = DateType.convert(new Timestamp(now));
        assertTrue("Should get Timestamp back", result instanceof java.sql.Date);
        assertEquals(expectedMillis, ((Date) result).getTime());
    }
    
    public void testConvertStringTypes() throws Exception {
        // August 1, 2002 12:00 AM GMT
        long GMT_2002_0801 = 32 * 365 * 24 * 60 * 60 * 1000L + //year
                8 * 24 * 60 * 60 * 1000L + // leap years '72,'76,'80,'84,'88,'92,'96,2000
                (31 + 28 + 31 + 30 + 31 + 30 + 31) * 24 * 60 * 60 * 1000L + // August
                0; // time

        Date expectedDate = new Date(GMT_2002_0801 - TimeZone.getDefault().getOffset(GMT_2002_0801));
        assertEquals("Should accept", expectedDate, DateType.convert("2002-08-01"));

        // August 1, 2002 12:00 PM CDT
        expectedDate = new Date(expectedDate.getTime() + 12 * 60 * 60 * 1000L // time (daylight
                // savings)
                );
        DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG,
                TimestampType.LOCALE);
        fmt.setTimeZone(TimeZone.getDefault());
        assertEquals("Make sure 'expected' is as expected",
                "01 August 2002 12:00:00 "
                + TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT, TimestampType.LOCALE),
                fmt.format(expectedDate));

        String toConvert = DATE_FORMAT.format(expectedDate);
        assertEquals("Check format", "2002-08-01", toConvert);

        String tExpected = "2002-08-01";
        String converted = DateType.convert(tExpected).toString();
        assertEquals(tExpected, converted);

        String expectedPlusTime = tExpected + " 08:45:00";
        converted = DateType.convert(expectedPlusTime).toString();
        assertEquals(tExpected, converted);

        expectedPlusTime = tExpected + " 23:59:59";
        converted = DateType.convert(expectedPlusTime).toString();
        assertEquals(tExpected, converted);
    }

}
