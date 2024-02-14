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
package org.netbeans.core.network.proxy.pac.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.core.network.utils.IpAddressUtils;
import org.netbeans.core.network.utils.IpAddressUtils.IpTypePreference;
import org.netbeans.core.network.utils.LocalAddressUtils;
import org.netbeans.core.network.proxy.pac.PacHelperMethods;
import org.netbeans.core.network.proxy.pac.PacUtils;
import org.netbeans.core.network.proxy.pac.datetime.PacUtilsDateTime;
import org.openide.util.lookup.ServiceProvider;

/**
 * NetBeans default implementation of a the PAC 'helper functions'.
 * 
 * <p>
 * This implementation aims to be as complete and compatible as possible. It 
 * implements both the original specification from Netscape, plus the additions
 * from Microsoft.
 * 
 * <ul>
 *    <li>Complete. Implements all methods from original Netscape specification
 *        as well as all IPv6 aware additions from Microsoft.</li>
 *    <li>Compatible. Aims for maximum compatibility with all browser 
 *         implementations and aims to never fail and never stall.</li>
 * </ul>
 * 
 * @author lbruun
 */
@ServiceProvider(service=PacHelperMethods.class)
public class NbPacHelperMethods extends PacHelperMethods {

    private static final Logger LOGGER = Logger.getLogger(NbPacHelperMethods.class.getName());

    /** 
     * Timeout (milliseconds) used for name service lookups.
     */    
    public static final int DNS_TIMEOUT_MS = 4000;

    
    // *************************************************************
    //  Official helper functions.
    // 
    //  These are pure-Java implementations of the JavaScript
    //  helper functions defined in Netscape's original document.
    // *************************************************************
    
    @Override
    public boolean isPlainHostName(String host) {
        return !host.contains(".");
    }

    @Override
    public boolean dnsDomainIs(String host, String domain) {
        int dotPos = host.indexOf(".");
        if (dotPos != -1 && (dotPos < host.length()-1)) {
            if (host.substring(dotPos).equals(domain)) {
                return true;
            }
            if (host.substring(dotPos+1).equals(domain)) {
                return true;
            }
        }
        return false;
    }

    
    @Override
    public boolean localHostOrDomainIs(String host, String hostdom) {
        if (host.equals(hostdom)) {
            return true;
        }
        return host.equals(getDomains(hostdom)[0]);
    }

    @Override
    public boolean isResolvable(String host) {
        try {
            IpAddressUtils.nameResolve(host, DNS_TIMEOUT_MS, IpTypePreference.IPV4_ONLY);
            return true;
        } catch (InterruptedException | UnknownHostException | TimeoutException ex) {
            return false;
        }
    }

    @Override
    public String dnsResolve(String host) {
        try {
            return IpAddressUtils.nameResolve(host, DNS_TIMEOUT_MS, IpTypePreference.IPV4_ONLY)
                    .getHostAddress();

        } catch (InterruptedException | UnknownHostException | TimeoutException ex) {
            // Returning null is what Chrome and Firefox do in this situation
            return null; 
        }
    }

    @Override
    public String myIpAddress() {
        return LocalAddressUtils.getMostLikelyLocalInetAddress(IpTypePreference.IPV4_ONLY).getHostAddress();
    }

    
    @Override
    public boolean isInNet(String host, String pattern, String mask) {
        try {            
            String hostIP = IpAddressUtils.nameResolve(host, 4000, IpTypePreference.IPV4_ONLY).getHostAddress();
            String[] hostIPElements = hostIP.split("\\.");
            String[] patternElements = pattern.split("\\.");
            String[] maskElements = mask.split("\\.");

            for (int i = 0; i < hostIPElements.length; i++) {
                if (i < maskElements.length && maskElements[i].trim().equals("255")) {
                    if (!hostIPElements[i].trim().equals(patternElements[i].trim())) {
                        return false;
                    }
                }
            }
            return true;
        } catch (InterruptedException | UnknownHostException | TimeoutException ex) {
            return false;
        }
    }

    @Override
    public int dnsDomainLevels(String host) {
        if (host == null) {
            return 0;
        }
        return getNoOfOccurrences(host, '.');
    }

    @Override
    public boolean shExpMatch(String str, String shexp) {
        Pattern pattern = PacUtils.createRegexPatternFromGlob(shexp);
        return pattern.matcher(str).matches();
    }
    

    @Override
    public boolean weekdayRange(Object... args) {
        try {
            return PacUtilsDateTime.isInWeekdayRange(new Date(), args);
        } catch (PacUtilsDateTime.PacDateTimeInputException ex) {
            LOGGER.log(Level.WARNING, "PAC script error : arguments passed to weekdayRange() function {0} are faulty: {1}", new Object[]{Arrays.toString(args), ex.getMessage()});
            return false;
        }
    }

