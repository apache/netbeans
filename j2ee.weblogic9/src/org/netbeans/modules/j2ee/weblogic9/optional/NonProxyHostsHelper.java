/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.weblogic9.optional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
// FIXME this is copied from core.ui and should be removed once #210679 is implemented
public final class NonProxyHostsHelper {

    public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final String NOT_PROXY_HOSTS = "proxyNonProxyHosts";

    private NonProxyHostsHelper() {
        super();
    }

    public static String getNonProxyHosts () {
        String hosts = getPreferences ().get (NOT_PROXY_HOSTS, getDefaultUserNonProxyHosts ());
        return compactNonProxyHosts(hosts);
    }

    private static Preferences getPreferences() {
        return NbPreferences.root().node ("/org/netbeans/core"); // NOI18N
    }

    private static String getDefaultUserNonProxyHosts () {
        return getModifiedNonProxyHosts (getSystemNonProxyHosts ());
    }

    private static String getSystemNonProxyHosts () {
        String systemProxy = System.getProperty ("netbeans.system_http_non_proxy_hosts"); // NOI18N

        return systemProxy == null ? "" : systemProxy;
    }

    private static String getPresetNonProxyHosts () {
        return System.getProperty ("http.nonProxyHosts", "");
    }

    private static String getModifiedNonProxyHosts (String systemPreset) {
        String fromSystem = systemPreset.replaceAll (";", "|").replaceAll (",", "|"); //NOI18N
        String fromUser = getPresetNonProxyHosts () == null ? "" : getPresetNonProxyHosts ().replaceAll (";", "|").replaceAll (",", "|"); //NOI18N
        if (Utilities.isWindows ()) {
            fromSystem = addReguralToNonProxyHosts (fromSystem);
        }
        final String staticNonProxyHosts = "localhost|127.0.0.1"; // NOI18N
        String nonProxy = concatProxies(fromUser, fromSystem, staticNonProxyHosts); // NOI18N
        String localhost;
        try {
            localhost = InetAddress.getLocalHost().getHostName();
            if (!"localhost".equals(localhost)) { // NOI18N
                nonProxy = nonProxy + "|" + localhost; // NOI18N
            } else {
                // Avoid this error when hostname == localhost:
                // Error in http.nonProxyHosts system property:  sun.misc.REException: localhost is a duplicate
            }
        }
        catch (UnknownHostException e) {
            // OK. Sometimes a hostname is assigned by DNS, but a computer
            // is later pulled off the network. It may then produce a bogus
            // name for itself which can't actually be resolved. Normally
            // "localhost" is aliased to 127.0.0.1 anyway.
        }
        /* per Milan's agreement it's removed. See issue #89868
        try {
            String localhost2 = InetAddress.getLocalHost().getCanonicalHostName();
            if (!"localhost".equals(localhost2) && !localhost2.equals(localhost)) { // NOI18N
                nonProxy = nonProxy + "|" + localhost2; // NOI18N
            } else {
                // Avoid this error when hostname == localhost:
                // Error in http.nonProxyHosts system property:  sun.misc.REException: localhost is a duplicate
            }
        }
        catch (UnknownHostException e) {
            // OK. Sometimes a hostname is assigned by DNS, but a computer
            // is later pulled off the network. It may then produce a bogus
            // name for itself which can't actually be resolved. Normally
            // "localhost" is aliased to 127.0.0.1 anyway.
        }
         */
        return compactNonProxyHosts (nonProxy);
    }

    private static String concatProxies(String... proxies) {
        StringBuilder sb = new StringBuilder();
        for (String n : proxies) {
            if (n == null) {
                continue;
            }
            n = n.trim();
            if (n.isEmpty()) {
                continue;
            }
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '|') {
                if (!n.startsWith("|")) {
                    sb.append('|');
                }
            }
            sb.append(n);
        }
        return sb.toString();
    }

    // avoid duplicate hosts
    public static String compactNonProxyHosts (String hosts) {
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
        st = new StringTokenizer (nonProxyHosts.toString(), "|"); //NOI18N
        Set<String> set = new HashSet<String> ();
        StringBuilder compactedProxyHosts = new StringBuilder();
        while (st.hasMoreTokens ()) {
            String t = st.nextToken ();
            if (set.add (t.toLowerCase (Locale.US))) {
                if (compactedProxyHosts.length() > 0) {
                    compactedProxyHosts.append('|');
                }
                compactedProxyHosts.append(t);
            }
        }
        return compactedProxyHosts.toString();
    }

    private static String addReguralToNonProxyHosts (String nonProxyHost) {
        StringTokenizer st = new StringTokenizer (nonProxyHost, "|");
        StringBuilder reguralProxyHosts = new StringBuilder();
        while (st.hasMoreTokens ()) {
            String t = st.nextToken ();
            if (t.indexOf ('*') == -1) { //NOI18N
                t = t + '*'; //NOI18N
            }
            if (reguralProxyHosts.length() > 0)
                reguralProxyHosts.append('|');
            reguralProxyHosts.append(t);
        }

        return reguralProxyHosts.toString();
    }
}
