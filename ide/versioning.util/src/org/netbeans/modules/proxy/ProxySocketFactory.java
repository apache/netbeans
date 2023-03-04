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
package org.netbeans.modules.proxy;

import javax.net.SocketFactory;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.NetworkSettings;

/**
 * Creates sockets capable of connecting through HTTPS and SOCKS proxies.
 * 
 * @author Maros Sandor
 */
public class ProxySocketFactory extends SocketFactory {
    
    private static final int CONNECT_TIMEOUT = 1000 * 20; /// 20 seconds timeout
    
    private static final String AUTH_NONE = "<none>";
    private static final String AUTH_BASIC = "Basic";
    private static final Pattern sConnectionEstablishedPattern = Pattern.compile("HTTP\\/\\d+\\.\\d+\\s+200\\s+");
    private static final Pattern sProxyAuthRequiredPattern = Pattern.compile("HTTP\\/\\d+\\.\\d+\\s+407\\s+");
        
    private static final ProxySocketFactory instance = new ProxySocketFactory();
    
    private final Map<InetSocketAddress, ConnectivitySettings> lastKnownSettings = Collections.synchronizedMap(new HashMap<InetSocketAddress, ConnectivitySettings>(2));
    
    public static ProxySocketFactory getDefault() {
        return instance;
    }
    
    private ProxySocketFactory() {
    }
    
    /**
     * Creates probe socket that supports only
     * connect(SocketAddressm, int timeout).
     */
    public Socket createSocket() throws IOException {
        return new Socket() {
            public void connect(SocketAddress endpoint, int timeout) throws IOException {
                Socket s = createSocket((InetSocketAddress)endpoint, timeout);
                s.close();
            }

            public void bind(SocketAddress bindpoint) {
                throw new UnsupportedOperationException();
            }

            protected Object clone() {
                throw new UnsupportedOperationException();
            }

            public synchronized void close() {
            }

            public void connect(SocketAddress endpoint) {
                throw new UnsupportedOperationException();
            }

            public SocketChannel getChannel() {
                throw new UnsupportedOperationException();
            }

            public InetAddress getInetAddress() {
                throw new UnsupportedOperationException();
            }

            public InputStream getInputStream() {
                throw new UnsupportedOperationException();
            }

            public boolean getKeepAlive() {
                throw new UnsupportedOperationException();
            }

            public InetAddress getLocalAddress() {
                throw new UnsupportedOperationException();
            }

            public int getLocalPort() {
                throw new UnsupportedOperationException();
            }

            public SocketAddress getLocalSocketAddress() {
                throw new UnsupportedOperationException();
            }

            public boolean getOOBInline() {
                throw new UnsupportedOperationException();
            }

            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException();
            }

            public int getPort() {
                throw new UnsupportedOperationException();
            }

            public synchronized int getReceiveBufferSize() {
                throw new UnsupportedOperationException();
            }

            public SocketAddress getRemoteSocketAddress() {
                throw new UnsupportedOperationException();
            }

            public boolean getReuseAddress() {
                throw new UnsupportedOperationException();
            }

            public synchronized int getSendBufferSize() {
                throw new UnsupportedOperationException();
            }

            public int getSoLinger() {
                throw new UnsupportedOperationException();
            }

            public synchronized int getSoTimeout() {
                throw new UnsupportedOperationException();
            }

            public boolean getTcpNoDelay() {
                throw new UnsupportedOperationException();
            }

            public int getTrafficClass() {
                throw new UnsupportedOperationException();
            }

            public boolean isBound() {
                throw new UnsupportedOperationException();
            }

            public boolean isClosed() {
                throw new UnsupportedOperationException();
            }

            public boolean isConnected() {
                throw new UnsupportedOperationException();
            }

            public boolean isInputShutdown() {
                throw new UnsupportedOperationException();
            }

            public boolean isOutputShutdown() {
                throw new UnsupportedOperationException();
            }

            public void sendUrgentData(int data) {
                throw new UnsupportedOperationException();
            }

            public void setKeepAlive(boolean on) {
                throw new UnsupportedOperationException();
            }

            public void setOOBInline(boolean on) {
                throw new UnsupportedOperationException();
            }

            public synchronized void setReceiveBufferSize(int size) {
                throw new UnsupportedOperationException();
            }

            public void setReuseAddress(boolean on) {
                throw new UnsupportedOperationException();
            }

            public synchronized void setSendBufferSize(int size) {
                throw new UnsupportedOperationException();
            }

            public void setSoLinger(boolean on, int linger) {
                throw new UnsupportedOperationException();
            }

            public synchronized void setSoTimeout(int timeout) {
                throw new UnsupportedOperationException();
            }

            public void setTcpNoDelay(boolean on) {
                throw new UnsupportedOperationException();
            }

            public void setTrafficClass(int tc) {
                throw new UnsupportedOperationException();
            }

            public void shutdownInput() {
                throw new UnsupportedOperationException();
            }

            public void shutdownOutput() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    public Socket createSocket(String host, int port) throws IOException {
        return createSocket(new InetSocketAddress(host, port), CONNECT_TIMEOUT);
    }

    public Socket createSocket(InetAddress inetAddress, int port) throws IOException {
        return createSocket(new InetSocketAddress(inetAddress, port), CONNECT_TIMEOUT);
    }

    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException {
        throw new IOException("Unsupported operation");
    }

    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
        throw new IOException("Unsupported operation");
    }

