/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.proxy;

import java.net.InetSocketAddress;

/**
 * This data object encapsulates parameters that are needed to establish connection to an arbitrary remote IP address.
 * There are basically 2 types of connectivity:
 * <ul>
 * <li>direct connection (without any firewall or proxy, or with a transparent proxy)
 * <li>mediated connection that routes via a firewall or proxy
 * </ul>
 * If this object represents a direct connection type, no further parameters are required. If this object
 * represents a proxy connection, it also hold the proxy type, address, and port. Other optional parameters
 * include proxy username and password.
 *
 * @author Maros Sandor
 */
public class ConnectivitySettings {
    /**
     * Connection type constant for a direct connection.
     */
    public static final int CONNECTION_DIRECT = 0;

    /**
     * Connection type constant for connection via SOCKS proxies.
     */
    public static final int CONNECTION_VIA_SOCKS = 1;

    /**
     * Connection type constant for connection via HTTP proxies.
     */
    public static final int CONNECTION_VIA_HTTPS = 2;

    private static final int CONNECTION_TYPE_MIN = CONNECTION_DIRECT;
    private static final int CONNECTION_TYPE_MAX = CONNECTION_VIA_HTTPS;

    private int     mConnectionType;
    private String  mProxyHost;
    private int     mProxyPort;
    private String  mProxyUsername;
    private char[]  mProxyPassword;
    private int     mKeepAliveIntervalSeconds;

    public String toString() {
        return "Type: " + mConnectionType + " Proxy: " + mProxyUsername + "@" + mProxyHost + ":" + mProxyPort;
    }

    /**
     * Constructs connectivity settings with the default connection setting (direct connection).
     */
    public ConnectivitySettings() {
        mConnectionType = CONNECTION_DIRECT;
        mKeepAliveIntervalSeconds = 60;
    }

    /**
     * Changes configuration of this connectivity settings.
     *
     * @param type          one of the connection type constants
     * @param host          proxy hostname, must not be null for proxy configurations, is ignored for direct connectivity.
     * @param port          proxy port, must be in range 1-65535 for proxy configurations, is ignored for direct connectivity.
     * @param username      a username to supply to proxy when it request user authentication, may be null if the proxy
     *                      does not require authentication or we use direct connection
     * @param proxyPassword password to supply to proxy when it request user authentication, may be null if the proxy
     *                      does not require authentication or we use direct connection
     * @throws java.lang.IllegalArgumentException
     *          if the connection type constant is illegal, the proxy number is out of range or
     *          the proxy host is empty or null (for non-direct connections)
     */
    public void setProxy(int type, String host, int port, String username, char[] proxyPassword) {
        if (type < CONNECTION_TYPE_MIN || type > CONNECTION_TYPE_MAX) throw new IllegalArgumentException("Illegal connection type");

        if (type != CONNECTION_DIRECT) {
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Illegal proxy port number: " + port);
            if (host == null || (host = host.trim()).length() == 0) throw new IllegalArgumentException("A proxy host must be specified");
        }

        mConnectionType = type;
        mProxyHost = host;
        mProxyPort = port;
        mProxyUsername = username;
        mProxyPassword = proxyPassword;
    }

    public int getKeepAliveIntervalSeconds() {
        return mKeepAliveIntervalSeconds;
    }

    public void setKeepAliveIntervalSeconds(int keepAliveIntervalSeconds) {
        mKeepAliveIntervalSeconds = keepAliveIntervalSeconds;
    }

    public int getConnectionType() {
        return mConnectionType;
    }

    public void setConnectionType(int connectionType) {
        mConnectionType = connectionType;
    }

    public String getProxyHost() {
        return mProxyHost;
    }

    public void setProxyHost(String proxyHost) {
        mProxyHost = proxyHost;
    }

    public int getProxyPort() {
        return mProxyPort;
    }

    public void setProxyPort(int proxyPort) {
        mProxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return mProxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        mProxyUsername = proxyUsername;
    }

    public char[] getProxyPassword() {
        return mProxyPassword;
    }

    public void setProxyPassword(char[] proxyPassword) {
        mProxyPassword = proxyPassword;
    }
}
