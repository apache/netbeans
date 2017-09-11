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

import java.sql.Timestamp;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author navaneeth
 */
public class TimestampTypeTest extends NbTestCase {
    private long now = System.currentTimeMillis();
    private TimestampType type = null;
    public TimestampTypeTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(TimestampTypeTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        type = new TimestampType();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        type = null;
    }

    //---------------------- Test Case -------------------------
    public void testToTime() throws Exception {
        assertNotNull(TimestampType.convert(new Timestamp(0)));
    }
    
    public void testConvertObject() {
        try {
            TimestampType.convert(new Object());
            fail("Expected Exception");
        } catch (Exception e) {
        // expected
        }
        try {
            TimestampType.convert("this is not a date");
            fail("Expected Exception");
        } catch (Exception e) {
        // expected
        }
    }
    
    public void testToTimestamp() throws Exception {
        Object result = TimestampType.convert(new Timestamp(now));
        assertNotNull("Should get object back", result);
        assertTrue("Should get Timestamp back", result instanceof java.sql.Timestamp);
    }
    
//    public void testConvertStringTypes() throws Exception {
//        // August 1, 2002 12:00 AM GMT
//        Timestamp expected = new Timestamp(
//                32 * 365 * 24 * 60 * 60 * 1000L + //year
//                8 * 24 * 60 * 60 * 1000L + // leap years '72,'76,'80,'84,'88,'92,'96,2000
//                (31 + 28 + 31 + 30 + 31 + 30 + 31) * 24 * 60 * 60 * 1000L + // August
//                0 // time
//                );
//        //assertEquals("Should accept", expected, type.convert("2002-08-01"));
//
//        // August 1, 2002 12:00 PM CDT
//        expected = new Timestamp(expected.getTime() +
//                12 * 60 * 60 * 1000L // time (daylight savings)
//                );
//        DateFormat fmt = DateFormat.getDateTimeInstance(
//                DateFormat.LONG, DateFormat.LONG, TimestampType.LOCALE);
//        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
//        assertEquals("Make sure 'expected' is as expected",
//                "01 August 2002 12:00:00 GMT", fmt.format(expected));
//
//        String toConvert = type.convert(expected).toString();
//        assertEquals("Check format", "2002-08-01 12:00:00.000", toConvert);
//
//        // August 1, 2002 12:00:00.001 PM CDT
//        expected = new Timestamp(expected.getTime() +
//                1L // 1 millisecond
//                );
//        toConvert = type.convert(expected).toString();
//        assertEquals("Check format", "2002-08-01 12:00:00.001", toConvert);
//
//        // August 1, 2002 12:00:00.050 PM CDT
//        expected = new Timestamp(expected.getTime() +
//                49L // 49 milliseconds
//                );
//        toConvert = type.convert(expected).toString();
//        assertEquals("Check format", "2002-08-01 12:00:00.050", toConvert);
//        assertEquals("Should be identical", expected, type.convert("2002-08-01 12:00:00.050"));
//
//        // August 1, 2002 12:00:00.050 PM CDT
//        expected = new Timestamp(expected.getTime() +
//                100L // 100 milliseconds
//                );
//        toConvert = type.convert(expected).toString();
//        assertEquals("Check format", "2002-08-01 12:00:00.150", toConvert);
//        assertEquals("Should be identical", expected, type.convert("2002-08-01 12:00:00.150"));
//    }
}
