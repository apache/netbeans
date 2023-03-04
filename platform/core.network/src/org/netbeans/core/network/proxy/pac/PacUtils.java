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

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.netbeans.core.network.utils.IpAddressUtils;
import org.netbeans.core.network.utils.SimpleObjCache;

/**
 * Methods and constants useful in PAC script evaluation.
 * 
 * @see org.netbeans.network.proxy.pac.datetime.PacUtilsDateTime
 * 
 * @author lbruun
 */
public class PacUtils {

    /**
     * Size of the cache used for precompiled GLOBs.
     */
    public static final int PRECOMPILED_GLOB_CACHE_MAX_ITEMS = 10;
    private static final SimpleObjCache<String, Pattern> PRECOMPILED_GLOB_CACHE
            = new SimpleObjCache<>(PRECOMPILED_GLOB_CACHE_MAX_ITEMS);


    

    /**
     * Translate a GLOB pattern into a RegExp pattern. GLOB patterns originate
     * from Unix hosts where they are primarily used for file pattern matching.
     * In the original PAC specification from Netscape a GLOB pattern is
     * referred to as a 'shell expression'.
     *
     * <p>
     * This method supports all GLOB wildcards, such as
     * <table border="0" style="order-collapse: separate;border-spacing: 50px 0;" summary="">
     * <tr align="left"><td>{@code *}</td><td>matches any number of any
     * characters including none</td>
     * <tr align="left"><td>{@code ?}</td><td>matches any single character</td>
     * <tr align="left"><td>{@code [abc]}</td><td>matches one character given in
     * the bracket</td>
     * <tr align="left"><td>{@code [a-z]}</td><td>matches one character from the
     * range given in the bracket</td>
     * <tr align="left"><td>{@code [!abc]}</td><td>matches one character
     * <i>not</i> given in the bracket</td>
     * <tr align="left"><td>{@code [!a-z]}</td><td>matches one character
     * <i>not</i> from the range given in the bracket</td>
     * </table>
     *
     * <p>
     * A small cache is used so that if a glob pattern has already been
     * translated previously, the result from the cache will be returned.
     *
     * @param glob
     * @return
     */
    public static Pattern createRegexPatternFromGlob(String glob) {

        // First try the cache
        Pattern pattern = PRECOMPILED_GLOB_CACHE.get(glob);
        if (pattern != null) {
            return pattern;
        }

        StringBuilder out = new StringBuilder();
        out.append("^");
        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*':
                    out.append(".*?");
                    break;
                case '?':
                    out.append(".{1}");
                    break;
                case '.':
                    out.append("\\.");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '!':
                    if (i > 0 && glob.charAt(i - 1) == '[') {
                        out.append('^');
                    } else {
                        out.append(c);
                    }
                    break;
                default:
                    out.append(c);
            }
        }
        out.append("$");
        pattern = Pattern.compile(out.toString());
        PRECOMPILED_GLOB_CACHE.put(glob, pattern);
        return pattern;
    }


    
    /**
     * Converts list into semi-colon separated string where each element 
     * is represented by the result of the {@code fn} function.
     * 
     * @param <T>
     * @param list list of objects
     * @param fn function which returns string
     * @return string with elements separated by semi-colon
     */
    public static <T> String toSemiColonList(List<T> list, Function<T,String> fn) {
        return list.stream()
                .map(i -> fn.apply(i))
                .collect(Collectors.joining(";"));
    }
    
    /**
     * Converts list into semi-colon separated string where each element 
     * is represented by the result of {@link Object#toString()}.
     * 
     * @see #toSemiColonList(java.util.List, java.util.function.Function) 
     * @param <T>
     * @param list list of objects
     * @return string with elements separated by semi-colon
     */
    public static <T> String toSemiColonList(List<T> list) {
        return toSemiColonList(list, Object::toString);
    }
    
    /**
     * Converts a list of {@code InetAddress} into a semi-colon
     * separated string. Each address is represented by the result
     * of {@link InetAddress#getHostAddress()}.
     * 
     * @see #toSemiColonList(java.util.List, java.util.function.Function) 
     * @param addresses
     * @return semi-colon separated string of addresses in literal form
     */
    public static String toSemiColonListInetAddress(InetAddress[] addresses) {
        return toSemiColonList(Arrays.asList(addresses), InetAddress::getHostAddress);
    }

    /**
     * Converts an array of {@code InetAddress} into a semi-colon
     * separated string. Each address is represented by the result
     * of {@link InetAddress#getHostAddress()}.
     * 
     * @see #toSemiColonList(java.util.List, java.util.function.Function) 
     * @param addresses
     * @return semi-colon separated string of addresses in literal form
     */
    public static String toSemiColonListInetAddress(List<InetAddress> addresses) {
        return toSemiColonList(addresses, InetAddress::getHostAddress);
    }

    
    /**
     * Checks if an IP address matches a given CIDR pattern.
     * 
     * The pattern must use the format:
     * <pre>
     *     ipLiteral/bitField
     * </pre>
     * 
     * Examples of valid patterns:
     * <pre>
     *   198.95.249.79/32
     *   198.95.0.0/16
     *   3ffe:8311:ffff/48
     * </pre>
     * 
     * <p>
     * For IPv6 the {@code ipLiteral} is allowed to be incomplete at the end. If
     * not complete, then a suffix of "::" is appended to the literal, e.g. the
     * method will translate {@code "3ffe:8311:ffff/48"} to
     * {@code "3ffe:8311:ffff::/48"}.
     *
     * <p>
     * A number of validation checks are carried out on the {@code ipPrefix}
     * argument and {@code false} will be returned if these checks fails:
     * <ul>
     *    <li>If {@code ipAddress} is IPv4 then {@code bitField}
     *        must be between 8 and 32. 
     *    </li>
     *    <li>If {@code ipAddress} is IPv6 then {@code bitField} 
     *        must be between 8 and 128. 
     *    </li>
     *    <li>The {@code ipLiteral} value must be a valid IPv4 literal or IPv6 literal.
     *    </li>
     *    <li>The IP protocol type of {@code ipLiteral} value must match the IP protocol
     *        type of {@code ipAddress}.
     *    </li>
     * </ul>
     * 
     * <p>
     * Note: The method was developed for the purpose of supporting the 
     * {@link PacHelperMethodsMicrosoft#isInNetEx(java.lang.String, java.lang.String) 
     * Microsoft isInNetEx()} extension to PAC scripting, but the method may
     * have an appeal broader than this particular use case.
     * 
     * <br><br>
     * 
     * @param ipAddress address
     * @param ipPrefix pattern
     * @return true if the address, {@code ipAddress}, match the pattern, {@code ipPrefix}.
     */
    public static boolean ipPrefixMatch(InetAddress ipAddress, String ipPrefix) {
        if (ipPrefix.indexOf('/') == -1) {
            return false;
        }
        String[] parts = ipPrefix.trim().split("\\/");

        int bitField = 0;
        try {
            bitField = Integer.parseInt(parts[1].trim());
            if (!(bitField >= 0 && bitField <= 128)) {
                return false;
            }
            if (ipAddress instanceof Inet4Address) {
                if (bitField > 32) {
                    return false;
                }
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        String ipPatternStr = parts[0].trim();

        InetAddress ipPattern = null;
        if (ipAddress instanceof Inet4Address) {
            if (IpAddressUtils.looksLikeIpv4Literal(ipPatternStr)) {
                try {
                    ipPattern = InetAddress.getByName(ipPatternStr);
                } catch (UnknownHostException ex) {
                    return false;
                }
            }
        }
        if (ipAddress instanceof Inet6Address) {
            String ipPatternStrC = correctIPv6Str(ipPatternStr);
            if (IpAddressUtils.looksLikeIpv6Literal(ipPatternStrC)) {
                try {
                    ipPattern = InetAddress.getByName(ipPatternStrC);
                } catch (UnknownHostException ex) {
                    return false;
                }
            }
        }
        if (ipPattern == null) {
            return false;
        }
        if (!ipAddress.getClass().equals(ipPattern.getClass())) {
            return false;
        }
        BigInteger mask = BigInteger.valueOf(-1).shiftLeft((ipPattern.getAddress().length * 8) - bitField); // mask = -1<<32-bits  or  mask = -1<<128-bits
        BigInteger subnetMask = new BigInteger(ipPattern.getAddress()).and(mask);

        return (new BigInteger(ipAddress.getAddress()))
                .and(mask).equals(subnetMask);
    }
    
    /**
     * Completes an IPv6 literal address if it is incomplete at the end. 
     * This is done by appending "::" if needed.
     * @param s
     * @return 
     */
    private static String correctIPv6Str(String s) {
        int counter = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ':') {
                counter++;
                if (i > 0) {
                    if (s.charAt(i - 1) == ':') {
                        return s;
                    }
                }
            }
        }
        if (counter != 7) {
            return s + "::";
        } else {
            return s;
        }
    }
    
    /**
     * Cleans a URI into a format suitable for passing to the PAC script.
     * (meaning suitable for passing as {@code url} argument to 
     * {@code FindProxyForURL(url, host)} or {@code FindProxyForURLEx(url, host)} 
     * functions).
     * <p>
     * Because a PAC script is downloaded from a potentially malicious source it
     * may contain harmful code. Therefore, the amount of information passed to
     * the script should be limited to what is strictly necessary for the script
     * to make decisions about choice of proxy. Anything in the URL which can
     * potentially identity the user or which may contain session specific
     * information should be removed before passing to script.
     * 
     * <p>
     * The following is removed:
     * <ul>
     *   <li><i>{@code user-info}</i></li>
     *   <li><i>{@code path}</i> and everything that follows after</li>
     * </ul>
     * 
     * <p>
     * Example:
     * <pre>
     *    https://mary@netbeans.apache.org:8081/path/to/something?x1=Christmas&amp;user=unknown
     * becomes
     *    https://netbeans.apache.org:8081/
     * </pre>
     * 
     * <p>
     * Note that the majority of PAC scripts out there do not make use of the
     * {@code url} parameter at all. Instead they only use the {@code host}
     * parameter. The stripping of information means that the {@code url}
     * parameter only has two pieces of information that the {@code host} 
     * parameter doesn't have: protocol and port number.
     * <br>
     *
     * @param uri URL to be cleansed
     * @return stripped URL string
     */
    public static String toStrippedURLStr(URI uri) {
        
        String portStr = (uri.getPort() == -1) ? "" : ":" + uri.getPort();
        return
                uri.getScheme()
                + "://"
                + uri.getHost()
                + portStr
                + "/";  // Chrome seems to always append the slash so we do it too
    }
}