    /**
     * Connects to the remote machine by establishing a tunnel through a HTTP proxy. It issues a CONNECT request and
     * eventually authenticates with the HTTP proxy. Supported authentication methods include: Basic.
     *
     * @param address remote machine to connect to
     * @return a TCP/IP socket connected to the remote machine
     * @throws UnknownHostException if the proxy host name cannot be resolved
     * @throws IOException          if an I/O error occurs during handshake (a network problem)
     */
    private Socket getHttpsTunnelSocket(InetSocketAddress address, ConnectivitySettings cs, int timeout) throws IOException {
        Socket proxy = new Socket();
        proxy.connect(new InetSocketAddress(cs.getProxyHost(), cs.getProxyPort()), timeout);
        BufferedReader r = new BufferedReader(new InputStreamReader(new InterruptibleInputStream(proxy.getInputStream())));
        DataOutputStream dos = new DataOutputStream(proxy.getOutputStream());

        dos.writeBytes("CONNECT ");
        dos.writeBytes(address.getHostName() + ":" + address.getPort());
        dos.writeBytes(" HTTP/1.0\r\n");
        dos.writeBytes("Connection: Keep-Alive\r\n\r\n");
        dos.flush();

        String line;
        line = r.readLine();

        if (sConnectionEstablishedPattern.matcher(line).find()) {
            for (; ;) {
                line = r.readLine();
                if (line.length() == 0) break;
            }
            return proxy;
        } else if (sProxyAuthRequiredPattern.matcher(line).find()) {
            boolean authMethodSelected = false;
            String authMethod = AUTH_NONE;
            for (; ;) {
                line = r.readLine();
                if (line.length() == 0) break;
                if (line.startsWith("Proxy-Authenticate:") && !authMethodSelected) {
                    authMethod = line.substring(19).trim();
                    if (authMethod.equals(AUTH_BASIC)) {
                        authMethodSelected = true;
                    }
                }
            }
            // TODO: need to read full response before closing connection?
            proxy.close();

            if (authMethod.startsWith(AUTH_BASIC)) {
                return authenticateBasic(address, cs);
            } else {
                throw new IOException("Unsupported authentication method: " + authMethod);
            }
        } else {
            proxy.close();
            throw new IOException("HTTP proxy does not support CONNECT command. Received reply: " + line);
        }
    }

    /**
     * Connects to the remote machine by establishing a tunnel through a HTTP proxy with Basic authentication.
     * It issues a CONNECT request and authenticates with the HTTP proxy with Basic protocol.
     *
     * @param address remote machine to connect to
     * @return a TCP/IP socket connected to the remote machine
     * @throws IOException     if an I/O error occurs during handshake (a network problem)
     */
    private Socket authenticateBasic(InetSocketAddress address, ConnectivitySettings cs) throws IOException {
        Socket proxy = new Socket(cs.getProxyHost(), cs.getProxyPort());
        BufferedReader r = new BufferedReader(new InputStreamReader(new InterruptibleInputStream(proxy.getInputStream())));
        DataOutputStream dos = new DataOutputStream(proxy.getOutputStream());

        String username = cs.getProxyUsername() == null ? "" : cs.getProxyUsername();
        String password = cs.getProxyPassword() == null ? "" : String.valueOf(cs.getProxyPassword());
        String credentials = username + ":" + password;
        String basicCookie = encodeCredentials(credentials);

        dos.writeBytes("CONNECT ");
        dos.writeBytes(address.getHostName() + ":" + address.getPort());
        dos.writeBytes(" HTTP/1.0\r\n");
        dos.writeBytes("Connection: Keep-Alive\r\n");
        dos.writeBytes("Proxy-Authorization: Basic " + basicCookie + "\r\n");
        dos.writeBytes("\r\n");
        dos.flush();

        String line = r.readLine();
        if (sConnectionEstablishedPattern.matcher(line).find()) {
            for (; ;) {
                line = r.readLine();
                if (line.length() == 0) break;
            }
            return proxy;
        }
        throw new IOException("Basic authentication failed: " + line);
    }

