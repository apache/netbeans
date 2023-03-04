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
 * Netscape's original PAC script helper functions. In their original
 * document, Netscape defined 12 helper functions which should be available
 * to the JavaScript PAC. The first browser to implement these were - unsurpringsly -
 * the Netscape Navigator.
 * 
 * <p>
 * The JavaScript helper functions are defined in Java terms by this interface.
 * The Java methods are named exactly as their JavaScript counterparts.
 * 
 * <p>
 * The documentation on each of the methods in the interface is more
 * or less copied verbatim from Netscape's original documentation. 
 * 
 * <p>
 * Beware that at the time when Netscape defined these functions IPv6 
 * wasn't on the radar. Therefore the concensus among browser makers
 * is that IPv4 is implied in all these functions.
 * 
 * @author lbruun
 */
public interface PacHelperMethodsNetscape {

    /**
     * True if there is no domain name in the hostname (no dots). 
     * 
     * <p>
     * Examples:
     * <pre>
     *   isPlainHostName("www")
     *     is true.
     *   isPlainHostName("www.netscape.com")
     *     is false.
     * </pre>
     * @param host host name
     * @return 
     */
    public boolean isPlainHostName(String host);

    /**
     * Returns true if the domain of hostname matches. 
     * 
     * <p>
     * Examples:
     * <pre>
     *   dnsDomainIs("www.netscape.com", ".netscape.com")
     *     is true.
     *   dnsDomainIs("www", ".netscape.com")
     *     is false.
     *   dnsDomainIs("www.mcom.com", ".netscape.com")
     *     is false.
     * </pre>
     * @param host the host name from the URL.
     * @param domain the domain name to test the host name against.
     * @return 
     */
    public boolean dnsDomainIs(String host, String domain);

    /**
     * Is true if the hostname matches exactly the specified hostname, or if
     * there is no domain name part in the hostname, but the unqualified
     * hostname matches.
     *
     * <p>
     * Examples of usage from JavaScript:
     * <pre>
     *   localHostOrDomainIs("www.netscape.com", "www.netscape.com")
     *     is true (exact match).
     *   localHostOrDomainIs("www", "www.netscape.com")
     *     is true (hostname match, domain not specified).
     *   localHostOrDomainIs("www.mcom.com", "www.netscape.com")
     *     is false (domain name mismatch).
     *   localHostOrDomainIs("home.netscape.com", "www.netscape.com")
     *     is false (hostname mismatch).
     * </pre>
     * 
     * @param host the hostname from the URL.
     * @param hostdom fully qualified hostname to match against.
     * @return 
     */
    public boolean localHostOrDomainIs(String host, String hostdom);

    /**
     * Tries to resolve the hostname. Returns true if succeeds. Strictly
     * speaking - and in the spirit of the original Netscape specification -
     * this method should only return {@code true} if the argument can be
     * resolved into an IPv4 address.
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
    public boolean isResolvable(String host);

    /**
     * Resolves the given DNS hostname into an IPv4 address, 
     * and returns it in the dot separated format as a string. 
     * 
     * <p>
     * Example of usage from JavaScript:
     * <pre>
     *   dnsResolve("home.netscape.com")
     *     returns the string "198.95.249.79". 
     * </pre>
     * @param host
     * @return 
     */
    public String dnsResolve(String host);

    /**
     * Returns the IPv4 address of the host that the application is running on, 
     * as a string in the dot-separated integer format.
     * 
     * <p>
     * Example of usage from JavaScript:
     * <pre>
     *   myIpAddress()
     *     would return the string "198.95.249.79" if you were running the 
     *     the application on that host. 
     * </pre>
     * 
     * @return IPv4 address in textual form
     */
    public String myIpAddress();

