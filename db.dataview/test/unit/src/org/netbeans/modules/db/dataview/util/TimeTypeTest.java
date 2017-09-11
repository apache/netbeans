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
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author navaneeth
 */
public class TimeTypeTest extends NbTestCase {

    private long now = System.currentTimeMillis();

    public TimeTypeTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(TimeTypeTest.class);
        return suite;
    }

    //---------------- Test Case ---------------
    public void testConvertObject() {
        try {
            TimeType.convert(new Object());
            fail("Expected Exception");
        } catch (Exception expected) {
        // expected
        }
        try {
            TimeType.convert("this is not a date");
            fail("Expected Exception");
        } catch (Exception expected) {
        // expected
        }
    }

    public void testConvertTimeTypes() throws Exception {
        long normalizedNow = TimeType.normalizeTime(now);
        Time expectedTime = new Time(normalizedNow);
        //assertEquals(expectedTime, (Time)type.convert(new Time(now)));
        assertEquals(expectedTime, TimeType.convert(expectedTime));

        try {
            TimeType.convert(new Object());
            fail("Expected Exception - cannot parse/convert object to timestamp");
        } catch (Exception expected) {
        // Ignore
        }
    }

    public void testToDate() throws Exception {
        try {
            TimeType.convert(new Date(now));
            fail("Expected Exception - not implemented.");
        } catch (Exception expected) {
        // expected
        }
    }

    public void testToTime() throws Exception {
        Object result = TimeType.convert(new Time(now));
        assertNotNull("Should get object back", result);
        assertTrue("Should get Time back", result instanceof java.sql.Time);
    }

    public void testConvertStringTypes() throws Exception {
        Time expectedTime = new Time(0);
        //assertEquals("Should accept - ", expectedTime, type.convert("00:00:00"));

        // 12:00 PM
        expectedTime = TimeType.convert (new Time (new Long (expectedTime.getTime () + 12 * 60 * 60 * 1000L)));
        DateFormat fmt = DateFormat.getTimeInstance(DateFormat.LONG,
                TimestampType.LOCALE);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals("Make sure 'expected' is as expected", "12:00:00 GMT", fmt.format(expectedTime));

        fmt = new SimpleDateFormat("HH:mm:ss");
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        expectedTime = new Time(((12L * 60L * 60L) + (5L * 60L)) * 1000L);
        assertEquals(fmt.format(expectedTime), TimeType.convert("12:05:00").toString());
    }

    public void testNormalizeTime() {
        // Check that already-normalized values don't get modified. 
        assertEquals(0, TimeType.normalizeTime(0L));

        long value = 13L * 60L * 60L * 1000L;
        assertEquals(value, TimeType.normalizeTime(value));

        Time normalOne = new Time(value);
        assertEquals(normalOne.getTime(), TimeType.normalizeTime(value));

        long largeValue = value + (34L * 365L * 24L * 60L * 60L * 1000L) // 2004
                + (8L * 24L * 60L * 60L * 1000L) // leap years (not including 2004)
                + ((31L + 29L + 15L) * 24L * 60L * 60L * 1000L); // March 15
        assertEquals(value, TimeType.normalizeTime(largeValue));

        // Post DST
        largeValue = value + (34L * 365L * 24L * 60L * 60L * 1000L) // 2004
                + (8L * 24L * 60L * 60L * 1000L) // leap years (not including 2004)
                + ((31L + 29L + 31L + 30L + 15) * 24L * 60L * 60L * 1000L); // May 15
        int offset = TimeType.TIME_ZONE.getDSTSavings();
        assertEquals(value + offset, TimeType.normalizeTime(largeValue));
    }
}
