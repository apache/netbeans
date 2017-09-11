/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.openide.util;

import java.net.Authenticator;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Useful static methods for getting Network Proxy required for make network
 * connection for specified resource.
 *
 * @since 8.13
 * @author Jiri Rechtacek
 */
public final class NetworkSettings {

    private static final String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword";
    private static final Logger LOGGER = Logger.getLogger(NetworkSettings.class.getName());
    private static ThreadLocal<Boolean> authenticationDialogSuppressed = new ThreadLocal<Boolean>();

    /** Returns the <code>hostname</code> part of network proxy address 
     * based on given URI to access the resource at.
     * Returns <code>null</code> for direct connection.
     * 
     * @param u The URI that a connection is required to
     * @return the hostname part of the Proxy address or <code>null</code>
     */
    public static String getProxyHost(URI u) {
        ProxyCredentialsProvider provider = Lookup.getDefault().lookup(ProxyCredentialsProvider.class);
        if (provider == null) {
            LOGGER.warning("No ProxyCredentialsProvider found in lookup " + Lookup.getDefault() + " thus no proxy information will provide!");
        }
        return provider == null ? null : provider.getProxyHost(u);
    }

    /** Returns the <code>port</code> part of network proxy address 
     * based on given URI to access the resource at.
     * Returns <code>null</code> for direct connection.
     * 
     * @param u The URI that a connection is required to
     * @return the port part of the Proxy address or <code>null</code>
     */
    public static String getProxyPort(URI u) {
        ProxyCredentialsProvider provider = Lookup.getDefault().lookup(ProxyCredentialsProvider.class);
        if (provider == null) {
            LOGGER.warning("No ProxyCredentialsProvider found in lookup " + Lookup.getDefault() + " thus no proxy information will provide!");
        }
        return provider == null ? null : provider.getProxyPort(u);
    }

    /** Returns the <code>username</code> for Proxy Authentication.
     * Returns <code>null</code> if no authentication required.
     * 
     * @param u The URI that a connection is required to
     * @return username for Proxy Authentication or <code>null</code>
     */
    public static String getAuthenticationUsername(URI u) {
        ProxyCredentialsProvider provider = Lookup.getDefault().lookup(ProxyCredentialsProvider.class);
        if (provider == null) {
            LOGGER.warning("No ProxyCredentialsProvider found in lookup " + Lookup.getDefault() + " thus no proxy information will provide!");
        }
        if (provider != null && provider.isProxyAuthentication(u)) {
            return provider.getProxyUserName(u);
        }
        return null;
    }

    /** Returns the <code>password</code> for Proxy Authentication.
     * Returns <code>null</code> if no authentication required.
     * 
     * @param u The URI that a connection is required to
     * @return password for Proxy Authentication
     * @since 9.8
     */
    public static char[] getAuthenticationPassword(URI u) {
        ProxyCredentialsProvider provider = Lookup.getDefault().lookup(ProxyCredentialsProvider.class);
        if (provider == null) {
            LOGGER.log(Level.WARNING, "No ProxyCredentialsProvider found in lookup {0} thus no proxy information will provide!", Lookup.getDefault());
        }
        if (provider != null && provider.isProxyAuthentication(u)) {
            return provider.getProxyPassword(u);
        }
        return null;
    }

    /** Returns the <code>key</code> for reading password for Proxy Authentication.
     * Use <a href="@org-netbeans-modules-keyring@/org/netbeans/api/keyring/Keyring.html"><code>org.netbeans.api.keyring.Keyring</code></a> for reading the password from the ring.
     * Returns <code>null</code> if no authentication required.
     * 
     * @param u The URI that a connection is required to
     * @return the key for reading password for Proxy Authentication from the ring or <code>null</code>
     * @deprecated use {@link #getAuthenticationPassword(java.net.URI)} instead
     */
    public static String getKeyForAuthenticationPassword(URI u) {
        ProxyCredentialsProvider provider = Lookup.getDefault().lookup(ProxyCredentialsProvider.class);
        if (provider == null) {
            LOGGER.warning("No ProxyCredentialsProvider found in lookup " + Lookup.getDefault() + " thus no proxy information will provide!");
        }
        if (provider != null && provider.isProxyAuthentication(u)) {
            return PROXY_AUTHENTICATION_PASSWORD;
        }
        return null;
    }

    /** Suppress asking user a question about the authentication credentials while
     * running <code>blockOfCode</code>. It's a contract with NetBeans implementation
     * of {@link Authenticator}.
     * In case a system is using other Authenticator implementation, it must call {@link #isAuthenticationDialogSuppressed} method. 
     * 
     * @param blockOfCode {@link Callable} containing code which will be executed while authentication is suppressed
     * @return a result of calling of <code>blockOfCode</code> and may throw an exception.
     * @throws Exception 
     * @see #isAuthenticationDialogSuppressed
     * @since 8.17
     */
    public static <R> R suppressAuthenticationDialog(Callable<R> blockOfCode) throws Exception {
        try {
            authenticationDialogSuppressed.set(Boolean.TRUE);
            return blockOfCode.call();
        } finally {
            authenticationDialogSuppressed.remove();
        }
    }

    /** A utility method for implementations of {@link Authenticator}
     * to suppress asking users a authentication question while running code posted
     * in {@link #authenticationDialogSuppressed}.
     * 
     * @return true while running code posted in {@link #authenticationDialogSuppressed} method.
     * @since 8.17
     * @see #authenticationDialogSuppressed
     */
    public static boolean isAuthenticationDialogSuppressed() {
        return Boolean.TRUE.equals(authenticationDialogSuppressed.get());
    }

    /** Allows
     * NetBeans Platform users to provide own proxy and network credentials separately.
     * 
     * @see <a href="http://wiki.netbeans.org/Authenticator">http://wiki.netbeans.org/Authenticator</a>
     * @author Jiri Rechtacek, Ondrej Vrabec
     * @since 8.17
     */
    public static abstract class ProxyCredentialsProvider {

        /** Returns the <code>username</code> for Proxy Authentication.
         * Returns <code>null</code> if no authentication required.
         * 
         * @param u The URI that a connection is required to
         * @return username for Proxy Authentication
         */
        protected abstract String getProxyUserName(URI u);

        /** Returns the <code>password</code> for Proxy Authentication.
         * Returns <code>null</code> if no authentication required.
         * 
         * @param u The URI that a connection is required to
         * @return password for Proxy Authentication
         */
        protected abstract char[] getProxyPassword(URI u);

        /** Returns <code>true</code> if Proxy Authentication is required.
         * 
         * @param u The URI that a connection is required to
         * @return <code>true</code> if authentication required.
         */
        protected abstract boolean isProxyAuthentication(URI u);

        /** Returns the <code>hostname</code> part of network proxy address 
         * based on given URI to access the resource at.
         * Returns <code>null</code> for direct connection.
         * 
         * @param u The URI that a connection is required to
         * @return the hostname part of the Proxy address or <code>null</code>
         */
        protected abstract String getProxyHost(URI u);

        /** Returns the <code>port</code> part of network proxy address 
         * based on given URI to access the resource at.
         * Returns <code>null</code> for direct connection.
         * 
         * @param u The URI that a connection is required to
         * @return the port part of the Proxy address or <code>null</code>
         */
        protected abstract String getProxyPort(URI u);
    }
}