    /**
     * True if the IP address of the host matches the specified IP address
     * pattern.
     * 
     * <p>
     * Examples of usage from JavaScript:
     * <pre>
     *   isInNet(host, "198.95.249.79", "255.255.255.255")
     *     is true if the IP address of host matches exactly 198.95.249.79.
     *   isInNet(host, "198.95.0.0", "255.255.0.0")
     *     is true if the IP address of the host matches 198.95.&#42;.&#42;. 
     * </pre>
     *
     * @param host a DNS hostname or IPv4 address. If a hostname is passed, it
     * will be resolved into an IP address by this function.
     * @param pattern an IPv4 address pattern in the dot-separated format.
     * @param mask mask for the IP address pattern informing which parts of the
     * IP address should be matched against. 0 means ignore, 255 means match.
     * @return
     */
    public boolean isInNet(String host, String pattern, String mask);

    /**
     * Returns the number of DNS domain levels (number of dots) 
     * in the hostname. 
     * 
     * <p>
     * Examples of usage from JavaScript:
     * <pre>
     *   dnsDomainLevels("www")
     *     returns 0.
     *   dnsDomainLevels("www.netscape.com")
     *     returns 2. 
     * </pre>
     * 
     * @param host hostname
     * @return 
     */
    public int dnsDomainLevels(String host);

    /**
     * Returns true if the string matches the specified shell expression.
     * Note that the argument is a <i>shell expression</i>, not a
     * regular expression. 
     * 
     * <p>
     * Examples of usage from JavaScript:
     * <pre>
     *   shExpMatch("http://home.netscape.com/people/ari/index.html", "&#42;/ari/&#42;")
     *     is true.
     *   shExpMatch("http://home.netscape.com/people/montulli/index.html", "&#42;/ari/&#42;")
     *     is false.
     * </pre>
     * @param str
     * @param shexp shell expression
     * @return 
     */
    public boolean shExpMatch(String str, String shexp);

    /**
     * Tests if current day is within a day-of-week range.
     * 
     * <p>
     * There are several forms of this method from JavaScript:
     * <pre>
     *    weekdayRange(wd1)
     *    weekdayRange(wd1, gmt)
     *    weekdayRange(wd1, wd2)
     *    weekdayRange(wd1, wd2, gmt)
     * </pre>
     * 
     * Parameters:
     * <ul>
     *    <li>{@code wd1} and {@code wd2} are weekday specifications. </li>
     *    <li>{@code gmt} is either the string "GMT", which makes time comparison 
     *         occur in GMT timezone; or if not present, times are taken to 
     *         be in the local timezone. If this parameter exists it 
     *         must always be the last parameter.
     *    </li>
     * </ul>     
     * 
     * <p>If only {@code wd1} is present, the function yields a true value on the
     * weekday that the parameter represents. If both {@code wd1} and
     * {@code wd2} are specified, the condition is true if the current weekday
     * is in between those two weekdays. Bounds are inclusive.
     *
     * <p>The weekday abbreviations used in {@code wd1} and {@code wd2} must be 
     * one of the following:
     * <pre>
     *    MON  TUE  WED  THU  FRI  SAT  SUN
     * </pre>
     * 
     * <p>
     * Examples of usage from JavaScript:
     * <pre>
     *   weekdayRange("MON", "FRI")
     *     Returns true Monday through Friday (local time zone).
     *   weekdayRange("MON", "FRI", "GMT")
     *     Returns true Monday through Friday, in Greenwich Mean Time.
     *   weekdayRange("SAT")
     *     Returns true on Saturdays, local time.
     *   weekdayRange("SAT", "GMT")
     *     Returns true on Saturdays, in Greenwich Mean Time.
     *   weekdayRange("FRI", "MON") 
     *     Returns true Friday through Monday (the order is important)
     *   weekdayRange("WED", "TUE") 
     *     Returns true always, all 7 days a week (although an alien way of specifying it)
     * </pre>
     * <br>
     * @param args up to max 3 arguments
     * @return 
     * @see org.netbeans.network.proxy.pac.datetime.PacUtilsDateTime#WEEKDAY_NAMES
     */
    public boolean weekdayRange(Object... args);

