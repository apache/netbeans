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

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author navaneeth
 */
public class DateTypeTest extends NbTestCase {
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
