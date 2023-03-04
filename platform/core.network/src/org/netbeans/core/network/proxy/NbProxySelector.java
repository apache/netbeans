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

package org.netbeans.core.network.proxy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.core.ProxySettings;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Rechtacek
 */
@ServiceProvider(service = ProxySelector.class, position = 1000)
public final class NbProxySelector extends ProxySelector {
    
    private final ProxySelector original;
    private static final Logger LOG = Logger.getLogger (NbProxySelector.class.getName ());
    private static Boolean useSystemProxies;
    private static final String DEFAULT_PROXY_SELECTOR_CLASS_NAME = "sun.net.spi.DefaultProxySelector";
    private static final RequestProcessor RP = new RequestProcessor(NbProxySelector.class.getName(), 5);
    private static final int DNS_TIMEOUT = 10000;
        
    /** Creates a new instance of NbProxySelector */
    public NbProxySelector() {
        original = ProxySelector.getDefault();
        LOG.log(Level.FINE, "java.net.useSystemProxies has been set to {0}", useSystemProxies());
        if (original == null || original.getClass().getName().equals(DEFAULT_PROXY_SELECTOR_CLASS_NAME)) {
            RP.post(() -> NetworkProxyReloader.reloadNetworkProxy());
        }
        ProxySettings.addPreferenceChangeListener(new ProxySettingsListener());
        copySettingsToSystem();
    } 
    
