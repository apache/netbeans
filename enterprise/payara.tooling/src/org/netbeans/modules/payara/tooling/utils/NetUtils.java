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
package org.netbeans.modules.payara.tooling.utils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.logging.Logger;
import java.io.PrintWriter;

/**
 * Networking utilities
 * <p/>
 * @author Tomas Kraus
 */
public class NetUtils {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Comparator for {@link InetAddress} instances to be sorted.
     */
    public static class InetAddressComparator
            implements Comparator<InetAddress> {

        /**
         * Compares values of <code>InetAddr</code> instances.
         * <p/>
         * @param ip1 First <code>InetAddr</code> instance to be compared.
         * @param ip2 Second <code>InetAddr</code> instance to be compared.
         * @return A negative integer, zero, or a positive integer as the first
         *         argument is less than, equal to, or greater than the second.
         */
        @Override
        public int compare(final InetAddress ip1, final InetAddress ip2) {
            byte[] addr1 = ip1.getAddress();
            byte[] addr2 = ip2.getAddress();
            int result = addr2.length - addr1.length;
            if (result == 0) {
                for (int i = 0; result == 0 && i < addr1.length; i++) {
                    result = addr1[i] - addr2[i];
                }
            }
            return result;
        }
        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(ServerUtils.class);

    /** Port check timeout [ms]. */
    public static final int PORT_CHECK_TIMEOUT = 2000;

    /** Comparator for {@link InetAddress} instances to be sorted. */
    private static final InetAddressComparator INET_ADDRESS_COMPARATOR
            = new InetAddressComparator();

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Finds out if server is running on remote host by connecting to remote
     * host and port.
     * <p/>
     * @param host Server host.
     * @param port Server port.
     * @param timeout Network connection timeout [ms].
     * @return Returns <code>true</code> when server port is accepting
     *         connections or <code>false</code> otherwise.
     */
    public static boolean isPortListeningRemote(final String host,
            final int port, final int timeout) {
        final String METHOD = "isPortListeningRemote";
        if (null == host) {
            return false;
        }
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, METHOD,
                            "closeError", ioe.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Finds out if server is running on remote host by connecting to remote
     * host and port.
     * <p/>
     * @param host Server host.
     * @param port Server port.
     * @return Returns <code>true</code> when server port is accepting
     *         connections or <code>false</code> otherwise.
     */
    public static boolean isPortListeningRemote(final String host,
            final int port) {
        return isPortListeningRemote(host, port, 0);
    }

    /**
     * Finds out if server is running on local host by binding to local port.
     * <p/>
     * @param host Server host or <code>null</code> value for address of the
     *             loopback interface. 
     * @param port Server port.
     * @return Returns <code>true</code> when server port is accepting
     *         connections or <code>false</code> otherwise.
     */
    public static boolean isPortListeningLocal(final String host,
            final int port) {
        final String METHOD = "isPortListeningLocal";
        ServerSocket socket = null;
        try {
            InetAddress ia = InetAddress.getByName(host);
            socket = new ServerSocket(port, 1, ia);
            return false;
        } catch (IOException ioe) {
            return true;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, METHOD,
                            "closeError", ioe.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Determine whether an HTTP listener is secure or not..
     * <p/>
     * This method accepts a host name and port #.  It uses this information
     * to attempt to connect to the port, send a test query, analyze the
     * result to determine if the port is secure or is not secure (currently
     * only HTTP / HTTPS is supported).
     * it might emit a warning in the server log for Payara cases.
     * No Harm, just an annoying warning, so we need to use this call only
     * when really needed.
     * <p/>
     * @param hostname The host for the HTTP listener.
     * @param port     The port for the HTTP listener.
     * @throws IOException
     * @throws SocketTimeoutException
     * @throws ConnectException
     */
    public static boolean isSecurePort(String hostname, int port)
            throws IOException, ConnectException, SocketTimeoutException {
        return isSecurePort(hostname,port, 0);
    }

    /**
     * Determine whether an HTTP listener is secure or not..
     * <p/>
     * This method accepts a host name and port #.  It uses this information
     * to attempt to connect to the port, send a test query, analyze the
     * result to determine if the port is secure or is not secure (currently
     * only HTTP / HTTPS is supported).
     * it might emit a warning in the server log for Payara cases.
     * No Harm, just an annoying warning, so we need to use this call only
     * when really needed.
     * <p/>
     * @param hostname The host for the HTTP listener.
     * @param port     The port for the HTTP listener.
     * @param depth     Method calling depth.
     * @throws IOException
     * @throws SocketTimeoutException
     * @throws ConnectException
     */
    private static boolean isSecurePort(String hostname, int port, int depth) 
            throws IOException, ConnectException, SocketTimeoutException {
        final String METHOD = "isSecurePort";
        boolean isSecure = true;
        try (Socket socket = new Socket()) {
            try {
                LOGGER.log(Level.FINE, METHOD, "socket");
                socket.connect(new InetSocketAddress(hostname, port), PORT_CHECK_TIMEOUT);
                socket.setSoTimeout(PORT_CHECK_TIMEOUT);
            // This could be bug 70020 due to SOCKs proxy not having localhost
            } catch (SocketException ex) {
                String socksNonProxyHosts = System.getProperty("socksNonProxyHosts");
                if(socksNonProxyHosts != null && socksNonProxyHosts.indexOf("localhost") < 0) {
                    String localhost = socksNonProxyHosts.length() > 0 ? "|localhost" : "localhost";
                    System.setProperty("socksNonProxyHosts",  socksNonProxyHosts + localhost);
                    ConnectException ce = new ConnectException();
                    ce.initCause(ex);
                    throw ce; //status unknow at this point
                    //next call, we'll be ok and it will really detect if we are secure or not
                }
            }
            java.io.InputStream istream = socket.getInputStream();
            //This is the test query used to ping the server in an attempt to
            //determine if it is secure or not.
            String testQuery = "GET / HTTP/1.0";
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            pw.println(testQuery);
            pw.println();
            pw.flush();
            byte[] respArr = new byte[1024];
            while (istream.read(respArr) != -1) {
                String resp = new String(respArr);
                if (checkHelper(resp) == false) {
                    isSecure = false;
                    break;
                }
            }
        }
        return isSecure;
    }

    private static boolean checkHelper(String respText) {
        boolean isSecure = true;
        if (respText.startsWith("http/1.") || respText.startsWith("HTTP/1.")) {
            isSecure = false;
        } else if (respText.contains("<html")) {
            isSecure = false;
        } else if (respText.contains("</html")) {
            // New test added to resolve 106245
            // when the user has the IDE use a proxy (like webcache.foo.bar.com),
            // the response comes back as "d><title>....</html>".  It looks like
            // something eats the "<html><hea" off the front of the data that
            // gets returned.
            //
            // This test makes an allowance for that behavior. I figure testing
            // the likely "last bit" is better than testing a bit that is close
            // to the data that seems to get eaten.
            //
            isSecure = false;
        } else if (respText.contains("connection: ")) {
            isSecure = false;
        }
        return isSecure;
    }

    /**
     * Retrieve {@link Set} of IP addresses of this host.
     * <p/>
     * @return {@link Set} of IP addresses of this host.
     * @throws PayaraIdeException if addresses of this host could not
     *         be retrieved.
     */
    public static Set<InetAddress> getHostIPs() {
        final String METHOD = "getHostIPs";
        Set<InetAddress> addrs = new TreeSet<>(INET_ADDRESS_COMPARATOR);
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.
                    getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                for (Enumeration<InetAddress> e = iface.getInetAddresses(); e.hasMoreElements(); ) {
                    InetAddress a = e.nextElement();
                    addrs.add(a);
                }
            }
        } catch (SocketException se) {
            addrs = null;
            throw new PayaraIdeException(LOGGER.excMsg(METHOD, "exception"));
        }
        return addrs;
    }

    /**
     * Retrieve {@link Set} of IPv4 addresses of this host.
     * <p/>
     * @return {@link Set} of IPv4 addresses of this host.
     */
    public static Set<Inet4Address> getHostIP4s() {
        Set<Inet4Address> addrs = new TreeSet<>(INET_ADDRESS_COMPARATOR);
        for (InetAddress a : getHostIPs()) {
            if (a instanceof Inet4Address) {
                addrs.add((Inet4Address) a);
            }
        }
        return addrs;
    }

    /**
     * Retrieve {@link Set} of IPv6 addresses of this host.
     * <p/>
     * @return {@link Set} of IPv6 addresses of this host.
     */
    public static Set<Inet6Address> getHostIP6s() {
        Set<Inet6Address> addrs = new TreeSet<>(INET_ADDRESS_COMPARATOR);
        for (InetAddress a : getHostIPs()) {
            if (a instanceof Inet6Address) {
                addrs.add((Inet6Address) a);
            }
        }
        return addrs;
    }

}
