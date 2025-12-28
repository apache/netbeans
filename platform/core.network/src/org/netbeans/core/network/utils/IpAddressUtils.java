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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * IP address utilities. Mainly providing functionality
 * for doing name resolve with explicit timeout.
 *
 * <p>
 * TODO: Support for reverse lookup with timeout isn't implemented. Hasn't been
 * any need for it.
 * 
 * @author lbruun
 */
public class IpAddressUtils {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    private static final RequestProcessor RP = new RequestProcessor("DNSBackgroundResolvers", 10);

    IpAddressUtils() {}

    private static IpAddressUtils INSTANCE;
    private static synchronized IpAddressUtils getDefault() {
        if (INSTANCE == null) {
            INSTANCE = Lookup.getDefault().lookup(IpAddressUtils.class);
            if (INSTANCE == null) {
                INSTANCE = new IpAddressUtils();
            }
        }
        return INSTANCE;
    }
    
    /**
     * Filters the result of a method according to IP protocol preference.
     */
    public enum IpTypePreference {
        /**
         * Only IPv4 address(es) in the returned value. 
         */
        IPV4_ONLY,
        /**
         * Only IPv6 address(es) in the returned value. 
         */
        IPV6_ONLY,
        /**
         * Any of IPv4 or IPv6 addresses are acceptable in the returned value,
         * but IPv4 address is preferred over IPv6. If the method returns
         * an array then IPv4 addresses will come before IPv6 addresses.
         */
        ANY_IPV4_PREF,
        /**
         * Any of IPv4 or IPv6 addresses are acceptable in the returned value,
         * but IPv6 address is preferred over IPv4. If the method returns
         * an array then IPv6 addresses will come before IPv4 addresses.
         */
        ANY_IPV6_PREF,
        /**
         * Any of IPv4 or IPv6 addresses are acceptable in the returned value,
         * but their internal preference is determined by the setting in the
         * JDK, namely the {@code java.net.preferIPv6Addresses} system property.
         * If this property is {@code true} then using this preference will be
         * exactly as {@link #ANY_IPV6_PREF}, if {@code false} it will be 
         * exactly as {@link #ANY_IPV4_PREF}.
         */
        ANY_JDK_PREF
    }
    