    @Override
    public List<Proxy> select(URI uri) {
        List<Proxy> res = new ArrayList<Proxy> ();
        int proxyType = ProxySettings.getProxyType ();
        switch (proxyType) {
            case ProxySettings.DIRECT_CONNECTION:
                res.add(Proxy.NO_PROXY);
                break;
            case ProxySettings.AUTO_DETECT_PROXY:
                if (useSystemProxies ()) {
                    if (original != null) {
                        res.addAll(original.select(uri));
                    }
                } else {                    
                    String protocol = uri.getScheme ();
                    assert protocol != null : "Invalid scheme of uri " + uri + ". Scheme cannot be null!";
                    if (dontUseProxy (ProxySettings.getSystemNonProxyHosts(), uri.getHost ())) {
                        res.add (Proxy.NO_PROXY);
                        break;
                    }
                    if (protocol.toLowerCase (Locale.US).startsWith("http")) {
                        String ports = ProxySettings.getSystemHttpPort();
                        if (ports != null && ports.length () > 0 && ProxySettings.getSystemHttpHost().length () > 0) {
                            int porti = Integer.parseInt(ports);
                            Proxy p = new Proxy (Proxy.Type.HTTP,  new InetSocketAddress (ProxySettings.getSystemHttpHost(), porti));
                            res.add (p);
                        }
                    } else { // supposed SOCKS
                        String ports = ProxySettings.getSystemSocksPort();
                        String hosts = ProxySettings.getSystemSocksHost();
                        if (ports != null && ports.length () > 0 && hosts.length () > 0) {
                            int porti = Integer.parseInt(ports);
                            Proxy p = new Proxy (Proxy.Type.SOCKS,  new InetSocketAddress (hosts, porti));
                            res.add (p);
                        }
                    }
                    if (original != null) {
                        res.addAll (original.select (uri));
                    }
                }
                break;
            case ProxySettings.MANUAL_SET_PROXY:
                String protocol = uri.getScheme ();
                assert protocol != null : "Invalid scheme of uri " + uri + ". Scheme cannot be null!";

                // handling nonProxyHosts first
                if (dontUseProxy (ProxySettings.getNonProxyHosts (), uri.getHost ())) {
                    res.add (Proxy.NO_PROXY);
                    break;
                }
                if (protocol.toLowerCase (Locale.US).startsWith("http")) {
                    String hosts = ProxySettings.getHttpHost ();
                    String ports = ProxySettings.getHttpPort ();
                    if (ports != null && ports.length () > 0 && hosts.length () > 0) {
                        int porti = Integer.parseInt(ports);
                        Proxy p = new Proxy (Proxy.Type.HTTP,  new InetSocketAddress (hosts, porti));
                        res.add (p);
                    } else {
                        LOG.log(Level.FINE, "Incomplete HTTP Proxy [{0}/{1}] found in ProxySelector[Type: {2}] for uri {3}. ", new Object[]{hosts, ports, ProxySettings.getProxyType (), uri});
                        if (original != null) {
                            LOG.log(Level.FINEST, "Fallback to the default ProxySelector which returns {0}", original.select (uri));
                            res.addAll (original.select (uri));
                        }
                    }
                } else { // supposed SOCKS
                    String ports = ProxySettings.getSocksPort ();
                    String hosts = ProxySettings.getSocksHost ();
                    if (ports != null && ports.length () > 0 && hosts.length () > 0) {
                        int porti = Integer.parseInt(ports);
                        Proxy p = new Proxy (Proxy.Type.SOCKS,  new InetSocketAddress (hosts, porti));
                        res.add (p);
                    } else {
                        LOG.log(Level.FINE, "Incomplete SOCKS Server [{0}/{1}] found in ProxySelector[Type: {2}] for uri {3}. ", new Object[]{hosts, ports, ProxySettings.getProxyType (), uri});
                        if (original != null) {
                            LOG.log(Level.FINEST, "Fallback to the default ProxySelector which returns {0}", original.select (uri));
                            res.addAll (original.select (uri));
                        }
                    }
                }
                res.add (Proxy.NO_PROXY);
                break;
            case ProxySettings.AUTO_DETECT_PAC:
                if (useSystemProxies ()) {
                    if (original != null) {
                        res.addAll(original.select(uri));
                    }
                } else {
                    ProxyAutoConfig pac = ProxyAutoConfig.get(getPacFile());
                    assert pac != null : "Instance of ProxyAutoConfig found for " + getPacFile();
                    if (pac == null) {
                        LOG.log(Level.FINEST, "No instance of ProxyAutoConfig({0}) for URI {1}", new Object[]{getPacFile(), uri});
                        res.add(Proxy.NO_PROXY);
                    }
                    if (pac.getPacURI().getHost() == null) {
                        LOG.log(Level.FINEST, "Identifying proxy for URI {0}---{1}, PAC LOCAL URI: {2}", //NOI18N
                                new Object[] { uri.toString(), uri.getHost(), pac.getPacURI().toString() });
                        res.addAll(pac.findProxyForURL(uri));
                    } else if (pac.getPacURI().getHost().equals(uri.getHost())) {
                        // don't proxy PAC files
                        res.add(Proxy.NO_PROXY);
                    } else {
                        LOG.log(Level.FINEST, "Identifying proxy for URI {0}---{1}, PAC URI: {2}---{3}", //NOI18N
                                new Object[] { uri.toString(), uri.getHost(), pac.getPacURI().toString(), pac.getPacURI().getHost() });
                        res.addAll(pac.findProxyForURL(uri));
                    }                    
                }
                
                if (original != null) {
                    res.addAll (original.select (uri));
                }
                
                res.add (Proxy.NO_PROXY);
                break;
            case ProxySettings.MANUAL_SET_PAC:
                // unused branch - never can setup PAC file from NetBeans
                
                ProxyAutoConfig pac = ProxyAutoConfig.get(getPacFile());
                assert pac != null : "Instance of ProxyAutoConfig found for " + getPacFile();
                if (pac == null) {
                    LOG.log(Level.FINEST, "No instance of ProxyAutoConfig({0}) for URI {1}", new Object[]{getPacFile(), uri});
                    res.add(Proxy.NO_PROXY);
                }
                if (pac.getPacURI().getHost() == null) {
                        LOG.log(Level.FINEST, "Identifying proxy for URI {0}---{1}, PAC LOCAL URI: {2}", //NOI18N
                                new Object[] { uri.toString(), uri.getHost(), pac.getPacURI().toString() });
                        res.addAll(pac.findProxyForURL(uri));
                } else if (pac.getPacURI().getHost().equals(uri.getHost())) {
                    // don't proxy PAC files
                    res.add(Proxy.NO_PROXY);
                } else {
                    LOG.log(Level.FINEST, "Identifying proxy for URI {0}---{1}, PAC URI: {2}---{3}", //NOI18N
                            new Object[] { uri.toString(), uri.getHost(), pac.getPacURI().toString(), pac.getPacURI().getHost() });
                    res.addAll(pac.findProxyForURL(uri)); // NOI18N
                }
                res.add (Proxy.NO_PROXY);
                break;
            default:
                assert false : "Invalid proxy type: " + proxyType;
        }
        LOG.log(Level.FINEST, "NbProxySelector[Type: {0}, Use HTTP for all protocols: {1}] returns {2} for URI {3}", 
                new Object[]{ProxySettings.getProxyType (), ProxySettings.useProxyAllProtocols (), res, uri});
        return res;
    }
    
    @Override
    public void connectFailed (URI arg0, SocketAddress arg1, IOException arg2) {
        LOG.log  (Level.INFO, "connectionFailed(" + arg0 + ", " + arg1 +")", arg2);
    }

