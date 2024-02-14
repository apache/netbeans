/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.core.network.proxy.pac;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class PacUtilsTest {
    
    public PacUtilsTest() {
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
     * Test of toStrippedURLStr method, of class PacUtils.
     */
    @Test
    public void testToStrippedURLStr() throws URISyntaxException {
        System.out.println("toStrippedURLStr");
        
        URI uri = new URI("https://mary@somehost.apache.org:8081/path/to/something?x1=Christmas&user=unknown");
        
        String str = PacUtils.toStrippedURLStr(uri);
        String expResult = "https://somehost.apache.org:8081/";
        assertEquals(expResult, str);
    }
   

    /**
     * Test of toSemiColonListStr method, of class PacUtils.
     */
    @Test
    public void testToSemiColonListStr_InetAddressArr() {
        System.out.println("toSemiColonListStr");
        InetAddress[] addresses = getInetAddressListArr(
                new String[]{
                    "192.168.1.34",
                    "192.168.1.56"
                });
        String expResult = "192.168.1.34;192.168.1.56";
        String result = PacUtils.toSemiColonListInetAddress(addresses);
        assertEquals(expResult, result);

        addresses = getInetAddressListArr(
                new String[]{
                    "192.168.1.34"
                });
        expResult = "192.168.1.34";
        result = PacUtils.toSemiColonListInetAddress(addresses);
        assertEquals(expResult, result);

        addresses = getInetAddressListArr(
                new String[]{});
        expResult = "";
        result = PacUtils.toSemiColonListInetAddress(addresses);
        assertEquals(expResult, result);

    }

   

    /**
     * Test of ipPrefixMatch method, of class PacUtils.
     */
    @Test
    public void testIpPrefixMatch() {
        System.out.println("ipPrefixMatch");
        testIpPrefixMatch0("192.168.2.34",  "192.168.2.45/32", false);
        testIpPrefixMatch0("192.168.2.122", "192.168.2.0/25", true);
        testIpPrefixMatch0("192.168.2.129", "192.168.2.0/25", false);
        testIpPrefixMatch0("192.168.2.129", "192.168.0.0/16", true);
        testIpPrefixMatch0("192.169.2.129", "192.168.0.0/16", false);
        
        testIpPrefixMatch0("192.169.2.129", "3ffe:8311:ffff/48", false);
        testIpPrefixMatch0("192.169.2.129", "3ffe:8311:ffff/24", false);
        testIpPrefixMatch0("3ffe:8311:ffff:1a2b::", "3ffe:8311:ffff/24", true);
        
    }
    
    private void testIpPrefixMatch0(String address, String ipPrefix, boolean expResult) {
        try {
            InetAddress ipAddress = InetAddress.getByName(address);
            if (PacUtils.ipPrefixMatch(ipAddress, ipPrefix) != expResult) {
                fail(" For address=" + address + ", ipPrefix=" + ipPrefix + " the expected result was " + expResult);
            }
        } catch (UnknownHostException ex) {
            fail("Error in test case : " + ex.getMessage() );
        }
    }
    
    
    private InetAddress[] getInetAddressListArr(String[] ipAddresses) {
        List<InetAddress> list = getInetAddressList(ipAddresses);
        return list.toArray(new InetAddress[0]);
    }
    
    private List<InetAddress> getInetAddressList(String[] ipAddresses) {
        List<InetAddress> list = new ArrayList<>();
        for(String s : ipAddresses) {
            try {
                list.add(InetAddress.getByName(s));
            } catch (UnknownHostException ex) {
                fail("Error in test case " + ex.getMessage() );
            }
        }
        return list;
    }
    
}
