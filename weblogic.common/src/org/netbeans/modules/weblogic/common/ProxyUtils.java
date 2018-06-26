/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