    // several modules listenes on these properties and propagates it futher
    private class ProxySettingsListener implements PreferenceChangeListener {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt.getKey ().startsWith ("proxy") || evt.getKey ().startsWith ("useProxy")) {
                copySettingsToSystem ();
            }
        }
    }
    
    private void copySettingsToSystem () {
        String host = null, port = null, nonProxyHosts = null;
        String socksHost = null, socksPort = null;
        String httpsHost = null, httpsPort = null;
        int proxyType = ProxySettings.getProxyType ();
        switch (proxyType) {
            case ProxySettings.DIRECT_CONNECTION:
                host = null;
                port = null;
                httpsHost = null;
                httpsPort = null;
                nonProxyHosts = null;
                socksHost = null;
                socksPort = null;
                break;
            case ProxySettings.AUTO_DETECT_PROXY:
                host = ProxySettings.getSystemHttpHost();
                port = ProxySettings.getSystemHttpPort();
                httpsHost = ProxySettings.getSystemHttpsHost();
                httpsPort = ProxySettings.getSystemHttpsPort();
                socksHost = ProxySettings.getSystemSocksHost();
                socksPort = ProxySettings.getSystemSocksPort();
                nonProxyHosts = ProxySettings.getSystemNonProxyHosts();
                break;
            case ProxySettings.MANUAL_SET_PROXY:
                host = ProxySettings.getHttpHost ();
                port = ProxySettings.getHttpPort ();
                httpsHost = ProxySettings.getHttpsHost ();
                httpsPort = ProxySettings.getHttpsPort ();
                nonProxyHosts = ProxySettings.getNonProxyHosts ();
                socksHost = ProxySettings.getSocksHost ();
                socksPort = ProxySettings.getSocksPort ();
                break;
            case ProxySettings.AUTO_DETECT_PAC:
                host = null;
                port = null;
                httpsHost = null;
                httpsPort = null;
                nonProxyHosts = null;
                socksHost = null;
                socksPort = null;
                break;
            case ProxySettings.MANUAL_SET_PAC:
                host = null;
                port = null;
                httpsHost = null;
                httpsPort = null;
                nonProxyHosts = ProxySettings.getNonProxyHosts();
                socksHost = null;
                socksPort = null;
                break;
            default:
                assert false : "Invalid proxy type: " + proxyType;
        }
        setOrClearProperty ("http.proxyHost", host, false);
        setOrClearProperty ("http.proxyPort", port, true);
        setOrClearProperty ("http.nonProxyHosts", nonProxyHosts, false);
        setOrClearProperty ("https.proxyHost", httpsHost, false);
        setOrClearProperty ("https.proxyPort", httpsPort, true);
        setOrClearProperty ("https.nonProxyHosts", nonProxyHosts, false);
        setOrClearProperty ("socksProxyHost", socksHost, false);
        setOrClearProperty ("socksProxyPort", socksPort, true);
        LOG.log (Level.FINE, "Set System''s http.proxyHost/Port/NonProxyHost to {0}/{1}/{2}", new Object[]{host, port, nonProxyHosts});
        LOG.log (Level.FINE, "Set System''s https.proxyHost/Port to {0}/{1}", new Object[]{httpsHost, httpsPort});
        LOG.log (Level.FINE, "Set System''s socksProxyHost/Port to {0}/{1}", new Object[]{socksHost, socksPort});
    }
    
    private void setOrClearProperty (String key, String value, boolean isInteger) {
        assert key != null;
        if (value == null || value.length () == 0) {
            System.clearProperty (key);
        } else {
            if (isInteger) {
                try {
                    Integer.parseInt (value);
                } catch (NumberFormatException nfe) {
                    LOG.log (Level.INFO, nfe.getMessage(), nfe);
                }
            }
            System.setProperty (key, value);
        }
    }

    // package-private for unit-testing
    static boolean dontUseProxy (String nonProxyHosts, String host) {
        if (host == null) {
            return false;
        }
        
        // try IP adress first
        if (dontUseIp (nonProxyHosts, host)) {
            return true;
        } else {
            return dontUseHostName (nonProxyHosts, host);
        }

    }
    
    private static boolean dontUseHostName (String nonProxyHosts, String host) {
        if (host == null) {
            return false;
        }
        
        boolean dontUseProxy = false;
        StringTokenizer st = new StringTokenizer (nonProxyHosts, "|", false);
        while (st.hasMoreTokens () && !dontUseProxy) {
            String token = st.nextToken ().trim();
            int star = token.indexOf ("*");
            if (star == -1) {
                dontUseProxy = token.equals (host);
                if (dontUseProxy) {
                    LOG.log(Level.FINEST, "NbProxySelector[Type: {0}]. Host {1} found in nonProxyHosts: {2}", new Object[]{ProxySettings.getProxyType (), host, nonProxyHosts});
                }
            } else {
                String start = token.substring (0, star - 1 < 0 ? 0 : star - 1);
                String end = token.substring (star + 1 > token.length () ? token.length () : star + 1);

                //Compare left of * if and only if * is not first character in token
                boolean compareStart = star > 0; // not first character
                //Compare right of * if and only if * is not the last character in token
                boolean compareEnd = star < (token.length() - 1); // not last character
                dontUseProxy = (compareStart && host.startsWith(start)) || (compareEnd && host.endsWith(end));

                if (dontUseProxy) {
                    LOG.log(Level.FINEST, "NbProxySelector[Type: {0}]. Host {1} found in nonProxyHosts: {2}", new Object[]{ProxySettings.getProxyType (), host, nonProxyHosts});
                }
            }
        }
        return dontUseProxy;
    }
    
    private static boolean dontUseIp (String nonProxyHosts, String host) {
        if (host == null) {
            return false;
        }
                   
        String ip;        
        DnsTimeoutTask dns = new DnsTimeoutTask(host);
        // fix #189195 - timeout when waiting for DNS response
        RequestProcessor.Task create = RP.post(dns);
        try {
            create.waitFinished(DNS_TIMEOUT);
        } catch (InterruptedException ex) {
            LOG.log(Level.INFO, "Timeout when waiting for DNS response. ({0})", host);
        }
        
        ip = dns.getIp();
        
        if (ip == null) {
            return false;
        }

        boolean dontUseProxy = false;
        StringTokenizer st = new StringTokenizer (nonProxyHosts, "|", false);
        while (st.hasMoreTokens () && !dontUseProxy) {
            String nonProxyHost = st.nextToken ().trim();
            int star = nonProxyHost.indexOf ("*");
            if (star == -1) {
                dontUseProxy = nonProxyHost.equals (ip);
                if (dontUseProxy) {
                    LOG.log(Level.FINEST, "NbProxySelector[Type: {0}]. Host''s IP {1} found in nonProxyHosts: {2}", new Object[]{ProxySettings.getProxyType (), ip, nonProxyHosts});
                }
            } else {
                // match with given dotted-quad IP
                try {
                    dontUseProxy = Pattern.matches (nonProxyHost, ip);
                    if (dontUseProxy) {
                        LOG.log(Level.FINEST, "NbProxySelector[Type: {0}]. Host''s IP{1} found in nonProxyHosts: {2}", new Object[]{ProxySettings.getProxyType (), ip, nonProxyHosts});
                    }
                } catch (PatternSyntaxException pse) {
                    // may ignore it here
                }
            }
        }
        return dontUseProxy;
    }
    
    static boolean useSystemProxies() {
        if (useSystemProxies == null) {
            final String netPropertiesFN = "net.properties"; // NOL10N
            final String propertyKey = "java.net.useSystemProxies"; // NOL10N

            Properties props = new Properties();
            String fname = System.getProperty("java.home");

            if (fname == null) {
                return false;
            }
            
            try {
                // JDK 8 and older
                File folder = new File(fname, "lib");
                File netProperties = new File(folder, netPropertiesFN);

                if (!netProperties.exists()) {
                    // JDK 9 and newer
                    folder = new File(fname, "conf");
                    netProperties = new File(folder, netPropertiesFN);
                }

                fname = netProperties.getCanonicalPath();
                InputStream in = new FileInputStream(fname);
                BufferedInputStream bin = new BufferedInputStream(in);
                props.load(bin);
                bin.close();

                String val = props.getProperty(propertyKey);
                val = System.getProperty(propertyKey, val);
                
                useSystemProxies = Boolean.valueOf(val);
            } catch (Exception e) {
                // set default value
                useSystemProxies = false;
            }           
        }
        
        return useSystemProxies;
    }
    
    static boolean usePAC() {
        String pacFile = ProxySettings.getSystemPac();
        return pacFile != null;
    }
    
    private static String getPacFile() {
        return ProxySettings.getSystemPac();
    }
    
    private static class DnsTimeoutTask implements Runnable {

        private final String host;      
        private String ip = null;
        
        public DnsTimeoutTask(String host) {
            this.host = host;
        }
        
        @Override
        public void run() {
            try {
                ip = InetAddress.getByName(host).getHostAddress();
            } catch (UnknownHostException ex) {
                LOG.log(Level.FINE, ex.getLocalizedMessage(), ex);
            }
        }
        
        public String getIp() {
            return ip;
        }
    }
}
