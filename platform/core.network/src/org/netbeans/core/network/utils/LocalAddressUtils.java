/*
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

package org.netbeans.core.network.utils;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.core.network.utils.IpAddressUtils.IpTypePreference;
import org.openide.util.RequestProcessor;

/**
 * Methods for determining the local host's own address. The methods provide
 * two benefits over the core JDK classes.:
 * <ul>
 *    <li><i>Caching</i>. Results from the methods are cached and can therefore
 *       be returned without blocking. An application should call 
 *       {@link #warmUp()} during the application's startup phase
 *       so that results are readily available when needed.
 *    </li>
 *    <li><i>IP protocol preference</i>. All methods allow to state
 *       an explicit preference for IPv4 vs IPv6. (<i>without</i> relation
 *       to the JVM's overall protocol preference settings).
 *    </li>
 * </ul>
 * 
 * <p>
 * The main method is 
 * {@link #getMostLikelyLocalInetAddress(IpAddressUtils.IpTypePreference) getMostLikelyLocalInetAddress()}.
 * The other methods essentially exist to provide input to this method but are
 * exposed nevertheless if anyone wants them.
 *  
 * <p>
 * These utility methods are in particular relevant in relation to
 * the PAC helper method {@code myIpAddress()}. However, the class may indeed 
 * be used for any use case.
 * 
 * <p>
 * Note, that there is no single correct answer to the question about
 * determining the local host's IP address in an environment with multiple
 * network interfaces or multiple addresses on each network interface.
 *
 * @author lbruun
 */
