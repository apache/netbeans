/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.core.network.proxy.pac.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 */
public class PacHelperMethodsImplTest {
    
    private final NbPacHelperMethods helpers = new NbPacHelperMethods();
    private final static List<String> WEEKDAY_NAMES = Collections.unmodifiableList(
            Arrays.asList("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"));

    private final static List<String> MONTH_NAMES = Collections.unmodifiableList(
            Arrays.asList("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"));
    
    public PacHelperMethodsImplTest() {
        
    }
    
    @BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isPlainHostName method, of class helpers.
     */
    @org.junit.Test
    public void testIsPlainHostName() {
        assertTrue(helpers.isPlainHostName("somehost"));
        assertFalse(helpers.isPlainHostName("somehost.dom1.com"));
    }

    /**
     * Test of dnsDomainIs method, of class helpers.
     */
    @org.junit.Test
    public void testDnsDomainIs() {
        assertTrue(helpers.dnsDomainIs("www.netscape.com", ".netscape.com"));
        assertTrue(helpers.dnsDomainIs("www.netscape.com", "netscape.com"));
        assertFalse(helpers.dnsDomainIs("www.netscape.com", ".com"));
        assertFalse(helpers.dnsDomainIs("www.netscape.com", "somethingelse.com"));
        assertFalse(helpers.dnsDomainIs("www.netscape.com", ""));
        assertFalse(helpers.dnsDomainIs("www.netscape.com", null));
    }

    /**
     * Test of localHostOrDomainIs method, of class helpers.
     */
    @org.junit.Test
    public void testLocalHostOrDomainIs() {
        assertTrue(helpers.localHostOrDomainIs("www.netscape.com", "www.netscape.com"));
        assertTrue(helpers.localHostOrDomainIs("www", "www.netscape.com"));
        assertFalse(helpers.localHostOrDomainIs("www.netscape.com", "www.netscape.com2"));
        assertFalse(helpers.localHostOrDomainIs("www1.netscape.com", "www2.netscape.com"));
        assertFalse(helpers.localHostOrDomainIs("www1", "www2.netscape.com"));
    }

    /**
     * Test of isResolvable method, of class helpers.
     */
    @org.junit.Test
    public void testIsResolvable() {
        assertTrue(helpers.isResolvable("localhost"));
        assertTrue(helpers.isResolvable("www.google.com"));   // will only work if we have access to Internet
    }

    /**
     * Test of dnsResolve method, of class helpers.
     */
    @org.junit.Test
    public void testDnsResolve() {
        assertEquals("127.0.0.1", helpers.dnsResolve("localhost"));
        assertEquals("8.8.8.8", helpers.dnsResolve("google-public-dns-a.google.com")); // will only work if we have access to Internet
    }

    /**
     * Test of myIpAddress method, of class helpers.
     */
    @org.junit.Test
    public void testMyIpAddress() {
        String x = helpers.myIpAddress();
    }

    /**
     * Test of isInNet method, of class helpers.
     */
    @org.junit.Test
    public void testIsInNet() {
        assertTrue(helpers.isInNet("localhost", "127.0.0.1", "255.255.255.255"));
        assertTrue(helpers.isInNet("google-public-dns-a.google.com", "8.8.8.8", "255.255.255.255"));
        assertFalse(helpers.isInNet("192.168.1.3", "192.168.1.1", "255.255.255.255"));
        assertTrue(helpers.isInNet("192.168.1.3", "192.168.1.1", "255.255.255.0"));
        assertTrue(helpers.isInNet("192.168.1.1", "192.168.3.1", "255.255.0.255"));
        assertTrue(helpers.isInNet("10.10.10.10", "12.12.12.12", "0.0.0.0"));
        assertFalse(helpers.isInNet("10.10.10.10", "12.12.12.12", "0.0.255.0"));
    }

    /**
     * Test of dnsDomainLevels method, of class helpers.
     */
    @org.junit.Test
    public void testDnsDomainLevels() {
        assertEquals(2, helpers.dnsDomainLevels("www.netscape.com"));
        assertEquals(0, helpers.dnsDomainLevels("www"));
        assertEquals(1, helpers.dnsDomainLevels("www."));  // somewhat undefined from Netscape spec what this should return, probably a corner case
    }

    /**
     * Test of shExpMatch method, of class helpers.
     */
    @org.junit.Test
    public void testShExpMatch() {
        assertTrue(helpers.shExpMatch("www.netscape.com", "*netscape*"));
        assertTrue(helpers.shExpMatch("www.netscape.com", "*net*"));
        assertFalse(helpers.shExpMatch("www.netscape.com", "*google*"));
        assertTrue(helpers.shExpMatch("www.netscape.com", "www*"));
    }

