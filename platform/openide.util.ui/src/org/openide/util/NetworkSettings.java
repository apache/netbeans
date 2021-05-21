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
    @Deprecated
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