    /**
     * Tests if current date is within a date range.
     * 
     * <p>There are several forms of this method from JavaScript:
     * <pre>
     *   dateRange(day)
     *   dateRange(day1, day2)
     *   dateRange(mon)
     *   dateRange(month1, month2)
     *   dateRange(year)
     *   dateRange(year1, year2)
     *   dateRange(day1, month1, day2, month2)
     *   dateRange(month1, year1, month2, year2)
     *   dateRange(day1, month1, year1, day2, month2, year2)
     *   dateRange(day1, month1, year1, day2, month2, year2, gmt)
     * </pre>
     * Even if not shown above, the {@code gmt} parameter can always be
     * added as an (optional) last parameter.
     * 
     * <p>
     * <ul>
     *    <li>{@code day} is the day of month between 1 and 31 (as an integer).
     *    </li>
     *    <li>{@code month} is one of the month strings: {@code JAN FEB MAR APR MAY JUN JUL AUG SEP OCT NOV DEC}
     *    </li>
     *    <li>{@code year} is the full year number with 4 digits, for example 1995. (as an integer)
     *    <li>{@code gmt} is either the string "GMT", which makes time comparison 
     *         occur in GMT timezone; or if not present, times are taken to 
     *         be in the local timezone. If this parameter exists it 
     *         must always be the last parameter.
     *    </li>
     * </ul>
     * <br>
     * <p>
     * Examples of usage from JavaScript:
     * <pre>
     *   dateRange(1)
     *     true on the first day of each month, local timezone.
     *   dateRange(1, "GMT")
     *     true on the first day of each month, GMT timezone.
     *   dateRange(1, 15)
     *     true on the first half of each month.
     *   dateRange(24, "DEC")
     *     true on 24th of December each year.
     *   dateRange(24, "DEC", 1995)
     *     true on 24th of December, 1995.
     *   dateRange("JAN", "MAR")
     *     true on the first quarter of the year.
     *   dateRange(1, "JUN", 15, "AUG")
     *     true from June 1st until August 15th, each year (including June 1st and August 15th).
     *   dateRange(1, "JUN", 15, 1995, "AUG", 1995)
     *     true from June 1st, 1995, until August 15th, same year.
     *   dateRange("OCT", 1995, "MAR", 1996)
     *     true from October 1995 until March 1996 (including the entire month of October 1995 and March 1996).
     *   dateRange(1995)
     *     true during the entire year 1995.
     *   dateRange(1995, 1997)
     *     true from beginning of year 1995 until the end of year 1997.
     * </pre>
     * 
     * @see org.netbeans.network.proxy.pac.datetime.PacUtilsDateTime#MONTH_NAMES
     * @param args
     * @return 
     */
    public boolean dateRange(Object... args);

    /**
     * Tests if current time is within a time range.
     * 
     * <p>There are several forms of this method from JavaScript:
     * <pre>
     *   timeRange(hour)
     *   timeRange(hour1, hour2)
     *   timeRange(hour1, min1, hour2, min2)
     *   timeRange(hour1, min1, sec1, hour2, min2, sec2)
     *   timeRange(hour1, min1, sec1, hour2, min2, sec2, gmt)
     * </pre>
     * Even if not shown above, the {@code gmt} parameter can always be
     * added as an (optional) last parameter.
     * <p>
     * <ul>
     *    <li>{@code hour} is the hour from 0 to 23. (0 is midnight, 23 is 11 pm.)
     *    </li>
     *    <li>{@code min} minutes from 0 to 59.
     *    </li>
     *    <li>{@code sec} seconds from 0 to 59.
     *    <li>{@code gmt} is either the string "GMT", which makes time comparison 
     *         occur in GMT timezone; or if not present, times are taken to 
     *         be in the local timezone. If this parameter exists it 
     *         must always be the last parameter.
     *    </li>
     * </ul>
     * <br>
     * <p>
     * Examples of usage from JavaScript:
     * <pre>
     *   timeRange(12, 13)
     *     This statement is true from noon to 1:00 p.m.
     *   timeRange(12, "GMT")
     *      This statement is true noon to 12:59 p.m. GMT.
     *   timeRange(9, 17)
     *     This statement is true from 9:00 a.m. to 5:00 p.m.
     *   timeRange(0, 0, 0, 0, 0, 30) 
     *     true between midnight and 30 seconds past midnight.
     * </pre>
     * 
     * @param args anywhere between 1 and 7 arguments
     * @return 
     */
    public boolean timeRange(Object... args);

}
