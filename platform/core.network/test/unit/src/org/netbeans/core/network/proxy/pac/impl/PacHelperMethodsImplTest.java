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
package org.netbeans.core.network.proxy.pac.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.netbeans.core.network.utils.FakeDns;
import org.netbeans.core.network.utils.IpAddressUtils;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;

/**
 *
 */
public class PacHelperMethodsImplTest {

    private FakeDns fakeDns;
    private InetAddress ipv4Addr;
    private InetAddress ipv6Addr;
    private final NbPacHelperMethods helpers = new NbPacHelperMethods();
    private static final List<String> WEEKDAY_NAMES = Collections.unmodifiableList(
            Arrays.asList("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"));

    private static final List<String> MONTH_NAMES = Collections.unmodifiableList(
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
    public void setUp() throws UnknownHostException {
        MockServices.setServices(FakeDns.class);
        fakeDns = Lookup.getDefault().lookup(FakeDns.class);
        fakeDns.install(true);
        ipv4Addr = InetAddress.getByName("172.217.17.36");    // just some random address
        ipv6Addr = InetAddress.getByName("2a00:1450:400e:804::2004"); // just some random address
    }

    @After
    public void tearDown() {
        fakeDns.unInstall();
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
        fakeDns.addForwardResolution("isresolvable1host", new InetAddress[]{ipv4Addr});
        fakeDns.addForwardResolution("isresolvable2host", new InetAddress[]{ipv6Addr});

        assertTrue(helpers.isResolvable("isresolvable1host"));
        assertFalse(helpers.isResolvable("isresolvable2host"));   // method should never return IPv6 addr
        assertFalse(helpers.isResolvable("foo-bar-does-not-exist"));
    }

    /**
     * Test of dnsResolve method, of class helpers.
     */
    @org.junit.Test
    public void testDnsResolve() {

        fakeDns.addForwardResolution("dnsresolve1host", new InetAddress[]{ipv4Addr});
        fakeDns.addForwardResolution("dnsresolve2host", new InetAddress[]{ipv6Addr});

        assertEquals(ipv4Addr.getHostAddress(), helpers.dnsResolve("dnsresolve1host"));

        assertNull(helpers.dnsResolve("dnsresolve2host"));  // should not be able to resolve IPv6 address

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
    public void testIsInNet() throws UnknownHostException {
        fakeDns.addForwardResolution("isinnet1host", new InetAddress[]{InetAddress.getByName("127.0.0.1")});
        assertTrue(helpers.isInNet("isinnet1host", "127.0.0.1", "255.255.255.255"));

        fakeDns.addForwardResolution("isinnet2host", new InetAddress[]{InetAddress.getByName("::1"), InetAddress.getByName("127.0.0.1")});
        assertTrue(helpers.isInNet("isinnet2host", "127.0.0.1", "255.255.255.255"));

        fakeDns.addForwardResolution("isinnet3host", new InetAddress[]{InetAddress.getByName("::1"), InetAddress.getByName("127.0.0.99")});
        assertFalse(helpers.isInNet("isinnet3host", "127.0.0.1", "255.255.255.255"));

        fakeDns.addForwardResolution("isinnet4host", new InetAddress[]{InetAddress.getByName("::1")});
        assertFalse(helpers.isInNet("isinnet4host", "127.0.0.1", "255.255.255.255"));

        fakeDns.addForwardResolution("google-public-dns-a.google.com", new InetAddress[]{InetAddress.getByName("8.8.8.8")});
        assertTrue(helpers.isInNet("google-public-dns-a.google.com", "8.8.8.8", "255.255.255.255"));

        assertFalse(helpers.isInNet("192.168.1.3", "192.168.1.1", "255.255.255.255"));
        assertTrue(helpers.isInNet("192.168.1.3", "192.168.1.1", "255.255.255.0"));
        assertTrue(helpers.isInNet("192.168.1.1", "192.168.3.1", "255.255.0.255"));
        assertTrue(helpers.isInNet("10.10.10.10", "12.12.12.12", "0.0.0.0"));
        assertFalse(helpers.isInNet("10.10.10.10", "12.12.12.12", "0.0.255.0"));
    }

    /**
     * Test of isInNetEx method, of class helpers.
     */
    @org.junit.Test
    public void testIsInNetEx() throws UnknownHostException {

        assertTrue(helpers.isInNetEx("198.95.249.79", "198.95.249.79/32"));
        assertFalse(helpers.isInNetEx("198.95.249.78", "198.95.249.79/32"));
        
        assertTrue(helpers.isInNetEx("198.95.33.22", "198.95.0.0/16"));
        assertFalse(helpers.isInNetEx("198.96.33.22", "198.95.0.0/16"));
        assertTrue(helpers.isInNetEx("198.96.33.22", "198.95.0.0/16;198.96.0.0/16"));
        
        fakeDns.addForwardResolution("isinnetex1host", new InetAddress[]{InetAddress.getByName("2a00:1450:400e:804::2004")});
        assertTrue(helpers.isInNetEx("isinnetex1host", "2a00:1450:400e/48"));
        assertFalse(helpers.isInNetEx("isinnetex1host", "198.95.0.0/16"));
        
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
        int weekdayNoBefore = weekdayNo - 1;
        if (weekdayNoBefore == 0) {
            weekdayNoBefore = 7;
        }
        int weekdayNoAfter = weekdayNo + 1;
        if (weekdayNoAfter == 8) {
            weekdayNoAfter = 1;
        }
        String weekdayNow = WEEKDAY_NAMES.get(weekdayNo - 1);
        String weekdayBefore = WEEKDAY_NAMES.get(weekdayNoBefore - 1);
        String weekdayAfter = WEEKDAY_NAMES.get(weekdayNoAfter - 1);
        assertTrue(helpers.weekdayRange(weekdayNow, null, null));
        assertTrue(helpers.weekdayRange(weekdayBefore, weekdayAfter, null));
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
        assertFalse(helpers.dateRange(nowDay + 1));
        assertTrue(helpers.dateRange(nowMonth));
        assertTrue(helpers.dateRange(nowYear));
        assertFalse(helpers.dateRange(nowYear + 1));
        assertTrue(helpers.dateRange(nowDay, nowMonth, nowDay, nowMonth));
        assertTrue(helpers.dateRange(nowMonth, nowYear, nowMonth, nowYear));
        assertFalse(helpers.dateRange(nowMonth, nowYear + 1, nowMonth, nowYear + 1));
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
        int nowMinuteNext = (nowMinute == 59) ? 0 : nowMinute + 1;
        int nowHourNext = (nowMinuteNext == 0) ? ((nowHour == 23) ? 0 : nowHour + 1) : nowHour;
        assertTrue(helpers.timeRange(nowHour));
        assertTrue(helpers.timeRange(nowHour, nowHour));
        assertTrue(helpers.timeRange(nowHour, nowMinute, nowHourNext, nowMinuteNext));
        assertFalse(helpers.timeRange(23, 31, 12, 23, 31, 5));     // second timestamp before first - should give false result
        assertFalse(helpers.timeRange(23, 31, 12, 23, 31, 5, "GMT"));     // second timestamp before first - should give false result
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
        fakeDns.addForwardResolution("isresolvableex1host", new InetAddress[]{ipv4Addr});
        fakeDns.addForwardResolution("isresolvableex2host", new InetAddress[]{ipv6Addr});

        assertTrue(helpers.isResolvableEx("isresolvableex1host"));
        assertTrue(helpers.isResolvableEx("isresolvableex2host"));
        assertFalse(helpers.isResolvableEx("foo-bar-does-not-exist"));
    }

    /**
     * Test of dnsResolveEx method, of class helpers.
     */
    @org.junit.Test
    public void testDnsResolveEx() {

        fakeDns.addForwardResolution("dnsresolveex1host", new InetAddress[]{ipv4Addr});
        fakeDns.addForwardResolution("dnsresolveex2host", new InetAddress[]{ipv6Addr});

        assertEquals(ipv4Addr.getHostAddress(), helpers.dnsResolveEx("dnsresolveex1host"));
        assertEquals(ipv6Addr.getHostAddress(), helpers.dnsResolveEx("dnsresolveex2host"));
    }

    /**
     * Test of sortIpAddressList method, of class helpers.
     */
    @org.junit.Test
    public void testSortIpAddressList() {

        String input
                = "10.2.3.9"
                + ";" + "2001:4898:28:3:201:2ff:feea:fc14"
                + ";" + "::1"
                + ";" + "127.0.0.1"
                + ";" + "::9";

        String expected
                = "::1"
                + ";" + "::9"
                + ";" + "2001:4898:28:3:201:2ff:feea:fc14"
                + ";" + "10.2.3.9"
                + ";" + "127.0.0.1";
        assertEquals(expected, helpers.sortIpAddressList(input));
    }
}
