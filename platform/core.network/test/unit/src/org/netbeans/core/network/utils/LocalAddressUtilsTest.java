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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author lbruun
 */
public class LocalAddressUtilsTest {

    @Test
    public void testWarmUp() {
        LocalAddressUtils.warmUp();
    }

    @Test
    public void testRefreshNetworkInfo() {
        LocalAddressUtils.refreshNetworkInfo(true);
    }

    @Test
    public void testGetLocalHost() throws Exception {
        InetAddress result = LocalAddressUtils.getLocalHost();
        assertNotNull(result);
    }

    @Test
    public void testGetLocalHostAddresses() throws Exception {
        InetAddress[] result = LocalAddressUtils.getLocalHostAddresses(IpAddressUtils.IpTypePreference.IPV4_ONLY);
        assertNotNull(result);
    }


    @Test
    public void testGetPrioritizedLocalHostAddresses() {
        List<InetAddress> result = LocalAddressUtils.getPrioritizedLocalHostAddresses(IpAddressUtils.IpTypePreference.IPV4_ONLY);
        assertNotNull(result);
    }

    @Test
    public void testGetMostLikelyLocalInetAddresses() {
        InetAddress[] result = LocalAddressUtils.getMostLikelyLocalInetAddresses(IpAddressUtils.IpTypePreference.IPV4_ONLY);
        assertNotNull(result);
    }

    @Test
    public void testGetMostLikelyLocalInetAddress() {
        InetAddress result = LocalAddressUtils.getMostLikelyLocalInetAddress(IpAddressUtils.IpTypePreference.IPV4_ONLY);
        assertNotNull(result);
    }

    @Test
    public void testGetLoopbackAddress() {
        InetAddress inetAddress = LocalAddressUtils.getLoopbackAddress(IpAddressUtils.IpTypePreference.IPV4_ONLY);
        assertEquals("local-ipv4-dummy", inetAddress.getHostName());
        assertEquals("127.0.0.1", inetAddress.getHostAddress());
        inetAddress = LocalAddressUtils.getLoopbackAddress(IpAddressUtils.IpTypePreference.IPV6_ONLY);
        assertEquals("local-ipv6-dummy", inetAddress.getHostName());
        assertEquals("0:0:0:0:0:0:0:1", inetAddress.getHostAddress());
        inetAddress = LocalAddressUtils.getLoopbackAddress(IpAddressUtils.IpTypePreference.ANY_JDK_PREF);
        assertEquals("local-ipv4-dummy", inetAddress.getHostName());
        assertEquals("127.0.0.1", inetAddress.getHostAddress());
    }


    @Test
    public void testIsSoftwareVirtualAdapter() throws SocketException {
        InetAddress inetAddress = LocalAddressUtils.getLoopbackAddress(IpAddressUtils.IpTypePreference.ANY_IPV4_PREF);
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
        assertFalse(LocalAddressUtils.isSoftwareVirtualAdapter(networkInterface));
    }
    
}