    /**
     * Performs a name service lookup with a timeout. 
     * 
     * <p>This method can be used when the JRE's default DNS timeout is not
     * acceptable. The method is essentially a wrapper around 
     * {@link java.net.InetAddress#getAllByName(java.lang.String) InetAddress.getAllByName()}.
     * 
     * <p>A reasonable timeout value is 4 seconds (4000 ms) as this value
     * will - with the JRE's default settings - allow each DNS server in a
     * list of 4 servers to be queried once.
     * 
     * <p>If the {@code host} is a literal representation
     * of an IPv4 address (using dot notation, for example {@code "192.168.1.44"}) 
     * or an IPv6 address (any form accepted by {@link java.net.Inet6Address}),
     * then the method will convert the text into {@code InetAddress} and 
     * return immediately. The timeout does not apply to this case.
     * 
     * <br>
     * <br>
     * <p>
     * <u>Java's default DNS timeout:</u>
     * <p>
     * The default timeout DNS lookup is described 
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/jndi/jndi-dns.html#PROP">
     * in the documentation for JNDI</a> in properties:
     * <p>
     * &nbsp;&nbsp;{@code com.example.jndi.dns.timeout.initial}  (defaults to 1 sec in Java 8)<br>
     * &nbsp;&nbsp;{@code com.example.jndi.dns.timeout.retries}  (defaults to 4 in Java 8)
     * <p>
     * The defaults mean that if the host OS has two servers defined in its DNS
     * server search string (usually there's <i>at least</i> two) then - if both
     * servers do not respond - the call to
     * {@link InetAddress#getByName(java.lang.String)} will block for
     * <b>30 seconds!</b> before it returns. With three servers it will be 45 seconds
     * and so forth. This wait may be unacceptable in some scenarios and this is 
     * where this method is then a better alternative.
     * <br>
     * <br>
     * @see #nameResolve(String, int, IpTypePreference) 
     * @param host either a host name or a literal IPv4
     *    address in dot notation.
     * @param timeoutMs Milliseconds to wait for the result from the name service
     *    query before aborting. A value of 0 means not to use any timeout for
     *    the background resolver task. In that case only the standard timeouts
     *    as defined in the JRE will apply.
     * @param ipTypePref IP protocol filter
     * @return the result of the name lookup (may be more than one address)
     * @throws InterruptedException if the background task was interrupted
     * @throws TimeoutException if the timeout ({@code timeoutMs}) expired
     *    before a result was obtained.
     * @throws UnknownHostException if no IP address for the host could be
     *    found.
     */
    public static @NonNull InetAddress[] nameResolveArr(String host, int timeoutMs, IpTypePreference ipTypePref)
            throws InterruptedException, UnknownHostException, TimeoutException {
        
        if (looksLikeIpv6Literal(host) || looksLikeIpv4Literal(host)) {
            // No DNS lookup is needed in this case so we can simply
            // call directly. It won't block.
            InetAddress addr = InetAddress.getByName(host);
            if (ipTypePref == IpTypePreference.IPV4_ONLY  && addr instanceof Inet6Address) {
                throw new UnknownHostException("Mismatch between supplied literal IP address \"" + host + "\" (which is IPv6) and value of ipTypePref : " + ipTypePref);
            }
            if (ipTypePref == IpTypePreference.IPV6_ONLY  && addr instanceof Inet4Address) {
                throw new UnknownHostException("Mismatch between supplied literal IP address \"" + host + "\" (which is IPv6) and value of ipTypePref : " + ipTypePref);
            }
            return new InetAddress[]{addr};
        }
        
        Callable<InetAddress[]> lookupTask = getDefault().createDnsTimeoutTask(host);
        Future<InetAddress[]> future = RP.submit(lookupTask);
        try {
            InetAddress[] ipAddresses;
            if (timeoutMs == 0) {
                ipAddresses = future.get();
            } else {
                ipAddresses = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            }
            List<InetAddress> resultList = IpAddressUtilsFilter.filterInetAddresses(Arrays.asList(ipAddresses), ipTypePref);
            if (resultList.isEmpty()) {
                throw new UnknownHostException("A positive result was returned from name lookup for \"" + host + "\" but none that matched a filter of " + ipTypePref);
            }
            return resultList.toArray(new InetAddress[0]);

        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof UnknownHostException) {
                throw (UnknownHostException) cause;
            }
            // Unlikely
            Exceptions.printStackTrace(cause);
            return new InetAddress[]{};
        } catch (TimeoutException ex) {
            // If the wait times out then cancel the background job too.
            // We're no longer interested in the result.
            // The downside of not letting the task finish is that the 
            // JRE's DNS cache will then not be populated with the result
            // - if a result is indeed obtained later than the timeout.
            future.cancel(true);
            throw new TimeoutException("No answer from name service within " + timeoutMs + " milliseconds when resolving \"" + host+ "\"");
        } 
    }

    /**
     * Performs a name service lookup with a timeout. Same as 
     * {@link #nameResolveArr(java.lang.String, int, IpTypePreference) nameResolveArr()}
     * but only returns a single address. 
     * 
     * @see #nameResolveArr(String, int, IpTypePreference) 
     * @param host either a host name or a literal IPv4
     *    address in dot notation.
     * @param timeoutMs Milliseconds to wait for the result from the name service
     *    query before aborting. A value of 0 means not to use any timeout for
     *    the background resolver task. In that case only the standard timeouts
     *    as defined in the JRE will apply.
     * @param ipTypePref IP protocol filter
     * @return IP address
     * @throws InterruptedException if the background task was interrupted
     * @throws TimeoutException if the timeout ({@code timeoutMs}) expired
     *    before a result was obtained.
     * @throws UnknownHostException if no IP address for the host could be
     *    found.
     */
    public static @NonNull InetAddress nameResolve(String host, int timeoutMs, IpTypePreference ipTypePref)
            throws InterruptedException, UnknownHostException, TimeoutException {
        InetAddress[] ipAddresses = nameResolveArr(host, timeoutMs, ipTypePref);
        // We're guaranteed the array will have length > 0 and never null,
        // so the following is safe.
        return ipAddresses[0];
    }
    
    /**
     * Performs a name service lookup with a timeout. Same as 
     * {@link #nameResolveArr(java.lang.String, int, IpTypePreference) nameResolveArr()}
     * but only returns a single address and uses 
     * {@link IpTypePreference#ANY_JDK_PREF IpTypePreference.ANY_JDK_PREF}.
     * 
     * <p>
     * There are several overloaded forms of this method. If you don't need
     * any specific filtering on the type of IP address returned then use
     * this method!
     * 
     * @see #nameResolveArr(String, int, IpTypePreference) 
     * @param host either a host name or a literal IPv4
     *    address in dot notation.
     * @param timeoutMs Milliseconds to wait for the result from the name service
     *    query before aborting. A value of 0 means not to use any timeout for
     *    the background resolver task. In that case only the standard timeouts
     *    as defined in the JRE will apply.
     * @return IP address
     * @throws InterruptedException if the background task was interrupted
     * @throws TimeoutException if the timeout ({@code timeoutMs}) expired
     *    before a result was obtained.
     * @throws UnknownHostException if no IP address for the host could be
     *    found.
     */
    public static @NonNull InetAddress nameResolve(String host, int timeoutMs)
            throws InterruptedException, UnknownHostException, TimeoutException {
        return nameResolve(host, timeoutMs, IpTypePreference.ANY_JDK_PREF);
    }
    
    /**
     * Validates if a string can be parsed as an IPv4 address.
     * 
     * <p>
     * The standard way to do this type of validation in Java is
     * {@link java.net.InetAddress#getByName(java.lang.String)} but this method
     * will block if the string is <i>not</i> an IP address literal, because it
     * will query the name service. In contrast, this method relies solely on
     * pattern matching techniques and will never block. Return value
     * {@code true} is a guarantee that the string can be parsed as an IPv4
     * address.
     *
     * @param ipAddressStr input string to be evaluated
     * @return true if the string is a valid IPv4 literal on the 
     *     form "#.#.#.#"
     */
    public static boolean isValidIpv4Address(String ipAddressStr) {
        if (IPV4_PATTERN.matcher(ipAddressStr).matches()) {

            String[] segments = ipAddressStr.split("\\.");

            if (segments.length != 4) {
                return false;
            }
            
            for (String segment : segments) {
                if (segment == null || segment.length() == 0) {
                    return false;
                }
                
                // leading zeroes are not allowed within the segment.
                if (segment.length() > 1 && segment.startsWith("0")) {
                    return false;
                }
                
                try {
                    int value = Integer.parseInt(segment);
                    if (value > 255) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;  // Unlikely. Already matched by regexp
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Does a shallow check if the argument looks like an IPv6 address as
     * opposed to a host name. Note, that a return value of {@code true} doesn't
     * guarantee that the argument is a <i>valid</i> IPv6 literal, but a return
     * value of {@code false} is a guarantee that it is not.
     * 
     * <p>
     * The method is not meant as a validity check. It is mainly useful
     * for predicting if the JDK's {@code InetAddress#get*ByName()} will block 
     * or not.
     * 
     * @param ipAddressStr
     * @return true if argument looks like an IPv6 literal.
     */
    public static boolean looksLikeIpv6Literal(String ipAddressStr) {
        if (ipAddressStr == null) {
            return false;
        }
        // "::d" is the shortest possible form of an IPv6 address.
        // and the longest form is 39 chars.
        if (ipAddressStr.length() < 3 || ipAddressStr.length() > 39) {
            return false;
        }
        if (ipAddressStr.startsWith(":") || ipAddressStr.endsWith(":")) {
            return true;
        }
        // Matches A80::8:800:200C:417A or 0A80::8:800:200C:417A
        if (ipAddressStr.length() >= 5) {
            if ((ipAddressStr.charAt(3) == ':') || (ipAddressStr.charAt(4) == ':')) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Does a shallow check if the argument looks like an IPv4 address as
     * opposed to a host name. Note, that a return value of {@code true} doesn't
     * guarantee that the argument is a <i>valid</i> IPv4 literal, but a return
     * value of {@code false} is a guarantee that it is not.
     *
     * <p>
     * The method is not meant as a validity check. It is mainly useful
     * for predicting if the JDK's {@code InetAddress#get*ByName()} will block 
     * or not.
     * 
     * <p>
     * Note, the method will err on the side of caution meaning return {@code 
     * false} if in doubt. Java allows "123" as a {@link java.net.Inet4Address 
     * valid IPv4 address}, but in this case it cannot be determined if the
     * input is a hostname or an IPv4 literal and therefore {@code false} is 
     * returned.
     *
     * @see #isValidIpv4Address(java.lang.String) 
     * @param ipAddressStr
     * @return true if argument looks like an IPv4 literal
     *     
     */
    public static boolean looksLikeIpv4Literal(String ipAddressStr) {
        if (ipAddressStr == null || ipAddressStr.isEmpty()) {
            return false;
        }
        if (!(isAsciiDigit(ipAddressStr.charAt(0)))) {
            return false;
        }
        int dotPos = ipAddressStr.indexOf('.');
        if (dotPos > 0 && dotPos < ipAddressStr.length()-1 && dotPos <=3) {
            for(int i = 0; i < dotPos; i++) {
                if (!(isAsciiDigit(ipAddressStr.charAt(i)))) {
                    return false;
                }
            }
            if (!(isAsciiDigit(ipAddressStr.charAt(dotPos+1)))) {
                return false;
            }
        } else {
            // No dot found or nothing after dot or the part before dot
            // it too long
            return false;
        }
        return true;
    }
    
    /**
     * Strips the domain part from a host name. Example: for {@code "foo.bar.com"}
     * then {@code "foo"} will be returned.
     * 
     * <p>
     * The method is safe to use even if the input is an IPv4 literal or IPv6
     * literal. In this case the input will be returned unchanged.
     *
     * @param hostname
     * @return hostname with domain stripped 
     */
    public static String removeDomain(String hostname) {
        if (hostname == null) {
            return hostname;
        }
        if (looksLikeIpv4Literal(hostname)) {
            return hostname;
        }

        int pos = hostname.indexOf('.');
        if (pos == -1) {
            return hostname;
        } else {
            int posColon = hostname.indexOf(':');
            if (posColon >=0 && posColon < pos) { // It is an IPv6 literal with an embedded IPv4 address
                return hostname;
            }
            
            return hostname.substring(0, pos);
        }
    }

    /**
     * Removes loopback addresses from the provided list.
     * @param addresses 
     */
    public static void removeLoopback(List<InetAddress> addresses) {
        if (addresses == null) {
            return;
        }
        Iterator<InetAddress> iterator = addresses.iterator();
        while (iterator.hasNext()) {
            InetAddress a = iterator.next();
            if (a.isLoopbackAddress()) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Sorts a list of IP addresses.
     * 
     * @param addresses IP addresses
     * @param ip4BeforeIp6 if IPv4 addresses are to come before IPv6 addresses 
     *     ({@code true}) or not ({@code false}).
     */
    public static void sortIpAddresses(List<InetAddress> addresses, boolean ip4BeforeIp6) {
        sortIpAddresses0(addresses, ip4BeforeIp6, false);
    }
    
    /**
     * Sorts a list of IP addresses, but only with respect to IPv4 vs IPv6. Addresses
     * within the same class of protocol (e.g. IPv4 addresses) are not sorted.
     * 
     * @param addresses IP addresses
     * @param ip4BeforeIp6 if IPv4 addresses are to come before IPv6 addresses 
     *     ({@code true}) or not ({@code false}).
     */
    public static void sortIpAddressesShallow(List<InetAddress> addresses, boolean ip4BeforeIp6) {
        sortIpAddresses0(addresses, ip4BeforeIp6, true);
    }
    
    private static void sortIpAddresses0(List<InetAddress> addresses, boolean ip4BeforeIp6, boolean shallow) {
        if (addresses != null && (addresses.size() > 1)) {
            addresses.sort(new InetAddressComparator(ip4BeforeIp6, shallow));
        }
    }



    private static class DnsTimeoutTask implements Callable<InetAddress[]> {

        private final String host;

        public DnsTimeoutTask(String host) {
            this.host = host;
        }

        @Override
        public InetAddress[] call() throws UnknownHostException {
            return InetAddress.getAllByName(host);
        }
    }
    
    
    private static class InetAddressComparator implements Comparator<InetAddress> {

        private final boolean ip4BeforeIp6;
        private final boolean shallow;

        public InetAddressComparator(boolean ip4BeforeIp6, boolean shallow) {
            this.ip4BeforeIp6 = ip4BeforeIp6;
            this.shallow = shallow;
        }
        
        @Override
        public int compare(InetAddress a1, InetAddress a2) {
            byte[] bArr1 = a1.getAddress();
            byte[] bArr2 = a2.getAddress();

            if ((a1 instanceof Inet4Address) && (a2 instanceof Inet6Address)) {
                return (ip4BeforeIp6) ? -1 : 1;
            }
            if ((a1 instanceof Inet6Address) && (a2 instanceof Inet4Address)) {
                return (ip4BeforeIp6) ? 1 : -1;
            }
            
            if (bArr1.length != bArr2.length) {
                // according to JDK spec, this shouldn't be possible
                // but we take no risks here. This could happen if one day
                // there are more sub-classes of InetAddress than just 
                // Inet4Address or Inet6Address.
                if (bArr1.length < bArr2.length) {
                    return (ip4BeforeIp6) ? -1 : 1;
                } else {
                    return (ip4BeforeIp6) ? 1 : -1;
                }
            }
            
            if (shallow) {
                return 0;
            }
            
            // Compare byte-by-byte.
            for (int i = 0; i < bArr1.length; i++) {
                int x1 = Byte.toUnsignedInt(bArr1[i]);
                int x2 = Byte.toUnsignedInt(bArr2[i]);
                
                if (x1 == x2) {
                    continue;
                }
                if (x1 < x2) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return 0;
        }
    }
    
    private static boolean isAsciiDigit(char c) {
        // Why doesn't the JDK have a shorthand for this?
        return c >= 48 && c <= 57;
    }

    //
    // hook for tests
    //
    
    Callable<InetAddress[]> createDnsTimeoutTask(String host) {
        return new DnsTimeoutTask(host);
    }

}