public class LocalAddressUtils {
    private static final Logger LOG = Logger.getLogger(LocalAddressUtils.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("LocalNetworkAddressFinder", 4);
    
    // Create some static InetAddress'es. This is done in a somewhat convoluted
    // way, but one which guarantees that no DNS lookup will be performed.
    
    private static final byte[] LOOPBACK_IPV4_RAW = new byte[]{0x7f,0x00,0x00,0x01};
    private static final byte[] LOOPBACK_IPV6_RAW = new byte[]{
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
    
    
    // 8.8.8.8  -- which incidentially is Google's DNS server, but we don't ever connect
    private static final byte[] SOMEADDR_IPV4_RAW = new byte[]{0x08, 0x08, 0x08, 0x08};

    // 2001:4860:4860::8888  -- which incidentially is Google's DNS server, but we don't ever connect
    private static final byte[] SOMEADDR_IPV6_RAW = new byte[]{
        0x20, 0x01, 0x48, 0x60, 0x48, 0x60, 0x00, 0x00, 
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0x88, (byte)0x88};
    
    private static final InetAddress LOOPBACK_IPV4;
    private static final InetAddress LOOPBACK_IPV6;
    private static final InetAddress SOMEADDR_IPV4;
    private static final InetAddress SOMEADDR_IPV6;
    
    static {
        try {
            LOOPBACK_IPV4 = InetAddress.getByAddress("local-ipv4-dummy", LOOPBACK_IPV4_RAW);
            LOOPBACK_IPV6 = InetAddress.getByAddress("local-ipv6-dummy", LOOPBACK_IPV6_RAW);
            SOMEADDR_IPV4 = InetAddress.getByAddress("some-ipv4-dummy", SOMEADDR_IPV4_RAW);
            SOMEADDR_IPV6 = InetAddress.getByAddress("some-ipv6-dummy", SOMEADDR_IPV6_RAW);
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }
            
   
    private static final Object LOCK = new Object();  
    private static Future<InetAddress> fut1;
    private static Future<InetAddress[]> fut2;
    private static Future<List<InetAddress>> fut3;
    private static Future<List<InetAddress>> fut4;

    private static final Callable<InetAddress> C1 = () -> InetAddress.getLocalHost();
    private static final Callable<InetAddress[]> C2 = () -> {
        try {
            String hostname = HostnameUtils.getNetworkHostname();
            return InetAddress.getAllByName(hostname);
        } catch (NativeException ex) {
            throw new UnknownHostException(ex.getMessage() + ", error code : " + ex.getErrorCode());
        }
    };
    private static final Callable<List<InetAddress>> C3 = () -> getLocalNetworkInterfaceAddr();

    private static final Callable<List<InetAddress>> C4 = () -> {
        // See https://stackoverflow.com/a/38342964
        // Note that no actual connection is ever made below. (UDP is connection-less!)
        // We just need the Sockets API to populate the local address
        // of the socket. We do this by pretending that we want to send
        // some UDP packages, but we never actually do so.

        List<InetAddress> list = new ArrayList<>(2);

        // IPv4 attempt
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(SOMEADDR_IPV4, 10002);   // doesn¨t need to be reachable .. and port it irrelevant
            InetAddress addr = socket.getLocalAddress();
            if ((addr instanceof Inet4Address) && (!addr.isAnyLocalAddress() && (!addr.isLoopbackAddress()))) {
                list.add(addr);
            }
        } catch (SecurityException | SocketException ex) {
        }
        // IPv6 attempt
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(SOMEADDR_IPV6, 10002);   // doesn¨t need to be reachable .. and port it irrelevant
            InetAddress addr = socket.getLocalAddress();
            if ((addr instanceof Inet6Address) && (!addr.isAnyLocalAddress() && (!addr.isLoopbackAddress()))) {
                list.add(addr);
            }
        } catch (SecurityException | SocketException ex) {
        }

        return list;
    };
    
    static {
        refreshNetworkInfo(false);
    }
    
    private LocalAddressUtils() {
    }
    

    /**
     * Starts collecting network information in a background task. This method
     * is ideal for calling once during application startup phase. Calling this
     * method during startup means that later calls to methods in this class
     * will perform very fast, because they will then return a cached result and
     * thus will never block.
     */
    public static void warmUp() {
        // Does nothing. The refreshNetworkInfo task will be fired when the class
        // is loaded by the classloader.
    }
    
    /**
     * Refreshes the cached network information. Normally there is no reason
     * to call this method as a computer's network information is unlikely
     * to change during the lifetime of the application, unless the computer
     * connects to a different network than was the case when the information
     * was first cached.
     * 
     * @param await if true, waits for the refresh to complete
     */
    public static void refreshNetworkInfo(boolean await) {
        synchronized (LOCK) {
            fut1 = RP.submit(C1);
            fut2 = RP.submit(C2);
            fut3 = RP.submit(C3);
            fut4 = RP.submit(C4);
            if (await) {
                try {
                    fut1.get();
                    fut2.get();
                    fut3.get();
                    fut4.get();
                } catch (InterruptedException | ExecutionException ex) {
                }
            }
        }
    }
    
    /**
     * Returns the address of the local host.
     * 
     * <p>This method returns a cached result of calling 
     * {@link InetAddress#getLocalHost()} and is therefore likely not to
     * block (unlike the underlying method) unless this is the first time 
     * this class is being referenced.
     * 
     * <p>Note that {@code InetAddress#getLocalHost()} is known to return
     * unpredictable results for hosts with multiple network adapters. The
     * {@link #getMostLikelyLocalInetAddresses(IpAddressUtils.IpTypePreference) getMostLikelyLocalInetAddresses()}
     * method is much more likely to return an acceptable result.
     * 
     * @see #getMostLikelyLocalInetAddresses(IpAddressUtils.IpTypePreference) 
     * @return
     * @throws UnknownHostException 
     */
    public static @NonNull InetAddress getLocalHost() throws UnknownHostException {
        synchronized (LOCK) {
            if (fut1 == null) {
                refreshNetworkInfo(false);
            }
            try {
                return fut1.get();
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof UnknownHostException) {
                    throw (UnknownHostException) ex.getCause();
                }
                throw new RuntimeException(ex.getCause());
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Returns the addresses of the local host.
     * 
     * <p>This is achieved by retrieving the
     * {@link HostnameUtils#getNetworkHostname() name-of-the-host} 
     * from the system, then resolving that name into a list of {@code InetAddress}es. 
     * 
     * <p>This method returns a cached result and is therefore likely not to
     * block unless this is the first time this class is being referenced.
     * 
     * @see HostnameUtils#getNetworkHostname()
     * @see InetAddress#getAllByName(java.lang.String) 
     * @param ipTypePref filter
     * @return
     * @throws UnknownHostException if no IP address for the host name could be found
     */
    public static @NonNull InetAddress[] getLocalHostAddresses(IpTypePreference ipTypePref) throws UnknownHostException {
        synchronized (LOCK) {
            if (fut2 == null) {
                refreshNetworkInfo(false);
            }
            try {
                InetAddress[] arr = fut2.get();
                if (arr == null || arr.length == 0) {
                    return new InetAddress[0];
                } else {
                    List<InetAddress> list = Arrays.asList(arr);
                    List<InetAddress> filteredList = IpAddressUtilsFilter.filterInetAddresses(list, ipTypePref);
                    return filteredList.toArray(new InetAddress[0]);
                }
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof UnknownHostException) {
                    throw (UnknownHostException) ex.getCause();
                }
                if (ex.getCause() instanceof SecurityException) {
                    throw (SecurityException) ex.getCause();
                }
                throw new RuntimeException(ex.getCause());
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }    
    
    /**
     * Returns a prioritized list of local host addresses. The further up
     * on this list, the more likely it is that the address is the host's IP
     * address.
     * 
     * <p>
     * Prioritization is done on the following basis:
     * <ul>
     *   <li>IPv4 addresses are prioritized higher than IPv6 addresses.</li>
     *   <li>Addresses belonging to a non-virtual interface are prioritized higher
     *       than addresses belonging to a virtual interface.</li>
     *   <li>Addresses belonging to an interface which supports multicast are
     *       prioritized higher than addresses belonging to an interface which doesn't
     *       support multicast.</li>
     *   <li>Addresses belonging to an interface which 
     *       {@link #isSoftwareVirtualAdapter(java.net.NetworkInterface) look
     *       like a software virtual adapters} are prioritized lower than addresses 
     *       belonging to interfaces which don't look software virtual adapters.</li>
     *   <li>Addresses with a broadcast address are prioritized higher than
     *       addresses with no broadcast address.</li>
     * </ul>
     * 
     * <p>
     * The method returns a cached result and is therefore likely not to block,
     * unless this is the first time this class is being referenced.
     * 
     * @param ipTypePref filter
     * @return prioritized list of addresses
     */
    public static @NonNull List<InetAddress> getPrioritizedLocalHostAddresses(IpAddressUtils.IpTypePreference ipTypePref)  {
        synchronized (LOCK) {
            if (fut3 == null) {
                refreshNetworkInfo(false);
            }
            try {
                return IpAddressUtilsFilter.filterInetAddresses(fut3.get(), ipTypePref);
            } catch (ExecutionException ex) {
                throw new RuntimeException(ex.getCause());
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }    
    
    /**
     * Returns the address of the local host, found by using UDP method.
     * 
     * <p>
     * This method works by creating a 'connected' UDP socket. Upon the connect
     * operation, the Berkeley sockets API will populate the local endpoint
     * (own host) according to the host's routing information. Hence we can
     * use this for finding the IP of the local host. Note, that there is 
     * never any actual UDP connection created. We only use the socket to see how
     * its metadata has been populated.
     * 
     * <p>
     * This method is known not to work on Mac OSX. It will most likely
     * return an empty list if on Mac OSX.
     * 
     * <p>
     * This method returns a cached result and is therefore likely not to block
     * unless this is the first time this class is being referenced.
     * 
     * 
     * @param ipTypePref IP protocol filter
     * @see #getMostLikelyLocalInetAddresses(IpAddressUtils.IpTypePreference) 
     * @return list of IP addresses, either of length 1 or length 0, never null.
     */
    public static @NonNull List<InetAddress> getDatagramLocalInetAddress(IpTypePreference ipTypePref) {
        synchronized (LOCK) {
            if (fut4 == null) {
                refreshNetworkInfo(false);
            }
            try {
                return IpAddressUtilsFilter.filterInetAddresses(fut4.get(), ipTypePref);
            } catch (ExecutionException ex) {
                throw new RuntimeException(ex.getCause());
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    /**
     * Returns the host's IP addresses. Or rather the IP addresses most likely
     * to be the ones the host is known by. This method is much more likely
     * to return a correct result than the JDKs {@link InetAddress#getLocalHost()},
     * in particular on hosts with multiple network interfaces or hosts
     * that are virtualized or operating in a PaaS environment.
     * 
     * <p>
     * The method uses the following prioritization for determining what
     * to return, by continously moving to the next step if the previous 
     * step yielded an empty result:
     * <ol>
     * <li>
     * The list from {@link #getLocalHostAddresses(IpAddressUtils.IpTypePreference) getLocalHostAddresses() } 
     * (List A) is compared to 
     * {@link #getPrioritizedLocalHostAddresses(IpAddressUtils.IpTypePreference) getPrioritizedLocalHostAddresses() }
     * (List B), 
     * picking the ones from List B list which is also on List A.
     * </li>
     * 
     * <li>
     * Use List B.
     * </li>
     * 
     * <li>
     * Use List A.
     * </li>
     * 
     * <li>
     * Use the result from {@link #getLocalHost()} if it matches the 
     * {@code ipTypePref} filter.
     * </li>
     * 
     * <li>
     * Use the result from {@link #getDatagramLocalInetAddress(IpAddressUtils.IpTypePreference) getDatagramLocalInetAddress() } if it matches the 
     * {@code ipTypePref} filter.
     * </li>
     * 
     * <li>
     * Finally, if everything else fails, return the result of 
     * {@link #getLoopbackAddress(IpAddressUtils.IpTypePreference) 
     * getLoopbackAddress()}.
     * </li>
     * </ol>
     * 
     * <p>
     * The method uses the other methods in the class and is therefore 
     * likely not to block, unless this is the first time this class is being 
     * referenced.
     * 
     * @see #getMostLikelyLocalInetAddress(IpAddressUtils.IpTypePreference) 
     * @param ipTypePref IP protocol filter
     * @return IP addresses, never null
     */
    public static @NonNull InetAddress[] getMostLikelyLocalInetAddresses(IpAddressUtils.IpTypePreference ipTypePref) {
        List<InetAddress> filteredList = getPrioritizedLocalHostAddresses(ipTypePref);
        IpAddressUtils.removeLoopback(filteredList);
        
        try {
            List<InetAddress> localHostAddresses = new ArrayList<>(Arrays.asList(getLocalHostAddresses(ipTypePref)));
            IpAddressUtils.removeLoopback(localHostAddresses);
            
            if (!localHostAddresses.isEmpty()) {
                List<InetAddress> tmpList = new ArrayList<>(5);
                for (InetAddress addr : filteredList) {
                    if (localHostAddresses.contains(addr)) {
                        tmpList.add(addr);
                    }
                }
                
                // #1 
                if (!tmpList.isEmpty()) {
                    return tmpList.toArray(new InetAddress[0]);
                }

                // #2
                if (!localHostAddresses.isEmpty()) {
                    return localHostAddresses.toArray(new InetAddress[0]);
                }
            }
        } catch (UnknownHostException ex) {
        }
         
        // #3
        if (!filteredList.isEmpty()) {
            return filteredList.toArray(new InetAddress[0]);
        }
            
            
        // #4
        try {
            InetAddress addr = IpAddressUtilsFilter.pickInetAddress(Collections.singletonList(getLocalHost()), ipTypePref);
            if (addr != null && (!addr.isAnyLocalAddress()) && (!addr.isLoopbackAddress())) {
                return new InetAddress[]{addr};
            }
        } catch (UnknownHostException ex) {
        }
        
        // #5 - nearly last resort
        List<InetAddress> datagramLocalInetAddress = getDatagramLocalInetAddress(ipTypePref);
        if (datagramLocalInetAddress != null && (!datagramLocalInetAddress.isEmpty())) {
            return datagramLocalInetAddress.toArray(new InetAddress[0]);
        }
        
        // #6 - last resort
        return new InetAddress[]{getLoopbackAddress(ipTypePref)};        
    }
    
    /**
     * Returns the host's IP address. Same as 
     * {@link #getMostLikelyLocalInetAddresses(IpAddressUtils.IpTypePreference) getMostLikelyLocalInetAddresses()}
     * but only returns a single IP address.
     * 
     * 
     * @see #getMostLikelyLocalInetAddresses(IpAddressUtils.IpTypePreference) 
     * @param ipTypePref IP protocol filter
     * @return IP address, never null
     */
    public static @NonNull InetAddress getMostLikelyLocalInetAddress(IpAddressUtils.IpTypePreference ipTypePref) {
        InetAddress[] ipAddresses = getMostLikelyLocalInetAddresses(ipTypePref);
        // We're guaranteed the array will have length > 0 and never null.
        return ipAddresses[0];
    }

    
    /**
     * Returns the loopback address.
     * 
     * <p>This method is similar to {@link InetAddress#getLoopbackAddress()} 
     * except that the preference for IPv4 vs IPv6 can be explicitly 
     * stated.
     * 
     * <p>For IPv4 the returned address is always {@code 127.0.0.1} and for
     * IPv6 it is {@code ::1}.
     * 
     * @param ipTypePref IPv4 vs IP4v6 preference
     * @return 
     */
    public static @NonNull InetAddress getLoopbackAddress(IpAddressUtils.IpTypePreference ipTypePref) {
        switch(ipTypePref) {
            case IPV4_ONLY:
            case ANY_IPV4_PREF:
                return LOOPBACK_IPV4;
            case IPV6_ONLY:
            case ANY_IPV6_PREF:
                return LOOPBACK_IPV6;
            default:    
                return LOOPBACK_IPV4;
        }
    }
    
 
    private static @NonNull List<InetAddress> getLocalNetworkInterfaceAddr() {
        final Map<InetAddress, Integer> mapWithScores = new HashMap<>();

        // Looping through all network interfaces on the host.
        // WARNING:  On Windows this is quite slow. On my Intel Core i7 it takes 
        // approximately 1200 msecs and I only have 3 network interfaces defined. 
        // The reason is that Windows creates a lot of virtual/bogus network 
        // interfaces. (nope, you don't see them with 'ipconfig /all' command)
        // On my Win10 host there are 46 entries returned from 
        // NetworkInterface.getNetworkInterfaces() !! (all but a few are really 
        // to be ignored). It is the actual call to NetworkInterface.getNetworkInterfaces() 
        // which consumes 95% of the total time of this method.
        // For a GUI oriented system a call to NetworkInterface.getNetworkInterfaces() 
        // can make the application seem slow if it is done on a critical thread.
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces(); // expensive call
        } catch (SocketException ex) {
            LOG.log(Level.WARNING, "Cannot get host's network interfaces", ex);
            return Collections.emptyList();
        }

        while (interfaces.hasMoreElements()) {
            int ifScore = 0;  // the score for the interface, higher is better
            NetworkInterface netIf = interfaces.nextElement(); // inexpensive call
            try {
                if (!netIf.isUp() || netIf.isLoopback()) {  // inexpensive call
                    continue;  // discard
                }
                // Solaris note: When inside a non-global zone a network interface which
                // is really virtual as seen from the global zone is seen as non-virtual
                // from within the non-global zone. So, it is safe to give virtual
                // interfaces a lower score.
                if (netIf.isVirtual()) {
                    ifScore -= 1;
                }
                if (!netIf.supportsMulticast()) {
                    ifScore -= 1;
                }
                if (isSoftwareVirtualAdapter(netIf)) {
                    ifScore -= 1;
                }
                    
            } catch (SocketException ex) {
                // isUp() and isLoopback() may throw exception. Discard
                // the interface if that's the case.
                continue;
            }
            List<InterfaceAddress> interfaceAddresses = netIf.getInterfaceAddresses(); // inexpensive call
            for(InterfaceAddress ifAddr : interfaceAddresses) {
                int addrScore = 0; // the score for the address, higher is better
                InetAddress address = ifAddr.getAddress();
                if (ifAddr.getBroadcast() == null) {
                    addrScore -= 1;
                }
                if (address instanceof Inet6Address) {
                    addrScore -= 1;
                }
                
                mapWithScores.put(address, ifScore + addrScore);
            }
        }
        
        List<InetAddress> list = new ArrayList<>(mapWithScores.keySet());
        
        // Sort descending according to the scores 
        list.sort(new Comparator<InetAddress>(){
            @Override
            public int compare(InetAddress o1, InetAddress o2) {
                return mapWithScores.get(o2).compareTo(mapWithScores.get(o1));
            }
        });
        
        return list;  // returns a prioritized list
    }
    
    
   
    
   
    
    /**
     * Tries to guess if the network interface is a virtual adapter
     * by one of the makers of virtualization solutions (e.g. VirtualBox,
     * VMware, etc). This is by no means a bullet proof method which is
     * why it errs on the side of caution.
     * 
     * @param nif network interface
     * @return 
     */
    public static boolean isSoftwareVirtualAdapter(NetworkInterface nif) {
        
        try {
            // VirtualBox uses a semi-random MAC address for their adapter
            // where the first 3 bytes are always the same:
            //    Windows, Mac OS X, Linux : begins with 0A-00-27
            //    Solaris:  begins with 10-00-27
            // (above from VirtualBox source code)
            //
            byte[] macAddress = nif.getHardwareAddress();
            if (macAddress != null && macAddress.length >= 3) {
                if ((macAddress[0] == 0x0A || macAddress[0] == 0x08) &&
                        (macAddress[1] == 0x00) &&
                        (macAddress[2] == 0x27)) {                
                    return true;
                }
            }
            return false;
        } catch (SocketException ex) {
            return false;
        }
    }
}