    /**
     * Test of weekdayRange method, of class helpers.
     */
    @org.junit.Test
    public void testWeekdayRange() {
        assertTrue(helpers.weekdayRange("MON", "SUN", "GMT"));
        assertTrue(helpers.weekdayRange("MON", "SUN", null));

        Calendar now = Calendar.getInstance();
        int weekdayNo = now.get(Calendar.DAY_OF_WEEK);  // 1-7  (1=Sunday, 7=Monday)
        int weekdayNoBefore = weekdayNo-1;
        if (weekdayNoBefore == 0) {
            weekdayNoBefore = 7;
        }
        int weekdayNoAfter = weekdayNo+1;
        if (weekdayNoAfter == 8) {
            weekdayNoAfter = 1;
        }
        String weekdayNow = WEEKDAY_NAMES.get(weekdayNo-1);
        String weekdayBefore = WEEKDAY_NAMES.get(weekdayNoBefore-1);
        String weekdayAfter = WEEKDAY_NAMES.get(weekdayNoAfter-1);
        assertTrue(helpers.weekdayRange(weekdayNow, null , null));        
        assertTrue(helpers.weekdayRange(weekdayBefore, weekdayAfter , null));        
    }

    /**
     * Test of dateRange method, of class helpers.
     */
    @org.junit.Test
    public void testDateRange() {
        System.out.println("dateRange");
        
        Calendar now = Calendar.getInstance();
        
        int nowDay = now.get(Calendar.DAY_OF_MONTH);
        String nowMonth = MONTH_NAMES.get(now.get(Calendar.MONTH));
        int nowYear = now.get(Calendar.YEAR);
        
        assertTrue(helpers.dateRange(nowDay));
        assertFalse(helpers.dateRange(nowDay+1));
        assertTrue(helpers.dateRange(nowMonth));
        assertTrue(helpers.dateRange(nowYear));
        assertFalse(helpers.dateRange(nowYear+1));
        assertTrue(helpers.dateRange(nowDay, nowMonth, nowDay, nowMonth));
        assertTrue(helpers.dateRange(nowMonth, nowYear, nowMonth, nowYear));
        assertFalse(helpers.dateRange(nowMonth, nowYear+1, nowMonth, nowYear+1));
    }

    /**
     * Test of timeRange method, of class helpers.
     */
    @org.junit.Test
    public void testTimeRange() {
        Calendar now = Calendar.getInstance();
        
        final int nowHour = now.get(Calendar.HOUR_OF_DAY);
        final int nowMinute = now.get(Calendar.MINUTE);
        final int nowSecond = now.get(Calendar.SECOND);  // not used
        int nowMinuteNext = (nowMinute == 59) ? 0 : nowMinute+1;
        int nowHourNext = (nowMinuteNext == 0) ? ((nowHour == 23) ? 0 : nowHour+1) : nowHour;
        assertTrue(helpers.timeRange(nowHour));
        assertTrue(helpers.timeRange(nowHour, nowHour));
        assertTrue(helpers.timeRange(nowHour, nowMinute, nowHourNext, nowMinuteNext));
        assertFalse(helpers.timeRange(23, 31, 12, 23, 31,  5));     // second timestamp before first - should give false result
        assertFalse(helpers.timeRange(23, 31, 12, 23, 31,  5, "GMT"));     // second timestamp before first - should give false result
        assertFalse(helpers.timeRange(23, 31, 12, 23, 31, 12, "GMT"));     // test will fail if we happen to run test at exactly 23:31:12 , unlikely!
        
        // We don't dare test something with nowSecond as there's a real
        // risk that two calendars (the one used in the actual code vs the one used in this
        // test class) are not on the same second .. and the test will then give incorrect result.
    }


    
    /**
     * Test of isResolvableEx method, of class helpers.
     */
    @org.junit.Test
    public void testIsResolvableEx() {        
        assertTrue(helpers.isResolvableEx("localhost"));
        assertTrue(helpers.isResolvableEx("google.com"));
        assertFalse(helpers.isResolvableEx("something-really-not42871.com-does-not-exist"));
    }

    /**
     * Test of dnsResolveEx method, of class helpers.
     */
    @org.junit.Test
    public void testDnsResolveEx() {       
        
        assertEquals("0:0:0:0:0:0:0:1", helpers.dnsResolveEx("localhost"));
        // Difficult to test so that it works for everyone
    }

    /**
     * Test of sortIpAddressList method, of class helpers.
     */
    @org.junit.Test
    public void testSortIpAddressList() {       
        
        String input = 
                        "10.2.3.9"
                + ";" + "2001:4898:28:3:201:2ff:feea:fc14"
                + ";" + "::1"
                + ";" + "127.0.0.1"
                + ";" + "::9";
        
        String expected = 
                        "::1"
                + ";" + "::9"
                + ";" + "2001:4898:28:3:201:2ff:feea:fc14"
                + ";" + "10.2.3.9"
                + ";" + "127.0.0.1";
        assertEquals(expected, helpers.sortIpAddressList(input));
    }
}