    @Override
    public boolean dateRange(Object... args) {
        try {
            return PacUtilsDateTime.isInDateRange(new Date(), args);
        } catch (PacUtilsDateTime.PacDateTimeInputException ex) {
            LOGGER.log(Level.WARNING, "PAC script error : arguments passed to dateRange() function {0} are faulty: {1}", new Object[]{Arrays.toString(args), ex.getMessage()});
            return false;
        }
    }

    @Override
    public boolean timeRange(Object... args) {
        try {
            return PacUtilsDateTime.isInTimeRange(new Date(), args);
        } catch (PacUtilsDateTime.PacDateTimeInputException ex) {
            LOGGER.log(Level.WARNING, "PAC script error : arguments passed to timeRange() function {0} are faulty: {1}", new Object[]{Arrays.toString(args), ex.getMessage()});
            return false;
        }
    }
    

    // *************************************************************
    //  Microsoft extensions
    // 
    // *************************************************************
    
    
    @Override
    public boolean isResolvableEx(String host) {
        try {
            IpAddressUtils.nameResolve(host, DNS_TIMEOUT_MS, IpTypePreference.ANY_JDK_PREF)
                    .getHostAddress();
            return true;
        } catch (InterruptedException | UnknownHostException | TimeoutException ex) {
            return false;
        }
    }

    @Override
    public String dnsResolveEx(String host) {
        try {
            return IpAddressUtils.nameResolve(host, DNS_TIMEOUT_MS, IpTypePreference.ANY_JDK_PREF)
                    .getHostAddress();
        } catch (InterruptedException | UnknownHostException | TimeoutException ex) {
            return "";
        }
    }

    @Override
    public String myIpAddressEx() {
        InetAddress[] addresses = LocalAddressUtils.getMostLikelyLocalInetAddresses(IpTypePreference.ANY_JDK_PREF);
        return PacUtils.toSemiColonListInetAddress(addresses);
    }

    @Override
    public String sortIpAddressList(String ipAddressList) {
        if (ipAddressList == null) {
            return "";
        }
        
        // We convert to InetAddress (because we know how to sort
        // those) but at the same time we have to preserve the way
        // the original input was represented and return in the same
        // format.
        String[] strComps = ipAddressList.split(";");
        List<InetAddress> addressesB = new ArrayList<>();
        for(String s : strComps) {
            try {
                addressesB.add(InetAddress.getByName(s.trim()));
            } catch (UnknownHostException ex) {
                return "";
            }
        }
        List<InetAddress> addressesS = new ArrayList<>(addressesB);
        IpAddressUtils.sortIpAddresses(addressesS, false);
        
        List<String> addressesStr = new ArrayList<>(addressesS.size());
        for (InetAddress addr : addressesS) {
            addressesStr.add(strComps[addressesB.indexOf(addr)].trim());
        }
        
        return PacUtils.toSemiColonList(addressesStr);
    }

    @Override
    public String getClientVersion() {
        return "1.0";
    }

    @Override
    public boolean isInNetEx(String ipAddress, String ipPrefix) {
        if (ipAddress == null) {
            return false;
        }
        if (ipPrefix == null) {
            return false;
        }
        
        InetAddress iAddr;
        try {
            iAddr = IpAddressUtils.nameResolve(ipAddress, DNS_TIMEOUT_MS, IpTypePreference.ANY_JDK_PREF);
        } catch (InterruptedException | UnknownHostException | TimeoutException ex) {
            return false;
        }
        
        String[] ipPrefixes = ipPrefix.split(";");
        for(String ipP : ipPrefixes) {
            if (PacUtils.ipPrefixMatch(iAddr, ipP)) {
                return true;
            }
        }
        return false;
    }

    
    
    // *************************************************************
    //  Utility functions.
    // 
    //  Other functions - not defined in PAC spec, but still 
    //  exposed to the JavaScript engine.
    // *************************************************************

    @Override
    public void alert(String message) {
        LOGGER.log(Level.INFO, "PAC script says : {0}", message);
    }

    
    // *************************************************************
    //  
    // Other methods.
    // Not exposed to JavaScript engine.
    //
    // *************************************************************
    
    

    private static String[] getDomains(String host) {
        String[] doms = host.split("\\.");
        List<String> domsList = new ArrayList<>(doms.length);
        for (String dom : doms) {
            if (dom != null && (!dom.isEmpty())) {
                domsList.add(dom);
            }
        }
        return domsList.toArray(new String[0]);
    }

    
    /**
     * Count no of occurrences of ch in str.
     * @param str
     * @param ch
     * @return 
     */
    private static int getNoOfOccurrences(String str, char ch) {
        int counter = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                counter++;
            }
        }
        return counter;
    }

    
}
