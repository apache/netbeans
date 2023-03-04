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
package org.netbeans.core.network.proxy.pac;

/**
 * Microsoft's extensions to complement Netscape's original PAC script 
 * helper functions. (as defined in {@link PacHelperMethodsNetscape}).
 * 
 * <p>
 * The JavaScript helper functions are defined in Java terms by this interface.
 * The Java methods are named exactly as their JavaScript counterparts.
 * 
 * <p>
 * Microsoft has added these methods in recognition that the original
 * Netscape functions were defined at a time before IPv6 became widely used
 * or was even finalized. The original Netscape functions simply didn't take
 * IPv6 into account and should therefore be interpreted in an IPv4-only context.
 * Support for these new functions were first introduced in Internet Explorer v7.
 * Other browsers, e.g. Chrome, have since then implemented them too.
 * 
 * <p>
 * Microsoft also defined a new entry-point function, {@code FindProxyForURLEx(url, host)},
 * which should replace the legacy {@code FindProxyForURL(url, host)} function. 
 * Microsoft only makes the new helper functions available from this new entry
 * function. However, Chrome does it differently in that the new helper
 * functions are indeed available, but there's no support for the new entry function, 
 * {@code FindProxyForURLEx}, meaning the new helper functions are
 * available from the old entry method, {@code FindProxyForURL}.
 * An implementation of {@link PacScriptEvaluator} should strive for maximum
 * compatibility meaning all helper functions should be available regardless
 * of entry point.
 * 
 * @author lbruun
 */
public interface PacHelperMethodsMicrosoft {

    /**
     * Tries to resolve the hostname. Returns true if succeeds. Unlike the
     * original Netscape function, {@code isResolvable()}, this function supports
     * both IPv4 and IPv6 addresses, meaning it should return {@code true} if the 
     * input can be resolved to any type of IP address.
     * 
     * <p>
     * Examples of usage from JavaScript:
     * <pre>
     *   isResolvable("www.netscape.com")
     *     is true (unless DNS fails to resolve it due to a firewall or some other reason).
     *   isResolvable("bogus.domain.foobar")
     *     is false. 
     * </pre>
     * @param host
     * @return 
     */
    public boolean isResolvableEx(String host);

    /**
     * Resolves the given host name into its address or addresses.
     * 
     * <p>
     * Example of usage from JavaScript:
     * <pre>
     *   dnsResolveEx("testmachine1");
     *      returns the string "2001:4898:28:7:982d:a3b3:97ad:7dd0;192.168.1.99"
     * </pre>
     * @param host
     * @return a semi-colon delimited string containing (potentially both)
     *    IPv6 and IPv4 addresses or an empty string if host is not resolvable
     */
    public String dnsResolveEx(String host);

    /**
     * Returns the IP addresses the local host is known by.
     * 
     * <p>
     * Example of usage from JavaScript:
     * <pre>
     *   myIpAddressEx()
     *     would return the string "2001:4898:28:7:982d:a3b3:97ad:7dd0;198.95.249.79" 
     *     if you were running the application on that host. 
     * </pre>
     * 
     * @return a semi-colon delimited string containing (potentially both)
     *    IPv6 and IPv4 addresses.
     */
    public String myIpAddressEx();

