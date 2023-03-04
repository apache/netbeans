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

package org.netbeans.modules.weblogic.common;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 *
 * @author Petr Hejl
 */
public final class ProxyUtils {

    public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final Logger LOGGER = Logger.getLogger(ProxyUtils.class.getName());

    private ProxyUtils() {
        super();
    }

    @CheckForNull
    public static String getNonProxyHosts(@NullAllowed Callable<String> nonProxy) {
        String originalNonProxyHosts = System.getProperty(HTTP_NON_PROXY_HOSTS);
        String configuredNonProxyHosts = null;
        if (nonProxy != null) {
            try {
                configuredNonProxyHosts = nonProxy.call();
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        if (configuredNonProxyHosts != null && !configuredNonProxyHosts.isEmpty()) {
            String nonProxyHosts;
            if (originalNonProxyHosts != null) {
                nonProxyHosts = ProxyUtils.compactNonProxyHosts(
                        originalNonProxyHosts + "," + configuredNonProxyHosts); // NOI18N
            } else {
                nonProxyHosts = configuredNonProxyHosts;
            }
            if (!nonProxyHosts.equals(originalNonProxyHosts)) {
                return nonProxyHosts;
            }
        }
        return originalNonProxyHosts;
    }

    // avoid duplicate hosts
    private static String compactNonProxyHosts(String hosts) {
        StringTokenizer st = new StringTokenizer(hosts, ","); //NOI18N
        StringBuilder nonProxyHosts = new StringBuilder();
        while (st.hasMoreTokens()) {
            String h = st.nextToken().trim();
            if (h.length() == 0) {
                continue;
            }
            if (nonProxyHosts.length() > 0) {
                nonProxyHosts.append("|"); // NOI18N
            }
            nonProxyHosts.append(h);
        }
        st = new StringTokenizer(nonProxyHosts.toString(), "|"); //NOI18N
        Set<String> set = new HashSet<String>();
        StringBuilder compactedProxyHosts = new StringBuilder();
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (set.add(t.toLowerCase(Locale.ENGLISH))) {
                if (compactedProxyHosts.length() > 0) {
                    compactedProxyHosts.append('|'); // NOI18N
                }
                compactedProxyHosts.append(t);
            }
        }
        return compactedProxyHosts.toString();
    }

}
