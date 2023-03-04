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
