/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.core.network.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;

/**
 *
 */
public class IpAddressUtilsTest {
    private FakeDns fakeDns;
    private InetAddress ipv4Addr;
    private InetAddress ipv6Addr;

    public IpAddressUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        MockServices.setServices(FakeDns.class);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws UnknownHostException {
        fakeDns = Lookup.getDefault().lookup(FakeDns.class);
        fakeDns.install(true);

        ipv4Addr = InetAddress.getByName("172.217.17.36"); // just some random address
        ipv6Addr = InetAddress.getByName("2a00:1450:400e:804::2004"); // just some random address
    }

    @After
    public void tearDown() {
        fakeDns.unInstall();
    }

    /**
     * Test of nameResolve method, of class IpAddressUtils.
     */
    @Test
    public void testNameResolveArr() throws Exception {
        System.out.println("nameResolve");

        InetAddress[] addrInput = new InetAddress[]{ipv4Addr, ipv6Addr};
        fakeDns.addForwardResolution("test1", addrInput);
        InetAddress[] addresses2 = IpAddressUtils.nameResolveArr("test1", 1000, IpAddressUtils.IpTypePreference.ANY_IPV4_PREF);
        assertArrayEquals(addrInput, addresses2);
        InetAddress[] addresses3 = IpAddressUtils.nameResolveArr("test1", 1000, IpAddressUtils.IpTypePreference.ANY_IPV6_PREF);
        assertArrayEquals(new InetAddress[]{ipv6Addr, ipv4Addr}, addresses3);
        InetAddress[] addresses4 = IpAddressUtils.nameResolveArr("test1", 1000, IpAddressUtils.IpTypePreference.IPV4_ONLY);
        assertArrayEquals(new InetAddress[]{ipv4Addr}, addresses4);
        InetAddress[] addresses5 = IpAddressUtils.nameResolveArr("test1", 1000, IpAddressUtils.IpTypePreference.IPV6_ONLY);
        assertArrayEquals(new InetAddress[]{ipv6Addr}, addresses5);
        
        
        fakeDns.delayAnsweringBy(4000);
        fakeDns.addForwardResolution("test2", addrInput);
        try {
            InetAddress[] addresses6 = IpAddressUtils.nameResolveArr("test2", 1000, IpAddressUtils.IpTypePreference.ANY_IPV4_PREF);
            fail("This should have timed out");
        } catch (TimeoutException ex) {
        }
        
    }

    /**
     * Test of isValidIpv4Address method, of class IpAddressUtils.
     */
    @Test
    public void testIsValidIP4Address() {
        System.out.println("isValidIP4Address");
        assertTrue(IpAddressUtils.isValidIpv4Address("192.168.1.33"));
        assertFalse(IpAddressUtils.isValidIpv4Address(".3245.23"));
        assertFalse(IpAddressUtils.isValidIpv4Address("192.923.1.33"));
        assertFalse(IpAddressUtils.isValidIpv4Address("192.168.1."));
    }

    /**
     * Test of looksLikeIpv4Literal method, of class IpAddressUtils.
     */
    @Test
    public void testLooksLikeIpv4Literal() {
        System.out.println("looksLikeIpv4Literal");
        assertTrue(IpAddressUtils.looksLikeIpv4Literal("192.168.1.33"));
        assertTrue(IpAddressUtils.looksLikeIpv4Literal("192.168"));
        assertFalse(IpAddressUtils.looksLikeIpv4Literal("1923.43.23.2"));
        assertFalse(IpAddressUtils.looksLikeIpv4Literal("192"));
        assertFalse(IpAddressUtils.looksLikeIpv4Literal("192.a68"));
        assertFalse(IpAddressUtils.looksLikeIpv4Literal("abc."));
        assertFalse(IpAddressUtils.looksLikeIpv4Literal(".abc"));
        assertFalse(IpAddressUtils.looksLikeIpv4Literal("abc.x"));
        
        // ::8.144.52.38  is a valid IPv6 address, but is not an IPv4 address
        assertFalse(IpAddressUtils.looksLikeIpv4Literal("::8.144.52.38"));
    }

    /**
     * Test of looksLikeIpv6Literal method, of class IpAddressUtils.
     */
    @Test
    public void testLooksLikeIpv6Literal() {
        System.out.println("looksLikeIpv6Literal");
        assertFalse(IpAddressUtils.looksLikeIpv6Literal("192.168.1.33"));
        assertFalse(IpAddressUtils.looksLikeIpv6Literal("3d"));
        
        assertTrue(IpAddressUtils.looksLikeIpv6Literal("::1"));
        assertTrue(IpAddressUtils.looksLikeIpv6Literal("::1:3d"));
        assertTrue(IpAddressUtils.looksLikeIpv6Literal("1080:46A2:6F3A:9D1C:F2AB:8D12:200C:417A"));
        assertFalse(IpAddressUtils.looksLikeIpv6Literal("1080:46A2:6F3A:9D1C:F2AB:8D12:200C:417A:"));
        assertFalse(IpAddressUtils.looksLikeIpv6Literal("1080:46A2:6F3A:9D1C:F2AB:8D12:200C:417A:3A"));
        
        // The values below are certainly not valid IPv6 addresses 
        // but they kinda look like one and they certainly cannot be
        // hostnames. This is enough for the method to return true.
        assertTrue(IpAddressUtils.looksLikeIpv6Literal(":foobarfoorbar"));
        assertTrue(IpAddressUtils.looksLikeIpv6Literal("foobarfoorbar:"));
    }
    
    /**
     * Test of removeDomain method, of class IpAddressUtils.
     */
    @Test
    public void testRemoveDomain() {
        System.out.println("removeDomain");

        String input;

        input = ".hardlymakessense";
        assertEquals("", IpAddressUtils.removeDomain(input));

        input = "chicago.internal.net";
        assertEquals("chicago", IpAddressUtils.removeDomain(input));

        input = "chicago";
        assertEquals(input, IpAddressUtils.removeDomain(input));
        
        input = "123.123";
        assertEquals(input, IpAddressUtils.removeDomain(input));
        
        input = "3D::123.123";
        assertEquals(input, IpAddressUtils.removeDomain(input));
    }

}