    /**
     * Sorts a list of IP addresses in ascending order. If both IPv6 and IPv4 
     * addresses are passed as input to this function, then the sorted IPv6
     * addresses are followed by the sorted IPv4 addresses.
     *
     * <p>
     * Example of usage from JavaScript:
     * <pre>
     *   sortIpAddressList("10.2.3.9;2001:4898:28:3:201:2ff:feea:fc14;::1;127.0.0.1;::9");
     *           returns   "::1;::9;2001:4898:28:3:201:2ff:feea:fc14;10.2.3.9;127.0.0.1";
     * </pre>
     * 
     * <p>
     * NOTE: Microsoft Internet Explorer 11 (tested on 11.608.15063.0) doesn't
     * give correct results on this function. For example the result of
     * {@code sortIpAddressList("10.2.3.9;2001:4898:28:3:201:2ff:feea:fc14;::1;127.0.0.1;::9")}
     * is
     * {@code "[::1];10.2.3.9;127.0.0.1;[2001:4898:28:3:201:2ff:feea:fc14];[::9]"}
     * which is incorrect in more ways than one. In contrast, Chrome does it as
     * expected and as in the example above. In summary the following problems
     * are seen with the IE 11 implementation of this function:
     * <ul>
     *   <li>IPv4 addresses are not in sort order <i>after</i> after
     *       the IPv6 addresses as the example shows. The documentation
     *       clearly states the opposite.
     *       </li>
     *   <li>IPv4-mapped IPv6 address (e.g. {@code fe80::5efe:157.59.139.22} 
     *       doesn't seem to be working. This is strange as it is used in the 
     *       example in <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/gg308482(v=vs.85).aspx">Microsoft's own documentation</a>. 
     *       Such an IP literal seems to throw an error which means
     *       the PAC script will not return a result, which means IE will choose 
     *       its default option, namely "DIRECT".
     *       </li>
     *   <li>IPv6 literals in the output will be enclosed in [], e.g. 
     *       {@code "1080:0:0:0:8:800:200c:417a"} in the input will become 
     *       {@code "[1080:0:0:0:8:800:200c:417a]"} in the output.
     *       This too is in contrast to the documentation.
     *       </li>
     * </ul>
     * <br>
     * 
     * @param ipAddressList a semi-colon delimited string containing IP addresses
     * @return a list of sorted semi-colon delimited IP addresses or an empty 
     *     string if unable to sort the IP address list.
     */
    public String sortIpAddressList(String ipAddressList);
    
    /**
     * Gets the version of the PAC script processing engine.
     * 
     * <p>
     * Microsoft added this function to allow IT administrators to update 
     * their PAC scripts to use different versions of the PAC processing engine 
     * without causing breaks to their existing deployment. 
     * For example, if Microsoft added a function to the 2.0 version of the the
     * Microsoft PAC processing engine, then administrators can check the 
     * version before attempting to call that function. This allows their 
     * script to work with client running versions 1.0 and 2.0 of the engine.
     * 
     * <p>
     * Note to implementers: Currently this function should simply return "1.0"
     * in order retain compatibility with Microsoft world.
     * 
     * @return 
     */
    public String getClientVersion();
    
    /**
     * True if the IP address of the host matches the specified IP address
     * pattern.
     * 
     * <p> 
     * Microsoft's documentation is vague / contradictory on what the pattern
     * argument, {@code ipPrefix}, is, meaning is it a <i>list</i> of patterns 
     * or is it a singular pattern?. We err on the side of caution and decide that
     * the {@code ipPrefix} is a semi-colon separated list of patterns and that
     * the method must return {@code true} if the supplied {@code ipAddress}
     * matches <i>any</i> of the patterns in the list.
     * 
     * <p> 
     * Similarly, the interpretation of the first argument, {@code host},
     * is open for discussion. In the sibling function, {@code isInNet}, it
     * is clearly stated that this argument can be either a host name or a 
     * literal IP address. But Microsoft's documentation on {@code isInNetEx}
     * limits the argument to only being a literal IP address. Again we err
     * on the side of caution and decide that this argument can be either. If
     * it is a host name then the method must do a name service lookup first
     * (to convert to IP), before the actual comparison can begin.
     * 
     * 
     * <p>
     * Examples of usage from JavaScript:
     * <pre>
     *   isInNetEx(ipAddress, "198.95.249.79/32");
     *     true if the IP address of host matches exactly 198.95.249.79
     *   isInNetEx(ipAddress, "198.95.0.0/16");
     *     true if the IP address of the host matches 198.95.&#42;.&#42;
     *   isInNetEx(ipAddress, "3ffe:8311:ffff/48");
     *     true if the IP address of the host matches 3ffe:8311:fff:&#42;:&#42;:&#42;:&#42;:&#42;
     *   isInNetEx(ipAddress, "198.95.249.0/24;198.122.0.0/16");
     *     true if the IP address of host matches 198.95.249.&#42; or matches 198.122.&#42;.&#42;
     * </pre>
     *
     * @param host a string containing an IPv6/IPv4 addresses or a host name.
     * @param ipPrefix a string containing semi-colon delimited IP prefixes with 
     *    top n bits specified in the bit field 
     *    (i.e. 3ffe:8311:ffff::/48 or 123.112.0.0/16).
     * @return {@code true} if the IP address matches the specified IP address
     *    pattern ({code ipPrefix}). Also returns {@code false} if the prefix 
     *    is not in the correct format or if addresses and prefixes of different 
     *    types are used in the comparison (i.e. IPv4 prefix and an IPv6 address).
     */
    public boolean isInNetEx(String host, String ipPrefix);

}
