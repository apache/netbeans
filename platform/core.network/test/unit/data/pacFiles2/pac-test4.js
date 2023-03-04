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


// 
// PAC script which tests all helper functions.
// All tests are "negative tests".  If something fails
// the test that fails will be returned in the proxy name.
// If everything works as expected, the value "DIRECT" will be returned.
//

function FindProxyForURL(url, host) {

    alert("This is pac-test4.js");


// 
// isPlainHostName
//

    alert("isPlainHostName(): Doing tests...");
  
    if (!isPlainHostName("somehost"))
        return "PROXY isPlainHostName:1";
    if (isPlainHostName("somehost.dom1.com"))
        return "PROXY isPlainHostName:2";


//
// dnsDomainIs
//
    alert("dnsDomainIs(): Doing tests...");

    if (!dnsDomainIs("www.netscape.com", ".netscape.com"))
        return "PROXY dnsDomainIs:1";
    if (!dnsDomainIs("www.netscape.com", "netscape.com"))
        return "PROXY dnsDomainIs:2";
    if (dnsDomainIs("www.netscape.com", ".com"))
        return "PROXY dnsDomainIs:3";
    if (dnsDomainIs("www.netscape.com", "somethingelse.com"))
        return "PROXY dnsDomainIs:4";
    if (dnsDomainIs("www.netscape.com", ""))
        return "PROXY dnsDomainIs:5";
    if (dnsDomainIs("www.netscape.com", null))
        return "PROXY dnsDomainIs:6";
    
// 
// localHostOrDomainIs
//
    alert("localHostOrDomainIs(): Doing tests...");
    
    if (!localHostOrDomainIs("www.netscape.com", "www.netscape.com"))
        return "PROXY localHostOrDomainIs:1";
    if (!localHostOrDomainIs("www", "www.netscape.com"))
        return "PROXY localHostOrDomainIs:2";
    if (localHostOrDomainIs("www.netscape.com", "www.netscape.com2"))
        return "PROXY localHostOrDomainIs:3";
    if (localHostOrDomainIs("www1.netscape.com", "www2.netscape.com"))
        return "PROXY localHostOrDomainIs:4";
    if (localHostOrDomainIs("www1", "www2.netscape.com"))
        return "PROXY localHostOrDomainIs:5";
    
//
// isResolvable
//
    alert("isResolvable(): Doing tests...");
    
    if (!isResolvable("localhost"))
        return "PROXY isResolvable:1";
    if (!isResolvable("www.google.com"))   // will only work if we have access to Internet during test
        return "PROXY isResolvable:2";
    if (isResolvable("gsd4hgbnw5xa.kd9greey934.kod82r"))   
        return "PROXY isResolvable:3";
    
//
// dnsResolve
//
    alert("dnsResolve(): Doing tests...");
    
    if (!("127.0.0.1" === dnsResolve("localhost")))
        return "PROXY dnsResolve:1";
    if (!("8.8.8.8" === dnsResolve("google-public-dns-a.google.com"))) // will only work if we have access to Internet
        return "PROXY dnsResolve:2";
    
//
// myIpAddress
//
    alert("myIpAddress(): Doing tests...");
    
    var myIp = myIpAddress();
    if ("127.0.0.1" === myIp)
        return "PROXY myIpAddress:1";
    
//
// isInNet
//
    alert("isInNet(): Doing tests...");
    
    if (!isInNet("localhost", "127.0.0.1", "255.255.255.255"))
        return "PROXY isInNet:1";
    if (!isInNet("google-public-dns-a.google.com", "8.8.8.8", "255.255.255.255"))
        return "PROXY isInNet:2";
    if (isInNet("192.168.1.3", "192.168.1.1", "255.255.255.255"))
        return "PROXY isInNet:3";
    if (!isInNet("192.168.1.3", "192.168.1.1", "255.255.255.0"))
        return "PROXY isInNet:4";
    if (!isInNet("192.168.1.1", "192.168.3.1", "255.255.0.255"))
        return "PROXY isInNet:5";
    if (!isInNet("10.10.10.10", "12.12.12.12", "0.0.0.0"))
        return "PROXY isInNet:6";
    if (isInNet("10.10.10.10", "12.12.12.12", "0.0.255.0"))
        return "PROXY isInNet:7";

//            
// dnsDomainLevels
//
    alert("dnsDomainLevels(): Doing tests...");

    if (!(2 === dnsDomainLevels("www.netscape.com")))
        return "PROXY dnsDomainLevels:1";
    if (!(0 === dnsDomainLevels("www")))
        return "PROXY dnsDomainLevels:2";
    if (!(1 === dnsDomainLevels("www.")))
        return "PROXY dnsDomainLevels:3";


//
// shExpMatch
// 
    alert("shExpMatch(): Doing tests...");
    
    if (!shExpMatch("www.netscape.com", "*netscape*"))
        return "PROXY shExpMatch:1";
    if (!shExpMatch("www.netscape.com", "*net*"))
        return "PROXY shExpMatch:2";
    if (shExpMatch("www.netscape.com", "*google*"))
        return "PROXY shExpMatch:3";
    if (!shExpMatch("www.netscape.com", "www*"))
        return "PROXY shExpMatch:4";

//
// weekdayRange
//
    alert("weekdayRange(): Doing tests...");
    
    if (!weekdayRange("MON", "SUN", "GMT"))
        return "PROXY weekdayRange:1";
    if (!weekdayRange("MON", "SUN", null))
        return "PROXY weekdayRange:2";

//
// dateRange
//
    // Difficult to test from JavaScript side because it will depend on 
    // date when test is executed.
    // We test if current date is between some values it will always be
    // in between, which isn't much of a test!
    
    alert("dateRange(): Doing tests...");
    
    if (!(dateRange(1998, 2199)))
        return "PROXY dateRange:1";
    if (!(dateRange(1998, 2199, "GMT")))
        return "PROXY dateRange:2";
    if (!(dateRange(1, 31)))
        return "PROXY dateRange:3";
    if (!(dateRange(1, 31, "GMT")))
        return "PROXY dateRange:4";
    if (!(dateRange("JAN", "DEC")))
        return "PROXY dateRange:5";
    if (!(dateRange("JAN", "DEC", "GMT")))
        return "PROXY dateRange:6";
    if (!(dateRange(1, "JAN", 31, "DEC")))
        return "PROXY dateRange:7";
    if (!(dateRange(1, "JAN", 31, "DEC", "GMT")))
        return "PROXY dateRange:8";
    if (!(dateRange(1, "JAN", 1998, 31, "DEC", 2199)))
        return "PROXY dateRange:9";
    if (!(dateRange(1, "JAN", 1998, 31, "DEC", 2199, "GMT")))
        return "PROXY dateRange:10";

//
// timeRange
//
    alert("timeRange(): Doing tests...");
    // Difficult to test from JavaScript side because it will depend on 
    // time when test is executed.
    // We test if current time is between 00:00:00 and 23:59:59, which
    // isn't much of a test!
    
    if (!(timeRange(0, 0, 0, 23, 59, 59)))
        return "PROXY timeRange:1";
    if (!(timeRange(0, 0, 0, 23, 59, 59, "GMT")))
        return "PROXY timeRange:2";
    if (!(timeRange(0, 0, 23, 59)))
        return "PROXY timeRange:3";
    if (!(timeRange(0, 0, 23, 59, "GMT")))
        return "PROXY timeRange:4";


//
// isResolvableEx
//
    alert("isResolvableEx(): Doing tests...");

    if (!(isResolvableEx("localhost")))
        return "PROXY isResolvableEx:1";

//
// dnsResolveEx
//
    alert("dnsResolveEx(): Doing tests...");

    if (!(dnsResolveEx("dfg3qyuaz.g4yst5.gfw58703sd") === ""))
        return "PROXY dnsResolveEx:1";

//
// myIpAddressEx
//
    alert("myIpAddressEx(): Doing tests...");

    var myIpEx = myIpAddressEx();
    if ((myIpEx === "127.0.0.1") || (myIpEx === "0:0:0:0:0:0:0:1") || (myIpEx === "::1"))
        return "PROXY myIpAddressEx:1";


    alert("pac-test4.js:  All tests passed");

    return "DIRECT";

}