    static String encodeCredentials(String credentials) throws UnsupportedEncodingException {
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Creates a new Socket connected to the given IP address. The method uses connection settings supplied
     * in the constructor for connecting the socket.
     *
     * @param address the IP address to connect to
     * @return connected socket
     * @throws java.net.UnknownHostException  if the hostname of the address or the proxy cannot be resolved
     * @throws java.io.IOException            if an I/O error occured while connecting to the remote end or to the proxy
     */
    private Socket createSocket(InetSocketAddress address, int timeout) throws IOException {
        String socksProxyHost = System.getProperty("socksProxyHost");
        System.getProperties().remove("socksProxyHost");
        try {
            ConnectivitySettings cs = lastKnownSettings.get(address);
            if (cs != null) {
                try {
                    return createSocket(cs, address, timeout);
                } catch (IOException e) {
                    // not good anymore, try all proxies
                    lastKnownSettings.remove(address);
                }
            }

            URI uri = addressToURI(address, "socket");
            try {
                return createSocket(uri, address,  timeout);
            } catch (IOException e) {
                // we will also try https
            }

            uri = addressToURI(address, "https");
            return createSocket(uri, address,  timeout);
            
        } finally {
            if (socksProxyHost != null) {
                System.setProperty("socksProxyHost", socksProxyHost);
            }
        }
    }

    private URI addressToURI(InetSocketAddress address, String schema) {
        URI uri;
        try {
            if (address.isUnresolved()) {
                uri = new URI(schema + "://" + address.getHostName() + ":" + address.getPort());
            } else {
                uri = new URI(schema + "://" + address.getAddress().getHostAddress() + ":" + address.getPort());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return uri;
    }

    private Socket createSocket(URI uri, InetSocketAddress address, int timeout) throws IOException {
        List<Proxy> proxies = ProxySelector.getDefault().select(uri);
        IOException lastFailure = null;
        for (Proxy proxy : proxies) {
            ConnectivitySettings cs = proxyToCs(proxy, uri);
            try {
                Socket s = createSocket(cs, address, timeout);
                lastKnownSettings.put(address, cs);
                return s;
            } catch (IOException e) {
                lastFailure = e;
            }
        }
        
        throw lastFailure;
    }
    
    private ConnectivitySettings proxyToCs(Proxy proxy, URI uri) {
        ConnectivitySettings cs = new ConnectivitySettings();
        InetSocketAddress isa = (InetSocketAddress) proxy.address();
        switch (proxy.type()) {
        case HTTP:
            setupProxy(cs, ConnectivitySettings.CONNECTION_VIA_HTTPS, isa);
            break;
        case SOCKS:
            setupProxy(cs, ConnectivitySettings.CONNECTION_VIA_SOCKS, isa);
            break;
        default:
        }
        
        String prosyUser = NetworkSettings.getAuthenticationUsername(uri);
        if (prosyUser != null && !prosyUser.isEmpty()) {
            cs.setProxyUsername(prosyUser);
            cs.setProxyPassword(Keyring.read(NetworkSettings.getKeyForAuthenticationPassword(uri)));
        }
        return cs;
    }

    private void setupProxy(ConnectivitySettings cs, int connectionType, InetSocketAddress inetSocketAddress) {
        cs.setConnectionType(connectionType);
        InetAddress address = inetSocketAddress.getAddress();
        cs.setProxyHost((address != null) ? address.getHostAddress() : inetSocketAddress.getHostName());
        cs.setProxyPort(inetSocketAddress.getPort());
    }

    private Socket createSocket(ConnectivitySettings cs, InetSocketAddress address, int timeout) throws IOException {
        switch (cs.getConnectionType()) {
        case ConnectivitySettings.CONNECTION_VIA_SOCKS:
        case ConnectivitySettings.CONNECTION_DIRECT:
            Socket s = new Socket();
            s.connect(address, timeout);
            return s;

        case ConnectivitySettings.CONNECTION_VIA_HTTPS:
            return getHttpsTunnelSocket(address, cs, timeout);

        default:
            throw new IllegalArgumentException("Illegal connection type: " + cs.getConnectionType());
        }
    }
}
