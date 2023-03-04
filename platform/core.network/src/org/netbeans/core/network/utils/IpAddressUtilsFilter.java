/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Package private methods for choosing IP addresses from a
 * list based on a stated preference.
 * 
 * @author lbruun
 */
class IpAddressUtilsFilter {

    private static final boolean JDK_PREFER_IPV6_ADDRESS;
    static {
        JDK_PREFER_IPV6_ADDRESS = java.security.AccessController.doPrivileged(
                new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return Boolean.getBoolean("java.net.preferIPv6Addresses");

            }
        });
    }
    private IpAddressUtilsFilter() {}
    
    protected static InetAddress pickInetAddress(Iterable<InetAddress> sortedList, IpAddressUtils.IpTypePreference ipTypePref) {
        IpAddressUtils.IpTypePreference pref = getIpTypePreferenceResolved(ipTypePref);
        for (InetAddress ipAddress : sortedList) {
            if (pref == IpAddressUtils.IpTypePreference.ANY_IPV4_PREF  || pref == IpAddressUtils.IpTypePreference.ANY_IPV6_PREF) {
                return ipAddress;
            }
            if (ipAddress instanceof Inet4Address) {
                if (pref == IpAddressUtils.IpTypePreference.IPV4_ONLY) {
                    return ipAddress;
                }
            }
            if (ipAddress instanceof Inet6Address) {
                if (pref == IpAddressUtils.IpTypePreference.IPV6_ONLY) {
                    return ipAddress;
                }
            }
        }
        return null;
    }
    
    protected static @NonNull List<InetAddress> filterInetAddresses(Iterable<InetAddress> list, IpAddressUtils.IpTypePreference ipTypePref) {
        IpAddressUtils.IpTypePreference pref = getIpTypePreferenceResolved(ipTypePref);
        List<InetAddress> newList = new ArrayList<>();
        if (list != null) {
            for (InetAddress ipAddress : list) {
                if (pref == IpAddressUtils.IpTypePreference.ANY_IPV4_PREF || pref == IpAddressUtils.IpTypePreference.ANY_IPV6_PREF) {
                    newList.add(ipAddress);
                } else {
                    if ((ipAddress instanceof Inet4Address) && (pref == IpAddressUtils.IpTypePreference.IPV4_ONLY)) {
                        newList.add(ipAddress);
                    }
                    if ((ipAddress instanceof Inet6Address) && (pref == IpAddressUtils.IpTypePreference.IPV6_ONLY)) {
                        newList.add(ipAddress);
                    }
                }
            }
        }
        if (pref == IpAddressUtils.IpTypePreference.ANY_IPV4_PREF) {
            IpAddressUtils.sortIpAddressesShallow(newList,true);
        }
        if (pref == IpAddressUtils.IpTypePreference.ANY_IPV6_PREF) {
            IpAddressUtils.sortIpAddressesShallow(newList,false);
        }
        return newList;
    }

    private static IpAddressUtils.IpTypePreference getIpTypePreferenceResolved(IpAddressUtils.IpTypePreference ipTypePref) {
        if (ipTypePref == IpAddressUtils.IpTypePreference.ANY_JDK_PREF) {
            if (JDK_PREFER_IPV6_ADDRESS) {
                return IpAddressUtils.IpTypePreference.ANY_IPV6_PREF;
            } else {
                return IpAddressUtils.IpTypePreference.ANY_IPV4_PREF;
            }
        } else {
            return ipTypePref;
        }
    }    
}
